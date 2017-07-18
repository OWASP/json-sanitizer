#!/bin/bash

echo This is not meant to be run automatically.

exit

set -e


# Make sure the build is ok via
mvn clean verify site javadoc:jar source:jar

echo
echo Browse to
echo "file://$PWD/target/site"
echo and check the findbugs and jacoco reports.

echo
echo Check https://central.sonatype.org/pages/apache-maven.html#nexus-staging-maven-plugin-for-deployment-and-release
echo and make sure you have the relevant credentials in your ~/.m2/settings.xml

echo
echo Check https://search.maven.org/#search%7Cga%7C1%7Cjson-sanitizer
echo and make sure that the current POM release number is max.

# Pick a release version
export VERSION="$(mvn -q \
     -Dexec.executable="echo" \
     -Dexec.args='${project.version}' \
     --non-recursive \
     org.codehaus.mojo:exec-maven-plugin:1.3.1:exec \
  2> /dev/null)";

export NEW_VERSION="$(echo "$VERSION" \
     | perl -pe 's/^(\d+\.\d+)(?:\.(\d+))?.*/$1.".".($2||0)/e')";

export NEW_DEV_VERSION="$(echo "$VERSION" \
     | perl -pe 's/^(\d+\.\d+)(?:\.(\d+))?.*/$1.".".($2+1)."-SNAPSHOT"/e')";

echo "
VERSION        =$VERSION
NEW_VERSION    =$NEW_VERSION
NEW_DEV_VERSION=$NEW_DEV_VERSION"


cd ~/work

export RELEASE_CLONE="$PWD/json-san-release"

rm -rf "$RELEASE_CLONE"

cd "$(dirname "$RELEASE_CLONE")"

git clone git@github.com:OWASP/json-sanitizer.git \
    "$(basename "$RELEASE_CLONE")"

cd "$RELEASE_CLONE"


# Update the version
# mvn release:update-versions puts -SNAPSHOT on the end no matter what
# so this is a two step process.
export VERSION_PLACEHOLDER=99999999999999-SNAPSHOT

mvn release:update-versions \
    -DautoVersionSubmodules=true \
    -DdevelopmentVersion="$VERSION_PLACEHOLDER"

find . -name pom.xml \
    | xargs perl -i.placeholder -pe "s/$VERSION_PLACEHOLDER/$NEW_VERSION/g"


# Make sure there're no snapshots left in any poms.
find . -name pom.xml | xargs grep -- -SNAPSHOT


# A dry run.
mvn clean source:jar javadoc:jar verify -DperformRelease=true


# Commit and tag
git commit -am "Release candidate $NEW_VERSION"
git tag -m "Release $NEW_VERSION" -s "v$NEW_VERSION"
git push origin "v$NEW_VERSION"


# Actually deploy.
mvn clean source:jar javadoc:jar verify deploy:deploy -DperformRelease=true


# Bump the development version.
for f in $(find . -name pom.xml.placeholder); do
    mv "$f" "$(dirname "$f")"/"$(basename "$f" .placeholder)"
done

find . -name pom.xml \
    | xargs perl -i -pe "s/$VERSION_PLACEHOLDER/$NEW_DEV_VERSION/"

git commit -am "Bumped dev version"

git push origin master

# Now Release
echo '1. Go to oss.sonatype.org'
echo '2. Look under staging repositories for one named'
echo '   commikesamuel-...'
echo '3. Close it.'
echo '4. Refresh until it is marked "Closed".'
echo '5. Check that its OK.'
echo '6. Release it.'
