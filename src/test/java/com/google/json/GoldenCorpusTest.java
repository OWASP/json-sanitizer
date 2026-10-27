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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.jupiter.api.Test;

/**
 * Replays the golden corpus recorded from the legacy build (567,123 inputs,
 * seven calls each) and requires byte-identical output. A failure prints which
 * blocks differ; {@code GoldenCorpus dump} shows their inputs and outputs.
 */
@SuppressWarnings("javadoc")
public final class GoldenCorpusTest {
  @Test
  public final void testOutputIsByteIdenticalToTheLegacyBaseline() throws Exception {
    URL digests = GoldenCorpusTest.class.getResource("golden-digests.txt");
    assertNotNull(digests, "golden-digests.txt is not on the test classpath");
    String file = new File(digests.toURI()).getPath();
    assertTrue(GoldenCorpus.verify(file), "golden corpus differs from " + file);
  }
}
