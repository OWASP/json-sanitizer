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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The output differences from the legacy release that have been approved
 * (brief Q4; analysis/json-sanitizer/APPROVED_DIFFERENCES.md lists the same
 * classes and counts). Each class names the inputs it covers by a predicate on
 * the input and the shape of the change, never by the output alone, so a
 * change outside the approved fixes stays unexplained and fails
 * GoldenCorpusTest.
 */
final class ApprovedDifferences implements GoldenCorpus.Classifier {

  /** Differing (input, leg) pairs expected per class over the golden corpus. */
  static final Map<String, Integer> EXPECTED_COUNTS;
  static {
    Map<String, Integer> m = new LinkedHashMap<String, Integer>();
    EXPECTED_COUNTS = Collections.unmodifiableMap(m);
  }

  public String classify(String input, int leg, String legacy, String current) {
    return null;
  }
}
