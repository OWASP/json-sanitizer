# Getting Started

## Importing

You can fetch the jars from
[Maven Central](https://central.sonatype.com/artifact/com.mikesamuel/json-sanitizer)
or you can let your favorite java package manager handle it for you via

```Maven
<dependency>
  <groupId>com.mikesamuel</groupId>
  <artifactId>json-sanitizer</artifactId>
  <version>1.3.0</version>
</dependency>
```

Pin an exact version, as above: an open range such as `[1.0,)` picks up new
releases unreviewed. (1.3.0 is the next release; until it is on Maven Central,
the latest is 1.2.3.) Version 1.3.0 needs Java 8 or later; it changes the output
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
is safe to pass to JavaScript's `eval` operator and which can be
easily embedded in XML or HTML.

If you have further questions, check our
[support list](https://groups.google.com/forum/#!forum/json-sanitizer-support).
