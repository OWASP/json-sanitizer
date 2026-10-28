# Changelog

## 1.3.0 (unreleased)

**Requires Java 8 or later** (was Java 6). The public API is binary-compatible and only adds methods. Code that uses `EvalMinifier` now gets a deprecation warning, which fails builds that compile with `-Werror`.

### Output changes
Only these inputs sanitize differently from 1.2.3. The build checks, over 567,123 inputs and seven API calls each, that everything else is byte-for-byte the same as the last pre-release master (`d9b2768`), which already included the two changes marked "since 1.2.3".

- **Security (CVE-2026-37736, denial of service, #35); over-deep input (since 1.2.3):** an array or object nested deeper than the limit is replaced by `null`. Every release from 1.0 to 1.2.3 threw `ArrayIndexOutOfBoundsException` instead (for example on 65 `[`).
- **`EvalMinifier` pooling (since 1.2.3):** pooled arguments come in a stable order (1.2.3 used hash order); tokens shorter than four characters at the end of the input are no longer pooled; unprofitable tokens no longer leave an unused parameter and argument; a string ending in a backslash no longer crashes the minifier.
- **Security (SEC-001, denial of service):** hex and octal literals with more than 1024 significant digits become `1e999` (JavaScript reads both as `Infinity`). Converting them exactly took time quadratic in their length: a 1,000,000-digit literal took about 26 s (releases 1.2.2 and 1.2.3). A fraction or exponent after such a literal is dropped, so a huge literal with a large negative exponent (a tiny value, and not valid JavaScript to begin with) now reads as `Infinity`.
- `\v` in strings is U+000B (vertical tab), as in JavaScript; it was U+0008.
- A value nested deeper than the limit no longer swallows the values after it when it contains a quote inside a word: `[[a'b],"keep",'x']` at depth 1 is `[null,"keep","x"]` (was `[null]`).
- Numeric object keys with exponents near the int limits get the correct exponent: `{11e2147483647:0}` gives `{"1.1e+2147483648":0}` (was `"1.1e--2147483648"`).
- `EvalMinifier` no longer splits `12345e+1` at the `+` (its output was not valid JavaScript).

### New API
- `sanitize(CharSequence)` returns a new `String`, never the caller's object.
- `sanitize(Reader, Appendable)` and `sanitize(InputStream, OutputStream)` (UTF-8) read the whole input, and reject input longer than `DEFAULT_MAXIMUM_INPUT_LENGTH` (4 Mi chars) with an `IOException`; overloads with a `maximumInputLength` change the limit.

### Other
- `EvalMinifier` is deprecated: its output needs `eval`, which a strict Content Security Policy forbids. It keeps working.
- Nullness annotations are [JSpecify](https://jspecify.dev/) (`provided` scope) instead of jsr305; `sanitize` parameters are `@Nullable`, as `null` was always accepted.
- The jar has the module name `com.google.json`.
- Built with JDK 21 and tested on Java 8, 11, 17, 21 and 25; fuzzed with Jazzer on every change.
