SRCS=src/main/com/google/json/*.java
CP=lib/jsr305/jsr305.jar
TESTS=src/tests/com/google/json/*.java
TEST_CP=out/classes:$(CP):lib/junit/junit.jar

all: test-classes jar runtests

clean:
	rm -rf out

classes: out/classes.tstamp
out/classes.tstamp: $(SRCS)
	mkdir -p out/classes
	javac -d out/classes -classpath "$(CP)" $(SRCS) \
	  && touch out/classes.tstamp

test-classes: out/test-classes.tstamp out/test-classes/com/google/json/alltests
out/test-classes.tstamp: classes $(TESTS)
	mkdir -p out/test-classes
	javac -d out/test-classes -classpath "$(TEST_CP)" $(TESTS) \
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
	java -classpath "out/test-classes:$(TEST_CP)" junit.textui.TestRunner \
	  com.google.json.AllTests
