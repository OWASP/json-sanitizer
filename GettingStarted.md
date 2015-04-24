# Getting Started #

## Importing ##

You can fetch the jars from <a href='http://search.maven.org/#search%7Cga%7C1%7Cjson-sanitizer'>Maven Central</a> or you can let your favorite java package manager handle it for you via

```
    <dependency>
      <groupId>com.mikesamuel</groupId>
      <artifactId>json-sanitizer</artifactId>
      <version>[1.0,)</version>
    </dependency>
```

Once you've got the JSON sanitizer JAR on your classpath,

```
    import com.google.json.JsonSanitizer;
```

will let you call

```
    String wellFormedJson = JsonSanitizer.sanitize(myJsonLikeString);
```

That's it.  Now `wellFormedJson` is a string of well-formed JSON that is safe to pass to JavaScript's `eval` operator and which can be easily embedded in XML or HTML.

If you have further questions, check our <a href='https://groups.google.com/forum/#!forum/json-sanitizer-support'>support list</a>.