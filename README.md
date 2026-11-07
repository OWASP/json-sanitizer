# OWASP JSON Sanitizer

[![CI](https://github.com/OWASP/json-sanitizer/actions/workflows/ci.yml/badge.svg)](https://github.com/OWASP/json-sanitizer/actions/workflows/ci.yml)

Converts JSON-like text into valid JSON, with escaping for HTML script elements
and XML CDATA sections. It repairs input rather than rejecting malformed JSON;
use a strict parser when invalid input must be rejected.

Requires **Java 8 or later**, with no runtime dependencies.

```xml
<dependency>
  <groupId>org.owasp.json-sanitizer</groupId>
  <artifactId>json-sanitizer</artifactId>
  <version>1.3.0</version>
</dependency>
```

```java
import com.google.json.JsonSanitizer;

String json = JsonSanitizer.sanitize("{name:'Ada',}");
// {"name":"Ada"}
```

[Getting started and migration](docs/getting_started.md) ·
[Changelog](CHANGELOG.md) · [Security policy](SECURITY.md) ·
[Contact](docs/contact.md)

## Input

The sanitizer repairs common JavaScript-like syntax without executing it.
It is not a JavaScript evaluator.

| Construct     | Policy                                                        |
|---------------|---------------------------------------------------------------|
| `'...'`       | Single quoted strings are converted to JSON strings.          |
| `\xAB`        | Hex escapes are converted to JSON unicode escapes.            |
| `\012`        | Octal escapes are converted to JSON unicode escapes.          |
| `0xAB`        | Hex integer literals are converted to JSON decimal numbers.   |
| `012`         | Octal integer literals are converted to JSON decimal numbers. |
| `+.5`         | Decimal numbers are coerced to JSON's stricter format.        |
| `[0,,2]`      | Elisions in arrays are filled with `null`.                    |
| `[1,2,3,]`    | Trailing commas are removed.                                  |
| `{foo:"bar"}` | Unquoted property names are quoted.                           |
| `//comments`  | JS style line and block comments are removed.                 |
| `(...)`       | Grouping parentheses are removed.                             |

The sanitizer fixes missing punctuation, end quotes, and mismatched or
missing close brackets.  If an input contains only white-space then
the valid JSON string `null` is substituted.


## Output

The output has these guarantees (also documented in the `JsonSanitizer` Javadoc):

 1. It is exactly one JSON value as defined by [RFC 8259](https://www.rfc-editor.org/rfc/rfc8259). Anything after the first complete value, a top-level comma or an unmatched close bracket is dropped: `[1]],alert(1)` becomes `[1]`.
 2. It does not contain `<script`, `</script`, `<!--` or `-->`, in any case, so it can be embedded in an HTML script element without further encoding.
 3. It does not contain `]]>`, so it can be embedded in an XML CDATA section without further encoding. (`<![CDATA[` itself is not escaped.)
 4. `JSON.parse` accepts it. It is also a JavaScript expression that `eval` can run after it is wrapped in parentheses, except that an object repeating the key `__proto__` is a syntax error there. No string contains a raw U+2028 or U+2029.
 5. Inside strings, U+0000 to U+001F, lone UTF-16 surrogates, U+2028, U+2029, U+FFFE and U+FFFF use JSON escapes (short forms such as `\t` and `\n`, or `\u`*XXXX*); between tokens only space, tab, line feed and carriage return appear. Other characters, U+007F to U+009F included, appear as they are, so the output is valid Unicode and allowed in XML 1.0. An escaped lone surrogate is valid RFC 8259 but not [I-JSON](https://www.rfc-editor.org/rfc/rfc7493).
 6. Sanitizing it again with the same `maximumNestingDepth` returns it unchanged (through a Reader or InputStream overload, with a `maximumInputLength` at least as long as the output).

Brackets nest at most 64 levels deep by default (`DEFAULT_NESTING_DEPTH`), or as set by a `maximumNestingDepth` parameter, which is clamped to 1 to 4096; a deeper array or object is replaced by `null`. Hex and octal literals of more than 1024 significant digits become `1e999`, JavaScript's Infinity. Numeric object keys are converted to a string form textually: an exponent outside the double range is kept (`{1e400:0}` becomes `{"1e+400":0}`, not `"Infinity"`), and one beyond the int range is kept as written (`{1e2147483648:0}` becomes `{"1e2147483648":0}`), so keys of equal value can differ. The Reader and InputStream overloads accept at most 4 Mi chars (`DEFAULT_MAXIMUM_INPUT_LENGTH`) unless given a `maximumInputLength`. The library runs on Java 8 and later.


## Requirements and limits

- Java 8 or later; automatic module name `com.google.json`.
- String and CharSequence input takes O(n) time in UTF-16 code units. Results
  are immutable strings; an already-safe String may be reused. These overloads
  have no input-length limit, so callers must bound untrusted input themselves.
- Reader and UTF-8 InputStream overloads buffer the whole input. The default
  limit is 4,194,304 UTF-16 code units, configurable with `maximumInputLength`.
  Exceeding it throws `IOException` before any output is written. The Reader
  loop may read up to 8,192 characters beyond the limit; UTF-8 decoding can read
  additional bytes ahead. This is not a byte or network-request limit.
- Output can expand to roughly six times the input length. Allow heap space for
  input, output and temporary buffers; the input limit is not a heap limit.
- Malformed UTF-8 decodes to U+FFFD. Streams are not closed; the OutputStream
  overload flushes its output.
- Nullness annotations use JSpecify in `provided` scope. Add
  `org.jspecify:jspecify:1.0.1` to your build if your analysis tools need them.

## Security

Parse output with `JSON.parse` or a JSON parser. Sanitization does not validate
business rules, authorize actions, or make parsed values safe for `innerHTML`,
SQL, shell commands, or later JavaScript execution. The embedding guarantees
above do not apply to HTML attributes or arbitrary output contexts.

`EvalMinifier` is deprecated. It produces a JavaScript expression requiring
execution, rather than JSON for `JSON.parse`; a strict Content Security Policy
blocks `eval`. Keep it only for existing integrations.

## Development

Build with JDK 17 or later and Maven 3.6.3 or later in the 3.x series:

```sh
mvn -B -ntp clean verify
```

CI builds on JDK 21 and tests on Java 8, 11, 17, 21 and 25, with Jazzer fuzzing
and an OSS-Fuzz build check. See [MODELS.md](MODELS.md) for model-assisted review
and [the release guide](docs/RELEASING.md) for publishing.
The [modernization archive](docs/modernization/README.md) preserves historical
decisions and evidence; it is not current setup guidance.
