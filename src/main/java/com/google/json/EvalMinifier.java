// Copyright (C) 2012 Google Inc.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Given a string of valid JSON that is going to be parsed via Javascript's
 * {@code eval} builtin, tries to reduce the number of bytes sent over
 * the wire by turning it into a Javascript expression that pools constants.
 */
public final class EvalMinifier {

  /**
   * Renders JSON-like content safe for use with Javascript {@code eval}.
   *
   * <p>The output is a Javascript expression, not a statement, so if it
   * contains an object (<code>{</code><i>properties</i><code>}</code>) then it
   * still needs to be wrapped in parentheses before being passed to
   * {@code eval} as via {@code eval('(' + s + ')')} or {@code eval('0,' + s)}.
   *
   * @param jsonish a string of JSON-like content as defined by
   *   {@link JsonSanitizer}.
   * @return a valid Javascript expression that has no free variables and whose
   *   execution will have no side-effects, and which can be embedded safely in
   *   an HTML {@code <script></script>} element or inside an XML
   *   {@code <![CDATA[...]]>} section.
   */
  public static String minify(String jsonish) {
    JsonSanitizer s = new JsonSanitizer(jsonish);
    s.sanitize();
    return minify(s.toCharSequence()).toString();
  }

  /**
   * Same as {@link EvalMinifier#minify(String)}, but allows to set custom maximum nesting depth.
   * @param jsonish a string of JSON-like content as defined by
   * {@link JsonSanitizer}.
   * @param maximumNestingDepth the maximum nesting depth for the {@link JsonSanitizer}
   * @return see {@link EvalMinifier#minify(String)}
   */
  public static String minify(String jsonish, int maximumNestingDepth) {
    JsonSanitizer s = new JsonSanitizer(jsonish, maximumNestingDepth);
    s.sanitize();
    return minify(s.toCharSequence()).toString();
  }

  @SuppressWarnings("synthetic-access")
  private static CharSequence minify(CharSequence json) {
    Map<Token, Token> pool = new HashMap<Token, Token>();
    int n = json.length();
    for (int i = 0; i < n; ++i) {
      char ch = json.charAt(i);
      int tokEnd;
      if (ch == '"') {
        for (tokEnd = i + 1; tokEnd < n; ++tokEnd) {
          char tch = json.charAt(tokEnd);
          if (tch == '\\') {
            ++tokEnd;
          } else if (tch == '"') {
            ++tokEnd;
            break;
          }
        }
      } else if (isLetterOrNumberChar(ch)) {
        tokEnd = i + 1;
        while (tokEnd < n && isLetterOrNumberChar(json.charAt(tokEnd))) {
          ++tokEnd;
        }
      } else {
        continue;
      }

      int nextNonWhitespace = tokEnd;
      for (; nextNonWhitespace < n; ++nextNonWhitespace) {
        char wch = json.charAt(nextNonWhitespace);
        if (!(wch == '\t' || wch == '\n' || wch == '\r' || wch == ' ')) {
          break;
        }
      }

      // If the string is followed by a ':' then it is a map key and cannot be
      // substituted with an identifier.
      // In JavaScript, { a: 1 } is the same as { "a": 1 } regardless of
      // what the identifier "a" resolves to.
      if (nextNonWhitespace == n || ':' != json.charAt(nextNonWhitespace)
          && tokEnd - i >= 4) {
        Token tok = new Token(i, tokEnd, json);
        @Nullable Token last = pool.put(tok, tok);
        if (last != null) {
          tok.prev = last;
        }
      }

      i = nextNonWhitespace - 1;
    }

    // Now look at all the token groups that have a next, and then count up the
    // savings to see if they meet the cost of the boilerplate.
    int potentialSavings = 0;
    List<Token> dupes = new ArrayList<Token>();
    for (Iterator<Token> values = pool.values().iterator(); values.hasNext();) {
      Token tok = values.next();
      if (tok.prev == null) {
        values.remove();
        continue;
      }
      int chainDepth = 0;
      for (Token t = tok; t != null; t = t.prev) {
        ++chainDepth;
      }
      int tokSavings = (chainDepth - 1) * (tok.end - tok.start)
          - MARGINAL_VAR_COST;
      if (tokSavings > 0) {
        potentialSavings += tokSavings;
        for (Token t = tok; t != null; t = t.prev) {
          dupes.add(t);
        }
      }
    }
    if (potentialSavings <= BOILERPLATE_COST + SAVINGS_THRESHOLD) {
      return json;
    }

    // Dump the tokens into an array and sort them.
    Collections.sort(dupes);

    int nTokens = dupes.size();

    StringBuilder sb = new StringBuilder(n);
    sb.append(ENVELOPE_P1);

    {
      NameGenerator nameGenerator = new NameGenerator();
      boolean first = true;
      for (Token tok : pool.values()) {
        String name = nameGenerator.next();
        for (Token t = tok; t != null; t = t.prev) { t.name = name; }
        if (first) { first = false; } else { sb.append(','); }
        sb.append(name);
      }
    }

    sb.append(ENVELOPE_P2);
    int afterReturn = sb.length();
    int pos = 0, tokIndex = 0;
    while (true) {
      Token tok = tokIndex < nTokens ? dupes.get(tokIndex++) : null;
      int limit = tok != null ? tok.start : n;
      boolean inString = false;
      for (int i = pos; i < limit; ++i) {
        char ch = json.charAt(i);
        if (inString) {
          if (ch == '"') {
            inString = false;
          } else if (ch == '\\') {
            ++i;
          }
        } else if (ch == '\t' || ch == '\n' || ch == '\r' || ch == ' ') {
          if (pos != i) {
            sb.append(json, pos, i);
          }
          pos = i + 1;
        } else if (ch == '"') {
          inString = true;
        }
      }
      // There should be no token boundaries inside strings.
      assert !inString;
      if (pos != limit) {
        sb.append(json, pos, limit);
      }
      if (tok == null) { break; }
      sb.append(tok.name);
      pos = tok.end;
    }
    {
      // Insert space after return if required.
      // This is unlikely to occur in practice.
      char ch = sb.charAt(afterReturn);
      if (ch != '{' && ch != '[' && ch != '"') {
        sb.insert(afterReturn, ' ');
      }
    }
    sb.append(ENVELOPE_P3);
    {
      boolean first = true;
      for (Token tok : pool.values()) {
        if (first) { first = false; } else { sb.append(','); }
        sb.append(tok.seq, tok.start, tok.end);
      }
    }
    sb.append(ENVELOPE_P4);

    return sb;
  }

  private static final String ENVELOPE_P1 = "(function(";
  private static final String ENVELOPE_P2 = "){return";
  private static final String ENVELOPE_P3 = "}(";
  private static final String ENVELOPE_P4 = "))";

  private static final int BOILERPLATE_COST =
      (ENVELOPE_P1 + ENVELOPE_P2 + ENVELOPE_P3 + ENVELOPE_P4).length();
  private static final int MARGINAL_VAR_COST = ",,".length();
  private static final int SAVINGS_THRESHOLD = 32;

  private static boolean isLetterOrNumberChar(char ch) {
    if ('0' <= ch && ch <= '9') { return true; }
    char lch = (char) (ch | 32);
    if ('a' <= lch && lch <= 'z') { return true; }
    return ch == '_' || ch == '$' || ch == '-' || ch == '.';
  }

  private static final class Token implements Comparable<Token> {
    private final int start, end, hashCode;
    private final @Nonnull CharSequence seq;
    @Nullable Token prev;
    @Nullable String name;

    Token(int start, int end, CharSequence seq) {
      this.start = start;
      this.end = end;
      this.seq = seq;
      int hc = 0;
      for (int i = start; i < end; ++i) {
        char ch = seq.charAt(i);
        hc = hc * 31 + ch;
      }
      this.hashCode = hc;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (!(o instanceof Token)) { return false; }
      Token that = (Token) o;
      if (this.hashCode != that.hashCode) { return false; }
      return regionMatches(
          this.seq, this.start, this.end, that.seq, that.start, that.end);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public int compareTo(Token t) {
      return start - t.start;
    }
  }

  static boolean regionMatches(
      CharSequence a, int as, int ae, CharSequence b, int bs, int be) {
    int n = ae - as;
    if (be - bs != n) { return false; }
    for (int ai = as, bi = bs; ai < ae; ++ai, ++bi) {
      if (a.charAt(ai) != b.charAt(bi)) { return false; }
    }
    return true;
  }


  private static final String[][] RESERVED_KEYWORDS = {
    {},
    {},
    {"do", "if", "in", },
    {"for", "let", "new", "try", "var"},
    {"case", "else", "enum", "eval", "null", "this", "true", "void", "with"},
    {"catch", "class", "const", "false", "super", "throw", "while", "yield"},
    {"delete", "export", "import", "return", "switch", "static", "typeof"},
    {"default", "extends", "public", "private"},
    {"continue", "function"},
    {"arguments"},
    {"implements", "instanceof"}
  };

  static final class NameGenerator {

    private final StringBuilder sb = new StringBuilder("a");

    @SuppressWarnings("synthetic-access")
    public String next() {
      while (true) {
        String name = sb.toString();

        int sbLen = sb.length();
        for (int i = sbLen; --i >= 0;) {
          int next = nextIdentChar(sb.charAt(i), i != 0);
          if (next < 0) {
            sb.setCharAt(i, 'a');
            if (i == 0) { sb.append('a'); }
          } else {
            sb.setCharAt(i, (char) next);
            break;
          }
        }

        int nameLen = name.length();
        if (nameLen >= RESERVED_KEYWORDS.length
            || Arrays.binarySearch(RESERVED_KEYWORDS[nameLen], name) < 0) {
          return name;
        }
      }
    }
  }

  static int nextIdentChar(char ch, boolean allowDigits) {
    if (ch == 'z') { return 'A'; }
    if (ch == 'Z') { return '_'; }
    if (ch == '_') { return '$'; }
    if (ch == '$') {
      if (allowDigits) { return '0'; }
      return -1;
    }
    if (ch == '9') { return -1; }
    return (char) (ch + 1);
  }

}
