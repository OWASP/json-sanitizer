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

import org.junit.Test;

import junit.framework.TestCase;

public final class JsonSanitizerTest extends TestCase {

  private static void assertSanitized(String golden, String input) {
    String actual = JsonSanitizer.sanitize(input);
    assertEquals(input, golden, actual);
    if (actual.equals(input)) {
      assertSame(input, input, actual);
    }
  }

  @Test
  public final void testSanitize() {
    assertSanitized("null", "");
    assertSanitized("null", "null");
    assertSanitized("false", "false");
    assertSanitized("true", "true");
    assertSanitized(" false ", " false ");
    assertSanitized("  false", "  false");
    assertSanitized("false\n", "false\n");
    assertSanitized("false", "false,true");
    assertSanitized("\"foo\"", "\"foo\"");
    assertSanitized("\"foo\"", "'foo'");
    assertSanitized(
        "\"<script>foo()<\\/script>\"", "\"<script>foo()</script>\"");
    assertSanitized(
        "\"<script>foo()<\\/script>\"", "\"<script>foo()</script>\"");
    assertSanitized("\"<\\/SCRIPT\\n>\"", "\"</SCRIPT\n>\"");
    assertSanitized("\"<\\/ScRIpT\"", "\"</ScRIpT\"");
    assertSanitized("\"<\\/ScR\u0130pT\"", "\"</ScR\u0130pT\"");
    assertSanitized("\"<b>Hello</b>\"", "\"<b>Hello</b>\"");
    assertSanitized("\"<s>Hello</s>\"", "\"<s>Hello</s>\"");
    assertSanitized("\"<[[\\u005d]>\"", "'<[[]]>'");
    assertSanitized("\"\\u005d]>\"", "']]>'");
    assertSanitized("[[0]]", "[[0]]>");
    assertSanitized("[1,-1,0.0,-0.5,1e2]", "[1,-1,0.0,-0.5,1e2,");
    assertSanitized("[1,2,3]", "[1,2,3,]");
    assertSanitized("[1,null,3]", "[1,,3,]");
    assertSanitized("[1 ,2 ,3]", "[1 2 3]");
    assertSanitized("{ \"foo\": \"bar\" }", "{ \"foo\": \"bar\" }");
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
    assertSanitized("1.0", "1.0");
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
    assertSanitized("{}", "{}");
    assertSanitized("{}", "({})");
  }

}
