# Modernization record

A snapshot (2026-10-28) of the analysis and planning behind the 1.3.0 modernization: moving json-sanitizer to Java 8 on a pinned, tested toolchain, fixing its security and correctness bugs, and publishing it as `org.owasp:json-sanitizer`. It records why each change was made and how each was proven; the code and tests in this repository are the source of truth.

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

The rest (`*.mmd`, `*.json`, `map/`, `*.py`) are the diagrams, extracted data and scripts behind those documents. Left out: the 8.9 MB file of full legacy golden-corpus outputs (regenerable from the legacy classes with `GoldenCorpus record`, as `BASELINE.md` describes) and compiled class files. Local paths such as `/Users/...` refer to the machine the work was done on.
