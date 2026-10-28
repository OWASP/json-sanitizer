# Releasing json-sanitizer

Releases go to Maven Central through the [Central Portal](https://central.sonatype.com/). The `release` workflow (`.github/workflows/release.yml`) builds, tests, signs and **stages** a version; nothing is published until a maintainer publishes the validated deployment in the portal by hand.

## One-time setup (maintainers)

1. **Namespace.** The groupId is `com.mikesamuel`. Whoever controls that namespace on the Central Portal must verify it there, or add the OWASP maintainers. If it cannot be shared, the groupId must move, which is a breaking coordinate change for consumers.
2. **Repository secrets:** `CENTRAL_TOKEN_USERNAME` and `CENTRAL_TOKEN_PASSWORD` (a Central Portal user token), `GPG_PRIVATE_KEY` (ASCII-armored) and `GPG_PASSPHRASE`. Publish the key's public half to a key server.
3. **Environment:** in Settings, Environments, create `release` and require reviewers. The workflow waits there before it touches any credential.

## Each release

1. Open a pull request that sets `<version>` in `pom.xml` to the release (for example `1.3.0`, no `-SNAPSHOT`) and moves the `CHANGELOG.md` entry from "Unreleased" to that version. Merge it once CI is green.
2. Tag that commit and push the tag: `git tag -s v1.3.0 && git push origin v1.3.0`. The workflow checks that the tag names the pom's version.
3. Approve the `release` environment when the workflow asks.
4. When the job ends, the deployment is **validated, not published**. In the Central Portal, under Deployments, check the files (jar, sources, javadoc, POM, signatures), then publish it.
5. Open a pull request that sets the next `-SNAPSHOT` version.

The previous `RELEASE-checklist.sh` described OSSRH, which Sonatype retired in 2025; it is gone.
