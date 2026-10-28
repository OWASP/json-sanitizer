# Approved output differences: json-sanitizer Phase 3

- **Date:** 2026-10-28
- **Baseline:** the legacy release at commit `d9b2768` (the oracle of `BASELINE.md`), kept verbatim in `src/test/java/com/google/json/legacy/` and proven by `GoldenCorpusTest` to reproduce `golden-digests.txt` (SHA-256 pinned).
- **Corpus:** 567,123 inputs, seven calls each (3,969,861 calls): `sanitize(String)`, `sanitize(CharSequence)`, `sanitize(String, 1)`, `sanitize(String, 3)`, `EvalMinifier.minify(String)`, `sanitize(Reader, Appendable)`, `sanitize(InputStream, OutputStream)`.
- **Approval:** each class below maps to a ticked Q4 sub-item (or RULE-001, approved in `RULE_REVIEWS.md`) in `MODERNIZATION_BRIEF.md` §7. The Q4 decisions were made by Claude on the owner's explicit delegation (2026-10-28); the owner signs this list off before the release (brief §6, Phase 3 "Manual UAT").
- **Sign-off:** signed off 2026-10-28 by Claude on the owner's explicit delegation ("make your best choices here for me"), after re-checking each class against its Q4 decision. Every class is a security or correctness fix; the one ordinary consumers are likely to notice is RULE-001 (`\v` now U+000B, as JavaScript reads it). The others need hostile or malformed input to appear. The owner can withdraw this sign-off.
  **Confirmed by the owner, Jim Manico, 2026-10-28** ("yes this is all good"), after a plain-language summary of every class above.
- **Enforced by:** `GoldenCorpusTest.testOnlyApprovedDifferencesFromTheLegacyBaseline` runs both implementations over the whole corpus on every CI leg. Every (input, call) whose output differs must be explained by `ApprovedDifferences`, and the count per class must equal the table below exactly. A change outside these classes, or one more or one fewer difference in a class, fails the build.

## How a difference is "explained"

Never by comparing the new output with itself. In order:

1. **Replay a rewrite.** The fix is stated as a rewrite of the *input* in terms of the legacy code ("an over-long hex literal reads as `1e999`"). The legacy implementation runs on the rewritten input, and its output must equal the new output exactly. If several fixes touch one input, all their rewrites are applied together.
2. **Output checks**, only where legacy cannot express the fix (it cannot print an exponent past the int range, pool a whole `12345e+1`, or treat every backslash as an escape). Each check accepts one narrow, stated shape of change and nothing else.

## Classes and counts

| Class (as named in `ApprovedDifferences`) | Approval | (input, call) pairs | How checked |
|---|---|---:|---|
| SEC-001 over-long hex/octal literal is 1e999 | Q4a | 35,280 | Replay: each hex or octal literal with more than 1024 significant digits, and the rest of its token, becomes `1e999` |
| SEC-005 over-deep value skipped as the main loop tokenizes it | Q4d | 12,106 | Replay: each over-deep value, found by an independent re-implementation of the main loop's token rules, becomes `[]` (which legacy turns into `null` by the same branch) |
| RULE-001 \v escape is U+000B | RULE-001 (RULE_REVIEWS) | 21,296 | Replay: each `\v` escape is written `\u000b` |
| combined: SEC-005 + RULE-001 | Q4d, RULE-001 | 722 | Replay of both rewrites together |
| RULE-001 \v escape is U+000B (checked on the output) | RULE-001 | 623 | Output: same length; every difference is the last character of a `\u0008` that is now `\u000b`; at most one per `\v` escape in the input. Covers positions where the legacy main loop does not treat the backslash as an escape |
| combined: SEC-005 + RULE-001 (checked on the output) | Q4d, RULE-001 | 6 | The same output check, against legacy replayed on the SEC-005 rewrite |
| SEC-006 numeric key exponent no longer wraps at the int limit | Q4e | 14 | Output: legacy printed `e--2147483648`; the new output is exactly that token replaced by the true exponent (`+2147483648` or `-2147483648` by the input's sign; an int is exact modulo 2^32) |
| SEC-004 minify keeps an exponent's + sign in its number | Q4f | 305 | Output, on the minify call only: legacy pooled a value ending in `e`/`E` (the bug's signature), and the new output with its pooled values substituted back is exactly the sanitized JSON less the whitespace pooling drops |
| **Total** | | **70,352** | of 3,969,861 calls (1.77%) |

**Attribution is first-match.** A difference is counted once, under the first check that explains it, in the order above. Where fixes overlap in one input, the split between classes depends on that order; the total and the "every difference is explained" guarantee do not. For example, minify outputs whose input has both the `e+` bug and a `\v` are counted under SEC-004, whose check is on meaning (the expansion equals the new sanitized JSON, whose own `\v` change is checked on the other calls).

**No output difference by design:** SEC-002 (`sanitize(CharSequence)` returns an immutable `String`; the text is the same) and SEC-003 (input over 4 Mi chars now throws `IOException`; no corpus input is near the limit).

## Examples (legacy, then new)

| Class | Input | Legacy output | New output |
|---|---|---|---|
| SEC-001 | `[0x` + 1025 × `f` + `]` | `[` + the exact 1,235-digit integer + `]` | `[1e999]` |
| SEC-001 | `{0x` + 2000 × `f` + `:0}` | `{"1.73766203…9375e+2408":0}` (all 2,409 digits) | `{"1e+999":0}` |
| SEC-001 | `[0` + 2000 × `7` + `e-2000]` | `[15134705823…2869375e-2000]`: the exact integer and the exponent, about 1.5e-194 | `[1e999]`, which reads as Infinity: **a value change, not just a form change** (Q4a residual) |
| SEC-005 | `[[a'b],"keep",'x']` at depth 1 | `[null]` | `[null,"keep","x"]` |
| RULE-001 | `["a\vb"]` | `["a\u0008b"]` | `["a\u000bb"]` |
| SEC-006 | `{11e2147483647:0}` | `{"1.1e--2147483648":0}` | `{"1.1e+2147483648":0}` |
| SEC-006 | `{1e-2147483648:0}` | `{"1e--2147483648":0}` | `{"1e-2147483648":0}` |
| SEC-004 | `minify` of twelve `12345e+1` | `(function(a){return[a+1,…]}(12345e))` | `(function(a){return[a,…]}(12345e+1))` |
| SEC-002 | `sanitize(new StringBuilder("[\"ok\"]"))` | the caller's `StringBuilder` | an equal, immutable `String` |
| SEC-003 | an endless `InputStream` | `OutOfMemoryError` (119 ms at `-Xmx256m`) | `IOException: JSON-like input is longer than 4194304 chars` |

## For the release notes

- Hex and octal literals with more than 1024 significant digits become `1e999` (JavaScript reads both as `Infinity`). A fraction or exponent after such a literal is dropped, so a huge literal with a large negative exponent (a tiny value, not valid JavaScript to begin with) now reads as `Infinity`.
- `\v` in strings is U+000B, as in JavaScript (was U+0008).
- A value nested deeper than the limit no longer swallows its siblings when it contains a quote inside a word.
- Numeric object keys with exponents near the int limits get the correct exponent (were printed with a wrapped, doubled sign).
- `EvalMinifier` no longer splits `e+` exponents (its output was invalid JavaScript).
- `sanitize(CharSequence)` always returns an immutable `String`.
- The Reader and InputStream overloads reject input over 4 Mi chars with an `IOException` (new overloads take a `maximumInputLength`).
