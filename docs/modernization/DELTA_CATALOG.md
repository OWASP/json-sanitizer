# Delta Catalog: json-sanitizer

- **Date:** 2026-10-27
- **Source:** Java 1.6 bytecode (`maven.compiler.source/target` 1.6, `pom.xml:238-239`; 1.7 in profile `jdk12on`, `pom.xml:276-284`), JUnit 4.13.1 with a JUnit 3/4 hybrid suite, jsr305 3.0.2 `provided`, unpinned compiler/surefire, jacoco 0.8.5, spotbugs 3.1.12.2, javadoc 3.1.1, source 3.2.1, nexus-staging 1.6.8, gpg 1.6, Travis CI.
- **Target (provisional, the human has not chosen):** `maven.compiler.release=8`, built on an LTS JDK (21 or 25), tested on JDK 8, 11, 17, 21 and 25; pinned modern plugins; JUnit 5; JSpecify; `Automatic-Module-Name`; Central Portal publishing; GitHub Actions.
- **How built:** the version-delta-analyst agent did the runs below in a scratch copy, then lost its network connection three times before writing up. This catalog was assembled in the calling session from the agent's own build logs, probe outputs and OpenRewrite dry-run patches, plus `jdeps` and `jdeprscan` re-run here. The repository was not modified.

## Tools run

| Tool | Result |
|---|---|
| `mvn clean test`, legacy pom, JDK 11 / 17 | ✅ 28/28 (jacoco 0.8.5 logs `IllegalClassFormatException` on 17) |
| `mvn clean test`, legacy pom, JDK 21 / 25 / 27 | ❌ `Source option 7 is no longer supported. Use 8 or later.` |
| `mvn clean test -Dmaven.compiler.source=8 -Dmaven.compiler.target=8`, JDK 21 / 25 / 27 | ✅ 28/28 each |
| Build with `maven.compiler.release=8` + JSpecify (OpenRewrite-applied pom) | ✅ 28/28 |
| **Cross-JDK output probe** (`DeltaProbe`): 200,000 fuzz inputs through `sanitize(String)`, `sanitize(CharSequence)` and `EvalMinifier.minify`; every UTF-16 code unit and supplementary samples; a numbers corpus (hex/octal/long/exponent/keys) | ✅ **byte-identical output on JDK 11, 17, 21, 25 and 27** (0 non-idempotent, 0 lone surrogates in output) |
| JUnit 5 migration, Jupiter engine only | ⚠️ **`Tests run: 0` and `BUILD SUCCESS`** (see DELTA-01) |
| JUnit 5 migration with the vintage engine | ✅ 28/28 |
| OpenRewrite 6.46.1 `dryRun`: `UpgradeToJava8`, `UpgradeToJava25`, JUnit 4→5, jsr305→JSpecify | Patches produced (pom-only for Java 8; see DELTA-05, -09, -13) |
| SpotBugs 3.1.12.2 on JDK 17 | ❌ `Java returned: 1` (site reporting only) |
| `jdeps --jdk-internals` (JDK 25) | ✅ none; depends on `java.base` only (plus the `javax.annotation` annotations, not found at runtime, which is harmless) |
| `jdeprscan --release 25` and `--for-removal` | ✅ no deprecated API use |
| Bytecode of the `release 8` build | major version 52 (Java 8) ✅ |

**Measured vs inferred runtimes.** Measured: JDK 11, 17, 21, 25, 27. **Not measured: JDK 8** (not installed here; Homebrew has no arm64 `openjdk@8`). The JDK 8 leg is inferred from `--release 8` (which checks against the JDK 8 API) and from `jdeprscan`; it must be proven in CI (Temurin 8) before any release claims Java 8 support.

## Deltas (ranked by blast radius)

| ID | Category | Delta | Site | Blast radius | Mechanical / Judgment | Test-before-touch | Notes |
|---|---|---|---|---|---|---|---|
| DELTA-01 | Project system (test harness) | **A naive JUnit 5 migration silently runs zero tests.** All 28 test methods are `public static` (JUnit 3/4 hybrid in `TestCase` subclasses). Jupiter does not run static `@Test` methods, and the OpenRewrite JUnit 4→5 recipe leaves them static. Surefire reports `Tests run: 0`, `BUILD SUCCESS`. | `JsonSanitizerTest.java` (24 methods, e.g. `:29`, `:47`), `FuzzyTest.java:35-37`, `EvalMinifierTest.java:28,35` | **High** | Judgment | yes | Remove `static`; set surefire `failIfNoSpecifiedTests` and add a test-count gate (must equal the baseline count: 28, or 29 with SEC-001's test). Keep the vintage engine until the count is proven. |
| DELTA-02 | Project system | **Legacy pom cannot compile on JDK 21+.** `source/target` 1.6/1.7 rejected. | `pom.xml:238-239`, `:276-284` | **High** | Mechanical | no | `maven.compiler.release=8` (or 11/17); delete the `jdk12on` profile. `release` (not `source/target`) also checks the JDK 8 API, which removes the "bootstrap class path not set" warning. |
| DELTA-03 | Project system | **Unpinned compiler and surefire.** Versions come from the Maven super-POM, so the build changes with the Maven version (3.10 picked compiler 3.16.0). | `pom.xml` (absent from `<build><plugins>`) | Med | Mechanical | no | Pin `maven-compiler-plugin` and `maven-surefire-plugin`; add `maven-enforcer-plugin` (`requireJavaVersion`, `requireMavenVersion`). |
| DELTA-04 | Dependency (release) | **nexus-staging 1.6.8 targets the retired OSSRH and loads on every build** (`Installing Nexus Staging features` appears in every test log), pulling XStream 1.4.7 (many CVEs incl. CISA-KEV CVE-2021-39144) onto the build classpath. `<executions>` nested in `<configuration>` is silently ignored; `autoReleaseAfterClose=true`. | `pom.xml:47-56`, `:93-119` | Med | Judgment | no | Replace with `central-publishing-maven-plugin` (Central Portal); manual release gate; SEC-007, SEC-010. Release credentials and GPG key are a human step. |
| DELTA-05 | Dependency (build) | **jacoco 0.8.5 cannot instrument JDK 17+ runtime classes** (`IllegalClassFormatException`); coverage numbers on 17+ are unreliable. | `pom.xml:158-178` | Med | Mechanical | no | ≥ 0.8.12 (OpenRewrite proposes 0.8.16). Coverage baseline must be re-recorded on the new version. |
| DELTA-06 | Dependency (build) | **SpotBugs 3.1.12.2 crashes on JDK 17** (`Java returned: 1`). Reporting section only, so `mvn test` is unaffected but `mvn site` fails. | `pom.xml:216-232` | Low | Mechanical | no | Upgrade to 4.9.x (OpenRewrite proposes 4.9.8.5); consider running it in CI as a check, not only a report. |
| DELTA-07 | Dependency (build) | **javadoc 3.1.1, source 3.2.1, gpg 1.6, site 3.8.2, project-info 3.0.0, sonar 3.7 are outdated**; javadoc and source pull vulnerable commons-io, httpclient, commons-lang3, plexus-archiver. | `pom.xml:74-90`, `:123-152`, `:260-270` | Low | Mechanical | no | SEC-008, SEC-011. Javadoc 3.11+ on JDK 21+ also needs `<source>8</source>`/`release` and may flag HTML errors in the javadoc. |
| DELTA-08 | Project system | **`--release 8` is flagged obsolete** by javac 21, 25 and 27 (`source/target value 8 is obsolete and will be removed in a future release`). | build output | Med (future) | Judgment | no | A Java 8 baseline ties the build JDK to a release that still accepts 8; pin the build JDK (21 or 25 LTS) in CI and the enforcer. A Java 11 baseline postpones this. |
| DELTA-09 | Dependency (annotations) | **jsr305 → JSpecify.** `javax.annotation` is a split package under JPMS; `@ParametersAreNonnullByDefault` contradicts the tested `null`-accepting API. OpenRewrite maps it to `@NullMarked`/`@NonNull`/`@Nullable` but adds jspecify 1.0.0 **without a scope** (would become a compile/runtime transitive dependency of every consumer). | `package-info.java:1`, `EvalMinifier.java:25-26,112,243-245,260` | Med | Judgment | no | Use `provided` or `optional` scope deliberately (JSpecify recommends a normal dependency; the library currently ships **zero** runtime dependencies, so adding one is a consumer-visible change). Mark `sanitize(...)` parameters `@Nullable` to match behavior. OpenRewrite's Java 25 recipe instead proposes `jakarta.annotation-api` 1.3.5, which is wrong for this library. |
| DELTA-10 | Project system (JPMS) | **No `Automatic-Module-Name`**; the derived name is `json.sanitizer`. Consumers on the module path get an unstable name. | jar manifest (`pom.xml`, no `maven-jar-plugin` config) | Med | Judgment | no | Choose the name once (e.g. `com.google.json` to match the package, or `org.owasp.json.sanitizer`); it is public API from then on. |
| DELTA-11 | Behavioral-silent (I/O) | **Byte-stream decoding** uses `InputStreamReader` with UTF-8 (`Charset.forName("UTF-8")`), replacing malformed input with U+FFFD. The probe covered `sanitize(String)`/`CharSequence`/`minify`, **not** the Reader/InputStream overloads across JDKs. | `JsonSanitizer.java:190-251` (`:251`) | Low | Judgment | **yes** | Behavior of the default replacement action is specified and stable since JDK 8; still add a cross-JDK characterization test (malformed UTF-8, BOM, split surrogate at the 8192 buffer boundary) before touching the overloads (RULE-034, RULE-058). |
| DELTA-12 | Project system (CI) | **No live CI.** `.travis.yml` lists openjdk 8/11/13 on the defunct travis-ci.org. | `.travis.yml`, `README.md:1` | Med | Judgment | no | GitHub Actions matrix (Temurin 8, 11, 17, 21, 25), fixed fuzz seed plus one random-seed job. |
| DELTA-13 | Language (optional) | **OpenRewrite `UpgradeToJava25` rewrites source** (switch expression in `FuzzyTest`, stray `;` in `JsonSanitizer`). Irrelevant at an 8 baseline; with 8 the Java recipe touches the pom only. | `FuzzyTest.java:273-279`, `JsonSanitizer.java:~280` | Low | Mechanical | no | Do not apply language-level rewrites in this uplift ("smallest diff that builds"). |
| DELTA-14 | Project system (docs) | **Stale version and runtime claims**: `getting_started.md:13` recommends the range `[1.0,)`; README Travis badge; RELEASE-checklist exits on line 5 and hardcodes `cd ~/work`; `git://` SCM URLs. | `docs/getting_started.md:13`, `README.md:1`, `RELEASE-checklist.sh:3-5`, `pom.xml:42-43` | Low | Mechanical | no | Pin a version in docs; state the minimum Java version; SEC-009. |

**Searched for and not found:** JDK internals (`jdeps`), deprecated or for-removal API (`jdeprscan`), reflection or `setAccessible`, serialization of public types (only the private `UnbracketedComma` marker, `JsonSanitizer.java:1391`), locale-sensitive case mapping, `String.format`, `Double.toString`/`Float` formatting in output paths, Unicode-property calls other than surrogate tests (`JsonSanitizer.java:801-802`, stable across Unicode versions), `Buffer.flip()`-style covariant-return traps. The cross-JDK probe confirms: **no behavioral-silent delta in the sanitizer core between JDK 11 and 27.**

## Test-harness verdict

**The existing suite runs unchanged on every measured target runtime** (JDK 21, 25, 27 with a Java 8 target: 28/28; JDK 11 and 17 with the legacy pom: 28/28). JUnit 4.13.1 supports Java 5+, so JDK 8 is expected to pass but is **unmeasured**. The hybrid suite runs under surefire's JUnit 4 provider today and under the JUnit Platform **only with the vintage engine**.

So the harness is **not** a forced Phase 1 by itself: the baseline can be recorded with the existing suite, and the toolchain can move first. The JUnit 5 migration is a separate, later step whose exit gate is test-count parity (DELTA-01). Doing it first would risk a green build that runs nothing.

## Baseline choice: Java 8 vs 11 vs 17

| | Java 8 (`release 8`) | Java 11 | Java 17 |
|---|---|---|---|
| Code changes needed | None (pom only) | None | None (optional idioms, not recommended) |
| Consumers cut off | Java 6/7 only (effectively none on supported JVMs) | + Java 8 users (still a large enterprise share; an OWASP security library should reach them) | + Java 8 and 11 users |
| Test stack | JUnit 5 (5.x supports Java 8) | JUnit 5 | JUnit 5 or 6 (JUnit 6 requires 17) |
| Fuzzing (Jazzer) | ✅ | ✅ | ✅ |
| Build JDK constraint | Must use a JDK that still accepts `--release 8` (DELTA-08); fine on 21/25 today | No warning on current JDKs | None |
| JPMS | `Automatic-Module-Name` only | Could ship a real `module-info` | Same as 11 |
| CI matrix | 8, 11, 17, 21, 25 | 11, 17, 21, 25 | 17, 21, 25 |

Only DELTA-08 (and the possible use of JUnit 6) changes with the baseline. Every other delta is the same for 8, 11 and 17. **Recommendation: Java 8.** It is the smallest break for consumers, it needs no code change, and its only cost is a future build-JDK constraint.

## Uplift vs rewrite signal

**Clearly an uplift.** The Java 8 recipe changes only the pom. No main-source line has to change to build and pass on JDK 21–27, and output is byte-identical across JDK 11–27. Every delta is in the build, test harness, dependencies or release path. The code changes this effort will make are the deliberate bug and security fixes the intent asks for ("fix known bugs as we go"), which are not version deltas and are tracked in the brief.

## Phase 1 pilot findings (2026-10-27)

The pilot (the whole single-module build, in `modernized/json-sanitizer-uplifted/`) hit **no delta the catalog had not predicted**. What it confirmed, and two environment facts worth recording:

| Delta | Pilot result |
|---|---|
| DELTA-02 | `maven.compiler.release` 8 builds on JDK 21; the `jdk12on` profile is deleted. |
| DELTA-03 | compiler 3.16.0, surefire 3.6.1, enforcer 3.6.3 (`requireMavenVersion [3.9.0,)`, `requireJavaVersion [21,)`), plus clean 3.5.0, resources 3.5.0, jar 3.5.1, install 3.2.0, deploy 3.2.0 pinned in `pluginManagement`. |
| DELTA-04 | nexus-staging removed from `<build><plugins>`; no `Installing Nexus Staging features` line in any build log. `distributionManagement` still names OSSRH; Phase 4 replaces it. |
| DELTA-05 | jacoco 0.8.16 instruments test JVMs 8, 11, 17, 21 and 25 with no `IllegalClassFormatException`; the agent runs inside the forked test JVM while Maven stays on 21. |
| DELTA-06, -07 | spotbugs 4.10.5.0 (reporting only), javadoc 3.12.0, source 3.4.0, gpg 3.2.8, site 3.22.0, project-info 3.9.0, sonar 5.1.0.4751 pinned. javadoc 3.12.0 on JDK 21 emits two doclint **warnings** (`package-info.java:2` no comment, `EvalMinifier.java:33` default constructor), no error. |
| DELTA-08 | Confirmed: javac 21 prints `source value 8 is obsolete and will be removed in a future release` on every compile. Warning only. |
| DELTA-12 | `.github/workflows/ci.yml`: build on Temurin 21, tests on Temurin 8/11/17/21/25 through surefire's `jvm` property, bytecode check, golden-corpus replay; actions pinned to commit SHAs. `.travis.yml` and the Travis badge removed. Running it on the OWASP organization is Q11 and happens at push time. |
| DELTA-14 | Only the README badge changed; `docs/getting_started.md` and `RELEASE-checklist.sh` stay for Phase 4. |

**Environment facts discovered (now in `PLAYBOOK.md`):**
- *JDK 8 on an Apple-silicon Mac.* Homebrew has no arm64 `openjdk@8` and the `zulu@8` cask is a `.pkg` that needs an administrator password. Azul's plain tarball (`zulu8.98.0.107-ca-jdk8.0.512-macosx_aarch64.tar.gz`, SHA-256 published by `api.azul.com`) runs from any directory with no installation, which turned the "JDK 8 unmeasured" risk into a measured leg before CI.
- *One build JDK, many test JVMs.* `mvn verify -Djvm=<other jdk>/bin/java` makes surefire fork the tests on another JDK while compilation, javadoc and the enforcer stay on 21. The surefire XML records the test JVM in its `java.version` property, which is how a leg proves it ran where it claims.

**Observed during the review fixes (not predicted by the catalog; now handled):**
- *Maven 3.10 super-POM defaults.* Maven 3.10.0 sets `project.build.outputTimestamp=1980-02-01T00:00:00Z` and picks jar plugin 3.5.1 (which writes a `Java-Version` manifest header). CI's Maven 3.9.x does neither, so a pom that relies on the local defaults produces a different jar in CI. The uplifted pom sets the timestamp explicitly and pins the jar plugin; the enforcer bounds Maven to `[3.9.0,4)`.
- *RULE-063 is real and bites CI.* The legacy `FuzzyTest` cannot parse the seeds it prints when they are negative longs (`Long.parseLong`, `FuzzyTest.java:46`): replaying the baseline's seed `ea41f76fb21e5107` errors on every JDK. A fixed CI seed must be positive until Phase 2 switches to `Long.parseUnsignedLong` (Q12).
