# Getting started

Use Java 8 or later. Add the published Maven dependency:

```xml
<dependency>
  <groupId>org.owasp.json-sanitizer</groupId>
  <artifactId>json-sanitizer</artifactId>
  <version>1.3.0</version>
</dependency>
```

Artifacts are on [Maven Central](https://central.sonatype.com/artifact/org.owasp.json-sanitizer/json-sanitizer/1.3.0).

```java
import com.google.json.JsonSanitizer;

String json = JsonSanitizer.sanitize("{name:'Ada',}");
// {"name":"Ada"}
```

Parse the result with a JSON parser, such as JavaScript's `JSON.parse`.
Sanitization repairs input; it does not validate a schema or business rules.
See the [output guarantees](../README.md#output) and
[resource limits](../README.md#requirements-and-limits) before handling untrusted input.

## Bounded input

Reader and UTF-8 stream overloads buffer the entire input. For example, this
accepts up to 100,000 UTF-16 code units with a nesting limit of 64:

```java
// reader is a java.io.Reader; writer is a java.io.Writer.
JsonSanitizer.sanitize(reader, writer, 64, 100_000);
```

Handle `IOException`. Input exceeding the length limit produces no output;
other I/O failures can leave partial output. Neither stream is closed.
Enforce a separate byte limit and timeout at the network boundary.

## Migrating from 1.2.3

- Change `com.mikesamuel:json-sanitizer` to
  `org.owasp.json-sanitizer:json-sanitizer:1.3.0`. No relocation is published
  under the old coordinates. Exclude the old artifact if a transitive dependency
  still brings it in, to avoid duplicate classes.
- Upgrade from Java 6 to Java 8 or later. Java imports stay `com.google.json`.
- On the module path, change `requires json.sanitizer;` to
  `requires com.google.json;`.
- Review the [changelog](../CHANGELOG.md) for repaired-input differences,
  stream limits and `EvalMinifier` deprecation. Deprecation warnings can fail
  builds that use `-Werror`.

For questions or bug reports, see [Contact](contact.md).
