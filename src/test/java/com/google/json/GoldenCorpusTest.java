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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * A dual run over the golden corpus (567,123 inputs, seven calls each): the
 * legacy release, kept verbatim in {@code com.google.json.legacy}, against the
 * build under test. The legacy copy must reproduce the digests recorded from
 * the legacy build, and every output the build under test changes must belong
 * to an approved class ({@link ApprovedDifferences}) in exactly the approved
 * number. {@code GoldenCorpus dump <first> <last> <file> [current|legacy]}
 * shows a block's inputs and outputs.
 */
@SuppressWarnings("javadoc")
public final class GoldenCorpusTest {

  /**
   * SHA-256 of golden-digests.txt as recorded from the legacy classes (commit
   * d9b2768, JDK 11). Pinned so the baseline cannot be re-recorded from the
   * build under test without a reviewed change to this line.
   */
  private static final String RECORDED_SHA256 =
      "675b17ede58656aca2318ae8571af4b14e1b2f4abc297189affcfd6fdff8c89c";

  private static String digestsFile;
  private static GoldenCorpus.Comparison comparison;

  @BeforeAll
  static void runBothImplementationsOverTheCorpus() throws Exception {
    URL digests = GoldenCorpusTest.class.getResource("golden-digests.txt");
    assertNotNull(digests, "golden-digests.txt is not on the test classpath");
    digestsFile = new File(digests.toURI()).getPath();
    long t0 = System.nanoTime();
    comparison = GoldenCorpus.compare(new ApprovedDifferences());
    System.out.println("golden corpus: " + comparison.legacy.inputs + " inputs, both implementations, "
        + GoldenCorpus.ms(t0) + " ms; differences by approved class: " + comparison.counts
        + "; unexplained: " + comparison.unexplainedCount);
  }

  @Test
  public final void testLegacyOracleReproducesTheRecordedBaseline() throws Exception {
    assertEquals(RECORDED_SHA256, sha256(digestsFile), "golden-digests.txt is not the recorded legacy baseline");
    List<String> recorded = GoldenCorpus.recordedLines(digestsFile);
    assertNotNull(recorded, "corpus parameters differ from " + digestsFile);
    assertEquals(recorded, GoldenCorpus.lines(comparison.legacy),
        "the legacy copy in com.google.json.legacy no longer reproduces " + digestsFile);
  }

  @Test
  public final void testOnlyApprovedDifferencesFromTheLegacyBaseline() {
    if (comparison.unexplainedCount != 0) {
      StringBuilder sb = new StringBuilder();
      sb.append(comparison.unexplainedCount).append(" output differences from the legacy release are not approved:");
      for (GoldenCorpus.Difference d : comparison.unexplained) { sb.append("\n  ").append(d); }
      fail(sb.toString());
    }
    assertEquals(ApprovedDifferences.EXPECTED_COUNTS, comparison.counts,
        "differences per approved class (update APPROVED_DIFFERENCES.md and ApprovedDifferences together)");
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
