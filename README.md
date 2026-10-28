# json-sanitizer [![CI](https://github.com/OWASP/json-sanitizer/actions/workflows/ci.yml/badge.svg)](https://github.com/OWASP/json-sanitizer/actions/workflows/ci.yml) [![Fuzzing Status](https://oss-fuzz-build-logs.storage.googleapis.com/badges/json-sanitizer.svg)](https://bugs.chromium.org/p/oss-fuzz/issues/list?sort=-opened&can=1&q=proj:json-sanitizer)


Given JSON-like content, The JSON Sanitizer converts it to valid JSON.

[Getting Started](https://github.com/OWASP/json-sanitizer/blob/master/docs/getting_started.md) - [Contact](https://github.com/OWASP/json-sanitizer/blob/master/docs/contact.md)

This can be attached at either end of a data-pipeline to help satisfy
Postel's principle:

> be conservative in what you do, be liberal in what you accept from others

Applied to JSON-like content from others, it will produce well-formed JSON
that should satisfy any parser you use.

Applied to your output before you send, it will coerce minor mistakes in
encoding and make it easier to embed your JSON in HTML and XML.


## Motivation

![Architecture](https://github.com/OWASP/json-sanitizer/blob/master/docs/JSON-Sanitizer-Arch.png)

Many applications have large amounts of code that uses ad-hoc methods
to generate JSON outputs.

Frequently these outputs all pass through a small amount of framework
code before being sent over the network.  This small amount of
framework code can use this library to make sure that the ad-hoc
outputs are standards compliant and safe to pass to (overly) powerful
deserializers like Javascript's `eval` operator.

Applications also often have web service APIs that receive JSON from a
variety of sources.  When this JSON is created using ad-hoc methods,
this library can massage it into a form that is easy to parse.

By hooking this library into the code that sends and receives requests
and responses, this library can help software architects ensure
system-wide security and well-formedness guarantees.


## Input

The sanitizer takes JSON like content, and interprets it as JS eval would.
Specifically, it deals with these non-standard constructs.

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

The output satisfies these guarantees, which the `JsonSanitizer` javadoc lists identically:

 1. It is exactly one JSON value as defined by [RFC 8259](https://www.rfc-editor.org/rfc/rfc8259). Anything after the first complete value, a top-level comma or an unmatched close bracket is dropped: `[1]],alert(1)` becomes `[1]`.
 2. It does not contain `<script`, `</script`, `<!--` or `-->`, in any case, so it can be embedded in an HTML script element without further encoding.
 3. It does not contain `]]>`, so it can be embedded in an XML CDATA section without further encoding. (`<![CDATA[` itself is not escaped.)
 4. It is a valid JavaScript expression, so it can be parsed by `JSON.parse`, or by `eval` after being wrapped in parentheses; no string contains a raw U+2028 or U+2029.
 5. It contains no raw control characters, lone UTF-16 surrogates, U+FFFE or U+FFFF; in strings these are written as `\u`*XXXX* escapes, so the output is valid Unicode and allowed in XML. An escaped lone surrogate is valid RFC 8259 but not [I-JSON](https://www.rfc-editor.org/rfc/rfc7493).
 6. Sanitizing the output again returns it unchanged.

Brackets nest at most 64 levels deep by default (`DEFAULT_NESTING_DEPTH`), or as set by a `maximumNestingDepth` parameter (at most 4096); a deeper array or object is replaced by `null`. Hex and octal literals of more than 1024 significant digits become `1e999`, JavaScript's Infinity. Numeric object keys are converted to their canonical string form textually: an exponent outside the double range is kept (`{1e400:0}` becomes `{"1e+400":0}`), not turned into `"Infinity"`.


## Requirements and limits

 * **Java 8 or later.** The jar's module name is `com.google.json`; it has no runtime dependencies.
 * `sanitize(String)` and `sanitize(CharSequence)` take time linear in the input. `sanitize(CharSequence)` always returns a new `String`.
 * `sanitize(Reader, Appendable)` and `sanitize(InputStream, OutputStream)` (UTF-8) read the whole input into memory, and output can be six times as long. They reject input longer than 4 Mi chars (`DEFAULT_MAXIMUM_INPUT_LENGTH`) with an `IOException` and write nothing; overloads with a `maximumInputLength` parameter change the limit. Malformed UTF-8 becomes U+FFFD; a byte-order mark before the value is dropped.


## EvalMinifier (deprecated)

`EvalMinifier` turns sanitized JSON into a smaller JavaScript expression that must be run with `eval`, which a strict Content Security Policy forbids, and compression makes the savings small. It keeps working and is covered by the same tests, but new code should send `JsonSanitizer` output and parse it with `JSON.parse`.


## Security

Since the output is well-formed JSON, passing it to `eval` will
have no side-effects and no free variables, so is neither a code-injection
vector, nor a vector for exfiltration of secrets.

This library only ensures that the JSON string → Javascript object
phase has no side effects and resolves no free variables, and cannot
control how other client side code later interprets the resulting
Javascript object.  So if client-side code takes a part of the parsed
data that is controlled by an attacker and passes it back through a
powerful interpreter like `eval` or `innerHTML` then that client-side
code might suffer unintended side-effects.

```JavaScript
var myValue = eval(sanitizedJsonString);  // safe
var myEmbeddedValue = eval(myValue.foo);  // possibly unsafe
```

Additionally, sanitizing JSON cannot protect an application from
[Confused Deputy attacks](http://en.wikipedia.org/wiki/Confused_deputy_problem)

```JavaScript
var myValue = JSON.parse(sanitizedJsonString);
addToAdminstratorsGroup(myValue.propertyFromUntrustedSource);
```

# Performance

The sanitize method will return the input string without allocating a new
buffer when the input is already valid JSON that satisfies the properties
above.  Thus, if used on input that is usually well formed, it has minimal
memory overhead.

The sanitize method takes O(n) time where n is the length of the input
in UTF-16 code-units, including for very long number literals.
