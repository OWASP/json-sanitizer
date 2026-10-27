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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Pins how the Reader and InputStream overloads decode their input, so a JDK
 * whose UTF-8 decoder or buffering behaves differently fails on its CI leg
 * (DELTA-11, RULE-034, RULE-058). The expected values are the legacy
 * behavior, the same on JDK 8, 11, 17, 21 and 25.
 */
@SuppressWarnings("javadoc")
public final class StreamCharacterizationTest {

  /** The sanitizer's read buffer, in chars (JsonSanitizer#sanitize(Reader, Appendable, int)). */
  private static final int READ_BUFFER = 8192;

  private static String sanitizeBytes(byte[] in) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    sanitize(new ByteArrayInputStream(in), out);
    return new String(out.toByteArray(), StandardCharsets.UTF_8);
  }

  private static byte[] bytes(int... values) {
    byte[] b = new byte[values.length];
    for (int i = 0; i < values.length; ++i) { b[i] = (byte) values[i]; }
    return b;
  }

  @Test
  public final void testMalformedUtf8BecomesReplacementCharacters() throws IOException {
    // RULE-034: {"a":"<FF>"}
    assertEquals("{\"a\":\"\ufffd\"}", sanitizeBytes(bytes(0x7B, 0x22, 0x61, 0x22, 0x3A, 0x22, 0xFF, 0x22, 0x7D)));
    // A lone continuation byte, a truncated 3-byte sequence, a CESU-8 surrogate.
    assertEquals("[\"\ufffd\"]", sanitizeBytes(bytes(0x5B, 0x22, 0x80, 0x22, 0x5D)));
    assertEquals("[\"\ufffd\"]", sanitizeBytes(bytes(0x5B, 0x22, 0xE2, 0x82, 0x22, 0x5D)));
    assertEquals("[\"\ufffd\"]", sanitizeBytes(bytes(0x5B, 0x22, 0xED, 0xA0, 0x80, 0x22, 0x5D)));
    // One U+FFFD per byte for an overlong form, a code point above U+10FFFF and a 5-byte form.
    assertEquals("[\"\ufffd\ufffd\"]", sanitizeBytes(bytes(0x5B, 0x22, 0xC0, 0xAF, 0x22, 0x5D)));
    assertEquals("[\"\ufffd\ufffd\ufffd\ufffd\"]",
        sanitizeBytes(bytes(0x5B, 0x22, 0xF4, 0x90, 0x80, 0x80, 0x22, 0x5D)));
    assertEquals("[\"\ufffd\ufffd\ufffd\ufffd\ufffd\"]",
        sanitizeBytes(bytes(0x5B, 0x22, 0xF8, 0x88, 0x80, 0x80, 0x80, 0x22, 0x5D)));
    // Truncated at end of input: one U+FFFD, then the string and array are closed.
    assertEquals("[\"\ufffd\"]", sanitizeBytes(bytes(0x5B, 0x22, 0xF0, 0x9F, 0x98)));
  }

  @Test
  public final void testByteOrderMark() throws IOException {
    // RULE-034 (open question Q12): the decoder keeps U+FEFF. Before the value
    // the sanitizer drops it as junk; inside a string it is kept raw.
    assertEquals("[1]", sanitizeBytes(bytes(0xEF, 0xBB, 0xBF, 0x5B, 0x31, 0x5D)));
    assertEquals("[\"\ufeff\"]", sanitizeBytes(bytes(0x5B, 0x22, 0xEF, 0xBB, 0xBF, 0x22, 0x5D)));
  }

  @Test
  public final void testSurrogatePairSplitAtTheReadBoundary() throws IOException {
    // RULE-058: U+1F600 as chars READ_BUFFER - 1 and READ_BUFFER, so the first
    // read ends between its high and low surrogate.
    StringBuilder sb = new StringBuilder("[\"");
    while (sb.length() < READ_BUFFER - 1) { sb.append('a'); }
    sb.append("\ud83d\ude00\"]");
    String input = sb.toString();
    assertEquals('\ud83d', input.charAt(READ_BUFFER - 1));
    String expected = sanitize(input);
    assertEquals(input, expected);

    StringBuilder viaReader = new StringBuilder();
    sanitize(new StringReader(input), viaReader);
    assertEquals(expected, viaReader.toString());

    // The same through the byte stream; in UTF-8 the 4-byte sequence starts at byte READ_BUFFER - 1.
    byte[] utf8 = input.getBytes(StandardCharsets.UTF_8);
    assertEquals(0xF0, utf8[READ_BUFFER - 1] & 0xff);
    assertEquals(expected, sanitizeBytes(utf8));
  }

  @Test
  public final void testOneCharOrByteAtATime() throws IOException {
    // RULE-058: a source that returns one char or one byte per read splits every
    // surrogate pair and every multi-byte sequence; the output must not change.
    String input = "['caf\u00e9',\"\ud83d\ude00\",'\u2028',{a:\"</script>\"}]";
    String expected = sanitize(input);

    StringBuilder viaReader = new StringBuilder();
    sanitize(new OneCharReader(new StringReader(input)), viaReader);
    assertEquals(expected, viaReader.toString());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    sanitize(new OneByteInputStream(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))), out);
    assertEquals(expected, new String(out.toByteArray(), StandardCharsets.UTF_8));
  }

  private static final class OneCharReader extends FilterReader {
    OneCharReader(Reader in) { super(in); }
    @Override public int read(char[] buf, int off, int len) throws IOException {
      return super.read(buf, off, Math.min(len, 1));
    }
  }

  private static final class OneByteInputStream extends FilterInputStream {
    OneByteInputStream(InputStream in) { super(in); }
    @Override public int read(byte[] buf, int off, int len) throws IOException {
      return super.read(buf, off, Math.min(len, 1));
    }
  }
}
