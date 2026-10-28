# Releasing json-sanitizer

Releases go to Maven Central through the [Central Portal](https://central.sonatype.com/). The `release` workflow (`.github/workflows/release.yml`) runs on a `v*` tag. Its first job verifies the tagged commit with no secrets. Its second job, once a maintainer approves the `release` environment, signs the artifacts and **stages** them. Nothing is published until a maintainer publishes the validated deployment in the portal by hand.

## One-time setup (repository administrators)

The manual gate is only as strong as these settings. Without them, anyone with write access could reach the signing key and the publishing token.

1. **Namespace.** The groupId is `com.mikesamuel`, held on the Central Portal by Mike Samuel, the original author. The new maintainer creates a Central Portal account; Mike, as a known publisher of the namespace, asks [Central Support](https://central.sonatype.org/faq/what-happened-to-issues-sonatype-org) to add that username as a publisher for `com.mikesamuel`. Keeping the groupId means consumers' existing coordinates and update tools keep working; moving to another groupId (for example under `org.owasp`) is a breaking coordinate change.
2. **Signing key.** The releasing maintainer's own GPG key; it need not be the key earlier releases were signed with. Publish its public half to a key server (for example `keys.openpgp.org`) before the first release.
3. **Environment `release`** (Settings, Environments):
   - Required reviewers: the maintainers allowed to release. With two or more, turn on **Prevent self-review**, so the person who pushes a tag cannot approve it. A sole maintainer leaves it off (it would block every release); the approval still stops an accidental tag, and the settings below still keep the key away from every other workflow.
   - Deployment branches and tags: **selected tags only**, pattern `v*`.
   - **Environment secrets** (not repository secrets, which every workflow on every branch can read): `CENTRAL_TOKEN_USERNAME` and `CENTRAL_TOKEN_PASSWORD` (a Central Portal user token), `GPG_PRIVATE_KEY` (ASCII-armored) and `GPG_PASSPHRASE`, for example `gh secret set GPG_PRIVATE_KEY --env release < private.asc`.
4. **Tag ruleset** (Settings, Rules): only release maintainers may create, update or delete tags matching `v*`.

## Each release

1. Open a pull request that sets `<version>` in `pom.xml` to the release (for example `1.3.0`, no `-SNAPSHOT`) and renames the `CHANGELOG.md` heading "1.3.0 (unreleased)" to the release date. Merge it once CI is green.
2. Tag that commit on master and push the tag: `git tag -s v1.3.0 && git push origin v1.3.0`. The `verify` job checks that the commit is on master, that the tag names the pom's version, that the POM declares no compile or runtime dependency, and runs every test (golden corpus included) on JDK 21 and Java 8.
3. When `verify` is green, a second maintainer approves the `release` environment.
4. When the `stage` job ends, the deployment is **validated, not published**. **Publish only if the whole workflow run is green;** otherwise press Drop in the portal. In the Central Portal, under Deployments, check the files (jar, sources, javadoc, POM, signatures), then publish.
5. Open a pull request that sets the next `-SNAPSHOT` version and starts a new "unreleased" changelog heading.

The previous `RELEASE-checklist.sh` described OSSRH, which Sonatype retired in 2025; it is gone.
