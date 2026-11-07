# Releasing JSON Sanitizer

Publish `org.owasp.json-sanitizer:json-sanitizer` through the
[Central Portal](https://central.sonatype.com/publishing/deployments).
The [release workflow](../.github/workflows/release.yml) verifies, signs and
stages a tagged release. A maintainer then publishes it in the portal.

## Credentials and repository controls

Before a release, check:

- The Central account has publishing access to `org.owasp.json-sanitizer` and
  its user token is valid. An expired token needs renewal before the next release;
  it does not affect artifacts already published.
- The signing key is valid and its public key is available from a keyserver.
  Keep a secure backup of the private key and its passphrase.
- GitHub environment `release` holds `CENTRAL_TOKEN_USERNAME`,
  `CENTRAL_TOKEN_PASSWORD`, `GPG_PRIVATE_KEY` (ASCII-armored) and `GPG_PASSPHRASE`.
  Keep these as environment secrets, not repository secrets.
- The environment requires release-maintainer approval and allows only tags
  matching `v*`. With multiple maintainers, enable **Prevent self-review**;
  a sole maintainer must be able to approve their own run.
- A tag ruleset restricts creation, updates and deletion of `v*` tags to release
  maintainers.

## Release steps

1. Open a PR setting `pom.xml` to the release version (no `-SNAPSHOT`) and dating
   its changelog entry. Update installation examples and supported-version
   guidance. Merge after review and green CI.
2. Sign and push a tag on that merged commit, matching the POM version:

   ```sh
   git tag -s vX.Y.Z -m 'Release X.Y.Z'
   git push origin vX.Y.Z
   ```

3. Wait for the workflow's `verify` job. It checks the tag and dependencies,
   then runs the full tests on JDK 21 and Java 8. Approve the `release`
   environment only after it passes.
4. Wait for the **whole workflow** to succeed. In Central, confirm the validated
   deployment has the right coordinates, jar, sources, Javadoc, POM and
   signatures. Publish it. If staging or verification failed, investigate and
   drop the unpublished deployment instead.
5. Wait for `PUBLISHED`. Download the public artifacts and verify their
   signatures and checksums. Publish GitHub release notes with migration steps.
   Central release versions cannot be replaced; corrections need a new version.
6. Merge a follow-up PR with the next `-SNAPSHOT` version and an unreleased
   changelog heading.

## Optional relocation from the old coordinates

**Not published for 1.3.0.** Users must change their dependency groupId manually.
This does not block releases under the OWASP project namespace.

[relocation/pom.xml](../relocation/pom.xml) can publish the POM-only artifact
`com.mikesamuel:json-sanitizer:1.3.0`, pointing to the new coordinates. It requires
publishing rights to `com.mikesamuel`; the existing publisher must arrange access
with Central Support or publish it themselves.

Once authorized, use a clone of the 1.3.0 tag, a signing key and Central token
configured as Maven server `central`:

```sh
mvn -B -f relocation/pom.xml clean deploy
```

This stages the relocation only. Verify its POM and signature in Central, then
publish it. Announce automatic relocation only after it is publicly available.
