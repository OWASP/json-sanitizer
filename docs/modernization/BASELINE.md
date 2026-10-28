# Baseline: json-sanitizer (legacy build, before the uplift)

- **Date:** 2026-10-27
- **Source:** `legacy/json-sanitizer` → `/Users/jmanico/Dropbox/github/json-sanitizer` at commit `d9b2768` (master). Built from a scratch copy; the source repository was not built in.
- **Runtime (the oracle):** OpenJDK 11.0.32.1 (Homebrew `openjdk@11`), Apache Maven 3.10.0, the **legacy `pom.xml` unchanged** (`maven.compiler.source/target` 1.6, JUnit 4.13.1, surefire from the Maven 3.10 super-POM). This is the source leg of the dual run.
- **Command:** `mvn -B clean test` with `JAVA_HOME` pointing at JDK 11.
- **Recorded:** `analysis/json-sanitizer/baseline/surefire-reports/TEST-*.xml` (surefire result files of that run); raw log `analysis/json-sanitizer/baseline/mvn-clean-test-jdk11.log`.
- **Fuzz seed of this run:** `-Dfuzz.seed=ea41f76fb21e5107` (the legacy `FuzzyTest` picks a random seed per run unless one is given). **It cannot be replayed by the legacy test:** the value is a negative long printed unsigned, and `Long.parseLong(seed, 16)` at `FuzzyTest.java:46` rejects it with `NumberFormatException` (RULE-063, confirmed on every JDK during Phase 1). CI replays the positive seed `6a736f6e2d73616e` instead.
- **Build facts seen in the log:** `Installing Nexus Staging features` (the nexus-staging plugin loads in every build, DELTA-04); `bootstrap class path not set in conjunction with -source 6` (DELTA-02).

## Totals

| Executed | Passed | Failed | Errors | Skipped |
|---|---|---|---|---|
| 28 | 28 | 0 | 0 | 0 |

## Per-test results

| Test | Result | Time (s) |
|---|---|---|
| `com.google.json.EvalMinifierTest#testMinify` | PASS | 0.007 |
| `com.google.json.EvalMinifierTest#testNameGenerator` | PASS | 0.002 |
| `com.google.json.EvalMinifierTest#testReservedKeywordsSorted` | PASS | 0.012 |
| `com.google.json.FuzzyTest#testSanitizerLikesFuzzyWuzzyInputs` | PASS | 0.378 |
| `com.google.json.JsonSanitizerTest#testBadNumber` | PASS | 0.001 |
| `com.google.json.JsonSanitizerTest#testCrash1` | PASS | 0.001 |
| `com.google.json.JsonSanitizerTest#testDashDashGtEscaped` | PASS | 0.001 |
| `com.google.json.JsonSanitizerTest#testDashDashGtUEscaped` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testDisallowedSubstrings` | PASS | 0.002 |
| `com.google.json.JsonSanitizerTest#testEscHtmlCommentClose` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testExcessiveNestingIsReplacedWithNull` | PASS | 0.001 |
| `com.google.json.JsonSanitizerTest#testHtmlParserStateChanges` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testInvalidOutput` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testIssue13` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testIssue3` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testIssue4` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testLongNumberInUnclosedInputWithU80` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testLongOctalNumberWithBadDigits` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testMaximumNestingLevel` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testMaximumNestingLevelAssignment` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testSanitize` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testSanitizeCharSequence` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testSanitizeReader` | PASS | 0.021 |
| `com.google.json.JsonSanitizerTest#testSanitizeStreams` | PASS | 0.001 |
| `com.google.json.JsonSanitizerTest#testSlashFour` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testUnopenedArray` | PASS | 0.001 |
| `com.google.json.JsonSanitizerTest#testUnterminatedObject` | PASS | 0.0 |
| `com.google.json.JsonSanitizerTest#testXssPayload` | PASS | 0.0 |

## Golden corpus (output oracle)

- **Recorded:** `analysis/json-sanitizer/baseline/golden-digests.txt` (139 per-block SHA-256 digests of inputs and outputs, 4,096 inputs per block, plus one total; 22 KB), produced by `analysis/json-sanitizer/baseline/tools/GoldenCorpus.java` run against the legacy classes of the JDK 11 build above. The file's header names the JDK (`recorded-on Homebrew 11.0.32.1`) and the provenance (`commit=d9b2768 pom=legacy(source/target 1.6) build-jdk=11.0.32.1 classes-sha256=414705e7...40eb61`, the digest of the legacy `target/classes`). A first recording with 256-input blocks (357 KB) was replaced by this one the same day; the inputs and outputs are the same, only the block size changed.
- **Corpus:** 567,123 inputs, 7 calls each (3,969,861 calls): `sanitize(String)`, `sanitize(CharSequence)`, `sanitize(String, 1)`, `sanitize(String, 3)`, `EvalMinifier.minify(String)`, `sanitize(Reader, Appendable)`, `sanitize(InputStream, OutputStream)` over UTF-8 bytes. Sections, in order: 165 unit-test inputs (every input of `JsonSanitizerTest` and `EvalMinifierTest` at d9b2768, `null` excepted); 39,208 number literals (hex, octal, decimal; 1 to 2,048 digits including 1,023 to 1,026; fraction and exponent tails; sign variants; each bare, as an array element and as an object key); 327,750 code-unit inputs (every UTF-16 code unit in five positions, plus supplementary and lone-surrogate samples); 200,000 fixed-seed fuzz inputs (seed `6a736f6e2d73616e`, a port of the suite's `FuzzyStringGenerator` that formats no doubles, so it is the same on every JDK). Input chars 178,731,950; largest input 351,901 chars; output chars 718,090,386.
- **Full outputs:** `analysis/json-sanitizer/baseline/golden-outputs-explicit.txt.gz` holds the input and all seven outputs for the 367,123 explicit inputs (indices 0 to 367122; tab-separated, control characters and surrogates as `\uXXXX`). The fuzz section is kept as digests only: its inputs regenerate from the seed, and `GoldenCorpus dump <from> <to> <file>` prints any range on demand.
- **Verified JDK-independent:** `GoldenCorpus verify` against the same legacy classes reports byte-identical on JDK 17.0.20.1, 21.0.12.1 and 25.0.4.1 (so a later mismatch is the build or the code, not the JDK or the corpus). The uplifted build then reproduced it on Zulu 8.0.512, 11, 17, 21 and 25 (`modernized/json-sanitizer-uplifted/UPLIFT_NOTES.md`).
- **Replay:** build the tool with `javac --release 8 -encoding UTF-8 -cp <classes> -d out GoldenCorpus.java`, then `java -cp out:<classes> GoldenCorpus verify analysis/json-sanitizer/baseline/golden-digests.txt`; about 20 s.
- **Note on the brief's wording:** the `DeltaProbe` program from the delta-catalog run was not preserved by that run, so the number corpus above was built fresh for this tool and supersedes it.
