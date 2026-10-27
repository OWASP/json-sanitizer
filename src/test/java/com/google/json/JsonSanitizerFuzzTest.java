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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.StreamReadConstraints;

import java.io.IOException;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Jazzer fuzz target for {@link JsonSanitizer#sanitize(String)}. Asserts that
 * sanitizing never throws (an exception is a finding), the output parses under
 * a strict RFC 8259 parser as exactly one value, the output is idempotent
 * (RULE-026), and no output contains a script or comment breakout (RULE-057).
 * <p>
 * A plain {@code mvn test} replays the seed inputs under
 * src/test/resources/com/google/json/JsonSanitizerFuzzTestInputs/sanitizeProperties/. Fuzzing
 * runs with the environment variable {@code JAZZER_FUZZ=1}, for maxDuration.
 */
@SuppressWarnings("javadoc")
public final class JsonSanitizerFuzzTest {

  private static final String[] BREAKOUTS = { "</script", "<script", "<!--", "-->", "]]>" };

  /** Jackson's defaults are strict RFC 8259; only its size limits are lifted. */
  private static final JsonFactory STRICT = JsonFactory.builder()
      .streamReadConstraints(StreamReadConstraints.builder()
          .maxNestingDepth(Integer.MAX_VALUE)
          .maxNumberLength(Integer.MAX_VALUE)
          .maxStringLength(Integer.MAX_VALUE)
          .build())
      .build();

  @FuzzTest(maxDuration = "5m")
  public final void sanitizeProperties(FuzzedDataProvider data) throws IOException {
    String input = data.consumeRemainingAsString();
    String out = JsonSanitizer.sanitize(input);

    assertStrictJson(out);
    assertEquals(out, JsonSanitizer.sanitize(out), "not idempotent");
    String lower = out.toLowerCase(Locale.ROOT);
    for (String breakout : BREAKOUTS) {
      assertFalse(lower.contains(breakout), breakout);
    }
  }

  /** Fails unless {@code json} is exactly one strict RFC 8259 value. */
  static void assertStrictJson(String json) throws IOException {
    JsonParser p = STRICT.createParser(withoutSurrogateEscapes(json));
    try {
      int depth = 0;
      do {
        JsonToken t = p.nextToken();
        assertNotNull(t, "no value");
        if (t.isStructStart()) {
          ++depth;
        } else if (t.isStructEnd()) {
          --depth;
        } else if (t == JsonToken.VALUE_STRING || t == JsonToken.FIELD_NAME) {
          p.getText();  // decode in full, so a bad escape or control character is reported
        }
      } while (depth > 0);
      assertNull(p.nextToken(), "content after the top-level value");
    } finally {
      p.close();
    }
  }

  /**
   * Replaces each {@code \}{@code uD800} to {@code \}{@code uDFFF} escape with
   * {@code \}{@code uFFFD}. RFC 8259 allows any four hex digits after
   * {@code \}{@code u}, lone surrogates included (section 7; section 8.2 only
   * warns about them), but Jackson rejects a lone surrogate escape in an object
   * key while it accepts one in a value. One four-hex-digit escape for another
   * leaves the text exactly as valid or invalid as it was.
   */
  static String withoutSurrogateEscapes(String json) {
    StringBuilder sb = null;
    int n = json.length();
    for (int i = 0; i < n; ++i) {
      if (json.charAt(i) != '\\') { continue; }
      if (i + 5 < n && json.charAt(i + 1) == 'u'
          && (json.charAt(i + 2) | 32) == 'd' && "89abcdefABCDEF".indexOf(json.charAt(i + 3)) >= 0
          && isHex(json.charAt(i + 4)) && isHex(json.charAt(i + 5))) {
        if (sb == null) { sb = new StringBuilder(json); }
        sb.replace(i + 2, i + 6, "FFFD");
        i += 5;
      } else {
        ++i;  // Skip the escaped character, so the second \ of \\ never starts an escape.
      }
    }
    return sb != null ? sb.toString() : json;
  }

  private static boolean isHex(char c) {
    return (c >= '0' && c <= '9') || ((c | 32) >= 'a' && (c | 32) <= 'f');
  }

  private static boolean rejected(String json) {
    try {
      assertStrictJson(json);
      return false;
    } catch (IOException | AssertionError e) {
      return true;
    }
  }

  /** The validity oracle itself: strict on syntax, no size limits. */
  @Test
  public final void testStrictJsonCheck() {
    StringBuilder deep = new StringBuilder();
    for (int i = 0; i < 4096; ++i) { deep.append('['); }
    for (int i = 0; i < 4096; ++i) { deep.append(']'); }
    StringBuilder digits = new StringBuilder("1");
    for (int i = 0; i < 5000; ++i) { digits.append('0'); }
    for (String ok : new String[] {
        "null", " 1 ", "1e999", "-0", "[]", "{\"a\":[1,{}]}", "\"\\ud800\"", deep.toString(), digits.toString(),
        // Lone surrogate escapes are valid RFC 8259, in keys as in values (Jazzer's first finding).
        "{\"\\uDDDD\":1}", "{\"\\uD800\":1}", "{\"\\ud83d\\ude00\":\"\\uDBFF\"}",
        // An escaped backslash followed by text that looks like an escape.
        "{\"\\\\uD800\":1}", "\"\\\\\\uDC00\"" }) {
      assertFalse(rejected(ok), ok);
    }
    for (String bad : new String[] {
        "", "[1]]", "1 2", "[1] [2]", "{'a':1}", "{a:1}", "[1,]", "01", "NaN", "+1", ".5",
        "\"\u0001\"", "\"\\x41\"", "\"\\v\"", "[1", "/* */1", "\"a\"\"b\"",
        // Malformed escapes stay malformed after the surrogate rewrite.
        "\"\\uD80\"", "\"\\uDZ00\"", "{\"\\uD8\":1}", "\\uD800", "[\\uD800]" }) {
      assertTrue(rejected(bad), bad);
    }
    assertEquals("{\"\\uFFFD\\\\uD800\":\"\\uFFFD\"}", withoutSurrogateEscapes("{\"\\udc00\\\\uD800\":\"\\uDBFF\"}"));
  }
}
