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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The output checks of {@link ApprovedDifferences} accept the approved change
 * and reject look-alikes, including the counterexamples of the Phase 3
 * security review.
 */
@SuppressWarnings("javadoc")
public final class ApprovedDifferencesTest {

  @Test
  public final void testSec004CheckRejectsUnsafeOrMalformedPools() {
    String input = "[12345e+1,12345e+1,12345e+1,12345e+1,12345e+1,12345e+1,12345e+1,12345e+1,12345e+1,12345e+1,12345e+1,12345e+1]";
    String legacy = com.google.json.legacy.EvalMinifier.minify(input);
    assertTrue(ApprovedDifferences.minifierKeepsExponentSigns(input, legacy, EvalMinifier.minify(input)));
    // An extra pooled value carrying a breakout, unused in the body.
    assertFalse(ApprovedDifferences.minifierKeepsExponentSigns(input, legacy,
        "(function(a,b){return[a,a,a,a,a,a,a,a,a,a,a,a]}(12345e+1,</script><script>alert`1`//))"));
    // A comment smuggled into the parameter list.
    assertFalse(ApprovedDifferences.minifierKeepsExponentSigns(input, legacy,
        "(function(a,/*</script>*/b){return[a,a,a,a,a,a,a,a,a,a,a,a]}(12345e+1,0))"));
    // A close bracket in a value slot ends the parse instead of looping.
    assertFalse(ApprovedDifferences.minifierKeepsExponentSigns(input, legacy,
        "(function(a){return[a,a,a,a,a,a,a,a,a,a,a,a]}(]))"));
  }

  @Test
  public final void testSec006CheckTakesTheSignFromTheKey() {
    String input = "{1e-2147483648:true}";
    String legacy = com.google.json.legacy.JsonSanitizer.sanitize(input);
    assertTrue(ApprovedDifferences.exponentNoLongerWraps(input, legacy, "{\"1e-2147483648\":true}"));
    assertFalse(ApprovedDifferences.exponentNoLongerWraps(input, legacy, "{\"1e+2147483648\":true}"));
  }

  @Test
  public final void testVerticalTabCheckOnlyAcceptsRealEscapes() {
    // An escaped backslash, the text u0008, then a \v escape.
    String input = "[\"\\\\u0008\\v\"]";
    String legacy = com.google.json.legacy.JsonSanitizer.sanitize(input);
    assertTrue(ApprovedDifferences.verticalTabEscapesNowU000b(input, legacy, JsonSanitizer.sanitize(input)));
    assertFalse(ApprovedDifferences.verticalTabEscapesNowU000b(input, legacy,
        "[\"\\\\u000b\\u0008\"]"));
  }
}
