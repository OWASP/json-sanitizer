# Preflight: json-sanitizer

- **Date:** 2026-10-27
- **Source:** `legacy/json-sanitizer` → symlink to `/Users/jmanico/Dropbox/github/json-sanitizer` (the repository itself)
- **Target stack:** none given. The goal is "understand" (see `INTENT.md`).

## Answers

1. **Scope.** Is this code the complete system, or one slice of a larger codebase? If a slice, what outside depends on it, and is breaking those consumers acceptable?
   > Complete system
2. **Build & test locally.** Can this environment restore, build and run the tests? Roughly how long does the full CI pipeline take?
   > Yes, mvn test works

   *Observed:* when preflight started, Maven was not installed and no JDK was on the PATH. Homebrew had openjdk@11/17/21/25 installed but not on the PATH (Homebrew calls these "keg-only"). The person then said "its ok to install maven", so Maven 3.10.0 was installed with `brew install maven`, which also installed OpenJDK 27 as Maven's default runtime. `mvn clean test` now passes on JDK 11 and 17 in about 3 s. It fails on JDK 21 and 27 (Check 3). **Open item:** CI duration was not given, and the only CI definition is `.travis.yml` (Check 3a).
3. **Bespoke build infrastructure.** Is there organization-specific build or dependency machinery a newcomer wouldn't guess? Where is it documented?
   > None
4. **Prior attempts.** Has anyone tried to modernize this before? What went wrong?
   > No
5. **Off limits.** Is anything in this code not allowed to change in this pass?
   > Nothing

## Scope boundary (Check 6)

`json-sanitizer` is a **standalone repository**, with no parent POM and no git repository or reactor above it. It reaches no source outside its own folder.
**Inbound consumers (the "blast radius"):** the library is published to Maven Central as `com.mikesamuel:json-sanitizer`. Every downstream project that depends on it is an outside consumer of the public API (`JsonSanitizer.sanitize(...)` overloads, `EvalMinifier`) and of the bytecode level (currently Java 6). Nothing in this workspace depends on it, but two choices affect those external users, and `brief` must decide each one explicitly: raising the minimum Java version, and changing behavior (the "fix known bugs as we go" choice in `INTENT.md`). Each needs a version and release-note decision.

## Checks

| # | Check | Status | Found | Fix |
|---|---|---|---|---|
| 1 | Stack | ✅ | Java library, Maven (`pom.xml`, no modules). 6 `.java` files: 1,890 LOC main (`JsonSanitizer` 1,541, `EvalMinifier` 347, `package-info`), 939 LOC tests (JUnit 4 + `junit.framework.TestCase`). Only dependency: `jsr305` 3.0.2 (annotations); test dependency: `junit` 4.13.1. Also `README.md`, `docs/` (getting started, architecture diagram `.png` + `.graffle`), `RELEASE-checklist.sh` and `.travis.yml`. | — |
| 2 | Analysis tooling | ⚠️ | `python3` 3.15.0 ✅, `git` 2.56.0 ✅, `semgrep` ✅ (an update is available). **Missing:** `scc`/`cloc` (LOC falls back to `wc`, which is fine at this size) and `lizard` (only used by portfolio runs). | `brew install scc` · `pipx install lizard` (optional) |
| 3a | Build definition | ⚠️ | `.travis.yml` lists `openjdk8/11/13`, but Travis CI for open source is defunct, so **there is no live CI**. The pom sets `maven.compiler.source/target` to **1.6**, and to **1.7** in profile `jdk12on` (JDK 12+). Dependencies come from Maven Central; there is no `settings.xml` and no `.mvn/`. Releases go through OSSRH (`oss.sonatype.org`) via `nexus-staging-maven-plugin` 1.6.8; OSSRH was retired in mid-2025 in favor of the Central Portal, so **the release path described in the pom no longer works**. The plugins are old: jacoco 0.8.5, spotbugs 3.1.12.2, javadoc 3.1.1, sonar 3.7. Maven warned about an unreachable `repository.sonatype.org` metadata lookup; the warning is harmless. | Let `brief` decide on a GitHub Actions workflow and the Central Portal publishing plugin. |
| 3b | Smoke test (L1 + L2) | ⚠️ | **L1:** `javac` of the main sources succeeds on JDK 11, 17, 21 and 25. **L2 (`mvn clean test`):** ✅ JDK 11, 28/28 tests (target 1.6, "obsolete" warnings). ✅ JDK 17, 28/28 (target 1.7; jacoco 0.8.5 logs `IllegalClassFormatException` instrumenting JDK classes, so coverage is unreliable). ❌ **JDK 21 and JDK 27:** `Source option 7 is no longer supported. Use 8 or later.` With `-Dmaven.compiler.source=8 -Dmaven.compiler.target=8`, JDK 21 builds and passes 28/28. **Watch out:** Homebrew's `mvn` defaults to JDK 27 and therefore fails. An incremental `mvn test` can look green on JDK 21 because it skips compiling when `target/` is up to date, so always use `clean`. `target/` was rebuilt with JDK 11 at the end. | Set `JAVA_HOME` before running Maven: `export JAVA_HOME=$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home` |
| 4 | Source completeness | ✅ | Every import resolves (`java.*`, `javax.annotation` from jsr305, JUnit). A library needs no deployment, config or data descriptors. No jars or classes in the source tree; `target/` is gitignored build output. | — |
| 5 | Optional context | ✅ | No runtime telemetry, which is normal for a library. Git: 120 commits from 2012 to 2026 (most active in 2014 and 2021; 9 so far in 2026), remote `github.com/OWASP/json-sanitizer`. | — |
| 6 | Scope boundary | ✅ | Standalone repository. Inbound consumers are external Maven Central users (see above). | Recorded for `brief` |
| 7 | Source protected from edits | ⚠️ | No `Edit` deny rule in `.claude/settings*.json`, `~/.claude/settings.json` or `$CLAUDE_CONFIG_DIR/settings.json` (`~/.claude-owasp`). | See the note below |

**Check 7 note.** `legacy/json-sanitizer` is a symlink to the repository root, and the root also contains `analysis/` and `legacy/`. Two consequences follow:
- **A deny rule on the whole target would also block `analysis/`**, where the commands write. Protect the real source paths instead. Add this to `.claude/settings.json`:
  ```json
  { "permissions": { "deny": [
    "Edit(/legacy/**)",
    "Edit(//Users/jmanico/Dropbox/github/json-sanitizer/src/**)",
    "Edit(//Users/jmanico/Dropbox/github/json-sanitizer/pom.xml)"
  ] } }
  ```
  Managed (organization) settings are not read here. A permission rule covers Claude's file tools and the shell commands it recognizes, but not a script that opens files itself. The hard guarantee is a read-only mount or a sandbox.
- **The symlink loops back into the repository** (`legacy/json-sanitizer/legacy/json-sanitizer/…`). A tool that follows symlinks (`find -L`, `grep -R`) will loop, so analysis should scan `src/` and `pom.xml` directly. `analysis/` and `legacy/` show as untracked in git; don't commit them.

## Readiness

| Command | Verdict | Why |
|---|---|---|
| `assess` | **Ready-with-gaps** | Without `scc`, LOC and complexity fall back to `wc`, which is fine at 2.8 kLOC. |
| `map` | **Ready** | Python 3 is present; 2 production classes. |
| `extract-rules` | **Ready** | The full source is in the tree, and there's a good test suite to cite. |
| `harden` | **Ready-with-gaps** | `semgrep` is present and the pom has a SpotBugs plugin. No dependency CVE scanner (`osv-scanner`, `dependency-check`), but with only jsr305 and JUnit the risk is low: `brew install osv-scanner`. |
| `brief` | **Ready** | Needs only the discovery artifacts. |
| `uplift` | **Ready-with-gaps** | Target JDKs 17, 21, 25 and 27 are present. The source runtime is not a true Java 6 or 7 (the oldest here is JDK 11), so equivalence is "built with JDK 11 vs target", not a real Java 6 run. OpenRewrite is not installed but runs on demand as a Maven plugin. Inbound consumers exist, so `brief` must set the new minimum Java version. |
| `transform` / `reimagine` | **Ready-with-gaps** | No target chosen yet; prove one when it is. Legacy dual-execution works on JDK 11/17. |

**Most important fix (done 2026-10-27):** `JAVA_HOME` is now set to JDK 17 in `.claude/settings.local.json` (gitignored globally); it takes effect from the next Claude Code session. Before this, builds had to set `JAVA_HOME` to JDK 17 or 11 by hand. The default `mvn` (JDK 27) and JDK 21 fail on the pom's `source/target 1.7`. That failure is itself the first uplift finding.

**Next:** `/code-modernization:modernize-assess json-sanitizer`
