package com.google.json;

import junit.framework.TestCase;

public final class JsonSanitizerTest extends TestCase {

  private static void assertSanitized(String golden, String input) {
    String actual = JsonSanitizer.sanitize(input);
    assertEquals(input, golden, actual);
    if (actual.equals(input)) {
      assertSame(input, input, actual);
    }
  }

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
    assertSanitized("\"<b>Hello</b>\"", "\"<b>Hello</b>\"");
    assertSanitized("\"<s>Hello</s>\"", "\"<s>Hello</s>\"");
    assertSanitized("\"<[[\\u005d]>\"", "'<[[]]>'");
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
