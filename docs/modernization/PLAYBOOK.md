# Playbook: json-sanitizer same-stack uplift (Phase 1, proven on the pilot)

- **Date:** 2026-10-27
- **Scope of this recipe:** Phase 1 of `MODERNIZATION_BRIEF.md`: move the build from Java 6/7 bytecode on an unpinned toolchain to Java 8 bytecode on a pinned one, with live CI, and prove the library's behavior did not change. One Maven module, so the pilot was the whole build. **No file under `src/` changes in this phase.**
- **Working copy:** `modernized/json-sanitizer-uplifted/` (its own git history; seed commit `9332747` is the legacy tree at `d9b2768`). `legacy/json-sanitizer` is a symlink to the real repository and is never built in or edited.
- **Oracle:** `analysis/json-sanitizer/BASELINE.md` (28 tests, legacy pom on JDK 11) and `analysis/json-sanitizer/baseline/golden-digests.txt` (567,123 inputs, 7 calls each, recorded from the legacy JDK 11 classes).

## 1. Environment facts you must know (discovered, not assumed)

| Fact | Detail |
|---|---|
| JDKs on this Mac | Homebrew `openjdk@11/17/21/25/27`, all keg-only: nothing is on `PATH`, `/usr/libexec/java_home` finds none. Always set `JAVA_HOME=/opt/homebrew/opt/openjdk@<v>/libexec/openjdk.jdk/Contents/Home` explicitly. |
| Maven | 3.10.0 (`brew`). `.claude/settings.local.json` in the source repo points `JAVA_HOME` at JDK 17 for Claude sessions, so `mvn -v` says 17 unless you override it. The uplifted pom's enforcer **requires JDK 21+ to run Maven**; builds on 17 fail at `validate` with a clear message. |
| JDK 8 (arm64) | Not in Homebrew; the `zulu@8` cask is a `.pkg` needing an administrator password. Use Azul's tarball instead: `https://cdn.azul.com/zulu/bin/zulu8.98.0.107-ca-jdk8.0.512-macosx_aarch64.tar.gz`, SHA-256 `f26e2a91062542cf22ef2171b2556e1c3f42cf87cf174258b3df7840365892aa` (from `https://api.azul.com/metadata/v1/zulu/packages/`). `tar xzf`, then `JAVA8=<dir>/zulu8.98.0.107-ca-jdk8.0.512-macosx_aarch64/Contents/Home`. No installation. |
| How a test leg runs on another JDK | `mvn -B -ntp clean verify -Djvm=$JAVA8/bin/java`. Surefire 3.x forks the tests on that binary; compile, javadoc, jar and the enforcer stay on the build JDK. JaCoCo's agent (set through `argLine`) loads inside the forked JVM. Proof that the leg ran where claimed: `grep 'name="java.version"' target/surefire-reports/TEST-*.xml`. |
| Plugin versions | Take them from `https://repo1.maven.org/maven2/<group path>/<artifact>/maven-metadata.xml` (`<release>` or the last GA 3.x). The `search.maven.org` index was stale for jacoco, surefire and spotbugs. Stay on the 3.x lines; the 4.0.0 betas target Maven 4. |
| Expected warnings | javac 21: `source value 8 is obsolete and will be removed in a future release` (twice per compile, DELTA-08). javadoc 3.12.0: two doclint warnings (`package-info.java:2`, `EvalMinifier.java:33`). Both are warnings only; a build that shows anything else new is a finding. |
| Must be absent | `Installing Nexus Staging features` in the build log (DELTA-04). `grep -c` must print 0. |
| Fuzz seed | `-Dfuzz.seed=<hex>` reaches the forked test JVM (the log shows `Fuzzing with -Dfuzz.seed=<hex>`). In Phase 1 **the value had to be a positive long**: the legacy `FuzzyTest` parsed it with `Long.parseLong(…, 16)`, so a 16-digit seed starting with 8-f (including the baseline's own `ea41f76fb21e5107`) threw `NumberFormatException` (RULE-063). Since Phase 2 (`6f20049`) every printed seed replays. CI uses `6a736f6e2d73616e`. |
| Maven 3.10 defaults | Maven 3.10's super-POM sets `project.build.outputTimestamp=1980-02-01T00:00:00Z` and picks jar plugin 3.5.1, so the local legacy and uplifted jars both have 1980 entry timestamps and a `Java-Version` manifest header. CI's Maven 3.9 does neither unless the pom says so; the uplifted pom now sets the timestamp and pins the jar plugin. |
| Tooling | `actionlint` is installed (`/opt/homebrew/bin/actionlint`); `pyyaml` is not, use `ruby -ryaml` to parse YAML. `gh` is logged in (used for the action SHAs). |

## 2. Ordered edits for the unit (what the pilot did, in order)

1. **`pom.xml` properties:** replace `maven.compiler.source`/`target` 1.6 with `<maven.compiler.release>8</maven.compiler.release>`; delete the `jdk12on` profile (it set 1.7 on JDK 12+).
2. **`pom.xml` `<build><plugins>`:** remove `nexus-staging-maven-plugin` (its `<extensions>true</extensions>` is why it loaded in every build). In its place add `maven-enforcer-plugin` 3.6.3 (`requireMavenVersion [3.9.0,4)`, `requireJavaVersion [21,)`, each with a `<message>` saying why), `maven-compiler-plugin` 3.16.0 (no configuration; `release` comes from the property) and `maven-surefire-plugin` 3.6.1 with `<failIfNoTests>true</failIfNoTests>`. Add `<project.build.outputTimestamp>1980-02-01T00:00:00Z</project.build.outputTimestamp>` next to the release property (reproducible jar on every Maven version).
3. **`pom.xml` versions:** source 3.4.0, javadoc 3.12.0, jacoco 0.8.16, spotbugs 4.10.5.0 (reporting), gpg 3.2.8 (release profile), site 3.22.0, project-info 3.9.0, sonar 5.1.0.4751; add clean 3.5.0, resources 3.5.0, jar 3.5.1, install 3.2.0, deploy 3.2.0 to `pluginManagement`. Leave `distributionManagement` (OSSRH) alone: Phase 4 replaces it with the Central Portal.
4. **Leave everything else in the pom verbatim** (comments, order, the jacoco `destFile`/`dataFile` config, the javadoc `nohelp`/`show`/`doctitle` config).
5. **`.github/golden/`:** copy `analysis/json-sanitizer/baseline/tools/GoldenCorpus.java` and `analysis/json-sanitizer/baseline/golden-digests.txt` verbatim (`cmp` them afterwards). The digests were recorded with `GoldenCorpus record <file> --provenance "commit=<legacy commit> pom=legacy(...) build-jdk=<v> classes-sha256=<sha256 of the sorted legacy target/classes>"`; a re-recording must come from the legacy classes, never the uplifted ones.
6. **`.github/workflows/ci.yml`:** one job, matrix `test-jdk: [8, 11, 17, 21, 25]`, `timeout-minutes`, a `concurrency` group; `setup-java` for the test JDK first, then for 21 (the last call owns `JAVA_HOME`); locate the test JVM through `JAVA_HOME_<major>_${{ runner.arch }}`; `mvn -B -ntp clean verify -Djvm=... -Dfuzz.seed=$FUZZ_SEED`; **assert the leg** (every `TEST-*.xml` has `java.specification.version` equal to the matrix JDK, `1.8` for 8, and the summed `tests` attributes are at least `BASELINE_TESTS`); class-file major version check on the classes unpacked from the built jar (`find ... -exec "$JAVA_HOME/bin/javap" -v {} + | sed -n 's/^ *major version: //p' | sort -u` must be exactly `52`); golden-corpus replay with the test JVM; `upload-artifact` of `target/surefire-reports` when a step failed. Pin actions to commit SHAs (`gh api repos/<owner>/<repo>/git/ref/tags/<tag>`, dereference annotated tags through `git/tags/<sha>`). Run `actionlint` before committing; shellcheck rejects `for x in $(find ...)` and `ls | grep`.
7. **Remove `.travis.yml`; README line 1:** replace the Travis `<img>` badge with the Actions badge for `ci.yml`.
8. Commit in the working copy only.

## 3. The exact proof (what "done" means for the unit)

Run from `modernized/json-sanitizer-uplifted/` with `JAVA_HOME` = JDK 21. For each test JVM `J` in Zulu 8, Homebrew 11, 17, 21, 25:

```bash
mvn -B -ntp clean verify -Djvm="$J/bin/java" -Dfuzz.seed=6a736f6e2d73616e > build-$v.log 2>&1   # exit 0, "Tests run: 28, Failures: 0, Errors: 0, Skipped: 0"
grep -c 'Fuzzing with -Dfuzz.seed=6a736f6e2d73616e' build-$v.log                 # 1: the fork received the seed
grep -c 'Installing Nexus Staging' build-$v.log                             # 0
grep -h -o 'name="java.specification.version" value="[^"]*"' target/surefire-reports/TEST-*.xml | sort -u   # the leg's version (1.8 for 8)
python3 "$PLUGIN/scripts/baseline_diff.py" analysis/json-sanitizer/BASELINE.md --junit target/surefire-reports
                                                                            # "OK: nothing regressed and 28 test(s) executed"
mkdir -p target/golden && javac --release 8 -encoding UTF-8 -cp target/classes -d target/golden .github/golden/GoldenCorpus.java
"$J/bin/java" -cp target/golden:target/classes GoldenCorpus verify .github/golden/golden-digests.txt
                                                                            # "GOLDEN CORPUS: byte-identical to ..." (about 20 s)
unzip -q target/json-sanitizer-<version>.jar 'com/*' -d target/jar-check
find target/jar-check -name '*.class' -exec javap -v {} + | sed -n 's/^ *major version: //p' | sort -u   # 52
```

Pilot timings on this machine: `mvn clean verify` 12 to 20 s per leg; the corpus 17 to 25 s per leg. The independent review (architecture-critic) and its dispositions are in `modernized/json-sanitizer-uplifted/UPLIFT_NOTES.md`.

## 4. Errors hit and how they were resolved

| Error | Fix |
|---|---|
| `actionlint`: SC2044 "for loops over find output are fragile" in the bytecode step | `find ... -exec javap -v {} +` into one `sed | sort -u`, then compare the whole result with `52`. |
| `python3 -c "import yaml"`: `ModuleNotFoundError` | Parse the workflow with `ruby -ryaml` (ships with macOS). |
| `claude plugin configure --values-stdin` rejected booleans | Every option value must be a JSON string (`"true"`), not a boolean. (Plugin setup, not the build.) |
| `FuzzyTest`: `NumberFormatException: For input string: "ea41f76fb21e5107" under radix 16` on every leg when CI replayed the baseline's seed | The legacy test cannot parse a negative seed (RULE-063). Use a positive seed (`6a736f6e2d73616e`) until Phase 2 fixes the parsing. |
| `actionlint`: SC2013 (`for n in $(grep ...)`) and SC2010 (`ls \| grep`) | Sum with `$(( $(... \| paste -sd+ -) ))`; pick the jar with a `for f in target/*.jar; case ... esac` loop. |

No compile, javadoc or jacoco error occurred on any leg. `mvn site` with the new reporting pins (site 3.22.0, project-info 3.9.0, spotbugs 4.10.5.0) ran once on JDK 21: BUILD SUCCESS. gpg 3.2.8 and sonar 5.1.0.4751 are unexercised (Phase 4).

## 5. Gaps (what the pilot could not prove here)

- **GitHub Actions has not run yet.** The workflow is validated by `actionlint` and parses, but Q11 (Actions on the OWASP organization) is answered only at the first push. The `JAVA_HOME_<major>_X64` variable names are those documented by `actions/setup-java` for x64 runners; an arm64 runner would need `_ARM64`.
- **Temurin vs Zulu on the 8 leg.** The local Java 8 leg ran on Azul Zulu 8.0.512; CI will use Temurin 8. Both are OpenJDK 8u builds, and the corpus verified identical on five vendor builds already, but the Temurin result is still unmeasured until CI runs.
- **Not in Phase 1 by design:** JUnit 5 migration, fixed fuzz seed, Jazzer (Phase 2); the security fixes (Phase 3); Central Portal publishing, JSpecify, `Automatic-Module-Name`, docs (Phase 4).

## 6. Phase 2 addendum: test harness (2026-10-27)

Same working copy, commits `d157b20`..`76f7bc7` on top of `475f264` (results and review dispositions: `modernized/json-sanitizer-uplifted/UPLIFT_NOTES.md`, Phase 2). No file under `src/main/` changed. The ordered steps, each its own commit, each proven before the next:

1. **JUnit 5 (`d157b20`).** Import `org.junit:junit-bom` **5.14.4** in `dependencyManagement`, depend on `junit-jupiter` (test), delete `junit:junit`. Do not add the vintage engine. In each test class: drop `extends TestCase`, change `public static final void test…` to `public final void test…` (Jupiter silently skips static methods: DELTA-01), switch `@Test` to `org.junit.jupiter.api.Test` and the asserts to static imports of `org.junit.jupiter.api.Assertions`. Then **reorder every assertion that carries a message**: JUnit 3/4 puts it first, Jupiter last. `assertEquals(String, String, String)` and `assertSame(Object, Object, Object)` still compile with the wrong meaning (four such sites here); `assertTrue(String, boolean)` does not compile. Mutation-check each reordered site (break its expected side, the test must fail).
2. **FuzzyTest (`6f20049`).** Seed parsing is `Long.parseUnsignedLong(hex, 16)` (brief Q12); diagnostic fixes at the "After:" line and in `excerpt()`.
3. **Golden corpus (`ef4de26`).** `git mv` the tool into `src/test/java/com/google/json/GoldenCorpus.java` (add the package, drop the two `com.google.json` imports) and the digests into `src/test/resources/com/google/json/golden-digests.txt` (`cmp` with `analysis/json-sanitizer/baseline/golden-digests.txt`). `GoldenCorpusTest` runs `verify` on the classpath copy. Delete `.github/golden/` and the CI replay step.
4. **Contract and stream tests (`c58fc07`).** `BehaviorContractTest` (one test per P0 rule) and `StreamCharacterizationTest` (DELTA-11). Expected values: print the actual output on all five JDKs first, compare with the rule cards, then pin.
5. **Jazzer (`6c8880b`).** `com.code-intelligence:jazzer-junit` 0.30.0 and `com.fasterxml.jackson.core:jackson-core` 2.22.4, both test scope. Seeds in `src/test/resources/com/google/json/JsonSanitizerFuzzTestInputs/sanitizeProperties/`. Fuzz job `JAZZER_FUZZ=1 mvn -B -ntp test -Dtest='JsonSanitizerFuzzTest#sanitizeProperties' -Djacoco.skip=true`, which must report at least 280 s (`Done N runs in S second(s)`).
6. **Review fixes (`76f7bc7`).** A summed `tests=` count passes with skipped tests (a `@Disabled` golden test kept CI green): gate on names instead, with `.github/scripts/check_test_reports.py <java> .github/required-tests.txt <min-seed-replays> target/surefire-reports`. Pin the digests' SHA-256 in `GoldenCorpusTest`, and in `.gitattributes` keep the digests `eol=lf` and the seeds `binary` (the repo has `* text=auto`).

| Fact | Detail |
|---|---|
| JUnit 6 | Released (6.1.3) but **requires Java 17**: stay on 5.x while the tests run on Java 8. |
| Jazzer on Java 8 | jazzer-junit 0.30.0 is class-file 52 and its **seed-replay mode works on Zulu 8** (and 11-25); no leg needs an exclusion. Its transitive JUnit 5.9 is overridden by the BOM. |
| Jazzer warnings | On JDK 21+ the forked JVM prints `A Java agent has been loaded dynamically` (Jazzer's byte-buddy agent). `-XX:+EnableDynamicAgentLoading` would silence it but does not exist on Java 8; harmless today, a future JDK may disallow it (residual). |
| Jazzer output | Fuzzing writes a finding as `crash-<sha1>` **into the seed directory under `src/test/resources`** (so it replays on every later run until removed or fixed) and its working corpus into `.cifuzz-corpus/` (git-ignored). Local throughput on this Mac: about 9,400 to 23,000 exec/s. |
| Decoding a finding by hand | `FuzzedDataProviderImpl.withJavaData(bytes).consumeRemainingAsString()` reproduces the input, but the class needs `com/code_intelligence/jazzer/runtime/jazzer_bootstrap.jar`, which is **nested inside** `jazzer-0.30.0.jar`: unzip it and put it on the classpath. |
| Jackson as the RFC 8259 oracle | 2.x only (3.x needs Java 17). Lift `StreamReadConstraints` (`maxNumberLength` 1000 and `maxNestingDepth` 1000 by default would reject valid sanitizer output). Jackson **rejects a lone surrogate escape in an object key** (`{"\uDDDD":1}`) though it accepts one in a value; RFC 8259 allows both, so the oracle rewrites `\uD800`-`\uDFFF` escapes to `\uFFFD` first (owner decision, brief Phase 2). |
| Stream decoding across JDKs | The UTF-8 replacement behavior (one U+FFFD per bad byte for overlong, above-U+10FFFF and 5-byte forms; one for truncated and CESU-8 sequences) and the BOM handling are identical on Zulu 8 and Homebrew 11, 17, 21, 25. |

**Proof for Phase 2**, per leg as in section 3, after `rm -rf .cifuzz-corpus` (the separate corpus replay is gone; `GoldenCorpusTest` prints `GOLDEN CORPUS: byte-identical`): `Tests run: 50, Failures: 0, Errors: 0, Skipped: 0`; `check_test_reports.py` "42 of 42 required tests ran; 8 seed replays"; `baseline_diff.py` "OK: nothing regressed and 50 test(s) executed" (the 28 baseline names are matched); `git diff --stat 475f264 -- src/main` empty.

**GitHub Actions** ([run 40599699046](https://github.com/OWASP/json-sanitizer/actions/runs/40599699046), [run 40600511953](https://github.com/OWASP/json-sanitizer/actions/runs/40600511953)): the same 50 tests and 42 of 42 required on every Temurin leg; the `fuzz` job about 1 million inputs in 301 s on a hosted runner (3,100 exec/s), no finding. **A GitHub step without `shell:` runs under `bash -e`, without pipefail:** any `cmd | tee` step hides `cmd`'s failure, so the workflow sets `defaults.run.shell: bash` (`92dc76b`).

**Phase 2 gaps:** Jazzer's dynamic-agent warning on JDK 21+ cannot be silenced while the 8 leg exists; the organization's Actions budget (Q11) is unknown.

## 7. Phase 3 addendum: approved output changes (2026-10-28)

Commits `e4999eb`..`c4372ab` on top of the Phase 2 working copy; branch `security-fixes` on GitHub. Each fix is one commit carrying its own test, and its approved class when it changes corpus output.

**Order that worked:** slice 0 first (the dual-run corpus test, allowing zero differences), then one slice per fix. Each slice: write the test; stash `src/main` and see the test fail; restore and see it pass; run `GoldenCorpusTest`; add or extend the fix's class in `ApprovedDifferences` until `unexplained: 0`; pin the count; commit.

| Fact | Detail |
|---|---|
| Oracle | `src/test/java/com/google/json/legacy/` holds `JsonSanitizer` and `EvalMinifier` from `d9b2768` with only the package line and a comment changed (`git show 9332747:src/main/... \| diff`, 6 lines each). It imports `javax.annotation` (jsr305), so jsr305 must stay on the **test** classpath when the main code drops it. |
| Explaining a difference | Prefer a **rewrite of the input** that the legacy oracle replays into exactly the new output (SEC-001, SEC-005, `\v`). Fall back to a narrow **output check** only where legacy cannot express the fix (SEC-006 exponents past the int range, SEC-004 pooling, backslashes the main loop does not treat as escapes). |
| Traps hit | (1) A context-free `\v` rewrite fails where the main loop drops a stray backslash (start of an unquoted token) and where legacy drops the backslash of a `\u` escape ending an unquoted token. (2) Pooled `minify` output drops whitespace outside strings, so compare expansions against the sanitized JSON with that whitespace removed. (3) Attribution is first-match, so per-class counts move when a later fix overlaps an earlier class; pin counts only after the last slice. (4) A `-1` placeholder count makes the test print the real count instead of guessing it. |
| Writing escapes through tool input | Text like `\uFFFD` or `\u0008` in a tool's input can arrive as the decoded character. Build such text with `chr(92)` in a script, then scan the file for control characters and U+FFFD. |
| Proving "fails before" for new API | SEC-003's test does not compile on the old build; show the old behavior directly (the legacy copy hits `OutOfMemoryError` in about 120 ms at `-Xmx256m`). |
| Cost | `GoldenCorpusTest` runs both implementations: 44 to 50 s per leg locally. |

**Proof for Phase 3**, per leg after `rm -rf .cifuzz-corpus`: `Tests run: 58, Failures: 0`; `check_test_reports.py` "50 of 50 required tests ran; 8 seed replays"; `baseline_diff.py` "OK: nothing regressed"; `GoldenCorpusTest` "unexplained: 0" with the pinned per-class counts (total 70,352). Fuzzing: 978,666 inputs in 301 s, no finding.

## 8. Phase 4 addendum: contract, packaging, release path (2026-10-28)

Commits `beaa1e6`, `17af472`, `5917527`; branch `public-contract-and-release` (PR #43). No release cut.

| Fact | Detail |
|---|---|
| JSpecify on Java 8 | `org.jspecify:jspecify` 1.0.1 is class-file 52 (its `module-info` is under `META-INF/versions/9`). With `--release 8`, javadoc warns `unknown enum constant ElementType.MODULE` (from `@NullMarked`'s targets): expected, harmless. Annotate nullable private fields too (`private @Nullable StringBuilder x;`, `private boolean @Nullable [] y;`) or `@NullMarked` lies. |
| `\u` in Java comments | javac decodes `\u` escapes in comments too: `{@code \u}` followed by a non-hex character is a compile error. Write `<code>&#92;u</code>` in javadoc. |
| Central Portal | `org.sonatype.central:central-publishing-maven-plugin` 0.12.0, `<extensions>true</extensions>` inside the release profile, `publishingServerId` `central`, `autoPublish` false, `waitUntil` validated. It takes over `deploy`; `mvn -DperformRelease=true validate` resolves it without credentials. |
| Release workflow shape | Two jobs: a secret-free `verify` (tag on master via `git merge-base --is-ancestor`, tag equals pom version, POM scope check including profiles, all tests) and an environment-gated `stage` (no cache, `-DskipTests`). The gate is only real with **environment** secrets, `v*`-only deployment tags, Prevent self-review and a tag ruleset (repository settings). |
| Contract text | Generate README and javadoc lists from one script; run every example in them against the build. The review found two overclaims that read plausibly: "no raw control characters" (false: raw whitespace between tokens, raw U+007F to U+009F) and "eval-safe" (false for a repeated `__proto__` key). |
| Changelog baseline | The golden corpus baseline is `d9b2768`, not the last release (1.2.3). Diff the release tag's main code (`git log v1.2.3..d9b2768 -- src/main`) for output changes that predate the corpus. |

## 9. OSS-Fuzz (learned the hard way, 2026-10-28)

OSS-Fuzz builds master daily (google/oss-fuzz `projects/json-sanitizer/build.sh`) with `mvn package` on Ubuntu 20.04's **Maven 3.6.3 and JDK 17**; its status is in `https://oss-fuzz-build-logs.storage.googleapis.com/status.json`. Any build floor above those breaks continuous fuzzing silently: the Phase 1 enforcer (Maven 3.9+, JDK 21+) did, and it was caught a day later. The enforcer floor is now Maven 3.6.3 and JDK 17, and the CI job `oss-fuzz-build` reproduces that build, including compiling OSS-Fuzz's own fuzzers (fetched from google/oss-fuzz) against the jar. Note for Maven 3.6.3's default dependency plugin: filter `build-classpath` with `-DincludeArtifactIds=...`; `-Dmdep.includeArtifactIds` is silently ignored and yields the whole test classpath. Before raising any build requirement, check OSS-Fuzz's image first.
