# Assessment: json-sanitizer

- **Date:** 2026-10-27
- **Source:** `legacy/json-sanitizer`, which links to `/Users/jmanico/Dropbox/github/json-sanitizer` (OWASP JSON Sanitizer, `com.mikesamuel:json-sanitizer` 1.2.4-SNAPSHOT)
- **Intent:** understand first; security review first; fix known bugs as we go (see `INTENT.md`)
- **Inputs:** `PREFLIGHT.md`, a line count with `find`/`wc` plus decision-keyword counts (`scc` and `cloc` are not installed), a JaCoCo coverage run on JDK 11, three analyst passes (structure, debt, security), and reproductions run by hand.

## Executive Summary

json-sanitizer is a small, widely used OWASP security library. It turns "JSON-like" text into well-formed JSON that is safe to embed in HTML `<script>` and XML CDATA. It is about **1.2k lines of production Java** in one class (`JsonSanitizer`) plus an optional `EvalMinifier`, with tests covering **98% of lines and 92% of branches**.
- **Security core:** 7 million fuzzed inputs found **no sanitizer bypass**. Every output was valid JSON and embed-safe.
- **Confirmed defects:** a **quadratic slowdown on long hex/octal numbers** (High, CWE-407), a **mutable-result aliasing hole** in the new `sanitize(CharSequence)` overload (Medium), **unbounded reads** in the new stream overloads (Medium), and a silent **`\v` decoding bug**.
- **Bigger risk, the toolchain:** the build targets Java 1.6/1.7 and fails on JDK 21+. CI (Travis) is dead, and the release path (OSSRH) was retired in 2025.

**Recommendation:** *Replatform* in place with `uplift`. Move to a modern Java baseline and toolchain, then fix the confirmed bugs behind the existing test and fuzz suite. Do not rewrite.

## System Inventory

Tool: `find` + `wc` with a comment-aware line classifier. Complexity is approximated by counting decision points (`if`/`for`/`while`/`case`/`catch`/`switch`/`&&`/`||`/`?`).

| File | Code | Comments | Decision points | Per KSLOC |
|---|---:|---:|---:|---:|
| `src/main/java/com/google/json/JsonSanitizer.java` | 973 | — | 371 | 381 |
| `src/main/java/com/google/json/EvalMinifier.java` | 259 | — | 72 | 278 |
| `src/main/java/com/google/json/package-info.java` | 2 | — | 0 | 0 |
| `src/test/java/com/google/json/FuzzyTest.java` | 315 | — | 69 | 219 |
| `src/test/java/com/google/json/JsonSanitizerTest.java` | 350 | — | 4 | 11 |
| `src/test/java/com/google/json/EvalMinifierTest.java` | 92 | — | 5 | 54 |
| **Java total** | **1,991** (1,234 main / 757 test) | 613 | 521 | |

- **Other files:** `pom.xml` (single module), `README.md`, `docs/` (getting started, contact, architecture `.png` + `.graffle`), `src/site/site.xml`, `RELEASE-checklist.sh`, `.travis.yml`.
- **Hotspot methods:** `sanitize()` runs about 270 lines (`JsonSanitizer.java:334-602`), `sanitizeString()` about 200 (`:620-817`), and `canonicalizeNumber()` about 180 (`:1151-1332`).
- **Change frequency** (git history, 120 commits, 2012–2026): `pom.xml` 37 commits, `JsonSanitizer.java` 24, `JsonSanitizerTest.java` 19. Build churn exceeds code churn.

### Technology fingerprint

| Area | Finding | Evidence |
|---|---|---|
| Language/runtime | Java; bytecode target **1.6**, or **1.7** on JDK 12+ | `pom.xml:239-240`, `:276-284`; jar manifest `Java-Version: 6` |
| Build | Maven, single module, no parent POM, Maven Central only | `pom.xml` |
| Dependencies | `jsr305` 3.0.2 (`provided`, not shipped); `junit` 4.13.1 (test). **No runtime dependencies.** | `pom.xml:200-213` |
| Build plugins | jacoco 0.8.5, spotbugs 3.1.12.2, javadoc 3.1.1, source 3.2.1, nexus-staging 1.6.8, gpg 1.6, sonar 3.7 | `pom.xml:72-283` |
| Data stores | None (pure in-memory transformation) | — |
| Integration points | Public static API only: `sanitize(String[,int])`, `sanitize(CharSequence[,int])`, `sanitize(Reader, Appendable[,int])`, `sanitize(InputStream, OutputStream[,int])`, `EvalMinifier.minify(String[,int])` | `JsonSanitizer.java:124-249`, `EvalMinifier.java:50-67` |
| Distribution | Maven Central via OSSRH (retired) | `pom.xml:47-56`, `:93-119` |
| Tests | 28 tests, all passing on JDK 11 and 17. JUnit 3 `TestCase` subclasses carry JUnit 4 `@Test`, which has no effect (they run because of the JUnit 3 base class). A randomized idempotence fuzz test runs 10k cases per run. | `src/test/java/com/google/json/*` |
| Coverage | `JsonSanitizer` 98% line / 92% branch; `EvalMinifier` 97% / 83% (JaCoCo, JDK 11, random fuzz seed) | `target/site/jacoco/jacoco.csv` |
| CI | `.travis.yml` (openjdk 8/11/13) on travis-ci.org, which has been shut down, so **no live CI**. The README also shows an OSS-Fuzz badge, but there is no fuzz target in this repo. | `.travis.yml`, `README.md:1` |

## Architecture at a Glance

One class does the work in one left-to-right pass. The output is copy-on-write: if the input is already clean, the original object comes back without allocating (`JsonSanitizer.java:303-314`, `:852-876`). Full diagram: `ARCHITECTURE.mmd`.

| # | Domain | Where | Role |
|---|---|---|---|
| D1 | Public API overloads | `JsonSanitizer.java:111-251`, `:318-332` | Entry points; depth clamped to 1..4096; null input becomes `null` |
| D2 | Tokenizer / state machine | `:257-284`, `:334-562`, `:819-850` | Single pass that dispatches each token |
| D3 | Structure repair + depth limit | `:377-466`, `:564-601`, `:922-995` | Balances brackets, fixes punctuation, turns over-deep values into `null`, truncates after a top-level comma |
| D4 | Comment / whitespace / junk removal | `:358-375`, `:468-475`, `:883-914` | Drops `//`, `/* */`, parentheses, stray characters |
| D5 | String normalization | `:620-817` | `'`→`"`, rewrites escapes, escapes control characters, lone surrogates, U+2028/9 and non-characters |
| D6 | Embedding-safety escapes | `:654-726`, `:1434-1540` | Neutralizes `<!--`, `<script`, `</script`, `-->`, `]]>`, including escaped spellings |
| D7 | Number normalization | `:997-1332` | Hex/octal to decimal, fills missing digits, ES5 canonical form for numeric keys |
| D8 | Copy-on-write buffer | `:291-314`, `:852-876` | Shared mutable state (`sanitizedJson`, `cleaned`) |
| D9 | `EvalMinifier` (optional) | `EvalMinifier.java:50-345` | Pools repeated tokens for `eval`; uses package-private sanitizer internals |
| D10 | Tests and fuzzing | `src/test/java/...` | Golden-output, regression and idempotence-fuzz tests |
| D11 | Build, release, docs | `pom.xml`, `RELEASE-checklist.sh`, `.travis.yml`, `README.md`, `docs/` | Packaging and the published contract |

## Production Runtime Profile

No telemetry is available. This is a library with no runtime of its own, and no APM data, logs or exports were supplied. Measured micro-timings for the attack cases are under Security Findings.

## Technical Debt (top 10, by remediation value)

| # | Item | Evidence | Remediation |
|---|---|---|---|
| 1 | **The build fails on current JDKs and CI is dead** | `pom.xml:239-240`, `:276-284` (1.6/1.7, which fails on JDK 21+); jacoco 0.8.5 breaks on JDK 17+ (`:159`); no compiler or surefire pinned; `.travis.yml`; dead Travis badge `README.md:1` | `maven.compiler.release` 8 or 11; pin plugins; jacoco ≥0.8.12; GitHub Actions matrix 8/11/17/21/25 |
| 2 | **Release path retired and misconfigured** | OSSRH `pom.xml:47-56`; nexus-staging 1.6.8 `:93-119` with `<executions>` nested inside `<configuration>` (`:109-117`, silently ignored); `autoReleaseAfterClose=true` (`:101`) contradicts the manual checklist; `RELEASE-checklist.sh` exits on line 5 and hardcodes `cd ~/work`; `git://` SCM URL (`:42-43`) | `central-publishing-maven-plugin`; tag-triggered release workflow; HTTPS/SSH SCM |
| 3 | **Escape decoding duplicated, with the same `\v` bug in both copies** | Rewrite path `JsonSanitizer.java:727-787` vs lookahead `:1439-1502`; `\v`→`\u0008` at `:739-740` and `0x20008` at `:1495`. **Reproduced:** `["a\vb"]` gives `["a\u0008b"]` (backspace); JS `\v` is U+000B. Untested. | One shared escape decoder; fix to `\u000b`; regression test |
| 4 | **God class and god methods** | `JsonSanitizer.java` is 1,541 lines; `sanitize()` about 270 lines with a labelled loop; `sanitizeString()` about 200; `canonicalizeNumber()` about 180 | Optional: split into tokenizer, string and number components behind the golden and fuzz suite |
| 5 | **Exceptions and `assert` used for control flow and invariants** | Shared mutable static `UNBRACKETED_COMMA` exception (`:1389-1412`) thrown at `:428`/`:845`; `AssertionError` at `:979`, `:990`, `:993`, `:849`; `assert`s at `:982`, `:1181`, `:1187-1192`; open `TODO` at `:1265` | Return a sentinel state; make invariant failures fail safe (emit `null`); fuzz-assert that sanitize never throws |
| 6 | **Stream overloads buffer unbounded input** | `:203-214` (magic `8192` chunk, then a full copy); InputStream overload `:243-249`; no null checks on `in`/`out` | `maxInputChars` overloads with a safe default (also fixes SEC-003) |
| 7 | **jsr305 annotations contradict behavior** | `package-info.java:1` `@ParametersAreNonnullByDefault`, yet `null` is accepted and tested (`JsonSanitizer.java:327`, `JsonSanitizerTest.java:51`); `javax.annotation` is a split package under JPMS; no `Automatic-Module-Name` | JSpecify (`@NullMarked` + `@Nullable`); add `Automatic-Module-Name` |
| 8 | **JUnit 3/4 hybrid tests; fuzz not reproducible** | `public static` `@Test` methods in `TestCase` subclasses (`JsonSanitizerTest.java:29,47`; `FuzzyTest.java:35-37`; `EvalMinifierTest.java:28,35`); random seed by default (`FuzzyTest.java:50`); idempotence failure message prints the wrong string (`FuzzyTest.java:89`) | JUnit 5 with parameterized golden tables; fixed CI seed plus `-Dfuzz.seed` override; Jazzer/OSS-Fuzz target in-repo |
| 9 | **Dead debug code and Java 6 idioms** | `SUPER_VERBOSE_AND_SLOW_LOGGING=false` (`:316`) guarding `:324-326`, `:350-356`, `:570-574`; `Charset.forName("UTF-8")` (`:251`); no diamond operator; Eclipse-only `synthetic-access` suppressions; `setStackTrace` hack (`:1410-1412`) | Delete; modernize after the baseline moves |
| 10 | **`EvalMinifier` is an eval-centric legacy API** | Whole class (`EvalMinifier.java:28-54`) is incompatible with CSP unless `'unsafe-eval'` is allowed; duplicated `minify` bodies (`:50-54`, `:63-67`); hand-maintained reserved-word table (`:290-303`); magic constants (`:232`) | Decide support status (deprecate?); delegate the overloads; fix SEC-004 |

## Security Findings

No credentials found, so no `SECRETS.local.md` is needed. The only identifier is a public Sonatype `stagingProfileId` (`5fd6****`, `pom.xml:108`), which is not a credential. semgrep (`p/java` + `p/security-audit`) found 0 issues. Findings come from code reading plus harnesses run on JDK 17 with `-ea`. **7M fuzzed inputs, checked by a strict RFC 8259 validator plus embed-safety checks, produced 0 failures, 0 exceptions and no bypass of the output guarantees.**

| ID | CWE | Sev | Location | Issue | Status |
|---|---|---|---|---|---|
| SEC-001 | CWE-407 / CWE-400 | **High** | `JsonSanitizer.java:1079-1086` (`new BigInteger(digits, base)` at `:1084`); reached from `:549` (values) and `:537` (keys) | A long hex or octal literal takes quadratic time. Measured (and re-run independently): 100k hex digits 0.33 s, 200k 1.08 s, 400k 4.22 s, 1M about 26 s. A decimal number of the same size takes 0.004 s. One request body can tie up a thread, which breaks the documented O(n) promise (`:106-107`). This code is in released versions. | CONFIRMED |
| SEC-002 | CWE-374 / CWE-367 | **Medium** | `JsonSanitizer.java:172-175` vs javadoc `:147-148` (added in d2b5421, unreleased) | `sanitize(CharSequence)` returns the caller's own mutable object when the input is clean. If the caller reuses the buffer, the "safe" result changes. Reproduced: the result became `["</script>"]` after the buffer was reused, a path to XSS. | CONFIRMED |
| SEC-003 | CWE-770 / CWE-400 | **Medium** | `:205-209`, `:243-249` (unreleased overloads) | Reader/InputStream input is read fully into memory with no cap, and output can grow up to 6×. An infinite stream caused OOM in 0.1 s at `-Xmx256m`. | CONFIRMED |
| SEC-004 | CWE-116 | Low | `EvalMinifier.java:88-92`, `:234-239` | `12345e+1` is split at `+`, so a pooled token produces invalid JS (`12345e`). Client-side `eval` throws (availability only). | CONFIRMED |
| SEC-005 | CWE-436 | Low | `JsonSanitizer.java:922-943` (added in 042752c) | The over-deep skip scanner tokenizes differently from the main loop. At depth 1, `[[a'b],"keep",'x']` becomes `[null]`, losing the sibling values. Output stays valid and safe. | CONFIRMED |
| SEC-006 | CWE-190 | Low | `:1199`, `:1239`, `:1250`, `:1327-1329` | Integer overflow in the exponent of numeric keys: `{11e2147483647:0}` gives `{"1.1e--2147483648":0}`. Keys can collide or get past an allowlist. | CONFIRMED |
| SEC-007 | CWE-1395 | Low (release machine) | `pom.xml:93-119` | nexus-staging 1.6.8 pulls in XStream 1.4.7 (CVE-2020-26217, CVE-2021-21344…, CVE-2021-39139…). A malicious staging server could get code execution on the machine that holds the signing key. | SUSPECTED |
| SEC-008 | CWE-1395 | Low (build only) | `pom.xml:136-152` | javadoc plugin 3.1.1 pulls in plexus-archiver 3.6.0, commons-io 2.5 and httpclient 4.5.8 with known CVEs. Not in the shipped jar. | SUSPECTED |
| SEC-009 | CWE-319 | Info | `pom.xml:42-43` | `git://` SCM URL (GitHub has disabled it, so it fails closed) | CONFIRMED |
| SEC-010 | CWE-1104 | Info | `pom.xml:101`, `.travis.yml` | Staged artifacts auto-release with no manual gate, and no CI runs the regression or fuzz suites | CONFIRMED |

Dependencies: junit 4.13.1 is not affected by CVE-2020-15250 (fixed in 4.13.1). jsr305 is `provided` scope with no known CVEs. **No runtime dependencies are shipped.**

## Documentation Gaps (top 5)

1. **Output guarantees are stated three different ways.**
   - The javadoc (`JsonSanitizer.java:71-86`) promises only no `</script` and says "three additional properties" but lists four.
   - The README (`README.md:72`) promises no `<script`, `</script` or `<!--`.
   - The code also neutralizes `-->` (`:693-713`), which is tested.
   - Both documents still cite RFC 4627; RFC 8259 is current.

   This is the library's main contract, so it needs one authoritative list.
2. **Nesting-depth behavior is missing from the README.** It covers the default of 64, the maximum of 4096, over-deep values becoming `null` without throwing, and (SEC-005) siblings being lost when the over-deep value contains odd tokens. This is documented only in the javadoc.
3. **The new overloads are undocumented outside the javadoc, and the javadoc is wrong.** The README doesn't mention the `CharSequence`, `Reader` or `InputStream` APIs. The javadoc claims later input changes don't affect the result (`:147-148`), which is false (SEC-002). The memory and size behavior of the stream overloads is buried in the javadoc.
4. **Silent truncation and repair rules are undocumented.** A top-level comma or stray close bracket drops everything after it (`"false,true"` becomes `false`). Numeric object keys are canonicalized with ES5 §9.8.1 rules. Neither appears in the README.
5. **`EvalMinifier` and the release process are undocumented or stale.** The minifier's purpose and status (supported? deprecated?) are not in the README. The release runbook (`RELEASE-checklist.sh`) is stale, and `getting_started.md:13` recommends the unpinned version range `[1.0,)`.

## Relative Scale

| Measure | Value |
|---|---|
| Production KSLOC | 1.23 |
| All Java KSLOC (incl. tests) | 1.99 |
| Complexity index, `2.94 × KSLOC^1.10` (production / all Java) | **3.71** / 6.27 |

The index is a relative size measure for ranking this system against others. **It is not a timeline or a cost estimate.**

## Recommended Modernization Pattern: **Replatform**, routed to `/code-modernization:modernize-uplift`

json-sanitizer is a small, single-purpose security library. Its core logic holds up under heavy fuzzing and has near-complete coverage. Thousands of downstream projects depend on its exact output, so a rewrite (transform) or rebuild (reimagine) would put a verified security contract at risk and gain nothing.

What actually blocks the project is the platform around the code: a Java 1.6/1.7 target that modern JDKs reject, a dead CI, a retired publishing path, outdated test and annotation libraries, and plugins with known CVEs. Those are exactly what a same-stack **uplift** fixes.

1. Move to a modern Java baseline: 8 or 11 for the widest consumer reach, or 17. **`brief` must pick one, because it is a breaking change for consumers.**
2. Restore CI and publishing.
3. Migrate the tests to JUnit 5 with a pinned fuzz seed.
4. Behind that safety net, fix the confirmed bugs you asked to have fixed as we go: SEC-001 through SEC-006 and the `\v` decoding bug.

The god-class split (debt #4) is optional and should only follow once the golden and fuzz suite is green on the new baseline.

Because you asked for a security review first, run **`harden` before `map`**. It turns SEC-001 to SEC-006 into a reviewable patch.
