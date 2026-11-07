# Security policy

## Reporting a vulnerability

Report vulnerabilities privately through GitHub: on the repository's **Security** tab, choose **Report a vulnerability** ([direct link](https://github.com/OWASP/json-sanitizer/security/advisories/new)). Please do not open a public issue, pull request or discussion for a suspected vulnerability.

Include the input that triggers it, the `sanitize` overload and arguments used, the output you got, and which guarantee it breaks. A one-line Java reproduction is ideal.

The project has one volunteer maintainer. Responses and fixes are best-effort; no response deadline is guaranteed. A confirmed vulnerability is fixed on master, released, and then disclosed in a GitHub security advisory that names the fixed version; reporters are credited unless they ask not to be.

## What counts as a vulnerability

Anything that breaks a guarantee listed under [Output](README.md#output) in the README, for any input, for example:

- output that contains `<script`, `</script`, `<!--`, `-->` or `]]>`, or otherwise escapes an HTML script element or an XML CDATA section;
- output that is not one valid RFC 8259 JSON value, or that changes when sanitized again;
- an unexpected exception for input accepted by the API contract (excluding failures in caller-supplied streams or objects);
- time worse than linear in the input, or unbounded buffering despite a configured input limit (see [Requirements and limits](README.md#requirements-and-limits)).

Out of scope: what the README says the library does not do, such as how client code later uses the parsed values, SQL or command injection, `<![CDATA[` (not escaped), `eval` of an object that repeats `__proto__`, and `EvalMinifier`'s use of `eval` itself.

## Supported versions

Security fixes go into the next release from master. Use 1.3.0 or a later stable release. The 1.2.x line is unsupported; follow the [migration guide](docs/getting_started.md#migrating-from-123).
