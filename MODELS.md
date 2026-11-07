# Model-assisted development and review

Use this workflow for OWASP JSON Sanitizer. Use the full lineup for substantial
code or security changes and focused review for small documentation edits.
Model reviews supplement maintainer review and existing repository controls.

## Model lineup

Keep these selected model IDs; report unavailable slots instead of silently
substituting models. Use Codex for OpenAI models and Claude Code for Anthropic
models. Record tool versions, requested IDs, reported identities and actual
review results; accepting a model name does not prove a review ran.

| Step | Model | Responsibility |
| --- | --- | --- |
| Build | Astra (`gpt-6-astra`) | Implement one scoped change with regression coverage. |
| Second build | Opus 5.5 (`claude-opus-5-5`) | Check implementation, tests and compatibility; correct gaps. |
| Independent review | Sol 6.1 (`gpt-6.1-sol`) | Check Java correctness, exact output, overloads and public APIs. |
| Independent review | Daybreak Blue (`gpt-daybreak-blue-latest`) | Check parser boundaries, malicious input and resource limits. |
| Independent review | Mythos 5.1 (`claude-mythos-5-1`) | Challenge security claims with specifications and adversarial tests. |
| Independent review | Sonnet 5.5 (`claude-sonnet-5-5`) | Check documentation, examples and migration guidance. |
| Resolve | Astra (`gpt-6-astra`) | Resolve findings, validate the final revision and report gaps. |

For Daybreak Blue, verify the authenticated Codex catalog and explicitly select
Blue access (`--cyber-access-program daybreak_blue`). Distinguish access probes
from completed reviews. Do not infer backend identity from a model's own claims.

## JSON Sanitizer contracts

Read [README.md](README.md), [JsonSanitizer.java](src/main/java/com/google/json/JsonSanitizer.java)
and the affected tests. Preserve the documented guarantees and limits:

- **JSON output:** produce exactly one RFC 8259 value; preserve documented repair,
  truncation, numeric normalization and escaping behavior. Check malformed input,
  comments, missing delimiters, trailing content and empty input.
- **Embedding and Unicode:** output must exclude case-insensitive `<script` and
  `</script`, plus `<!--`, `-->` and `]]>`. Preserve required escapes for control
  characters, lone surrogates, U+2028, U+2029, U+FFFE and U+FFFF. These guarantees
  do not make parsed values safe for later use in `innerHTML` or other interpreters.
- **Idempotence:** sanitizing output again with the same nesting limit must leave
  it unchanged; stream input limits must accommodate the output length.
- **Resource limits:** preserve linear-time sanitization, nesting limits and
  bounded Reader/InputStream input. Those overloads buffer the whole input;
  they are not incremental parsers. Test oversized input, deep nesting, long
  numbers, malformed UTF-8 and documented exception/output behavior.
- **Compatibility:** preserve Java 8 APIs/bytecode, `com.google.json` packages and
  module name, zero runtime dependencies, and overload contracts. Review exact
  output changes against the golden corpus, legacy oracle and
  [approved differences](docs/modernization/APPROVED_DIFFERENCES.md).
- **EvalMinifier:** keep its deprecated API working and tested. Its output is a
  JavaScript expression, not necessarily JSON; recommend `JsonSanitizer` with
  `JSON.parse` for new code.

Follow [SECURITY.md](SECURITY.md) for suspected vulnerabilities. Keep undisclosed
reproductions and credentials out of public reports and model review packets.

## Review workflow

1. Record the baseline SHA and working-tree status; preserve unrelated work.
   Base new branches on `master`. Run build and second-build steps sequentially.
2. Give independent reviewers the same frozen revision, complete diff, relevant
   source/tests and task intent. Reviews may run in parallel; reviewers must not
   edit the shared branch, launch other reviewers or publish results themselves.
3. Require findings with severity, file/line, evidence and a concrete fix, plus
   a **MERGE**, **REQUEST_CHANGES** or **CLOSE** recommendation. Separate confirmed
   regressions from hypotheses; model agreement alone is not security evidence.
4. Resolve findings against code, specifications and reproducible tests. Add a
   regression that fails before the fix; re-review material subsequent changes.
5. Report the final revision, actual reviews, validation and remaining gaps.
   Update relevant Javadocs, README and changelog for behavior changes. Merging,
   publishing and releases require authorization; follow
   [docs/RELEASING.md](docs/RELEASING.md) for releases.

## Validation

Build with JDK 17+ and Maven 3.6.3 or later in the 3.x series (CI uses JDK 21).
The library targets Java 8. For code changes, run:

```sh
mvn -B -ntp clean verify
```

For sanitizer or minifier changes, also exercise the existing Jazzer target:

```sh
JAZZER_FUZZ=1 JAZZER_MAX_DURATION=120s mvn -B -ntp test -Dtest='JsonSanitizerFuzzTest#sanitizeProperties' -Djacoco.skip=true
```

Use [CI](.github/workflows/ci.yml) as the source of truth for the JDK
8/11/17/21/25 test matrix, required test-report checks, fuzzing and OSS-Fuzz build
compatibility. Never weaken checks to make a change pass; report unrun checks.
For documentation-only changes, verify claims and local links and run
`git diff --check`; runtime tests are unnecessary unless executable behavior changes.
