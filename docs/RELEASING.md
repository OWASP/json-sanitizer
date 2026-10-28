# Releasing json-sanitizer

Releases go to Maven Central through the [Central Portal](https://central.sonatype.com/). The `release` workflow (`.github/workflows/release.yml`) runs on a `v*` tag. Its first job verifies the tagged commit with no secrets. Its second job, once a maintainer approves the `release` environment, signs the artifacts and **stages** them. Nothing is published until a maintainer publishes the validated deployment in the portal by hand.

## One-time setup (repository administrators)

The manual gate is only as strong as these settings. Without them, anyone with write access could reach the signing key and the publishing token.

1. **Namespace.** The groupId is `com.mikesamuel`. Whoever controls that namespace on the Central Portal must verify it there, or add the OWASP maintainers. If it cannot be shared, the groupId must move, which is a breaking coordinate change for consumers.
2. **Environment `release`** (Settings, Environments):
   - Required reviewers: the maintainers allowed to release, with **Prevent self-review** on, so the person who pushes a tag cannot approve it.
   - Deployment branches and tags: **selected tags only**, pattern `v*`.
   - **Environment secrets** (not repository secrets, which every workflow on every branch can read): `CENTRAL_TOKEN_USERNAME` and `CENTRAL_TOKEN_PASSWORD` (a Central Portal user token), `GPG_PRIVATE_KEY` (ASCII-armored) and `GPG_PASSPHRASE`. Publish the key's public half to a key server.
3. **Tag ruleset** (Settings, Rules): only release maintainers may create, update or delete tags matching `v*`.

## Each release

1. Open a pull request that sets `<version>` in `pom.xml` to the release (for example `1.3.0`, no `-SNAPSHOT`) and renames the `CHANGELOG.md` heading "1.3.0 (unreleased)" to the release date. Merge it once CI is green.
2. Tag that commit on master and push the tag: `git tag -s v1.3.0 && git push origin v1.3.0`. The `verify` job checks that the commit is on master, that the tag names the pom's version, that the POM declares no compile or runtime dependency, and runs every test (golden corpus included) on JDK 21 and Java 8.
3. When `verify` is green, a second maintainer approves the `release` environment.
4. When the `stage` job ends, the deployment is **validated, not published**. **Publish only if the whole workflow run is green;** otherwise press Drop in the portal. In the Central Portal, under Deployments, check the files (jar, sources, javadoc, POM, signatures), then publish.
5. Open a pull request that sets the next `-SNAPSHOT` version and starts a new "unreleased" changelog heading.

The previous `RELEASE-checklist.sh` described OSSRH, which Sonatype retired in 2025; it is gone.
