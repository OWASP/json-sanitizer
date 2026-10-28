# Security findings: json-sanitizer

- **Date:** 2026-10-27
- **Source:** `legacy/json-sanitizer`, which links to `/Users/jmanico/Dropbox/github/json-sanitizer`
- **Scan:** harden-scan workflow `wf_ceca73e6-c01` with 19 agents: 5 class finders (injection, auth/session, secrets, dependency CVEs, input validation), 13 refuters and 1 second judge.
- **Inputs from the assessment:** findings from `ASSESSMENT.md` that this scan did not raise again are listed separately below and marked as such.

## Scorecard

| Severity | Count |
|---|---:|
| Critical | 0 |
| **High** | **1** |
| Medium | 0 |
| Low | 6 |

The scan returned 8 findings. Two of them are the same issue (`JsonSanitizer.java:175`, reported as both CWE-375 and CWE-367), so they are merged here as SEC-002, leaving 7.

**Top CWE categories:**
- CWE-1395/1104/502: vulnerable or outdated build plugins (3 findings)
- CWE-407: algorithmic complexity (1)
- CWE-374/375/367: mutable result aliasing (1)
- CWE-770: unbounded allocation (1)
- CWE-436: interpretation conflict (1)

**Verification:**
- 13 distinct raw findings were judged and **5 refuted as false positives** (false-positive rate 38%, counted over judged findings).
- A second judge confirmed the one High.
- No hardcoded credentials were found. The only identifier is a public Sonatype `stagingProfileId` (`5fd6****`, `pom.xml:108`), which is not a credential, so no `SECRETS.local.md` was written.
- No instruction-shaped text was found in the source (`injectionFlags` is empty). The run's own harness flagged the word "settings" in three finders' outputs, where they described `~/.m2/settings.xml`. That is analysis text, not repository content.

## Coverage gaps

All 5 finder classes returned and every finding was judged.

## Findings

| ID | Sev | CWE | Location | Issue | Exploit scenario | Fix | Status |
|---|---|---|---|---|---|---|---|
| SEC-001 | **High** | CWE-407 | `src/main/java/com/google/json/JsonSanitizer.java:1084` (from `:549` value position and `:537` key position) | `new BigInteger(digits, base)` on an unbounded hex or octal literal takes quadratic time. This breaks the documented O(n) promise (`:106-107`). It is in released versions. | One request carrying `[0xfff…]` with about 800k digits ties up a thread for about 17 s. Measured: 100k digits 0.33 s, 200k 1.08 s, 400k 4.22 s. A few concurrent requests exhaust the worker pool. | Cap the significant digits that get exact reencoding; see Remediation Log | Patched, see review |
| SEC-002 | Low | CWE-375 / CWE-367 | `JsonSanitizer.java:175` (javadoc `:147-148`) | `sanitize(CharSequence)` returns the caller's own mutable object when the input is already clean, contrary to its javadoc. This is new in d2b5421 and unreleased. | The app reuses a pooled `StringBuilder` and later writes the stored "sanitized" result into a `<script>`. Reproduced: the result became `["</script>"]`. | Return the immutable `String` copy; fix the javadoc; add a mutate-after test | Open, goes to `uplift` |
| SEC-003 | Low | CWE-770 | `JsonSanitizer.java:205-209`, `:243-249` | The Reader/InputStream overloads read input with no limit, and output can grow up to 6× (documented in javadoc only). Unreleased. | An app pipes an untrusted request body into `sanitize(InputStream, OutputStream)`. An endless body causes OOM (0.1 s at `-Xmx256m`). | `maxInputChars` overloads with a safe default | Open, goes to `uplift` |
| SEC-005 | Low | CWE-436 | `JsonSanitizer.java:922-943` | The over-deep skip scanner splits tokens differently from the main loop. At depth 1, `[[a'b],"keep",'x']` becomes `[null]`. Output stays valid and safe. | Sibling values are dropped depending on how unquoted text in a skipped value is spelled. | Skip with the main loop in a discard mode | Open, goes to `uplift` |
| SEC-007 | Low | CWE-502 | `pom.xml:93-119` | nexus-staging-maven-plugin 1.6.8 pulls in XStream 1.4.7, which deserializes Nexus REST XML on the release machine. | A spoofed or compromised staging endpoint gets code execution on the machine holding the GPG key and could publish a backdoored release. This needs a TLS or server compromise, and OSSRH has been retired. | Remove it; use `central-publishing-maven-plugin` | Open, goes to `uplift` |
| SEC-008 | Low | CWE-1395 | `pom.xml:135-152` | maven-javadoc-plugin 3.1.1 pulls in vulnerable commons-io, httpclient, commons-lang3 and plexus-archiver. Build only, not in the shipped jar. | Only reachable through malicious archives or URLs at javadoc build time. | Upgrade to javadoc plugin 3.11 or later | Open, goes to `uplift` |
| SEC-011 | Low | CWE-1104 | `pom.xml:121-124` and others | maven-source-plugin 3.2.1 pulls in plexus-archiver 4.2.1. jacoco 0.8.5, spotbugs 3.1.12.2, gpg 1.6 and sonar 3.7 are also unmaintained. Toolchain maintenance, nothing exploitable. | — | Upgrade the plugins; add a plugin-update check in CI | Open, goes to `uplift` |

### Confirmed in the assessment, not raised again by this scan

These were reproduced during `assess` (`ASSESSMENT.md`) and remain open. All are Low or Info, so none gets a patch hunk here.

| ID | Sev | CWE | Location | Issue |
|---|---|---|---|---|
| SEC-004 | Low | CWE-116 | `EvalMinifier.java:88-92` | `12345e+1` is split at `+`, so `minify` outputs invalid JS (`12345e`) |
| SEC-006 | Low | CWE-190 | `JsonSanitizer.java:1199`, `:1239`, `:1250`, `:1327-1329` | Integer overflow in numeric-key exponents (`{11e2147483647:0}` gives `{"1.1e--2147483648":0}`) |
| SEC-009 | Info | CWE-319 | `pom.xml:42-43` | `git://` SCM URLs |
| SEC-010 | Info | CWE-1104 | `pom.xml:101`, `.travis.yml` | Auto-release with no manual gate; no live CI |

**Also open (correctness bug, not a security finding):** `\v` is decoded as U+0008 instead of U+000B (`JsonSanitizer.java:739-740`, `:1495`). This goes to `uplift` under "fix known bugs as we go".

### Refuted (false positives removed)

| CWE | Location | Claim | Why refuted |
|---|---|---|---|
| CWE-94 | `RELEASE-checklist.sh:70` | Perl code injection through the pom version | Only a maintainer who already controls `pom.xml` can set it, so no trust boundary is crossed. The script also exits on line 5. |
| CWE-209 | `JsonSanitizer.java:993` | The AssertionError message leaks the raw input | The throw cannot be reached from any input, given the state handling at the call sites. |
| CWE-532 | `JsonSanitizer.java:316` | Debug logging writes untrusted payloads | The flag is a `private static final false` constant, so the compiler removes that code. |
| CWE-1395 | `pom.xml:78` | Velocity 1.7 through maven-site-plugin 3.8.2 | Site generation only renders the project's own trusted templates, so there is no attacker input. |
| CWE-1104 | `.travis.yml:3` | End-of-life JDKs in CI | Accurate as a config fact but not a vulnerability (already tracked as tech debt). |

## Dependency CVEs

There are **no runtime dependencies.** jsr305 3.0.2 is `provided` scope and has no known CVEs. junit 4.13.1 is test scope and is not affected by CVE-2020-15250, which was fixed in 4.13.1. Everything below is build or release tooling resolved from `~/.m2` POMs; `dependency-check` was not run (offline, read-only).

| Package (via) | Installed | CVE | Fixed in |
|---|---|---|---|
| com.thoughtworks.xstream:xstream (nexus-staging 1.6.8) | 1.4.7 | CVE-2016-3674 | 1.4.9 |
| 〃 | 1.4.7 | CVE-2017-7957 | 1.4.10 |
| 〃 | 1.4.7 | CVE-2020-26217, CVE-2020-26258, CVE-2020-26259 | 1.4.14 / 1.4.15 |
| 〃 | 1.4.7 | CVE-2021-21341 … CVE-2021-21351 | 1.4.16 |
| 〃 | 1.4.7 | CVE-2021-39139 … CVE-2021-39154 (incl. CISA KEV CVE-2021-39144) | 1.4.18 |
| 〃 | 1.4.7 | CVE-2021-43859, CVE-2022-41966 | 1.4.19 / 1.4.20 |
| 〃 | 1.4.7 | CVE-2024-47072 | 1.4.21 |
| commons-io:commons-io (javadoc plugin 3.1.1) | 2.5 | CVE-2021-29425 | 2.7 |
| 〃 | 2.5 | CVE-2024-47554 | 2.14.0 |
| org.apache.httpcomponents:httpclient (javadoc plugin 3.1.1) | 4.5.8 | CVE-2020-13956 | 4.5.13 |
| org.apache.commons:commons-lang3 (javadoc plugin 3.1.1) | 3.5 | CVE-2025-48924 | 3.18.0 |
| org.codehaus.plexus:plexus-archiver (javadoc plugin 3.1.1) | 3.6.0 | CVE-2023-37460 | 4.8.0 |
| org.codehaus.plexus:plexus-archiver (source plugin 3.2.1) | 4.2.1 | CVE-2023-37460 | 4.8.0 |

## Remediation Log

The patch covers Critical and High findings only. No credential hunks exist, so there is no `security_remediation.local.patch`.

| ID | Fix | Patch |
|---|---|---|
| SEC-001 | `normalizeNumber`:<br>• Strip leading zeros before reencoding.<br>• Copy already-decimal digits (octal-looking literals that contain 8 or 9) instead of round-tripping through `BigInteger`.<br>• Keep exact `long` / `BigInteger` reencoding up to `MAX_EXACT_REENCODED_DIGITS = 1024` significant digits. That is at least 8^1024 = 2^3072, far past `Double.MAX_VALUE` < 2^1024.<br>• Past the cap, emit `1e999` (JS `Infinity`) and drop any trailing fraction or exponent.<br>• Adds `testLongReencodedNumbers`. | `security_remediation.patch` (3 hunks) |

**Behavior change:** none for any literal with at most 1024 significant octal or hex digits. A differential run of 20,000 random literals, original against patched, matched byte for byte below the cap. Literals past the cap now produce `1e999`, or `-1e999`, or the key `"1e+999"` instead of an exact decimal integer of thousands of digits. JavaScript evaluates both to `Infinity`, and all 159 outputs past the cap in the differential run were valid JSON.

**Pre-review testing:** 29/29 tests pass on JDK 11 and JDK 17. 1M-digit hex and octal inputs finish in milliseconds; they took about 26 s before the fix.

## Patch Review

An independent security-auditor reviewed the patch in round 1/3. The patch was applied to a scratch copy, the suite passed 29/29 on JDK 17, and the main source still compiles with `javac --release 7`. The reviewer ran a 60,000-literal differential test against the original covering signs, `0x`/`0X`, up to 3000 leading zeros, 8/9 digits, hex `e` digits, the 18–23, 341–343 and 1023–1026 digit boundaries, and fraction or exponent suffixes, in value, key and top-level positions with `-ea`. All 3,004 differing outputs involve more than 1024 significant digits, none is invalid JSON, and none fires an assertion.

| Hunk | Verdict | Reason |
|---|---|---|
| `JsonSanitizer.java` @@ -1070 (`normalizeNumber`) | **RESOLVES** | Every `BigInteger` path is capped at 1024 significant digits. Decimal digits are copied, leading zeros are stripped in linear time, and past the cap the literal becomes `1e999` in O(1). Output below the cap is unchanged, and the copy-on-write `cleaned` invariant is preserved. |
| `JsonSanitizer.java` @@ -1430 (`MAX_EXACT_REENCODED_DIGITS`) | **RESOLVES** | 1025 or more significant digits means at least 2^3072 (octal) or 2^4096 (hex), both far above `Double.MAX_VALUE` < 2^1024. |
| `JsonSanitizerTest.java` @@ -292 (`testLongReencodedNumbers`) | **RESOLVES** | Passes in 85 ms on the patched build and fails on the original (wrong output, and about 26 s per 1M-digit case). |

**Credentials:** no raw credential appears in the shareable patch. Having no `.local.patch` is consistent with zero credential findings.

**Timings after the patch (JDK 17):**
- A 1M-digit hex value takes 13 ms (about 26 s before). A 200k-digit hex takes 1 ms (1058 ms before).
- Worst case under the cap: input packed with 1024-digit hex literals scales linearly at about 60 ms/MB, roughly 60× the cost of decimal input. Lowering the cap to 256 hex digits only gains about 2×, so the cap stays at 1024.

**Residual notes (none block the fix; documented for `brief` and `uplift`):**
1. **Low: value change past the cap when a fraction or exponent follows.** For example, `0` + 1025×`7` + `e-2000` used to underflow to 0 and is now `1e999`. A fraction or exponent after a legacy-octal or hex literal is not valid JavaScript, so there is no exploit, but the "Infinity" description doesn't cover these inputs. *Follow-up:* document it, or emit `0` when the exponent is very negative.
2. **Info: arbitrary-precision consumers now see exactly 10^999** for over-cap literals. JS and double consumers see ±Infinity, as before.
3. **Info: all over-cap numeric keys become `"1e+999"`,** so several such keys collide. Duplicate keys were already possible (`{"a":1,"a":2}`), and JS would collapse them to `"Infinity"`.
4. **Info: test gaps.** Not covered: the octal 1024/1025 boundary, `+`/`0X` past the cap, a base-10 literal in key position, and the dropped-exponent behavior. The 2 s wall-clock check has about 20× headroom but could be flaky on an overloaded CI machine. *Follow-up:* add these cases during `uplift`.

**Outcome:** shipped as reviewed, after 1 review round and 0 revisions. No hunk is marked "needs manual remediation".
