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

import static com.google.json.JsonSanitizer.DEFAULT_NESTING_DEPTH;
import static com.google.json.JsonSanitizer.sanitize;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.junit.Test;

@SuppressWarnings("javadoc")
public final class JsonSanitizerTest extends TestCase {

  private static void assertSanitized(String golden, String input) {
    assertSanitized(golden, input, DEFAULT_NESTING_DEPTH);
  }

  private static void assertSanitized(String golden, String input, int maximumNestingDepth) {
    String actual = sanitize(input, maximumNestingDepth);
    assertEquals(input, golden, actual);
    if (actual.equals(input)) {
      assertSame(input, input, actual);
    }
  }

  private static void assertSanitized(String sanitary) {
    assertSanitized(sanitary, sanitary);
  }

  @Test
  public static final void testSanitize() {
    // On the left is the sanitized output, and on the right the input.
    // If there is a single string, then the input is fine as-is.
    assertSanitized("null", null);
    assertSanitized("null", "");
    assertSanitized("null");
    assertSanitized("false");
    assertSanitized("true");
    assertSanitized(" false ");
    assertSanitized("  false");
    assertSanitized("false\n");
    assertSanitized("false", "false,true");
    assertSanitized("\"foo\"");
    assertSanitized("\"foo\"", "'foo'");
    assertSanitized(
        "\"<script>foo()<\\/script>\"", "\"<script>foo()</script>\"");
    assertSanitized(
        "\"<script>foo()<\\/script>\"", "\"<script>foo()</script>\"");
    assertSanitized("\"<\\/SCRIPT\\n>\"", "\"</SCRIPT\n>\"");
    assertSanitized("\"<\\/ScRIpT\"", "\"</ScRIpT\"");
    // \u0130 is a Turkish dotted upper-case 'I' so the lower case version of
    // the tag name is "script".
    assertSanitized("\"<\\/ScR\u0130pT\"", "\"</ScR\u0130pT\"");
    assertSanitized("\"<b>Hello</b>\"");
    assertSanitized("\"<s>Hello</s>\"");
    assertSanitized("\"<[[\\u005d]>\"", "'<[[]]>'");
    assertSanitized("\"\\u005d]>\"", "']]>'");
    assertSanitized("[[0]]", "[[0]]>");
    assertSanitized("[1,-1,0.0,-0.5,1e2]", "[1,-1,0.0,-0.5,1e2,");
    assertSanitized("[1,2,3]", "[1,2,3,]");
    assertSanitized("[1,null,3]", "[1,,3,]");
    assertSanitized("[1 ,2 ,3]", "[1 2 3]");
    assertSanitized("{ \"foo\": \"bar\" }");
    assertSanitized("{ \"foo\": \"bar\" }", "{ \"foo\": \"bar\", }");
    assertSanitized("{\"foo\":\"bar\"}", "{\"foo\",\"bar\"}");
    assertSanitized("{ \"foo\": \"bar\" }", "{ foo: \"bar\" }");
    assertSanitized("{ \"foo\": \"bar\"}", "{ foo: 'bar");
    assertSanitized("{ \"foo\": [\"bar\"]}", "{ foo: ['bar");
    assertSanitized("false", "// comment\nfalse");
    assertSanitized("false", "false// comment");
    assertSanitized("false", "false// comment\n");
    assertSanitized("false", "false/* comment */");
    assertSanitized("false", "false/* comment *");
    assertSanitized("false", "false/* comment ");
    assertSanitized("false", "/*/true**/false");
    assertSanitized("1");
    assertSanitized("-1");
    assertSanitized("1.0");
    assertSanitized("-1.0");
    assertSanitized("1.05");
    assertSanitized("427.0953333");
    assertSanitized("6.0221412927e+23");
    assertSanitized("6.0221412927e23");
    assertSanitized("6.0221412927e0", "6.0221412927e");
    assertSanitized("6.0221412927e-0", "6.0221412927e-");
    assertSanitized("6.0221412927e+0", "6.0221412927e+");
    assertSanitized("1.660538920287695E-24");
    assertSanitized("-6.02e-23");
    assertSanitized("1.0", "1.");
    assertSanitized("0.5", ".5");
    assertSanitized("-0.5", "-.5");
    assertSanitized("0.5", "+.5");
    assertSanitized("0.5e2", "+.5e2");
    assertSanitized("1.5e+2", "+1.5e+2");
    assertSanitized("0.5e-2", "+.5e-2");
    assertSanitized("{\"0\":0}", "{0:0}");
    assertSanitized("{\"0\":0}", "{-0:0}");
    assertSanitized("{\"0\":0}", "{+0:0}");
    assertSanitized("{\"1\":0}", "{1.0:0}");
    assertSanitized("{\"1\":0}", "{1.:0}");
    assertSanitized("{\"0.5\":0}", "{.5:0}");
    assertSanitized("{\"-0.5\":0}", "{-.5:0}");
    assertSanitized("{\"0.5\":0}", "{+.5:0}");
    assertSanitized("{\"50\":0}", "{+.5e2:0}");
    assertSanitized("{\"150\":0}", "{+1.5e+2:0}");
    assertSanitized("{\"0.1\":0}", "{+.1:0}");
    assertSanitized("{\"0.01\":0}", "{+.01:0}");
    assertSanitized("{\"0.005\":0}", "{+.5e-2:0}");
    assertSanitized("{\"1e+101\":0}", "{10e100:0}");
    assertSanitized("{\"1e-99\":0}", "{10e-100:0}");
    assertSanitized("{\"1.05e-99\":0}", "{10.5e-100:0}");
    assertSanitized("{\"1.05e-99\":0}", "{10.500e-100:0}");
    assertSanitized("{\"1.234e+101\":0}", "{12.34e100:0}");
    assertSanitized("{\"1.234e-102\":0}", "{.01234e-100:0}");
    assertSanitized("{\"1.234e-102\":0}", "{.01234e-100:0}");
    assertSanitized("{}");
    // Remove grouping parentheses.
    assertSanitized("{}", "({})");
    // Escape code-points and isolated surrogates which are not XML embeddable.
    assertSanitized("\"\\u0000\\u0008\\u001f\"", "'\u0000\u0008\u001f'");
    assertSanitized("\"\ud800\udc00\\udc00\\ud800\"",
                    "'\ud800\udc00\udc00\ud800'");
    assertSanitized("\"\ufffd\\ufffe\\uffff\"", "'\ufffd\ufffe\uffff'");
    // These control characters should be elided if they appear outside a string
    // literal.
    assertSanitized("42", "\uffef\u000042\u0008\ud800\uffff\udc00");
    assertSanitized("null", "\uffef\u0000\u0008\ud800\uffff\udc00");
    assertSanitized("[null]", "[,]");
    assertSanitized("[null]", "[null,]");
    assertSanitized("{\"a\":0,\"false\":\"x\",\"\":{\"\":-1}}",
                    "{\"a\":0,false\"x\":{\"\":-1}}");
    assertSanitized("[true ,false]", "[true false]");
    assertSanitized("[\"\\u00a0\\u1234\"]");
    assertSanitized("{\"a\\b\":\"c\"}", "{a\\b\"c");
    assertSanitized("{\"a\":\"b\",\"c\":null}", "{\"a\":\"b\",\"c\":");
    assertSanitized(
        "{\"1e0001234567890123456789123456789123456789\":0}",
        // Exponent way out of representable range in a JS double.
        "{1e0001234567890123456789123456789123456789:0}"
                    );
    // This is an odd consequence of the way we recode octal literals.
    // Our octal recoder does not fail on digits '8' or '9'.
    assertSanitized("-2035208041", "-016923547559");
  }

  @Test
  public static final void testIssue3() {
    // These triggered index out of bounds and assertion errors.
    assertSanitized("[{\"\":{}}]", "[{{},\u00E4");
    assertSanitized("[{\"\":{}}]", "[{{\u00E4\u00E4},\u00E4");
  }

  @Test
  public static final void testIssue4() {
    // Make sure that bare words are quoted.
    assertSanitized("\"dev\"", "dev");
    assertSanitized("\"eval\"", "eval");
    assertSanitized("\"comment\"", "comment");
    assertSanitized("\"fasle\"", "fasle");
    assertSanitized("\"FALSE\"", "FALSE");
    assertSanitized("\"dev/comment\"", "dev/comment");
    assertSanitized("\"devcomment\"", "dev\\comment");
    assertSanitized("\"dev\\ncomment\"", "dev\\ncomment");
    assertSanitized("[\"dev\", \"comment\"]", "[dev\\, comment]");
  }

  @Test
  public static final void testMaximumNestingLevel() {
    String nestedMaps = "{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}";
    String sanitizedNestedMaps = "{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{\"\":{}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}";

    boolean exceptionIfTooMuchNesting = false;
    try {
      assertSanitized(sanitizedNestedMaps, nestedMaps, DEFAULT_NESTING_DEPTH);
    } catch (ArrayIndexOutOfBoundsException e) {
      Logger.getAnonymousLogger().log(Level.FINEST, "Expected exception in testing maximum nesting level", e);
      exceptionIfTooMuchNesting = true;
    }
    assertTrue("Expecting failure for too nested JSON", exceptionIfTooMuchNesting);
    assertSanitized(sanitizedNestedMaps, nestedMaps, DEFAULT_NESTING_DEPTH + 1);
  }

  @Test
  public static final void testMaximumNestingLevelAssignment() {
    assertEquals(1, new JsonSanitizer("", Integer.MIN_VALUE).getMaximumNestingDepth());
    assertEquals(JsonSanitizer.MAXIMUM_NESTING_DEPTH, new JsonSanitizer("", Integer.MAX_VALUE).getMaximumNestingDepth());
  }

  @Test
  public static final void testClosedArray() {
    // Discovered by fuzzer with seed -Dfuzz.seed=df3b4778ce54d00a
    assertSanitized("-68348121520322", "\ufeff-01742461140214282]");
  }

  @Test
  public static final void testIssue13() {
    assertSanitized(
        "[ { \"description\": \"aa##############aa\" }, 1 ]",
        "[ { \"description\": \"aa##############aa\" }, 1 ]");
  }
}
