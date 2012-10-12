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

import junit.framework.TestCase;

import org.junit.Test;

import com.google.json.EvalMinifier.NameGenerator;

public final class EvalMinifierTest extends TestCase {

  private void assertMinified(String golden, String input) {
    String actual = EvalMinifier.minify(JsonSanitizer.sanitize(input));
    assertEquals(input, golden, actual);
  }

  @Test
  public final void testMinify() {
    assertMinified("null", "null");
    assertMinified("[null,null]", "[null,null]");
    assertMinified("[null,null,null,null]", "[null,null,null,null]");
    assertMinified(
        "[null,null,null,null,null,null,null]",
        "[null,null,null,null,null,null,null]");
    assertMinified(
        "[null,null,null,null,null,null,null,null,null,null,null,null,null]",
        "[null,null,null,null,null,null,null,null,null,null,null,null,null]");
    assertMinified(
        "(function(a){"
        + "return[a,a,a,a,a,a,a,a,a,a,a,a,a,a,a,a,a,a,a,a,a,a,a,a,a,a]}(null))",
        "[null,null,null,null,null,null,null,null,null,null,null,null,null, "
        + "null,null,null,null,null,null,null,null,null,null,null,null,null]");
    assertMinified(
        "(function(a,b){return"
        + "[a,b,a,b,a,b,a,b,a,b,a,b,a,b,a,b,a,b,a,b,a,\"z\"]"
        + "}(\"foo\",\"bar\"))",
        "['foo','bar','foo','bar','foo','bar','foo','bar','foo','bar','foo',"
        + "'bar','foo','bar','foo','bar','foo','bar','foo','bar','foo','z']");
  }

  @Test
  public final void testNameGenerator() {
    NameGenerator ng = new NameGenerator();
    assertEquals("a", ng.next());
    assertEquals("b", ng.next());
    assertEquals("c", ng.next());
    for (int i = 30; --i >= 0;) { ng.next(); }
    assertEquals("H", ng.next());
    for (int i = 30; --i >= 0;) { ng.next(); }
    assertEquals("ak", ng.next());
    for (int i = 511; --i >= 0;) { ng.next(); }
    assertEquals("im", ng.next());
    // "in" is a reserved word.
    assertEquals("io", ng.next());
  }

}
