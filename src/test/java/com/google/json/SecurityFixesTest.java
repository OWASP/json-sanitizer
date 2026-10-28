// Copyright (C) 2026 OWASP json-sanitizer contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.json;

import static com.google.json.JsonSanitizer.sanitize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * One test per security or correctness fix of Phase 3, named by its finding
 * (analysis/json-sanitizer/SECURITY_FINDINGS.md) and approved in the brief's
 * Q4. Each test fails on the Phase 2 build and passes after its fix.
 */
@SuppressWarnings("javadoc")
public final class SecurityFixesTest {

  private static void assertSanitized(String golden, String input) {
    assertEquals(golden, sanitize(input), input.length() > 200 ? input.substring(0, 200) + "..." : input);
  }

  private static String repeat(char ch, int n) {
    char[] chars = new char[n];
    Arrays.fill(chars, ch);
    return new String(chars);
  }

  /**
   * SEC-001 (CWE-407, High): hex and octal literals were reencoded through
   * BigInteger in time quadratic in their length. Up to 1024 significant
   * digits they are still reencoded exactly; past that they become 1e999,
   * which JavaScript and doubles already read as Infinity (Q4a).
   */
  @Test
  public final void testSec001LongHexAndOctalLiteralsTakeLinearTime() {
    // Leading zeroes do not count toward the limit.
    assertSanitized("[255]", "[0x" + repeat('0', 5000) + "ff]");
    assertSanitized("[83]", "[" + repeat('0', 5000) + "123]");  // Octal.
    // Decimal digits after a leading zero are copied, not reencoded.
    String nines = repeat('9', 100000);
    assertSanitized("[" + nines + "]", "[0" + nines + "]");
    // Up to 1024 significant digits, hex and octal are reencoded exactly.
    String maxHex = repeat('f', 1024);
    assertSanitized("[" + new BigInteger(maxHex, 16) + "]", "[0x" + maxHex + "]");
    String maxOctal = repeat('7', 1024);
    assertSanitized("[" + new BigInteger(maxOctal, 8) + "]", "[0" + maxOctal + "]");
    // From 1025, they become Infinity.
    assertSanitized("[1e999]", "[0x" + repeat('f', 1025) + "]");
    assertSanitized("[1e999]", "[0" + repeat('7', 1025) + "]");
    assertSanitized("[1e999]", "[0X" + repeat('F', 2000) + "]");
    assertSanitized("[-1e999]", "[-0" + repeat('7', 2000) + "]");
    assertSanitized("[1e999]", "[+0x" + repeat('f', 2000) + "]");
    // In key position, the key is Infinity's canonical form.
    assertSanitized("{\"1e+999\":0}", "{0x" + repeat('f', 2000) + ":0}");
    // A fraction or exponent after an over-limit literal is dropped (Q4a, documented).
    assertSanitized("[1e999]", "[0" + repeat('7', 2000) + ".5e+3]");
    assertSanitized("[1e999]", "[0" + repeat('7', 2000) + "e-2000]");
    assertSanitized("{\"1e+999\":0}", "{0" + repeat('7', 2000) + ".5e-3:0}");
    // A long decimal key keeps every digit.
    assertSanitized("{\"9." + repeat('9', 1999) + "e+1999\":0}", "{0" + repeat('9', 2000) + ":0}");

    // The finding's exit criterion: a 1,000,000-digit hex literal in under 1 s.
    String million = "[0x" + repeat('f', 1000000) + "]";
    long t0 = System.nanoTime();
    String out = sanitize(million);
    long elapsedMs = (System.nanoTime() - t0) / 1000000;
    assertEquals("[1e999]", out);
    assertTrue(elapsedMs < 1000, "1,000,000 hex digits took " + elapsedMs + " ms");
  }

  /**
   * SEC-002 (CWE-375): sanitize(CharSequence) returned the caller's own mutable
   * object when it was already clean, so a later change to it changed the
   * "sanitized" result (Q4b).
   */
  @Test
  public final void testSec002CharSequenceResultCannotChangeLater() {
    StringBuilder pooled = new StringBuilder("[\"ok\"]");
    CharSequence result = sanitize(pooled);
    assertTrue(result instanceof String, result.getClass().getName());
    pooled.setLength(0);
    pooled.append("[\"</script>\"]");
    assertEquals("[\"ok\"]", result.toString());
    // Also when the input needed changes: no internal buffer is handed out.
    CharSequence fixed = sanitize(new StringBuilder("{a:1}"), JsonSanitizer.DEFAULT_NESTING_DEPTH);
    assertTrue(fixed instanceof String, fixed.getClass().getName());
    assertEquals("{\"a\":1}", fixed);
  }

  /**
   * SEC-003 (CWE-770): the Reader and InputStream overloads buffered input
   * without limit, so an endless request body exhausted the heap. Past
   * maximumInputLength (default 16 Mi chars) they throw IOException before
   * sanitizing, and write nothing (Q4c).
   */
  @Test
  public final void testSec003StreamInputIsLimited() throws Exception {
    String thousand = "[" + repeat('1', 998) + "]";
    StringBuilder out = new StringBuilder();
    JsonSanitizer.sanitize(new StringReader(thousand), out, 64, 1000);
    assertEquals(thousand, out.toString());
    final StringBuilder notWritten = new StringBuilder();
    IOException e = assertThrows(IOException.class, () ->
        JsonSanitizer.sanitize(new StringReader(thousand + " "), notWritten, 64, 1000));
    assertEquals("JSON-like input is longer than 1000 chars", e.getMessage());
    assertEquals("", notWritten.toString());
    // The byte-stream limit counts decoded chars: 1000 two-byte chars are 1000.
    String twoByteChars = "[\"" + repeat('\u00e9', 996) + "\"]";
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    JsonSanitizer.sanitize(new ByteArrayInputStream(twoByteChars.getBytes(StandardCharsets.UTF_8)), bytes, 64, 1000);
    assertEquals(twoByteChars, new String(bytes.toByteArray(), StandardCharsets.UTF_8));
    final ByteArrayOutputStream noBytes = new ByteArrayOutputStream();
    assertThrows(IOException.class, () -> JsonSanitizer.sanitize(
        new ByteArrayInputStream((twoByteChars + " ").getBytes(StandardCharsets.UTF_8)), noBytes, 64, 1000));
    assertEquals(0, noBytes.size());
    assertThrows(IllegalArgumentException.class, () ->
        JsonSanitizer.sanitize(new StringReader("1"), new StringBuilder(), 64, -1));
    assertEquals(1 << 24, JsonSanitizer.DEFAULT_MAXIMUM_INPUT_LENGTH);

    // The exit criterion: endless input, default limit, a 256 MB heap.
    for (String overload : new String[] { "reader", "stream" }) {
      String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
      Process p = new ProcessBuilder(java, "-Xmx256m", "-cp", System.getProperty("java.class.path"),
          EndlessInput.class.getName(), overload).redirectErrorStream(true).start();
      assertTrue(p.waitFor(120, TimeUnit.SECONDS), overload + ": did not finish in 120 s");
      Scanner sc = new Scanner(p.getInputStream(), "UTF-8").useDelimiter("\\A");
      String log = sc.hasNext() ? sc.next().trim() : "";
      assertEquals(0, p.exitValue(), overload + ": " + log);
      assertEquals("rejected: JSON-like input is longer than 16777216 chars", log, overload);
    }
  }

  /**
   * SEC-005 (CWE-436): the scanner that skips an over-deep value split
   * unquoted text differently from the main loop, so a quote inside a word
   * (a'b) started a string there and swallowed the value's siblings (Q4d).
   */
  @Test
  public final void testSec005OverDeepValueKeepsItsSiblings() {
    assertEquals("[null,\"keep\",\"x\"]", sanitize("[[a'b],\"keep\",'x']", 1));
    assertEquals("[null,\"keep\"]", sanitize("[[a\"b],\"keep\"]", 1));
    assertEquals("{\"a\":null,\"b\":1}", sanitize("{\"a\":[it's],\"b\":1}", 1));
    // A quote that starts a token still starts a string, as in the main loop.
    assertEquals("[null,2]", sanitize("[['a]b'],2]", 1));
    // At the default depth the same input already kept them.
    assertEquals("[[\"a'b\"],\"keep\",\"x\"]", sanitize("[[a'b],\"keep\",'x']"));
    // The skip scanner ends a value exactly where the main loop would: every
    // test input that the main loop reads as one closed value is skipped whole.
    int checked = 0;
    for (String t : GoldenCorpus.testInputs()) {
      String trimmed = t.trim();
      if (trimmed.isEmpty() || (trimmed.charAt(0) != '[' && trimmed.charAt(0) != '{')) { continue; }
      if (!sanitize("[" + t + ",7]").endsWith(",7]")) { continue; }
      assertEquals("[null,7]", sanitize("[" + t + ",7]", 1), t);
      ++checked;
    }
    assertTrue(checked > 20, "only " + checked + " inputs checked");
  }

  /**
   * SEC-006 (CWE-190): a numeric key's decimal exponent was an int, so it
   * overflowed near the int limits ({11e2147483647:0} gave the key
   * "1.1e--2147483648"). The digits are kept, with exact arithmetic (Q4e).
   */
  @Test
  public final void testSec006NumericKeyExponentDoesNotOverflow() {
    assertSanitized("{\"1.1e+2147483648\":0}", "{11e2147483647:0}");
    // The same overflow downward: four places of fraction push the exponent below the int range.
    assertSanitized("{\"1e-2147483652\":0}", "{0.0001e-2147483648:0}");
    // Unchanged: in range, the int limits themselves, out-of-range exponents and values.
    assertSanitized("{\"1.1e+2147483647\":0}", "{11e2147483646:0}");
    assertSanitized("{\"1.1e-2147483647\":0}", "{11e-2147483648:0}");
    assertSanitized("{\"1e+400\":0}", "{1e400:0}");
    assertSanitized("{\"11e99999999999\":0}", "{11e99999999999:0}");
    assertSanitized("[11e2147483647]", "[11e2147483647]");
  }

  /**
   * SEC-004 (CWE-116): EvalMinifier split 12345e+1 at the '+' and pooled
   * "12345e", so the output was not valid JavaScript (Q4f).
   */
  @Test
  public final void testSec004MinifierKeepsExponentSigns() {
    StringBuilder input = new StringBuilder("[");
    StringBuilder body = new StringBuilder();
    for (int i = 0; i < 12; ++i) {
      input.append(i == 0 ? "" : ",").append("12345e+1");
      body.append(i == 0 ? "" : ",").append('a');
    }
    input.append(']');
    assertEquals("(function(a){return[" + body + "]}(12345e+1))", EvalMinifier.minify(input.toString()));
    assertEquals("(function(a){return[" + body + "]}(12345E+1))",
        EvalMinifier.minify(input.toString().replace('e', 'E')));
    // A '-' exponent already stayed in one token.
    assertEquals("(function(a){return[" + body + "]}(12345e-1))",
        EvalMinifier.minify(input.toString().replace('+', '-')));
  }

  /**
   * RULE-001 correctness fix: \v is U+000B (vertical tab) in JavaScript, but
   * the normalizer wrote U+0008 (backspace) and the embed-safety look-ahead
   * decoded it as U+0008 too (approved in RULE_REVIEWS.md).
   */
  @Test
  public final void testRule001VerticalTabEscape() throws Exception {
    assertSanitized("[\"a\\u000bb\"]", "[\"a\\vb\"]");
    assertSanitized("[\"a\\u000bb\"]", "['a\\vb']");
    // An escaped backslash before v is not a \v escape.
    assertSanitized("[\"a\\\\vb\"]", "[\"a\\\\vb\"]");
    // Backspace is still backspace, in every spelling.
    assertSanitized("[\"a\\bb\"]", "[\"a\\bb\"]");
    assertSanitized("[\"a\\u0008b\"]", "[\"a\\u0008b\"]");
    assertSanitized("[\"a\\u0008b\"]", "[\"a\\x08b\"]");
    // The look-ahead decoder: a simple escape, two raw chars, decoding to U+000B.
    java.lang.reflect.Method unescapedChar =
        JsonSanitizer.class.getDeclaredMethod("unescapedChar", String.class, int.class);
    unescapedChar.setAccessible(true);
    assertEquals(0x2000B, unescapedChar.invoke(null, "\\v", 0));
  }

  /** Feeds an endless input to the default stream overloads; run by testSec003 in a 256 MB JVM. */
  static final class EndlessInput {
    public static void main(String[] args) {
      try {
        if ("reader".equals(args[0])) {
          JsonSanitizer.sanitize(new Reader() {
            @Override public int read(char[] buf, int off, int len) {
              Arrays.fill(buf, off, off + len, 'x');
              return len;
            }
            @Override public void close() {}
          }, new StringBuilder());
        } else {
          JsonSanitizer.sanitize(new InputStream() {
            @Override public int read() { return '['; }
            @Override public int read(byte[] buf, int off, int len) {
              Arrays.fill(buf, off, off + len, (byte) '[');
              return len;
            }
          }, new ByteArrayOutputStream());
        }
        System.out.println("returned without rejecting the input");
        System.exit(1);
      } catch (IOException e) {
        System.out.println("rejected: " + e.getMessage());
      }
    }
  }
}
