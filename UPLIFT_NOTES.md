# Uplift notes: json-sanitizer, Phase 1 (toolchain, baseline, CI)

- **Date:** 2026-10-27
- **Version pair:** Java 6/7 bytecode from the legacy `pom.xml` built on JDK 11 (the oracle) → Java 8 bytecode (`maven.compiler.release` 8) built on JDK 21, tested on JDK 8, 11, 17, 21 and 25.
- **Working copy:** this directory, seeded from the repository at `d9b2768` (seed commit `9332747`). The source repository was not edited or built in.
- **Scope:** `pom.xml`, `.github/**`, `.travis.yml` (removed), the README badge. **No file under `src/` changed.**
- **Proof:** a true dual run. The legacy leg is recorded in `analysis/json-sanitizer/BASELINE.md`; every target leg below reproduced it (same 28 tests by name, no regression, golden corpus byte-identical).
- **Playbook:** `analysis/json-sanitizer/PLAYBOOK.md` holds the proven recipe, the environment facts and the gap list.

## Delta to fix

| Delta | Fix | How |
|---|---|---|
| DELTA-02 legacy pom cannot compile on JDK 21+ | `maven.compiler.release` 8; `jdk12on` profile deleted | by hand (OpenRewrite's dry run from the catalog agreed the Java 8 recipe is pom-only) |
| DELTA-03 unpinned compiler and surefire | compiler 3.16.0, surefire 3.6.1 (`failIfNoTests`), enforcer 3.6.3 (`requireMavenVersion [3.9.0,)`, `requireJavaVersion [21,)`); clean, resources, jar, install, deploy pinned in `pluginManagement` | by hand |
| DELTA-04 nexus-staging loads in every build | plugin removed from `<build><plugins>`; `distributionManagement` left for Phase 4 | by hand |
| DELTA-05 jacoco 0.8.5 cannot instrument JDK 17+ | jacoco 0.8.16 | by hand |
| DELTA-06 SpotBugs 3.1.12.2 crashes on JDK 17 | spotbugs-maven-plugin 4.10.5.0 (reporting section, unchanged role) | by hand |
| DELTA-07 outdated javadoc, source, gpg, site, project-info, sonar | javadoc 3.12.0, source 3.4.0, gpg 3.2.8, site 3.22.0, project-info 3.9.0, sonar 5.1.0.4751 | by hand |
| DELTA-08 `--release 8` flagged obsolete by javac 21+ | accepted as a warning; the enforcer pins the build JDK at 21+ and CI builds on 21 | judgment (see residuals) |
| DELTA-12 no live CI | `.github/workflows/ci.yml` (matrix, test-JVM assertion, fixed fuzz seed, bytecode check, corpus replay, report upload on failure); `.travis.yml` and the Travis badge removed | by hand |
| DELTA-14 stale docs and badge | README badge only; `docs/getting_started.md` and `RELEASE-checklist.sh` wait for Phase 4 | by hand |
| DELTA-01, -09, -10, -11, -13 | not Phase 1 (test harness, JSpecify, `Automatic-Module-Name`, stream characterization, language rewrites) | deferred by the brief |

## Dual-run diff

Build JDK 21 (Homebrew 21.0.12.1) for every leg; the test JVM varies through `-Djvm`, and every leg ran `-Dfuzz.seed=6a736f6e2d73616e` (the forked JVM's `Fuzzing with -Dfuzz.seed=...` line appears in each log). "Tests" is surefire's count on that leg; the test JVM is read back from the surefire XML `java.specification.version` property, the same check CI makes. Maven 3.10.0 locally.

| Test JVM | Tests executed | vs `BASELINE.md` (28 tests) | Golden corpus (567,123 inputs, 7 calls each) | Class-file major |
|---|---|---|---|---|
| Zulu 8.0.512 (aarch64) | 28, 0 failures | OK: nothing regressed, 28 executed | byte-identical | 52 |
| Homebrew 11.0.32.1 | 28, 0 failures | OK: nothing regressed, 28 executed | byte-identical | 52 |
| Homebrew 17.0.20.1 | 28, 0 failures | OK: nothing regressed, 28 executed | byte-identical | 52 |
| Homebrew 21.0.12.1 | 28, 0 failures | OK: nothing regressed, 28 executed | byte-identical | 52 |
| Homebrew 25.0.4.1 | 28, 0 failures | OK: nothing regressed, 28 executed | byte-identical | 52 |

Legacy leg (oracle): 28 tests, 28 passed, legacy pom on JDK 11; recorded with its surefire XML and log under `analysis/json-sanitizer/baseline/`. No `Installing Nexus Staging features` line in any target-leg log. The javadoc, sources and main jars are produced on JDK 21. The main jar's manifest differs from the legacy build's only in `Java-Version: 8` (was 6) and `Build-Jdk-Spec: 21` (was 11); both builds use jar plugin 3.5.1 and 1980-02-01 entry timestamps, now pinned in the pom so CI's Maven produces the same.

## Residual manual deltas

- **GitHub Actions has not run.** The workflow parses and is `actionlint`-clean, but its first run happens at the first push to GitHub, which also answers Q11 (Actions on the OWASP organization). Until then the Temurin 8 leg in particular is unmeasured; locally the Java 8 leg ran on Azul Zulu 8.
- **DELTA-08:** a Java 8 baseline ties the build to a JDK that still accepts `--release 8`. JDK 21, 25 and 27 do, with a warning. When a future JDK drops it, the enforcer's `[21,)` upper bound (none today) is the place to cap the build JDK, or the baseline moves to 11.
- **`distributionManagement` still names OSSRH**, and `RELEASE-checklist.sh` still describes the retired staging flow. `mvn deploy` does not work today and did not before; Phase 4 replaces both with the Central Portal.
- **javadoc 3.12.0 reports two doclint warnings** (`package-info.java:2` no comment; `EvalMinifier.java:33` default constructor). They are warnings, and fixing them means touching `src/`, which Phase 4 (docs) owns.
- **Local tooling:** the enforcer makes `mvn` on JDK 17 fail at `validate`; set `JAVA_HOME` to JDK 21 or newer (the playbook has the paths).
- **RULE-063 confirmed by the pilot:** the legacy `FuzzyTest` prints its seed with `Long.toHexString`, which gives 16 hex digits for a negative long, and reads it back with `Long.parseLong(seed, 16)`, which rejects anything above `Long.MAX_VALUE`. The baseline run's seed `ea41f76fb21e5107` therefore cannot be replayed (every leg errored with `NumberFormatException` when CI first tried it). CI uses the positive seed `6a736f6e2d73616e`; Phase 2 (Q12) fixes the parsing.
- **Untested pins:** `mvn site` with site 3.22.0, project-info 3.9.0 and spotbugs 4.10.5.0 was run once locally on JDK 21 (BUILD SUCCESS, SpotBugs report generated). gpg 3.2.8 and sonar 5.1.0.4751 are not exercised by any build here; the release profile is Phase 4's.

## Deferred modernization (deliberately not done)

JUnit 5 migration, fixed fuzz seed and Jazzer (Phase 2); SEC-001 to SEC-006 and the `\v` fix (Phase 3); JSpecify in place of jsr305, `Automatic-Module-Name`, Central Portal publishing, HTTPS SCM URLs, README guarantees and pinned docs version (Phase 4); the internal cleanups (Phase 5, optional). OpenRewrite's language-level rewrites (DELTA-13) are not applied: nothing in `src/` changed.

## Units

One unit, the whole Maven module: builds on the target (`mvn clean verify`, JDK 21) and reproduces its baseline on every test JVM listed above.

## Architecture-critic review (independent, read-only, 2026-10-27)

**Scope verdict:** no change outside `pom.xml`, `.github/**`, `.travis.yml` (deleted) and the README badge line; `src/` untouched. No instruction-shaped text in the diff. No Blocker.

| # | Finding | Disposition |
|---|---|---|
| H1 | CI never proved the tests ran on the matrix JDK, nor that 28 ran; `-Djvm` was a promise, not a proof. | **Fixed.** A step reads every `TEST-*.xml` and fails unless `java.specification.version` equals the matrix JDK (`1.8` for 8) and the summed `tests` attribute is at least `BASELINE_TESTS` (28). The same check ran locally on all five legs. |
| M1 | The fuzz leg is nondeterministic (DELTA-12 asked for a fixed seed). | **Fixed, with a twist.** `-Dfuzz.seed` is passed on every leg. The baseline's own seed is unusable (RULE-063 above), so the seed is the positive `6a736f6e2d73616e`. The one-random-seed job waits for Phase 2 (Jazzer). |
| M2 | "Does not change with the Maven version" was false for the jar: Maven 3.10's super-POM sets `project.build.outputTimestamp=1980-02-01T00:00:00Z`, CI's Maven 3.9 would not; the enforcer admitted Maven 4. | **Fixed.** `project.build.outputTimestamp` is set explicitly (the same value), the enforcer is bounded to `[3.9.0,4)` with a message, and the comment now claims only what it does (plugin versions). |
| M3 | jar plugin 3.5.1 adds `Java-Version` to the manifest. | **Kept, recorded.** The legacy baseline build under Maven 3.10 already used 3.5.1 and carries `Java-Version: 6`; the pin makes CI match the local artifact. Manifest difference recorded above. |
| M4 | Golden tooling in two places with no provenance; digests could be re-recorded from the uplifted classes unnoticed. | **Partly fixed.** `golden-digests.txt` now carries `# provenance commit=d9b2768 pom=legacy(source/target 1.6) build-jdk=11.0.32.1 classes-sha256=4147...eb61`; the repo copy is byte-identical to `analysis/json-sanitizer/baseline/golden-digests.txt`. CODEOWNERS or branch protection for `.github/golden/` is a repository setting for the maintainer. A `Proposed revision:` line in the brief asks Phase 2 to delete `.github/golden/` once `src/test` replays the corpus. |
| M5 | site, sonar, spotbugs and gpg pins were never exercised. | **Partly fixed.** `mvn site` ran locally with the new pins (success). gpg and sonar stay unexercised until Phase 4; recorded above. |
| M6 | Enforcer `[21,)` gives a javac error, not an enforcer message, when a JDK drops `--release 8`. | **Fixed (message).** The rule now says why JDK 21+ is required; no upper bound, since no JDK has dropped `--release 8` yet. |
| Nit | `failIfNoTests` is Phase 2's criterion pulled forward. | **Kept**, on purpose: it protects the Phase 1 proof (a leg that runs zero tests fails). Phase 2's criterion is thereby met early. |
| Nit | Lifecycle pins (clean, resources, jar, install, deploy) and site/project-info/sonar bumps were not in the brief's list. | **Kept**: DELTA-03's reason (the super-POM must not choose versions) applies to them too. |
| Nit | `GoldenCorpus.java` had no license header. | **Fixed.** Apache-2.0 header added. The copyright line reads "OWASP json-sanitizer contributors"; the maintainer may prefer other wording. |
| Nit | Workflow hygiene: no timeout or concurrency, `_X64` hardcoded, `javac`/`javap` from PATH, no report upload, bytecode read from `target/classes` rather than the jar. | **Fixed**: `timeout-minutes: 30`, a `concurrency` group that cancels superseded runs, `${{ runner.arch }}`, `$JAVA_HOME/bin/...`, `upload-artifact` of the surefire reports on failure, bytecode check on the classes unpacked from the built jar. `push` and `pull_request` stay unfiltered because the brief says every push and pull request. |
| Nit | Digest file: 357 KB at 256 inputs per block. | **Fixed.** Blocks of 4,096 inputs: 139 blocks, 22 KB; `GoldenCorpus dump <from> <to>` still localizes a mismatch. |
| Nit | `RELEASE-checklist.sh` still describes the nexus-staging flow the pom no longer has. | **Flagged, not changed** (Phase 4). Nobody should follow that script in between. |

The critic's "one thing": the post-`verify` assertion (H1). It is in the workflow and was exercised locally on every leg.

# Phase 2: test harness (JUnit 5, golden corpus, reproducible fuzzing)

- **Date:** 2026-10-27
- **Commits:** `d157b20`..`76f7bc7` on top of `475f264` (the end of Phase 1). Entry criteria met first: Phase 1's CI ran green on GitHub ([run 40588718585](https://github.com/OWASP/json-sanitizer/actions/runs/40588718585), branch `java8-toolchain-and-ci`).
- **Scope:** `pom.xml` (test dependencies only), `src/test/**`, `.github/**`, `.gitattributes`, `.gitignore`. **No file under `src/main/` changed** (`git diff 475f264 -- src/main` is empty).
- **Playbook:** `analysis/json-sanitizer/PLAYBOOK.md` section 6.

## What changed and why

| Brief criterion / delta | Change |
|---|---|
| Jupiter only, every baseline name mapped (DELTA-01) | JUnit 5.14.4 via the BOM (JUnit 6 needs Java 17); `junit:junit` removed, no vintage engine. The 28 methods keep their classes and names, lose `static` and `TestCase`. Eight message-first assertions reordered (four would have compiled with the wrong meaning); each mutation-checked. |
| Surefire fails on zero tests | `failIfNoTests` (since Phase 1). CI additionally requires each test in `.github/required-tests.txt` to run unskipped on every leg. |
| FuzzyTest: fixed seed in CI, `-Dfuzz.seed`, RULE-026 diagnostics | CI keeps the fixed seed; seeds parse with `Long.parseUnsignedLong` (Q12, RULE-063), so the baseline's `ea41f76fb21e5107` replays; the "After:" excerpt and `excerpt()` escaping are fixed. |
| Golden-corpus test | `GoldenCorpus` and its digests moved (`git mv`) under `src/test`; `GoldenCorpusTest` verifies on every leg's JVM and pins the digests' SHA-256. `.github/golden/` and its CI step are gone (accepted revision). |
| One test per P0 rule | `BehaviorContractTest`: RULE-001, -024, -025, -026, -056, -057. RULE-001 pins the legacy `\v` → U+0008 that Phase 3 changes. |
| Stream characterization (DELTA-11, RULE-034, RULE-058) | `StreamCharacterizationTest`: eight malformed-UTF-8 shapes, BOM before and inside a value, a surrogate pair split at the 8192-char boundary, a truncated sequence split at the 8192-byte boundary, one-char and one-byte reads. Identical on JDK 8 to 25. |
| Jazzer target and CI job | `JsonSanitizerFuzzTest`: no throw, strict RFC 8259 (Jackson, UTF-8-encoded, size limits lifted), idempotence, no breakout; at the default and a fuzzed depth; Reader/InputStream equal to String; `minify` safe. Seeds replay in every test run; the `fuzz` CI job fuzzes 5 minutes and must show at least 280 s. |

## Dual-run diff

Build JDK 21 for every leg; the test JVM varies through `-Djvm`; seed `6a736f6e2d73616e`. The legacy leg (oracle) is unchanged: `analysis/json-sanitizer/BASELINE.md`, 28 tests.

| Test JVM | Tests executed | Required tests (42) | vs `BASELINE.md` | Golden corpus | Class-file major |
|---|---|---|---|---|---|
| Zulu 8.0.512 | 50, 0 failures, 0 skipped | 42 ran | OK: nothing regressed | byte-identical | 52 |
| Homebrew 11.0.32.1 | 50, 0 failures, 0 skipped | 42 ran | OK: nothing regressed | byte-identical | 52 |
| Homebrew 17.0.20.1 | 50, 0 failures, 0 skipped | 42 ran | OK: nothing regressed | byte-identical | 52 |
| Homebrew 21.0.12.1 | 50, 0 failures, 0 skipped | 42 ran | OK: nothing regressed | byte-identical | 52 |
| Homebrew 25.0.4.1 | 50, 0 failures, 0 skipped | 42 ran | OK: nothing regressed | byte-identical | 52 |

50 = the 28 baseline tests (same names) + 14 new named tests + 8 Jazzer seed replays.

**Fuzzing (JDK 21, local, 5 minutes each):** the first campaign found `{"\uDDDD":1}` after 83 s (below); after the owner's decision a full campaign ran 2,834,362 inputs and the widened target 1,069,698 inputs, both with no finding.

## Jazzer's first finding

The sanitizer keeps an escaped lone surrogate (`\uDDDD`) and escapes a raw one; Jackson rejects such an escape in an object key but accepts it in a value. RFC 8259 §7 allows it (§8.2 only warns), so it is **not a sanitizer defect under the brief's criterion**. Owner decision: the check is RFC 8259 as written; the oracle swaps `\uD800`-`\uDFFF` escapes for `\uFFFD` before parsing (grammar-preserving, self-tested in `testStrictJsonCheck`), and the input stays as two seeds. Whether output should avoid lone surrogates for I-JSON (RFC 7493) is the proposed Q13 in the brief.

## Residual manual deltas

- **Two exit criteria wait for CI:** the stream tests "pass on every CI leg" and "a CI job runs [Jazzer] for a fixed time with zero findings" need a GitHub run of this code. Local proof only so far.
- **Dynamic agent warning on JDK 21+:** Jazzer's agent loads dynamically (`A Java agent has been loaded dynamically`). `-XX:+EnableDynamicAgentLoading` would silence it but does not exist on Java 8, so the forked JVM of the 8 leg would not start. A future JDK may disallow dynamic loading; revisit then.
- **Local seed replay differs from CI:** after a local fuzzing run, `mvn test` also replays the git-ignored `.cifuzz-corpus` (over 1,000 entries); CI never has it. Delete it before a counting proof (done for the table above). The corpus is deliberately not committed: every CI fuzz run starts from the seeds.
- **Fuzz budget (Q11):** the `fuzz` job adds about 6 minutes of runner time per push and pull request; the organization's budget limit is still unknown.
- **Repository settings for the maintainer:** CODEOWNERS or branch protection on `src/test/resources/com/google/json/golden-digests.txt`, `GoldenCorpusTest.java` (the pinned hash) and `.github/required-tests.txt`; these are what Phase 3 relies on.
- **Phase 1 review note M4 above** referred to `.github/golden/`; that copy no longer exists, and the pinned hash replaces the "unnoticed re-recording" risk it described.

## Deferred modernization (deliberately not done)

Converting the 28 legacy tests to parameterized tables (kept one-to-one: the smallest diff and an exact name mapping); a committed, minimized fuzz corpus; OSS-Fuzz integration; fuzzing on more than one JDK.

## Units

One unit, the whole Maven module: builds on JDK 21 and reproduces its baseline on every test JVM above.

## Architecture-critic review (independent, read-only, 2026-10-27)

Reviewed `475f264..6c8880b`. **No Blocker, no High.** Scope verdict: in scope, nothing under `src/main/`, every change maps to a Phase 2 criterion or the accepted revision. It confirmed the 28 names in the surefire XML, each reordered assertion's meaning, the digest file's bytes, and that all new dependencies are test-scoped and Java 8 bytecode.

| # | Finding | Disposition |
|---|---|---|
| M1 | CI's count gate summed `tests`, skipped included: a `@Disabled` `GoldenCorpusTest` kept CI green. | **Fixed** (`76f7bc7`): named, unskipped required tests plus a seed-replay minimum. Reproduced the hole (mvn exit 0) and the fix (gate fails). |
| M2 | The documented `record` command could re-baseline from the uplifted classes; `verify` ignores provenance. | **Fixed:** the digests' SHA-256 is pinned in `GoldenCorpusTest`; `.gitattributes` keeps the file's bytes stable on every OS. |
| M3 | The fuzz target covered only `sanitize(String)` at the default depth, though Phase 3 changes depth handling, the stream overloads and `minify`. | **Fixed:** fuzzed depth, Reader/InputStream equivalence, `minify` checks. Coverage 607 → 778 edges. |
| M4 | Nothing proved the fuzz job fuzzed. | **Fixed:** the job fails unless Jazzer reports at least 280 s. |
| L1 | A raw lone surrogate passed the oracle. | **Fixed:** strict UTF-8 encoding first. |
| L2 | Jackson's `maxNameLength` (50,000) not lifted. | **Fixed.** |
| L3 | 100,000-bracket test asserted only `contains("null")`. | **Fixed:** exact output. |
| L4 | Local runs replay `.cifuzz-corpus`; corpus not kept. | **Recorded** (residual above), deliberate. |
| L5 | Finding upload skipped on timeout; corpus and stray `crash-*` not uploaded. | **Fixed:** `failure() \|\| cancelled()`, hidden files included. |
| Nits | Pin the one-char test's literal; a minimized seed; malformed UTF-8 at the byte boundary. | **Fixed**, all three. |

Also fixed without a finding: `.gitattributes` `* text=auto` could have given the pinned digest file CRLF endings on a Windows checkout.
