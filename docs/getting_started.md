# Getting Started

## Importing

You can fetch the jars from
[Maven Central](https://central.sonatype.com/artifact/org.owasp.json-sanitizer/json-sanitizer)
or you can let your favorite java package manager handle it for you via

```Maven
<dependency>
  <groupId>org.owasp.json-sanitizer</groupId>
  <artifactId>json-sanitizer</artifactId>
  <version>1.3.0</version>
</dependency>
```

**New coordinates from 1.3.0.** Releases up to 1.2.3 were published as
`com.mikesamuel:json-sanitizer`; from 1.3.0 on the library is
`org.owasp.json-sanitizer:json-sanitizer`. The artifactId and Java
package (`com.google.json`) are unchanged. The automatic module name changes
from `json.sanitizer` to `com.google.json`: update `requires json.sanitizer;`
to `requires com.google.json;` in `module-info.java`. Update the groupId in your
build, and make sure the old artifact does
not also arrive through another dependency: with both on the classpath, the
same classes exist twice. Exclude `com.mikesamuel:json-sanitizer` there if it
does.

Pin an exact version, as above: an open range such as `[1.0,)` picks up new
releases unreviewed. Version 1.3.0 needs Java 8 or later; it changes the output
for a few malformed or hostile inputs, listed in the
[changelog](https://github.com/OWASP/json-sanitizer/blob/master/CHANGELOG.md).

Once you've got the JSON sanitizer JAR on your classpath,

```Java
import com.google.json.JsonSanitizer;
```

will let you call

```Java
String wellFormedJson = JsonSanitizer.sanitize(myJsonLikeString);
```

That's it.  Now `wellFormedJson` is a string of well-formed JSON that
can be parsed with `JSON.parse`. See the [output guarantees](../README.md#output)
for embedding in HTML script elements or XML CDATA sections; they do not cover
arbitrary HTML attributes or other contexts.

If you have further questions, check our
[support list](https://groups.google.com/forum/#!forum/json-sanitizer-support).
