SRCS=src/main/com/google/json/*.java
TESTS=src/tests/com/google/json/*.java
JUNIT=lib/junit/junit.jar

all: test-classes jar runtests

clean:
	rm -rf out

classes: out/classes.tstamp
out/classes.tstamp: $(SRCS)
	mkdir -p out/classes
	javac -d out/classes $(SRCS) && touch out/classes.tstamp

test-classes: out/test-classes.tstamp out/test-classes/com/google/json/alltests
out/test-classes.tstamp: classes $(TESTS)
	mkdir -p out/test-classes
	javac -d out/test-classes -classpath "out/classes:$(JUNIT)" $(TESTS) \
	  && touch out/test-classes.tstamp
out/test-classes/com/google/json/alltests: $(TESTS)
	echo $^ | tr ' ' '\n' | \
	  perl -ne 's#^src/tests/|\.java$$##g; s#/#.#g; print if m/Test$$/' \
	  > $@ \
	  || rm $@

jar: classes
	jar -cf "out/json-sanitizer-$$(date '+%Y-%m-%d').jar" \
	  -C out/classes com

runtests: test-classes
	java -classpath out/classes:out/test-classes:${JUNIT} \
	  junit.textui.TestRunner com.google.json.AllTests
