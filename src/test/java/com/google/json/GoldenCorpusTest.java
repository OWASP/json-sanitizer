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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;

import org.junit.jupiter.api.Test;

/**
 * Replays the golden corpus recorded from the legacy build (567,123 inputs,
 * seven calls each) and requires byte-identical output. A failure prints which
 * blocks differ; {@code GoldenCorpus dump} shows their inputs and outputs.
 */
@SuppressWarnings("javadoc")
public final class GoldenCorpusTest {

  /**
   * SHA-256 of golden-digests.txt as recorded from the legacy classes (commit
   * d9b2768, JDK 11). Pinned so the baseline cannot be re-recorded from the
   * build under test without a reviewed change to this line; an approved
   * output change is listed in analysis/json-sanitizer/APPROVED_DIFFERENCES.md.
   */
  private static final String RECORDED_SHA256 =
      "675b17ede58656aca2318ae8571af4b14e1b2f4abc297189affcfd6fdff8c89c";

  @Test
  public final void testOutputIsByteIdenticalToTheLegacyBaseline() throws Exception {
    URL digests = GoldenCorpusTest.class.getResource("golden-digests.txt");
    assertNotNull(digests, "golden-digests.txt is not on the test classpath");
    String file = new File(digests.toURI()).getPath();
    assertEquals(RECORDED_SHA256, sha256(file), "golden-digests.txt is not the recorded legacy baseline");
    assertTrue(GoldenCorpus.verify(file), "golden corpus differs from " + file);
  }

  private static String sha256(String file) throws Exception {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    InputStream in = new FileInputStream(file);
    try {
      byte[] buf = new byte[8192];
      for (int n; (n = in.read(buf)) >= 0;) { md.update(buf, 0, n); }
    } finally {
      in.close();
    }
    return GoldenCorpus.hex(md.digest());
  }
}
