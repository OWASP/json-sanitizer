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
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * One regression test per P0 rule of the behavior contract
 * (analysis/json-sanitizer/MODERNIZATION_BRIEF.md section 5). Each test names
 * its rule; the rule cards are in analysis/json-sanitizer/BUSINESS_RULES.md.
 */
@SuppressWarnings("javadoc")
public final class BehaviorContractTest {

  private static void assertSanitized(String golden, String input) {
    assertEquals(golden, sanitize(input), input);
  }

  /** RULE-001: the embed-safety look-ahead decodes escapes as JavaScript would. */
  @Test
  public final void testRule001EscapeAwareLookAhead() {
    // Identity escape: \s is 's', so <\script is a <script.
    assertSanitized("\"\\u003cscript>\"", "\"<\\script>\"");
    // Hex, unicode and octal escapes of 's'.
    assertSanitized("\"\\u003c\\u0073cript>\"", "\"<\\x73cript>\"");
    assertSanitized("\"\\u003c\\u0073cript>\"", "\"<\\u0073cript>\"");
    assertSanitized("\"\\u003c\\u0073cript>\"", "\"<\\163cript>\"");
    assertSanitized("\"\\u003c/\\u0073cript>\"", "\"</\\x73cript>\"");
    // Look-behind for --> and look-ahead for ]]> decode escapes too.
    assertSanitized("\"x\\u002d\\u002d\\u003e\"", "\"x\\x2d\\x2d>\"");
    assertSanitized("\"\\u005d\\u005d>\"", "\"]\\x5d>\"");
    // \v is U+000B, as in JavaScript (legacy wrote U+0008; approved for Phase 3).
    assertSanitized("[\"a\\u000bb\"]", "[\"a\\vb\"]");
  }

  /** RULE-024: an over-deep value becomes null, whole; siblings stay; nothing throws. */
  @Test
  public final void testRule024OverDeepValueBecomesNull() {
    assertEquals("[[null],2]", sanitize("[[[1]],2]", 2));
    assertEquals("{\"a\":null,\"b\":1}", sanitize("{\"a\":{\"c\":2},\"b\":1}", 1));
    // Never closed: the null swallows the rest, the outer brackets are closed.
    assertEquals("[null]", sanitize("[[1,2,3", 1));
    StringBuilder deep = new StringBuilder();
    for (int i = 0; i < 100000; ++i) { deep.append('['); }
    StringBuilder want = new StringBuilder();
    for (int i = 0; i < JsonSanitizer.DEFAULT_NESTING_DEPTH; ++i) { want.append('['); }
    want.append("null");
    for (int i = 0; i < JsonSanitizer.DEFAULT_NESTING_DEPTH; ++i) { want.append(']'); }
    assertEquals(want.toString(), sanitize(deep.toString()));
  }

  /** RULE-025: exactly one top-level value; whatever would start a second is dropped. */
  @Test
  public final void testRule025OneTopLevelValue() {
    assertSanitized("[1]", "[1]],alert(1)");
    assertSanitized("1", "1,2");
    assertSanitized("1 ", "1 2");
    assertSanitized("{\"a\":1}", "{\"a\":1}}x");
  }

  /** RULE-026: sanitizing the output again changes nothing. */
  @Test
  public final void testRule026Idempotence() {
    List<String> inputs = new ArrayList<String>();
    inputs.add("[1f,'a' true,]");
    inputs.addAll(GoldenCorpus.testInputs());
    inputs.addAll(GoldenCorpus.numberCorpus());
    for (String input : inputs) {
      String once = sanitize(input);
      assertEquals(once, sanitize(once), input);
    }
  }

  /** RULE-056: the minifier sanitizes first and pools only sanitized text. */
  @Test
  public final void testRule056MinifySanitizesFirst() {
    assertEquals("{\"a\":1}", EvalMinifier.minify("{'a':1}"));
    assertEquals("[[null]]", EvalMinifier.minify("[[[", 2));
    StringBuilder input = new StringBuilder("[");
    StringBuilder body = new StringBuilder();
    for (int i = 0; i < 26; ++i) {
      input.append(i == 0 ? "" : ",").append("'</script>'");
      body.append(i == 0 ? "" : ",").append('a');
    }
    input.append(']');
    String minified = EvalMinifier.minify(input.toString());
    assertEquals("(function(a){return[" + body + "]}(\"\\u003c/script>\"))", minified);
  }

  /** RULE-057: no string in the output contains a script or comment breakout. */
  @Test
  public final void testRule057NoBreakoutSequences() {
    assertSanitized("\"\\u003c/script>\"", "\"</script>\"");
    assertSanitized("\"\\u003c/SCRIPT\"", "\"</SCRIPT\"");
    assertSanitized("\"\\u003c!--x\"", "\"<!--x\"");
    assertSanitized("\"a--\\u003e\"", "\"a-->\"");
    assertSanitized("\"\\u005d]>\"", "\"]]>\"");
    // The match is on a prefix, so harmless text is escaped too (safe).
    assertSanitized("\"\\u003cscroll\"", "\"<scroll\"");
    // <![CDATA[ itself is not escaped, only its ]]> (out of scope per the brief, Q8).
    assertSanitized("\"<![CDATA[x\\u005d]>\"", "\"<![CDATA[x]]>\"");
    for (String input : GoldenCorpus.testInputs()) {
      String out = sanitize(input).toLowerCase(Locale.ROOT);
      for (String bad : new String[] { "</script", "<script", "<!--", "-->", "]]>" }) {
        assertFalse(out.contains(bad), input + " => " + out);
      }
    }
  }
}
