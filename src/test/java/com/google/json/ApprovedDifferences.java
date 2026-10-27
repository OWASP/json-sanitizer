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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The output differences from the legacy release that have been approved
 * (brief Q4; analysis/json-sanitizer/APPROVED_DIFFERENCES.md lists the same
 * classes and counts).
 * <p>
 * Each approved fix is described as a rewrite of the <em>input</em> that says,
 * in terms of the legacy implementation, what the fix does: for example
 * "an over-long hex literal now reads as 1e999". A difference is explained
 * when the legacy implementation, run on the rewritten input, gives exactly the
 * new output. The new output is never compared with itself, so a change outside
 * the approved fixes stays unexplained and fails GoldenCorpusTest.
 */
final class ApprovedDifferences implements GoldenCorpus.Classifier {

  /** Differing (input, call) pairs expected per class over the golden corpus. */
  static final Map<String, Integer> EXPECTED_COUNTS;
  static {
    Map<String, Integer> m = new LinkedHashMap<String, Integer>();
    m.put("SEC-001 over-long hex/octal literal is 1e999", 35280);
    EXPECTED_COUNTS = Collections.unmodifiableMap(m);
  }

  /** An approved fix, as a rewrite of the input in terms of the legacy implementation. */
  private interface Rewrite {
    String id();
    /** The input as the legacy implementation must see it to behave like the fix; the input itself if the fix does not apply. */
    String rewrite(String input);
  }

  private final List<Rewrite> rewrites = new ArrayList<Rewrite>();

  ApprovedDifferences() {
    rewrites.add(new Rewrite() {
      public String id() { return "SEC-001 over-long hex/octal literal is 1e999"; }
      public String rewrite(String input) { return overLongLiteralsAsInfinity(input); }
    });
  }

  private String cachedInput;
  private final Map<String, String[]> cachedLegs = new HashMap<String, String[]>();

  public String classify(String input, int leg, String legacy, String current) {
    if (!input.equals(cachedInput)) {
      cachedInput = input;
      cachedLegs.clear();
    }
    for (Rewrite r : rewrites) {
      String[] legs = cachedLegs.get(r.id());
      if (legs == null) {
        String rewritten = r.rewrite(input);
        legs = rewritten.equals(input) ? new String[0] : GoldenCorpus.LEGACY.legs(rewritten);
        cachedLegs.put(r.id(), legs);
      }
      if (legs.length != 0 && legs[leg].equals(current)) {
        return r.id();
      }
    }
    return null;
  }

  // ---- SEC-001 ------------------------------------------------------------------------------

  /** Most significant hex or octal digits that are still reencoded exactly (JsonSanitizer). */
  private static final int MAX_EXACT_REENCODED_DIGITS = 1024;

  /**
   * SEC-001: a hex or octal literal with more than 1024 significant digits, and
   * whatever follows it in its token (a fraction or exponent), reads as 1e999.
   * Tokens are runs of the characters the sanitizer's main loop groups into one
   * value: [A-Za-z0-9+-._$]; a sign before the literal is kept.
   */
  static String overLongLiteralsAsInfinity(String input) {
    StringBuilder sb = new StringBuilder(input.length());
    int copied = 0;
    int n = input.length();
    int i = 0;
    while (i < n) {
      if (!isRunChar(input.charAt(i))) { ++i; continue; }
      int runEnd = i;
      while (runEnd < n && isRunChar(input.charAt(runEnd))) { ++runEnd; }
      int lit = i;
      while (lit < runEnd && (input.charAt(lit) == '+' || input.charAt(lit) == '-')) { ++lit; }
      if (isOverLongLiteral(input, lit, runEnd)) {
        sb.append(input, copied, lit).append("1e999");
        copied = runEnd;
      }
      i = runEnd;
    }
    if (copied == 0) { return input; }
    return sb.append(input, copied, n).toString();
  }

  private static boolean isRunChar(char c) {
    return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9')
        || c == '+' || c == '-' || c == '.' || c == '_' || c == '$';
  }

  /** A 0x hex or 0-prefixed octal literal at [start, end) with more than the exact limit of significant digits. */
  private static boolean isOverLongLiteral(String s, int start, int end) {
    if (start >= end || s.charAt(start) != '0') { return false; }
    int digits = start + 1;
    boolean hex = digits < end && (s.charAt(digits) | 32) == 'x';
    if (hex) { ++digits; }
    while (digits < end && s.charAt(digits) == '0') { ++digits; }
    int significant = 0;
    int j = digits;
    for (; j < end; ++j) {
      char c = s.charAt(j);
      boolean ok = hex
          ? ('0' <= c && c <= '9') || ('a' <= (c | 32) && (c | 32) <= 'f')
          : ('0' <= c && c <= '7');
      if (!ok) { break; }
      ++significant;
    }
    // An octal-looking literal with an 8 or 9 is decimal and is copied, not reencoded.
    if (!hex && j < end && (s.charAt(j) == '8' || s.charAt(j) == '9')) { return false; }
    return significant > MAX_EXACT_REENCODED_DIGITS;
  }
}
