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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;

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
}
