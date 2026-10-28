# Rule reviews — json-sanitizer

A person reviewed the P0 business rules flagged for judgement in BUSINESS_RULES.md (a suspected defect, an SME question, or less than High confidence). An outside reviewer then checked each one against the source. 5 rules are recorded, all confirmed. RULE-001 is confirmed with one deliberate change: the rewrite fixes the legacy \v → \u0008 mapping to \v → \u000b, so equivalence tests must expect that difference.

| Rule | Verdict | When | Title | Note |
|---|---|---|---|---|
| RULE-001 | confirmed | 2026-10-27T08:48:55+00:00 | Escape-aware forward decoding for embedding look-ahead | Look-ahead logic is right. The \v mapping is a defect to FIX in the rewrite: \v must become \u000b (U+000B, vertical tab), and the decoder must return 0x2000B, not \u0008 / 0x20008 (JsonSanitizer.java:740, :1495). Legacy is left unpatched, so equivalence tests must expect this one difference. |
| RULE-024 | confirmed | 2026-10-27T08:48:55+00:00 | Over-deep arrays/objects are replaced with null instead of failing | Confirmed with the outside reviewer. The no-throw policy is new: 042752c (PR #39, 2026-10-27) reverted the short-lived JsonNestingDepthException from 0c90410 (PR #37) before any release, so earlier published releases behave differently. |
| RULE-025 | confirmed | 2026-10-27T08:46:41+00:00 | Output is exactly one top-level value; trailing extra content is discarded |  |
| RULE-026 | confirmed | 2026-10-27T08:46:41+00:00 | Sanitizer output must be idempotent (sanitizing twice changes nothing) | The rule is right. The test-diagnostic bugs (:89 excerpts sanitized0 instead of sanitized1; excerpt() double-escapes \r/\n; ellipse flags unused) don't affect pass/fail and are not part of the contract. |
| RULE-057 | confirmed | 2026-10-27T08:48:55+00:00 | Neutralize HTML script and XML CDATA breakout sequences in strings | Confirmed with the outside reviewer. '<![CDATA[' is not escaped by the code; JsonSanitizerTest.java:343 asserts its absence only for the fuzz inputs it uses. Out of scope, not a security gap. |
