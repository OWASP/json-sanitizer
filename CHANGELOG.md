# Changelog

## 1.3.0 (unreleased)

**Requires Java 8 or later** (was Java 6). The public API is source- and binary-compatible.

### Output changes
Only these inputs sanitize differently; everything else is byte-for-byte the same as 1.2.3, which the build checks over 567,123 inputs.

- **Security (SEC-001, denial of service):** hex and octal literals with more than 1024 significant digits become `1e999` (JavaScript reads both as `Infinity`). Converting them exactly took time quadratic in their length: a 1,000,000-digit literal took about 26 s. A fraction or exponent after such a literal is dropped, so a huge literal with a large negative exponent (a tiny value, and not valid JavaScript to begin with) now reads as `Infinity`.
- `\v` in strings is U+000B (vertical tab), as in JavaScript; it was U+0008.
- A value nested deeper than the limit no longer swallows the values after it when it contains a quote inside a word: `[[a'b],"keep",'x']` at depth 1 is `[null,"keep","x"]` (was `[null]`).
- Numeric object keys with exponents near the int limits get the correct exponent: `{11e2147483647:0}` gives `{"1.1e+2147483648":0}` (was `"1.1e--2147483648"`).
- `EvalMinifier` no longer splits `12345e+1` at the `+` (its output was not valid JavaScript).

### Changes to API that has not been released before
- `sanitize(CharSequence)` (new since 1.2.3) always returns a new `String`, never the caller's object, and declares `String`.
- `sanitize(Reader, Appendable)` and `sanitize(InputStream, OutputStream)` (new since 1.2.3) reject input longer than `DEFAULT_MAXIMUM_INPUT_LENGTH` (4 Mi chars) with an `IOException`; new overloads take a `maximumInputLength`.

### Other
- Over-deep input is replaced with `null` instead of throwing (new since 1.2.3).
- `EvalMinifier` is deprecated: its output needs `eval`, which a strict Content Security Policy forbids. It keeps working.
- Nullness annotations are [JSpecify](https://jspecify.dev/) (`provided` scope) instead of jsr305; `sanitize` parameters are `@Nullable`, as `null` was always accepted.
- The jar has the module name `com.google.json`.
- Built with JDK 21 and tested on Java 8, 11, 17, 21 and 25; fuzzed with Jazzer on every change.
