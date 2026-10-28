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
    m.put("SEC-005 over-deep value skipped as the main loop tokenizes it", 12834);
    m.put("SEC-006 numeric key exponent no longer wraps at the int limit", 14);
    EXPECTED_COUNTS = Collections.unmodifiableMap(m);
  }

  /** The maximum nesting depth of each of GoldenCorpus's seven calls. */
  private static final int[] LEG_DEPTHS = {
    JsonSanitizer.DEFAULT_NESTING_DEPTH, JsonSanitizer.DEFAULT_NESTING_DEPTH, 1, 3,
    JsonSanitizer.DEFAULT_NESTING_DEPTH, JsonSanitizer.DEFAULT_NESTING_DEPTH, JsonSanitizer.DEFAULT_NESTING_DEPTH,
  };

  /** An approved fix, as a rewrite of the input in terms of the legacy implementation. */
  private interface Rewrite {
    String id();
    /**
     * The input as the legacy implementation must see it, at this maximum
     * nesting depth, to behave like the fix; the input itself if the fix does
     * not apply.
     */
    String rewrite(String input, int depth);
  }

  private final List<Rewrite> rewrites = new ArrayList<Rewrite>();

  ApprovedDifferences() {
    rewrites.add(new Rewrite() {
      public String id() { return "SEC-001 over-long hex/octal literal is 1e999"; }
      public String rewrite(String input, int depth) { return overLongLiteralsAsInfinity(input); }
    });
    rewrites.add(new Rewrite() {
      public String id() { return "SEC-005 over-deep value skipped as the main loop tokenizes it"; }
      public String rewrite(String input, int depth) { return overDeepValuesAsEmptyArrays(input, depth); }
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
      String key = r.id() + "@" + LEG_DEPTHS[leg];
      String[] legs = cachedLegs.get(key);
      if (legs == null) {
        String rewritten = r.rewrite(input, LEG_DEPTHS[leg]);
        legs = rewritten.equals(input) ? new String[0] : GoldenCorpus.LEGACY.legs(rewritten);
        cachedLegs.put(key, legs);
      }
      if (legs.length != 0 && legs[leg].equals(current)) {
        return r.id();
      }
    }
    if (exponentNoLongerWraps(input, legacy, current)) {
      return "SEC-006 numeric key exponent no longer wraps at the int limit";
    }
    return null;
  }

  // ---- SEC-006 ------------------------------------------------------------------------------

  /** What the legacy key canonicalizer printed when its int exponent wrapped to Integer.MIN_VALUE. */
  private static final String WRAPPED_EXPONENT = "e--2147483648";

  /**
   * SEC-006: the legacy code printed a numeric key's exponent from an int, so
   * an exponent of magnitude 2^31 wrapped to Integer.MIN_VALUE and printed as
   * "e--2147483648", which no correct output contains. An int is exact modulo
   * 2^32, so the true exponent is 2147483648 with the input exponent's sign:
   * the new output must be the legacy output with exactly that replacement.
   * Legacy cannot be asked to print that exponent, so this class is checked
   * on the output instead of by replaying a rewritten input.
   */
  static boolean exponentNoLongerWraps(String input, String legacy, String current) {
    int at = legacy.indexOf(WRAPPED_EXPONENT);
    if (at < 0 || legacy.indexOf(WRAPPED_EXPONENT, at + 1) >= 0) { return false; }
    int e = Math.max(input.lastIndexOf('e'), input.lastIndexOf('E'));
    if (e < 0 || e + 1 >= input.length()) { return false; }
    char sign = input.charAt(e + 1) == '-' ? '-' : '+';
    String expected = legacy.substring(0, at) + "e" + sign + "2147483648"
        + legacy.substring(at + WRAPPED_EXPONENT.length());
    return expected.equals(current);
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

  // ---- SEC-005 ------------------------------------------------------------------------------

  /**
   * SEC-005: a value nested deeper than the limit is skipped as the main loop
   * tokenizes text, so a quote inside a word (a'b) no longer starts a string
   * and swallows the value's siblings. In legacy terms: each over-deep value,
   * found with the main loop's token rules (re-implemented here, independently
   * of the fix), is replaced by "[]", which the legacy code then turns into
   * null through the same branch, keeping the siblings it would keep anyway.
   */
  static String overDeepValuesAsEmptyArrays(String s, int maxDepth) {
    StringBuilder sb = new StringBuilder(s.length());
    int copied = 0;
    int n = s.length();
    int depth = 0;
    int i = 0;
    while (i < n) {
      char ch = s.charAt(i);
      switch (ch) {
        case '"': case '\'':
          i = endOfQuotedString(s, i);
          break;
        case '/':
          i = endOfComment(s, i);
          break;
        case '{': case '[':
          if (depth >= maxDepth) {
            int end = endOfValue(s, i);
            sb.append(s, copied, i).append("[]");
            copied = end;
            i = end;
          } else {
            ++depth;
            ++i;
          }
          break;
        case '}': case ']': case ',':
          // At depth 0 the main loop drops the rest of the input.
          if (depth == 0) { i = n; break; }
          if (ch != ',') { --depth; }
          ++i;
          break;
        default:
          int end = endOfToken(s, i);
          i = end > i ? end : i + 1;
          break;
      }
    }
    if (copied == 0) { return s; }
    return sb.append(s, copied, n).toString();
  }

  /** Past the close bracket matching the open bracket at start, by the main loop's token rules. */
  private static int endOfValue(String s, int start) {
    int n = s.length();
    int depth = 0;
    int i = start;
    while (i < n) {
      char ch = s.charAt(i);
      switch (ch) {
        case '"': case '\'':
          i = endOfQuotedString(s, i);
          break;
        case '/':
          i = endOfComment(s, i);
          break;
        case '{': case '[':
          ++depth;
          ++i;
          break;
        case '}': case ']':
          ++i;
          if (--depth == 0) { return i; }
          break;
        default:
          int end = endOfToken(s, i);
          i = end > i ? end : i + 1;
          break;
      }
    }
    return n;
  }

  /**
   * The end of the unquoted token at i as the main loop reads it: a run of
   * [A-Za-z0-9+-._$]; unless it is a number or keyword, extended to the next
   * JSON special character and past a double quote there. i itself if no run starts at i.
   */
  private static int endOfToken(String s, int i) {
    int n = s.length();
    int end = i;
    while (end < n && isRunChar(s.charAt(end))) { ++end; }
    if (end == i) { return i; }
    char c = s.charAt(i);
    boolean number = ('0' <= c && c <= '9') || c == '.' || c == '+' || c == '-';
    String word = s.substring(i, end);
    boolean keyword = word.equals("true") || word.equals("false") || word.equals("null");
    if (number || keyword) { return end; }
    while (end < n && !isSpecial(s.charAt(end))) { ++end; }
    if (end < n && s.charAt(end) == '"') { ++end; }
    return end;
  }

  private static boolean isSpecial(char c) {
    return c <= ' ' || c == '"' || c == ',' || c == ':' || c == '[' || c == ']' || c == '{' || c == '}';
  }

  /** Past the closing quote of the string at start, or the end of input; a quote after an odd run of backslashes is escaped. */
  private static int endOfQuotedString(String s, int start) {
    char quote = s.charAt(start);
    for (int i = start; (i = s.indexOf(quote, i + 1)) >= 0;) {
      int slashes = 0;
      while (i - slashes - 1 > start && s.charAt(i - slashes - 1) == '\\') { ++slashes; }
      if ((slashes & 1) == 0) { return i + 1; }
    }
    return s.length();
  }

  /** Past the comment at start: // to a line end, /* to the next star-slash, a lone / is one char. */
  private static int endOfComment(String s, int start) {
    int n = s.length();
    if (start + 1 >= n) { return start + 1; }
    char c = s.charAt(start + 1);
    if (c == '/') {
      for (int j = start + 2; j < n; ++j) {
        char cc = s.charAt(j);
        if (cc == '\n' || cc == '\r' || cc == '\u2028' || cc == '\u2029') { return j + 1; }
      }
      return n;
    }
    if (c == '*') {
      if (start + 3 < n) {
        for (int j = start + 2; (j = s.indexOf('/', j + 1)) >= 0;) {
          if (s.charAt(j - 1) == '*') { return j + 1; }
        }
      }
      return n;
    }
    return start + 1;
  }
}
