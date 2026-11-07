# Modernization archive

Historical analysis and evidence from October 2026. Preserve these records for
traceability; their proposed coordinates, pending approvals, local paths and
intermediate tool requirements are not current instructions.

**Published outcome:** [1.3.0](https://github.com/OWASP/json-sanitizer/releases/tag/v1.3.0)
shipped November 7, 2026 under `org.owasp.json-sanitizer:json-sanitizer`.
Signing and Central publishing were completed. The old-coordinate relocation
was not published. The development branch is now 1.3.1-SNAPSHOT; further
internal cleanup is optional, not a release prerequisite.

Use the [README](../../README.md), [migration guide](../getting_started.md),
[changelog](../../CHANGELOG.md) and [release guide](../RELEASING.md) for current
behavior and procedures. The [original phase notes](UPLIFT_HISTORY.md) preserve
the detailed implementation history.

Start with:

| File | What it is |
|---|---|
| [`MODERNIZATION_BRIEF.md`](MODERNIZATION_BRIEF.md) | The plan: phases, entry and exit criteria (with evidence), every decision and who made it, the approval |
| [`ASSESSMENT.md`](ASSESSMENT.md) | The starting point: size, debt, risks and the recommended approach |
| [`SECURITY_FINDINGS.md`](SECURITY_FINDINGS.md), [`security_remediation.patch`](security_remediation.patch) | The security review of the legacy code (SEC-001 to SEC-011) and the reviewed SEC-001 patch |
| [`APPROVED_DIFFERENCES.md`](APPROVED_DIFFERENCES.md) | Every output change since the legacy baseline, by class and exact count, signed off by the maintainer |
| [`BASELINE.md`](BASELINE.md), [`baseline/`](baseline/) | The legacy build's test results and golden-corpus digests the new code was checked against |
| [`PLAYBOOK.md`](PLAYBOOK.md), [`DELTA_CATALOG.md`](DELTA_CATALOG.md) | How the uplift was done, and the Java 6/7 to 8 deltas that mattered |
| [`BUSINESS_RULES.md`](BUSINESS_RULES.md), [`RULE_REVIEWS.md`](RULE_REVIEWS.md) | The sanitizer's behavior as testable rules, and the maintainer's review of them |
| [`REPORT.html`](REPORT.html), [`TOPOLOGY.html`](TOPOLOGY.html) | Rendered assessment report and code map (download and open in a browser) |

The rest (`*.mmd`, `*.json`, `map/`, `*.py`) are the diagrams, extracted data and scripts behind those documents. Left out: the 8.9 MB file of full legacy golden-corpus outputs (regenerable from the legacy classes with `GoldenCorpus record`, as `BASELINE.md` describes) and compiled class files. Paths such as `analysis/json-sanitizer/`, `legacy/`, `modernized/` and `/Users/...` refer to local working directories, not files shipped in this repository. Historical source line numbers refer to the revision analyzed, not current master.
