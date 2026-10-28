# Business Rules — json-sanitizer

At extraction: 64 confirmed rules (6 P0); later steps may add or correct rules below. Each citation was checked by a second agent that read the cited lines; 2 candidate rules were refuted and left out.

| ID | Name | Category | Priority | Source | Confidence |
|---|---|---|---|---|---|
| RULE-001 | Escape-aware forward decoding for embedding look-ahead | Calculation | P0 | `src/main/java/com/google/json/JsonSanitizer.java:1439-1502` | High |
| RULE-002 | Eval-minifier pooling profitability threshold | Calculation | P1 | `src/main/java/com/google/json/EvalMinifier.java:110` | Medium |
| RULE-003 | Per-constant savings test | Calculation | P1 | `src/main/java/com/google/json/EvalMinifier.java:121-146` | High |
| RULE-004 | Assemble the pooled-constant function expression | Calculation | P1 | `src/main/java/com/google/json/EvalMinifier.java:151-221` | High |
| RULE-005 | Identify candidate constant tokens | Calculation | P1 | `src/main/java/com/google/json/EvalMinifier.java:70-95` | High |
| RULE-006 | Number literal normalization to JSON grammar | Calculation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:1010-1123` | Medium |
| RULE-007 | Hexadecimal and legacy-octal integer literals are converted to decimal | Calculation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:1031-1088` | Medium |
| RULE-008 | Significant digits and decimal exponent are extracted without rounding | Calculation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:1217-1272` | High |
| RULE-009 | Canonical numeric key layout: plain, decimal, small-fraction, or exponent notat… | Calculation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:1278-1331` | High |
| RULE-010 | Legacy octal escape length and value | Calculation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:1451-1468` | High |
| RULE-011 | Backward decoding to detect disguised '-->' before a '>' | Calculation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:1504-1532` | High |
| RULE-012 | Numeric object keys are canonicalized to JavaScript number-to-string form | Calculation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:525-541` | Medium |
| RULE-013 | Unquoted object keys are quoted; numeric keys canonicalised to JS string form | Calculation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:525-545` | High |
| RULE-014 | String literal delimiter normalization and termination | Calculation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:634-653` | High |
| RULE-015 | Normalize JavaScript escape sequences to valid JSON escapes | Calculation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:728-787` | High |
| RULE-016 | Convert JavaScript octal escapes to JSON unicode escapes | Calculation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:759-780` | High |
| RULE-017 | Edit splice: copy the untouched input up to the edit, swap in the replacement,… | Calculation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:852-876` | High |
| RULE-018 | Finding the end of an over-deep value (bracket matching that ignores strings an… | Calculation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:922-942` | High |
| RULE-019 | Trailing comma removal before a close bracket or end of input | Calculation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:966-993` | High |
| RULE-020 | Random JSON document shape (depth, breadth, value kinds, whitespace) | Calculation | P1 | `src/test/java/com/google/json/FuzzyTest.java:179-260` | Medium |
| RULE-021 | Random numeric literal formats (float and integer) | Calculation | P1 | `src/test/java/com/google/json/FuzzyTest.java:189-223` | High |
| RULE-022 | Random string-character generation and escaping | Calculation | P1 | `src/test/java/com/google/json/FuzzyTest.java:262-306` | Medium |
| RULE-023 | Mutation algorithm: 1-16 sorted insert/replace/remove edits | Calculation | P1 | `src/test/java/com/google/json/FuzzyTest.java:325-377` | High |
| RULE-024 | Over-deep arrays/objects are replaced with null instead of failing | Validation | P0 | `src/main/java/com/google/json/JsonSanitizer.java:377-395` | Medium |
| RULE-025 | Output is exactly one top-level value; trailing extra content is discarded | Validation | P0 | `src/main/java/com/google/json/JsonSanitizer.java:397-401` | Medium |
| RULE-026 | Sanitizer output must be idempotent (sanitizing twice changes nothing) | Validation | P0 | `src/test/java/com/google/json/FuzzyTest.java:37-100` | High |
| RULE-027 | Generated short variable names skip JavaScript reserved words | Validation | P1 | `src/main/java/com/google/json/EvalMinifier.java:290-345` | Medium |
| RULE-028 | Minifier must not substitute property names or partial numeric tokens | Validation | P1 | `src/main/java/com/google/json/EvalMinifier.java:76-118` | Medium |
| RULE-029 | Pooling eligibility: exclude object keys and short tokens | Validation | P1 | `src/main/java/com/google/json/EvalMinifier.java:97-118` | Medium |
| RULE-030 | Leading plus sign is stripped from numbers; minus sign is kept | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:1012-1025` | High |
| RULE-031 | Empty integer, fraction, or exponent digits are filled with zero | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:1027-1119` | High |
| RULE-032 | Characters after a valid number are dropped | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:1120-1122` | High |
| RULE-033 | Backslash-run parity decides whether the trigger character is escaped | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:1534-1540` | High |
| RULE-034 | Byte streams are read and written as UTF-8; malformed bytes become U+FFFD; outp… | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:243-249` | Medium |
| RULE-035 | Missing (null) input is treated as JSON null | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:322-328` | High |
| RULE-036 | Whole-input normalization: empty input, trailing content, comments and grouping… | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:327` | Medium |
| RULE-037 | Close-bracket repair: fill missing values, drop trailing commas, fix mismatched… | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:397-426` | High |
| RULE-038 | Structural repair of malformed arrays and objects | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:403-465` | Medium |
| RULE-039 | Separator repair: array elisions become null, stray commas and colons are dropp… | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:427-466` | High |
| RULE-040 | Bare words and single-quoted text become double-quoted JSON strings | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:477-555` | Medium |
| RULE-041 | Bare token classification: number, keyword, or unquoted string | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:486-523` | High |
| RULE-042 | Character escaping and elision for XML and JavaScript safety | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:620-631` | Medium |
| RULE-043 | Escape JavaScript line terminators and tab inside strings | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:625-631` | High |
| RULE-044 | Escaped double-dash before '>' is still treated as an HTML comment close | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:693-713` | Medium |
| RULE-045 | Escape control characters, lone surrogates and non-characters that XML disallows | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:788-813` | High |
| RULE-046 | JS line comments are stripped up to and including the line terminator | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:883-898` | High |
| RULE-047 | JS block comments are stripped up to the first closing '*/'; an unterminated bl… | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:899-910` | High |
| RULE-048 | Quoted string boundaries: single or double quotes, backslash-parity termination… | Validation | P1 | `src/main/java/com/google/json/JsonSanitizer.java:950-963` | High |
| RULE-049 | Reserved-keyword table integrity (length-indexed, strictly sorted) | Validation | P1 | `src/test/java/com/google/json/EvalMinifierTest.java:104-117` | High |
| RULE-050 | A slash that does not start a comment is discarded as junk | Validation | P2 | `src/main/java/com/google/json/JsonSanitizer.java:883-886` | High |
| RULE-051 | End-of-input completion and whitespace-only input | Lifecycle | P1 | `src/main/java/com/google/json/JsonSanitizer.java:564-601` | High |
| RULE-052 | End-of-input completion: finish dangling key/value and close all open brackets | Lifecycle | P1 | `src/main/java/com/google/json/JsonSanitizer.java:576-601` | High |
| RULE-053 | Missing punctuation is inserted between adjacent tokens | Lifecycle | P1 | `src/main/java/com/google/json/JsonSanitizer.java:819-850` | High |
| RULE-054 | Copy-on-write output: input that needs no fixes is returned unchanged | Lifecycle | P1 | `src/main/java/com/google/json/JsonSanitizer.java:860-866` | Medium |
| RULE-055 | Fuzz corpus lifecycle: fresh basis versus mutated variant | Lifecycle | P1 | `src/test/java/com/google/json/FuzzyTest.java:146-177` | High |
| RULE-056 | Sanitize before minifying, with optional custom nesting limit | Policy | P0 | `src/main/java/com/google/json/EvalMinifier.java:50-66` | High |
| RULE-057 | Neutralize HTML script and XML CDATA breakout sequences in strings | Policy | P0 | `src/main/java/com/google/json/JsonSanitizer.java:662-726` | Medium |
| RULE-058 | Reader and byte-stream entry points match the string sanitizer | Policy | P1 | `src/main/java/com/google/json/JsonSanitizer.java:190-251` | Medium |
| RULE-059 | Stream input is fully buffered with no size limit; caller-owned streams are not… | Policy | P1 | `src/main/java/com/google/json/JsonSanitizer.java:203-214` | High |
| RULE-060 | Maximum nesting depth is configurable but clamped to 1..4096 (default 64) | Policy | P1 | `src/main/java/com/google/json/JsonSanitizer.java:318-332` | Medium |
| RULE-061 | Non-JSON cruft removal: grouping parentheses, JS comments and unrecognised char… | Policy | P1 | `src/main/java/com/google/json/JsonSanitizer.java:358-375` | Medium |
| RULE-062 | Empty or null input becomes the JSON value null | Policy | P1 | `src/main/java/com/google/json/JsonSanitizer.java:564-568` | High |
| RULE-063 | Fuzz run budget and reproducible seed | Policy | P1 | `src/test/java/com/google/json/FuzzyTest.java:39-59` | Medium |
| RULE-064 | Escaped characters use lowercase four-digit hex | Policy | P2 | `src/main/java/com/google/json/JsonSanitizer.java:1423-1426` | High |

## Calculation

### RULE-001: Escape-aware forward decoding for embedding look-ahead
**Category:** Calculation
**Priority:** P0
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:1439-1502`
**Plain English:** When the sanitizer checks whether a '<' or ']' inside a string starts a dangerous sequence (<!--, <script, </script, ]]>), it reads the characters that follow as JavaScript would decode them, so a disguised sequence like <\script still counts.
**Specification:**
  Given A string literal containing the raw text <\script (a '<' followed by the identity escape \s, then 'cript')
  When  The '<' check (line 662-691) decodes the next three logical characters using this routine
  Then  The escape \s decodes to 's' with a length of 2 raw characters, the next characters decode to 'c' and 'r', the pattern 'scr' matches (case-insensitively), and the '<' is rewritten to \u003c. The \s case works through the default fall-through at line 1498 (return 0x20000 | nc), which treats any unrecognized escape as an identity escape. This matters because the escape normalizer later drops the backslash (line 781-785), which would otherwise leave a literal <script in the output.
**Parameters:** Return value packs the decoded character in the low 16 bits and the raw sequence length in the high bits. Lengths: literal char = 1; simple escape (\b \f \n \r \t \v or any identity escape) = 2; \xHH = 4; \uHHHH = 6; octal = 2-4. Hex digits are case-insensitive (0-9, a-f, A-F; helpers at lines 1357-1366).
**Edge cases handled:** Position at or past the end of the input: returns 0 (no character, length 0); A lone trailing backslash returns character NUL with length 1. This matches the normalizer, which deletes a trailing backslash (line 729-731).; \x without two hex digits, or \u without four hex digits, decodes to the letter 'x' or 'u' (length 2). This matches the normalizer, which drops the backslash on malformed hex escapes (lines 749, 757).; Identity escapes such as \-, \/, \s, \8 and \9 decode to the escaped character itself (length 2); s decodes to 's' even though the output keeps it escaped. This can over-escape a harmless '<', which is safe.; The routine checks bounds against the whole input length, not the end of the current string token, so look-ahead can decode characters past the closing quote. The normalizer checks against the token…
**Suspected defect:** \v decodes to U+0008 (backspace), but in JavaScript \v is U+000B (vertical tab). The normalizer makes the same choice at line 739-740 (\v becomes \u0008), so the sanitized output changes the meaning of the data. Embedding safety is not affected because neither character is part of a dangerous pattern.
**Confidence:** High

### RULE-002: Eval-minifier pooling profitability threshold
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/EvalMinifier.java:110`
**Also cited:** 131-149,223-232; src/main/java/com/google/json/EvalMinifier.java:147-149
**Plain English:** Repeated literals are pooled into a function-parameter wrapper only when the characters saved are more than the wrapper's fixed cost (22) plus a margin of 32. Each literal must save more than the 2-character marginal cost of an extra parameter.
**Specification:**
  Given An array of 13 nulls versus an array of 26 nulls
  When  EvalMinifier.minify runs
  Then  13 nulls save (13-1)×4-2 = 46, which is not above 54, so the output stays `[null,...]` unchanged. 26 nulls save 25×4-2 = 98 > 54, so the output is `(function(a){return[a,a,...]}(null))`
**Parameters:** BOILERPLATE_COST = len('(function(' + '){return' + '}(' + '))') = 22; SAVINGS_THRESHOLD = 32; MARGINAL_VAR_COST = len(',,') = 2; minimum pooled token length = 4 chars (EvalMinifier.java:110); per-token savings = (occurrences-1)×tokenLength - 2 (EvalMinifier.java:135-136); pool only if total savings > 54 (EvalMinifier.java:147)
**Edge cases handled:** The single-quoted 'foo'/'bar' are normalized to "foo"/"bar" by the sanitizer before pooling; "z" is 3 chars including quotes, below the 4-char minimum, so it is never pooled and stays inline; Literals that occur only once are never given a name; Whitespace outside strings is removed in the pooled output (input ', null' becomes ',a'); Names are given to pooled literals in order of first appearance: 'foo' becomes a, 'bar' becomes b
**Confidence:** Medium — Citation was corrected by referee (The rule is accurate, but the cited lines don't implement it. EvalMinifierTest.java:36-56 is a JUnit golden-output test. It shows the behavior from outside: 13 nulls stay unchanged at lines 43-45, and 26 nulls become the '(function(a){return[a,...]}(null))' wrapper at lines 46-50. It contains no calculation logic. The executable rule is in EvalMinifier.java, and…

### RULE-003: Per-constant savings test
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/EvalMinifier.java:121-146`
**Plain English:** A repeated constant is worth pooling only if (occurrences - 1) x token length - 2 is greater than zero. Constants that appear once, or whose savings are zero or less, are dropped from the pool.
**Specification:**
  Given An eligible token "category" (10 chars including quotes) appearing 3 times, and an eligible token true (4 chars) appearing once
  When  savings are computed
  Then  "category" saves (3-1)x10-2 = 18 > 0, so all 3 occurrences are marked for replacement and 18 is added to potentialSavings. true appears only once and is removed.
**Parameters:** MARGINAL_VAR_COST = 2 (length of ",,", line 231)
**Edge cases handled:** A 4-char token appearing twice saves 4-2 = 2 > 0, so it qualifies.; A 2-char token can never qualify because of the 4-char minimum, so the formula is never applied to it.
**Suspected defect:** The estimate ignores the cost of the generated variable name: 1 char per replaced occurrence plus 1 in the parameter list, and 2 or more chars once 54 names are used. For example, a 4-char token used twice is scored +2 but actually adds bytes: it removes 8 chars and adds 2 names, 1 parameter, 4 chars of argument and 2 commas, 9 in total. The global threshold of 54 partly hides this.
**Confidence:** High

### RULE-004: Assemble the pooled-constant function expression
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/EvalMinifier.java:151-221`
**Plain English:** When pooling is worthwhile, the output becomes (function(p1,p2,...){return BODY}(v1,v2,...)). Each pooled constant gets one parameter name, in order of first occurrence. The body is the sanitized JSON with every qualifying occurrence replaced by its name and whitespace outside strings removed. The original constant values are passed as arguments in the same order.
**Specification:**
  Given Sanitized JSON [ "widget-blue", "widget-blue", ... ] where the pooled constant "widget-blue" (13 chars) appears 6 times, saving 5x13-2 = 63 > 54
  When  the output is generated
  Then  The output is (function(a){return [a,a,a,a,a,a]}("widget-blue")).
**Parameters:** Envelope fragments: "(function(", "){return", "}(", "))". Whitespace removed outside strings: tab, LF, CR, space.
**Edge cases handled:** A single space is inserted after 'return' only if the body does not start with '{', '[' or '"'. For example, if the body starts with a pooled name, the output is 'return a'.; Replacement positions are processed in source order (tokens sorted by start offset).; Whitespace inside string literals is preserved. Escapes inside strings skip the next character.; An assertion requires that no token boundary falls inside a string. It is checked only when Java assertions are enabled.
**Confidence:** High

### RULE-005: Identify candidate constant tokens
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/EvalMinifier.java:70-95`
**Plain English:** The minifier scans the sanitized JSON for two kinds of repeatable constants: complete quoted strings (backslash escapes respected) and unbroken runs of word characters (ASCII letters, digits, underscore, dollar, hyphen, period), which covers numbers, true, false and null.
**Specification:**
  Given Sanitized JSON ["alpha", -12.5, true, 1e+10]
  When  the token scan runs
  Then  The candidate tokens are "alpha" (7 chars including quotes), -12.5, true, 1e and 10. The '+' is not a word character, so it splits the exponent number. Brackets, commas, colons and whitespace are skipped.
**Parameters:** Word characters (isLetterOrNumberChar, lines 234-239): 0-9, a-z, A-Z (case-folded with ch|32), '_', '$', '-', '.'
**Edge cases handled:** A backslash inside a string skips the next character, so \" does not end the string.; A trailing backslash at the end of input would step past the end. The token end is clamped to the input length (line 87).; Non-ASCII letters are not word characters and break a bare token.; Numbers with a '+' exponent (1e+10) split into two smaller tokens, which are less likely to qualify for pooling.
**Confidence:** High

### RULE-006: Number literal normalization to JSON grammar
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:1010-1123`
**Plain English:** Number formats that JSON does not allow are rewritten into valid JSON numbers with the same value. A missing leading or trailing digit gets a 0, a unary plus is removed, an incomplete exponent gets 0, and octal or hex literals are converted to decimal.
**Specification:**
  Given Input .5, +.5e2, 1. and 6.0221412927e
  When  Each input is sanitized
  Then  The outputs are 0.5, 0.5e2, 1.0 and 6.0221412927e0
**Parameters:** Missing digit filler = 0; octal (leading 0) and hex (0x) are converted to decimal unless an octal-looking literal contains 8 or 9
**Edge cases handled:** Valid JSON numbers, including exponent forms like 6.0221412927e+23 and 1.660538920287695E-24, are kept unchanged (lines 91-103); An incomplete exponent sign is completed: 6.0221412927e- gives e-0; e+ gives e+0 (lines 100-101); -.5 gives -0.5; +1.5e+2 gives 1.5e+2 (lines 106, 109); A leading-zero literal that contains 8 or 9 is read as decimal: -016923547559 gives -16923547559 (lines 156-158); -0888888888888888888888 gives -888888888888888888888 (lines 276-283); A long octal literal beyond 64 bits is converted exactly: -053333333304233333333333 gives -400557869725698078427 (lines 286-291); A long hex literal is converted exactly: -0X29295909049550970 gives -47455995597866469744 (lines 360-363); A broken number fragment '0x.' gives 0.0 (lines 367-371); Stray '<!--' or '-->' outside strings are read as number fragments and give -0 (lines 268, 271)
**Confidence:** Medium — Citation was corrected by referee (The behavior is real, but the cited lines are the wrong place to point. src/test/java/com/google/json/JsonSanitizerTest.java:91-110 holds only test assertions. They check the spec examples: line 99 ("6.0221412927e" -> "6.0221412927e0"), line 104 ("1." -> "1.0"), line 105 (".5" -> "0.5") and line 108 ("+.5e2" -> "0.5e2"). Tests show the behavior but don't impleme…

### RULE-007: Hexadecimal and legacy-octal integer literals are converted to decimal
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:1031-1088`
**Also cited:** src/main/java/com/google/json/JsonSanitizer.java:1030-1088 (re-encoding logic), with the parameter table at src/main/java/com/google/json/JsonSanitizer.java:1428-1432
**Plain English:** JavaScript-style hex numbers (0x...) and leading-zero octal numbers (0...) are not allowed in JSON, so their digits are rewritten as the equivalent decimal integer. If an 'octal' number contains an 8 or 9 it is read as decimal, matching legacy JavaScript.
**Specification:**
  Given A number token whose integer part starts with '0' and is either '0' followed by 'x' or 'X' (hex) or has more than one digit (octal)
  When  The token is normalized
  Then  '0x1F' becomes '31', '0X1f' becomes '31', '012' becomes '10', '018' and '09' become '18' and '9' (decimal fallback), '-0x10' becomes '-16', and '0x1e5' becomes '485' because 'e' is read as a hex digit, not an exponent
**Parameters:** Base selection: base = probableBase if probableBase > maxDigit, else (maxDigit > 10 ? 16 : 10). probableBase is 16 for the 0x prefix and 8 for a leading 0. Hex digits are 0-9 and a-f, case-insensitive. The value is rendered with Long when the digit count is at or below DIGITS_BY_BASE_THAT_FIT_IN_63B[base] (base 8: 21, base 10: 18, base 16: 15; table at lines 1428-1432), and with BigInteger otherwise, so no precision is lost.
**Edge cases handled:** '0x' with no hex digits becomes '0'; A single '0' or '0.5' is not re-encoded (single-digit integer part with no x); '000' becomes '0'; '00.5' becomes '0.5'; Very long hex such as '0xFFFFFFFFFFFFFFFFFF' (18 digits) goes through BigInteger and becomes '4722366482869645213695', with no overflow or clamping; Hex digits stop at the first non-hex character; for example '0x1.5' becomes '1.5' because the fraction is kept after the converted integer; An octal integer followed by a fraction or exponent ('012.5', '012e2') is converted and keeps the suffix ('10.5', '10e2'), although JS would treat these as syntax errors; Converted values are always non-negative; any '-' sign is kept in front of them
**Suspected defect:** The comment at lines 1069-1070 ('Avoid multiple signs. Putting out the underflowed value is the least bad option.') does not match the code: BigInteger is used, so nothing underflows. Separately, an octal-looking integer with a fraction or exponent ('012.5') produces a value that JS itself would reject. An SME should confirm this is acceptable. | If a table entry were too large, Long.parseLong would throw NumberFormatException on hostile input. The current values are correct (for example 16^15 < 2^63 < 16^16), but this table must be copied exactly in a rewrite.
**Confidence:** Medium — Citation was corrected by referee (The behavior is real and the rule describes it accurately, but the cited lines only hold part of it. Lines 1428-1432 contain just the constant table DIGITS_BY_BASE_THAT_FIT_IN_63B. Its values match the rule's Parameters exactly: index 2=63, 3=39, 4=31, 5=27, 6=24, 7=22, 8=21, 9=19, 10=18, 11=18, 12=17, 13=17, 14=16, 15=16, 16=15. Indices 0 and 1 are -1. The re-e…

### RULE-008: Significant digits and decimal exponent are extracted without rounding
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:1217-1272`
**Plain English:** For a numeric key, the sanitizer strips leading and trailing zeros to get the significant digits (s), counts them (k), and works out where the decimal point falls (n). Unlike JavaScript, it keeps every significant digit and never rounds to double precision.
**Specification:**
  Given A normalized numeric key such as '00120.0500'
  When  The key is canonicalized
  Then  s = '12005', k = 5, n = 3, so the key is rendered as "120.05". Leading integer zeros are discarded, zeros after the decimal point and before the first non-zero digit lower n, zeros between non-zero digits are kept, and trailing zeros are dropped
**Parameters:** n starts at the parsed exponent, plus 1 for each integer digit from the first non-zero digit onward, minus the number of fraction zeros before the first non-zero digit. Follows ECMAScript 5 section 9.8.1 step 5. No digit limit (the code has a TODO about limiting to 21 digits).
**Edge cases handled:** Key 9007199254740993 stays "9007199254740993"; JavaScript would produce "9007199254740992" because of double rounding; Key 0.1000000000000000000000001 stays as written; JavaScript would produce "0.1"; The minus sign is kept in front of the digits
**Suspected defect:** The comment at line 1156 says the fraction is truncated to 24 digits, but no truncation or rounding happens in the code. High-precision numeric keys therefore do not match the property name JavaScript would actually produce (JS rounds to about 17 significant digits).
**Confidence:** High

### RULE-009: Canonical numeric key layout: plain, decimal, small-fraction, or exponent notation
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:1278-1331`
**Plain English:** The canonical key text is chosen by where the decimal point falls (ECMAScript Number-to-String). Zero is always '0'. Integers of up to 21 digits are written out in full. Fractions down to 6 leading zeros are written as 0.000ddd. Anything larger or smaller uses d.ddde+/-x notation with a lowercase 'e' and an explicit sign.
**Specification:**
  Given Significant digits s (k digits) and decimal-point position n from the extraction step
  When  The canonical string is built
  Then  '-0.0' becomes "0" and '0e5' becomes "0" (sign removed). 1e20 becomes "100000000000000000000" (k<=n<=21, padded with zeros). 1.5e2 becomes "150". 1.25 becomes "1.25" (0<n<=21, point inserted). 0.000001 becomes "0.000001" (-6<n<=0). 1e21 becomes "1e+21". 123e20 becomes "1.23e+22". 1e-7 becomes "1e-7"
**Parameters:** Upper threshold n <= 21 for positional notation. Lower threshold n > -6 for 0.xxx notation. Exponent printed is n-1, with '+' or '-' and no leading zeros. Lowercase 'e'. Zero padding comes from the literal "0.000000".substring(0, 2-n).
**Edge cases handled:** Any value whose digits are all zero becomes "0", including negative zero; k == 1 in exponent form has no decimal point ('1e+21'); k > 1 puts a point after the first digit
**Suspected defect:** Integer overflow on extreme exponents: n is a 32-bit int incremented per integer digit, and abs(n-1) is not guarded. Key '1e-2147483648' yields "1e--2147483648", and key '12e2147483647' overflows n and yields "1.2e--2147483648". The output is still a valid quoted JSON string but is not a correct number representation.
**Confidence:** High

### RULE-010: Legacy octal escape length and value
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:1451-1468`
**Plain English:** A backslash followed by octal digits is read as a JavaScript octal escape of at most three digits, and a third digit is allowed only when the first digit is 0-3, so the value never goes above 255.
**Specification:**
  Given Raw string text \055 (backslash, 0, 5, 5)
  When  The escape is decoded during embedding look-ahead or look-behind
  Then  It decodes to code point 45 ('-') with a raw length of 4. For \477 only \47 is consumed (value 39, an apostrophe) with length 3, and the final '7' is read as a separate literal character.
**Parameters:** Octal digits are 0-7. Maximum 3 digits. A third digit is allowed only if the lead digit is 3 or lower, so the maximum value is octal 377 (decimal 255). Value = digits accumulated in base 8.
**Edge cases handled:** A single octal digit, for example \0, gives value 0 with length 2; \55 (two digits) gives '-' with length 3; \8 and \9 are not octal. They fall through to the identity-escape rule and decode to the characters '8' and '9'.; Uses the same length rule as the escape normalizer at line 759-780, so the look-ahead sees the same character the output will contain
**Confidence:** High

### RULE-011: Backward decoding to detect disguised '-->' before a '>'
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:1504-1532`
**Plain English:** To decide whether a '>' ends a '-->' sequence, the sanitizer walks backwards one logical character at a time. A preceding character counts as escaped only if an unescaped backslash 2 to 6 positions earlier starts an escape that ends exactly on that character.
**Specification:**
  Given Raw string text \x2d\x2d> inside a string literal
  When  The '>' check (line 693-713) reads the two logical characters before the '>'
  Then  Each \x2d is found by scanning back to the backslash: the backslash run has odd length (1), the escape decodes forward with length 4, and 4-1 equals the 3-position offset, so it counts as '-'. Both characters are '-', so the '>' is rewritten to \u003e.
**Parameters:** The look-back window is offsets 1 to 5 before the target character, which covers escape lengths 2 to 6: \?, \0N, \0NN, \xHH, \uHHHH. A backslash run must have odd length to start an escape. The decoded length minus 1 must equal the offset.
**Edge cases handled:** Target index below 0 returns 0 (no character); Only the nearest backslash in the window is considered, and the scan stops there whether or not it matches; An even backslash run before the character (for example \\-) means the backslash is itself escaped, so the '-' is read as a literal. In \\-> only one dash is seen and the '>' is not escaped.; An escape that does not end exactly on the target (for example \0555, where the escape \055 ends one character early) causes the target to be read as a literal; If no qualifying escape is found, the raw character at the target position is returned with length 1
**Confidence:** High

### RULE-012: Numeric object keys are canonicalized to JavaScript number-to-string form
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:525-541`
**Also cited:** (key quoting + canonicalizeNumber call) and src/main/java/com/google/json/JsonSanitizer.java:1130-1332 (canonicalizeNumber, ES5 9.8.1 Number-to-String); src/main/java/com/google/json/JsonSanitizer.java:1130-1206
**Plain English:** An unquoted numeric object key is converted to the string JavaScript would use as the property name (the canonical number-to-string form), so JSON.parse and eval give the same key.
**Specification:**
  Given Input {10e100:0}, {+.5e2:0} and {.01234e-100:0}
  When  Each input is sanitized
  Then  Keys become "1e+101", "50" and "1.234e-102" respectively
**Parameters:** Canonical form is the JavaScript Number-to-String format (normalized mantissa, no trailing zeros, explicit '+' for positive exponents, exponent notation outside the plain-decimal range)
**Edge cases handled:** -0 and +0 keys give "0" (lines 112-113); 1.0 and 1. give "1" (lines 114-115); +.01 gives "0.01" and +.5e-2 gives "0.005" (lines 122-123); 10.500e-100 drops trailing zeros: "1.05e-99" (line 127); An exponent too large for a double (1e0001234567890123456789123456789123456789) keeps the key text as written (lines 151-155); Lines 129 and 130 are the same test case
**Suspected defect:** For an exponent outside the double range, the key keeps its original text. A JavaScript eval of the original input would give the key "Infinity", so the canonical-key rule does not hold at this extreme. Lines 129-130 also duplicate one assertion, which may be a copy-paste error that left out a planned case.
**Confidence:** Medium — For numeric keys whose exponent overflows a double, should the sanitizer keep the original digits (current behavior) or produce the JavaScript-equivalent key "Infinity"? Also, was the duplicate assertion at lines 129-130 meant to test a different input?

### RULE-013: Unquoted object keys are quoted; numeric keys canonicalised to JS string form
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:525-545`
**Plain English:** In key position every bare token is wrapped in double quotes; a numeric key is first converted to the string JavaScript would use for it as a property name, and keywords used as keys become string keys.
**Specification:**
  Given Inputs { foo: "bar" }, { .5e-1: "bar" }, { true: 1 }
  When  the bare token is in AFTER_KEY position
  Then  outputs are { "foo": "bar" }, { "0.05": "bar" }, { "true": 1 }
**Parameters:** Canonical form follows ECMAScript Number::toString (canonicalizeNumber, lines ~1200-1332: plain decimal for exponent 0<n<=21, 0.000xxx for -6<n<=0, otherwise d.ddde+/-N)
**Edge cases handled:** If canonicalizeNumber fails (number cannot be canonicalised), the raw text is quoted as-is; return value is intentionally ignored (lines 538-541); Non-numeric keys go through sanitizeString for escaping
**Confidence:** High

### RULE-014: String literal delimiter normalization and termination
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:634-653`
**Plain English:** Every string value comes out wrapped in double quotes. A single-quoted string is converted to double quotes, a double quote inside the string is backslash-escaped, and a string with no closing quote gets a closing double quote added.
**Specification:**
  Given A string token taken from the input, which may be single-quoted, double-quoted, unterminated, or a bare word whose opening quote was inferred
  When  The string range [start,end) is sanitized
  Then  An opening ' becomes ". The last character in the range counts as the closing delimiter only when it matches the opening delimiter (any opening character other than ' is treated as "). A matching closing ' is rewritten to ". Any other " in the body is escaped as \". If no closing delimiter was found, a " is appended at the end of the range (line 816). Examples: 'foo' -> "foo"; 'it"s' -> "it\"s"; "abc (unterminated) -> "abc"; "abc' -> "abc'"
**Parameters:** Delimiters recognized: ' (U+0027) and " (U+0022). Closing-quote insertion is at line 816. For bare words, the caller inserts the opening quote (line 552) before calling this routine.
**Edge cases handled:** A single-character string ' becomes "" (the opening quote is rewritten, and because nothing closes it a closing quote is appended); A single quote inside a double-quoted string stays as a literal '; When a string opens with " and the range ends with ', the ' is not treated as closing. It stays literal and a " is appended.; For a bare word or a string whose start was inferred, a trailing " is always treated as closing
**Confidence:** High

### RULE-015: Normalize JavaScript escape sequences to valid JSON escapes
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:728-787`
**Plain English:** Backslash escapes that JSON allows are kept. JavaScript-only escapes are rewritten into JSON form, and any other backslash is dropped so that the character after it is kept as a literal.
**Specification:**
  Given A string value that contains backslash escape sequences
  When  The string is sanitized
  Then  \b \f \n \r \t \\ \/ \" are kept as they are. \v becomes \u0008. \xHH becomes \u00HH. A well-formed \uHHHH is kept. A malformed \x or \u, or any other escaped character, loses its backslash. A backslash at the very end of the range is removed. Examples: "\x41" -> "A"; "\-" -> "-"; "\u12" -> "u12"; "\'" -> "'"
**Parameters:** Hex digit test: 0-9, a-f, A-F (isHex, lines 1357-1361; the case-folding trick is ch|32). \x needs 2 hex digits and i+4 < end. \u needs 4 hex digits and i+6 < end. Hex digits copied from \x keep their original case.
**Edge cases handled:** Trailing lone backslash: removed (lines 729-731); \8 and \9 are not octal, so the backslash is dropped and the digit kept; \x or \u with too few hex digits: the backslash is dropped and the letter and digits stay as literal text; Octal escapes are handled by a separate rule
**Suspected defect:** \v (JavaScript vertical tab, U+000B) is rewritten as \u0008 (backspace) at line 740, and unescapedChar maps it the same way at line 1495. The string's meaning changes. The correct mapping is \u000b. Also, the bounds checks i+4 < end for \x (line 744) and i+6 < end for \u (line 752) each require one more character after the escape than it needs. In a quoted string the closing quote supplies that character, but in a bare word that ends with a valid escape (for example foo\x41) the check fails and the backslash is dropped, producing "foox41" instead of "fooA".
**Confidence:** High

### RULE-016: Convert JavaScript octal escapes to JSON unicode escapes
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:759-780`
**Plain English:** A JavaScript octal escape such as \101 is converted to the equivalent JSON escape A. If the first digit is 0-3, up to three octal digits are read; otherwise at most two.
**Specification:**
  Given A string value containing \ followed by a digit 0-7
  When  The string is sanitized
  Then  The octal digits are read greedily: up to 3 digits if the first digit is 0-3, otherwise up to 2. The value (digits combined base 8) is written as \u00 plus 2 lowercase hex digits. Examples: \0 -> \u0000; \101 -> A; \377 -> ÿ; \400 ->   followed by a literal '0'; \7 -> \u0007
**Parameters:** Octal digit set 0-7 (isOct, lines 1349-1351). Maximum value 255 (\377). Output width: 2 hex digits, lowercase (appendHex, lines 1382-1387).
**Edge cases handled:** A first digit of 4-7 limits the escape to 2 digits, so the third digit stays literal; Octal digits are only read while they are inside the string range (octalEnd < end); The decoded value is always 0x00-0xFF, so the 2-digit hex output always fits
**Confidence:** High

### RULE-017: Edit splice: copy the untouched input up to the edit, swap in the replacement, skip the replaced span
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:852-876`
**Plain English:** Every fix the sanitizer makes works the same way. It copies the original input verbatim from the last edit point up to the start of the fix, writes the replacement text, and moves the edit point past the replaced span. An insertion is a replacement of an empty span, and a deletion is a replacement with nothing.
**Specification:**
  Given The input `[1 2]` with no edits made yet (edit point `cleaned` = 0, no output buffer)
  When  The sanitizer finds a missing comma before the value at position 3 and calls insert(3, ',')
  Then  The output buffer holds `[1 ,` and `cleaned` = 3. The final flush (lines 576-582) appends input[3..5) = `2]`, so the result is `[1 ,2]`. Each replace(start, end, text) appends input[cleaned, start), then the text, then sets cleaned = end.
  And   elide(start, end) does the same copy and skips input[start, end) without writing anything, so it is the deletion primitive (used to drop comments, trailing commas and similar). The output is only correct if the input is processed strictly left to right.
**Parameters:** insert(pos, x) is replace(pos, pos, x), a zero-width span. elide(start, end) is a delete. The replacement can be a single char (replace/insert with a char) or a String (for example `"":` or `,"":` from lines 826 and 839).
**Edge cases handled:** start == end: a pure insertion. Nothing from the input is dropped and the edit point only moves up to 'start'.; elide(start, start) only flushes the pending verbatim input up to 'start'. Callers then append directly to sanitizedJson (for example the hex escapes at line 811 and the number rewriting at line 1078…; Edits must not move backwards: start >= cleaned and end >= start. Nothing checks this. If start < cleaned, StringBuilder.append(CharSequence, int, int) throws IndexOutOfBoundsException. If end < star…; At end of input, sanitize() (lines 576-582) copies the remaining input[cleaned, n) and then adds any closing brackets or null placeholders.; Content between 'cleaned' and the current scan position stays in the input and is never copied twice, so every input character is either copied once or deliberately skipped.
**Suspected defect:** There is no guard that edits are monotonic (start >= cleaned, end >= start). A caller bug produces either a runtime IndexOutOfBoundsException or duplicated or reordered output instead of a clear error. This is low risk because the scanner only moves forward, but a rewrite should assert it.
**Confidence:** High

### RULE-018: Finding the end of an over-deep value (bracket matching that ignores strings and comments)
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:922-942`
**Also cited:** src/main/java/com/google/json/JsonSanitizer.java:922-943
**Plain English:** To work out how much text an over-deep value covers, the sanitizer counts open and close brackets from that value's first bracket. It skips over quoted strings and JS comments, lets any close bracket match any open bracket, and stops when the count returns to zero. If that never happens, the value runs to the end of the input.
**Specification:**
  Given Input fragment [ "a]" , /* ] */ {1] ] , 5 starting at the first '['
  When  The end of the bracketed value is computed
  Then  The ']' inside the string and the one inside the comment are ignored. '{' raises the depth to 2 and the mismatched ']' closes it back to 1. The next ']' brings the depth to 0, and the end is just past that bracket, so ', 5' is outside the value.
**Parameters:** Depth counter starts at 0; '{' or '[' adds 1; '}' or ']' subtracts 1; end = position after the bracket that brings the depth back to 0; fallback = input length
**Edge cases handled:** A mismatched close bracket (such as '{' closed by ']') still counts as a match, the same as in the main sanitizer loop; A string with no closing quote runs to the end of the input, so the over-deep value does too; A '/' that does not start a comment is skipped as a single character; A // comment ends at CR, LF, U+2028 or U+2029. A /* comment ends at */, or at the end of the input if there is none.; Quote characters are double or single quotes. A quote preceded by an odd number of backslashes does not end the string.
**Confidence:** High

### RULE-019: Trailing comma removal before a close bracket or end of input
**Category:** Calculation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:966-993`
**Plain English:** When an array or object ends right after a comma, the sanitizer removes the last comma. It looks back over whitespace, first in the not-yet-copied input and then in the already-sanitized output.
**Specification:**
  Given Input [1,2,   ] or {"a":0, /*c*/ }
  When  The closing bracket is reached in state BEFORE_ELEMENT or BEFORE_KEY (also at end of input, line 586)
  Then  Outputs are [1,2   ] and {"a":0  } (whitespace kept, comma gone)
**Parameters:** Characters skipped while looking back: tab, LF, CR, space. Target character: ','
**Edge cases handled:** The comma may already have been copied to the sanitized buffer (for example when a comment or parenthesis between it and the bracket forced a copy). In that case the buffer is truncated at the comma.; [1,,] becomes [1,null] because the elided slot is filled with null first and then the last comma is removed; Any other character found while looking back, or no comma at all, raises an AssertionError. This is an internal invariant, not a business outcome.; Also applied at end of input for unclosed content: [1, becomes [1]
**Confidence:** High

### RULE-020: Random JSON document shape (depth, breadth, value kinds, whitespace)
**Category:** Calculation
**Priority:** P1
**Source:** `src/test/java/com/google/json/FuzzyTest.java:179-260`
**Also cited:** src/test/java/com/google/json/FuzzyTest.java:179-260
**Plain English:** A random document gets a maximum nesting depth of 1-8 and a maximum breadth of 4-19. Each value is chosen at random from null, true, false, float, integer, string, array and object. Containers are only allowed while depth remains, and each child level has its breadth reduced by one (never below 1).
**Specification:**
  Given maxDepth = 1 + rnd(8) = 3 and maxBreadth = 4 + rnd(16) = 10
  When  A value is generated at depth 0 (maxDepth reaches 0)
  Then  Only one of the 6 leaf kinds is chosen (null/true/false/float/int/string). At depth > 0 all 8 kinds are possible, and an array or object holds rnd(maxBreadth) elements, i.e. 0-9, whose children use breadth max(1, 10-1) = 9
**Parameters:** maxDepth = 1..8; maxBreadth = 4..19; kind choice uniform over 8 (depth>0) or 6 (depth 0); child breadth = max(1, breadth-1); optional whitespace: 1/4 chance of 0-3 chars from {space, tab, CR, LF} (lines 308-314) around values, keys, colons and commas
**Edge cases handled:** Comma rule 'if (i != 1)' with a down-counting i: the comma between the second-to-last and last element is left out, and a trailing comma follows the last element. For 3 elements the output is '[a,b c…; So non-empty arrays and objects are never well-formed JSON even before mutation; Object keys come from the random-string generator and may contain unescaped double quotes (see string rule); Empty containers '[]' and '{}' happen when rnd(maxBreadth) returns 0
**Suspected defect:** The comma condition `i != 1` (lines 233 and 251) looks like an off-by-one for `i != 0`. As written, every non-empty container gets a trailing comma and is missing the comma before its last element, so the 'basis' is never valid JSON. | The array/object separator condition `i != 1` (lines 233, 251) looks like an off-by-one, since `i != 0` would give well-formed lists. It may be intentional, to produce malformed input. If so, it is undocumented.
**Confidence:** Medium — Is the missing comma plus trailing comma in generated arrays and objects meant to exercise the sanitizer's repair logic, or was 'i != 0' intended so that the basis is valid JSON before mutation? / Is the comma-placement rule 'i != 1' (trailing comma, missing separator before the last element) intended to exercise malformed-list repair, or is it a bug where valid lists were meant?

### RULE-021: Random numeric literal formats (float and integer)
**Category:** Calculation
**Priority:** P1
**Source:** `src/test/java/com/google/json/FuzzyTest.java:189-223`
**Plain English:** Floats are the reciprocal of a standard-normal random number, written in one of the %g/%G/%e/%E/%f styles. Integers are random BigIntegers of 1 to 2×breadth decimal digits with an optional leading '-' or '+', written in lowercase hex, uppercase hex, or decimal.
**Specification:**
  Given maxBreadth = 10 and the integer branch is chosen
  When  The integer literal is generated
  Then  A digit string of max(1, rnd(20)) digits (e.g. '00742') is parsed into a BigInteger (742). With equal 1/3 probability it gets no prefix, '-' or '+', and it is formatted with %x ('2e6'), %X ('2E6') or %d ('742') using Locale.ROOT
**Parameters:** FLOAT_FORMAT_STRING = {%g, %G, %e, %E, %f}; INT_FORMAT_STRING = {%x, %X, %d}; float value = 1.0 / nextGaussian(); integer digit count = max(1, rnd(maxBreadth*2)) (lines 316-323); sign choice = {none, '-', '+'} each 1/3; Locale.ROOT
**Edge cases handled:** Hex output has no 0x prefix, so values like '2e6' look like exponent notation and 'abc' looks like an identifier; A leading '+' is not valid JSON and is meant to test sanitizer normalization; BigInteger parsing drops leading zeros from the digit string; 1.0/nextGaussian can produce very large magnitudes; %f then writes long digit runs; A Gaussian of exactly 0.0, which would give Infinity, is theoretically possible but practically never happens
**Confidence:** High

### RULE-022: Random string-character generation and escaping
**Category:** Calculation
**Priority:** P1
**Source:** `src/test/java/com/google/json/FuzzyTest.java:262-306`
**Plain English:** Strings are double-quoted runs of 0 to 4×breadth-1 random code points weighted toward Latin-1. Characters that must be escaped (the chosen delimiter, control characters below 0x20, backslash), plus a random one-eighth of the rest, are escaped either as \uXXXX per UTF-16 unit or as a backslash before the raw character.
**Specification:**
  Given A code point 0x0A (newline) is drawn
  When  The character is appended
  Then  Because it is below 0x20 it must be escaped. With 50% probability it is written as `\u000a`, otherwise as a backslash followed by a raw newline character
**Parameters:** String length = rnd(maxBreadth*4); code-point range weights: 4/7 below 0x100, 2/7 below 0x10000, 1/7 up to Character.MAX_CODE_POINT (0x10FFFF); per-char delimiter = single quote with probability 1/8, else double quote; extra random escape probability = 1/8; escape style 50/50; \u escapes use 4 lowercase hex digits per UTF-16 code unit
**Edge cases handled:** The delimiter is picked per character but the string is always wrapped in double quotes (line 263). When ' is picked, a raw '"' can appear unescaped and end the string early; Code points in the surrogate range D800-DFFF can be emitted as lone surrogates; Supplementary code points escaped in \u style become a surrogate pair of two \uXXXX escapes; A backslash before a raw CR/LF produces a JS-style line continuation that JSON does not allow; The 0xA and 0xD switch cases produce the same output as the default appendCodePoint branch
**Confidence:** Medium — Is choosing the delimiter per character (so an unescaped '"' can break out of a double-quoted string) intentional fuzz pressure, or was the delimiter meant to be chosen once per string and used for the surrounding quotes?

### RULE-023: Mutation algorithm: 1-16 sorted insert/replace/remove edits
**Category:** Calculation
**Priority:** P1
**Source:** `src/test/java/com/google/json/FuzzyTest.java:325-377`
**Also cited:** src/test/java/com/google/json/FuzzyTest.java:325-377
**Plain English:** A mutant is built by picking 1 to 16 random positions in the basis, sorting them and removing duplicates, then at each position doing an insert, replace or remove (equal odds) of a random-length slice that never goes past the next edit position.
**Specification:**
  Given Basis 'null' (length 4) with chosen locations [1, 3]
  When  mutate runs
  Then  For location 1 the gap to the next location is 2, so the slice length is rnd(2) (0 or 1). For location 3 the gap to the end is 1, so the slice length is 1. Each location does an insert (random chars added, original kept), replace (random chars added, slice skipped) or remove (slice skipped). The rest of the basis is copied unchanged
**Parameters:** Edit count = rnd(16)+1 before dedupe; locations uniform in [0, len); operation choice uniform over {insert, replace, remove}; slice length = rnd(gap) if gap > 1 else 1, where gap = next location (or len) minus current location
**Edge cases handled:** Duplicate locations collapse, so the real edit count can be lower than drawn; A slice length of 0 (possible when gap > 1) turns replace and remove into no-ops and insert into adding nothing; Inserted and replacement characters come from the same random-char generator as strings, so they may be escape sequences like "; An empty basis would make rnd.nextInt(0) throw, but makeRandomJson never returns an empty string
**Confidence:** High

## Validation

### RULE-024: Over-deep arrays/objects are replaced with null instead of failing
**Category:** Validation
**Priority:** P0
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:377-395`
**Also cited:** src/main/java/com/google/json/JsonSanitizer.java:377-395 (depth check and null replacement at 382-389; DEFAULT_NESTING_DEPTH at :112); src/main/java/com/google/json/JsonSanitizer.java:377-395
**Plain English:** An array or object that would go deeper than the nesting limit is replaced, whole, by the value null. Sanitizing then continues after that value's closing bracket. No error is thrown.
**Specification:**
  Given maximumNestingDepth = 2 and input [[[1]],2]
  When  The sanitizer sees the third '[' while two brackets are already open
  Then  The output is [[null],2]: the whole inner [1] (up to its matching close bracket) becomes null and the sibling 2 is kept
**Parameters:** Trigger condition: bracketDepth >= maximumNestingDepth when an open bracket ('{' or '[') is seen; replacement literal = "null"
**Edge cases handled:** The value-position fixes (adding a missing comma or colon, or a "": key in map-key position) are applied before the null substitution, so {"a":1 [[x]]} at depth 1 gets punctuation plus null; If the over-deep value is never closed, everything to the end of the input is swallowed into the null. The outer brackets are then closed at end of input.; Brackets inside quoted strings or comments within the over-deep region do not count (see the bracket-matching rule); Behavior changed in commit 042752c: older versions threw an exception on over-deep input. A rewrite must keep the no-throw behavior.; The no-throw policy is new: 0c90410 (PR #37) briefly added JsonNestingDepthException for issue #35, and 042752c (PR #39, 2026-10-27) reverted that to null substitution before any release. Earlier pub…; endOfBracketedValue (lines 922-943) skips quoted strings and comments while counting brackets, so a ']' inside a string or comment in the skipped value does not end it early (test at JsonSanitizerTes…
**Confidence:** Medium — Citation was corrected by referee (The rule is accurate, but the citation points at the unit test that checks it, not the code that does it. JsonSanitizerTest.java:194-230 (testExcessiveNestingIsReplacedWithNull) asserts the stated cases. Line 213: with depth 2, "[[[1]],2]" becomes "[[null],2]". Line 210: input exactly at the limit (d '[' then d ']') comes back unchanged. Lines 214-215 cover the…

### RULE-025: Output is exactly one top-level value; trailing extra content is discarded
**Category:** Validation
**Priority:** P0
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:397-401`
**Plain English:** Once the first complete top-level value has been read, anything that would start a second top-level value, a top-level comma, or an unmatched closing bracket causes the rest of the input to be thrown away.
**Specification:**
  Given Input [1]],alert(1) or 1,2 or 1 2
  When  the sanitizer reaches the stray ']' at depth 0, the top-level ',' or the second top-level value
  Then  everything from that character to end of input is dropped: outputs are [1], 1 and '1 ' (trailing whitespace before the cut is kept)
**Parameters:** Top level is modelled as state START_ARRAY with bracketDepth 0; signal is the UNBRACKETED_COMMA exception (lines 1389-1412)
**Edge cases handled:** Unmatched close bracket at depth 0 elides to end of input (lines 398-401); Top-level comma throws UNBRACKETED_COMMA (line 428) caught at lines 558-561 which elides to end; A second value after AFTER_ELEMENT at depth 0 also throws (requireValueState line 845); Inside arrays the same situation instead inserts a comma
**Confidence:** Medium — Citation was corrected by referee (The behavior is real, but the cited lines don't implement it. JsonSanitizer.java:1389-1393 only declares the private static final class UnbracketedComma extends Exception. It is a marker type with a serialVersionUID and no logic ("No members. Used for nominal type."), and the singleton with no stack trace is at 1408-1412. The rule is enforced elsewhere. At :428,…

### RULE-026: Sanitizer output must be idempotent (sanitizing twice changes nothing)
**Category:** Validation
**Priority:** P0
**Source:** `src/test/java/com/google/json/FuzzyTest.java:37-100`
**Plain English:** For every fuzzed input, running the sanitizer on its own output must return exactly the same string, so a sanitized document is already final, well-formed JSON.
**Specification:**
  Given A pseudo-random JSON-like input such as the malformed `[1f,'a' true,]` produced by the fuzz generator
  When  JsonSanitizer.sanitize is applied once (sanitized0) and then again to that result (sanitized1)
  Then  sanitized1 must equal sanitized0 character for character. Otherwise the test fails, reports the input, a UTF-16 hex dump and the differing region, and rethrows
**Parameters:** nRuns = 10000 inputs per test run (line 39); comparison is exact string equality (lines 91-92)
**Edge cases handled:** Each run checks 10,000 inputs and stops once the counter reaches 0 (line 99); The generator emits both fresh random documents and mutated variants, so the check covers syntactically broken input as well as near-valid input; Any Throwable from sanitize, not only an inequality, counts as a failure and is rethrown after diagnostics (lines 93-97)
**Suspected defect:** The diagnostic 'After:' line (line 89) excerpts sanitized0 using sanitized1's right bound (right1) when it should excerpt sanitized1. excerpt() (lines 131-134) also escapes backslashes after inserting \r/\n escapes, so those escapes get doubled. Both bugs affect only the failure message, not the pass/fail result. Also in excerpt(): the ellipseLeft (:122) and ellipseRight (:126) flags are computed but never used, so truncated excerpts aren't marked. Fixes: excerpt(sanitized1, commonPrefixLen, right1) at :89; do the backslash replace first in excerpt().
**Confidence:** High

### RULE-027: Generated short variable names skip JavaScript reserved words
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/EvalMinifier.java:290-345`
**Also cited:** src/main/java/com/google/json/EvalMinifier.java:291-345
**Plain English:** Pooled-literal parameter names are produced in the order a-z, A-Z, _, $, then two-character names (digits allowed after the first character), skipping any name that is a JavaScript reserved word so the emitted wrapper stays valid, side-effect-free JavaScript.
**Specification:**
  Given A fresh NameGenerator
  When  Names are drawn one after another
  Then  The first three are 'a','b','c' and the 34th is 'H'. Within the first ~1,000 two-character names, 'as', 'do', 'if' and 'in' never appear while neighbours like 'ar', 'au', 'dn', 'dp', 'ie', 'ig'..'im', 'io' do
**Parameters:** First-position alphabet: a-z, A-Z, _, $ (54 chars); later positions also allow 0-9 (EvalMinifier.java:335-345); reserved words indexed by length, from 2-char {as, do, if, in, of} up to 10-char {implements, instanceof} (EvalMinifier.java:291-303)
**Edge cases handled:** After '$' in the first position, the name length grows ('$' then 'aa'); Reserved words longer than 10 characters are never checked (nameLen >= table length returns the name); Generated names cover more than strict ES reserved words: contextual words like get, set, of, from, async, target and eval are also skipped
**Confidence:** Medium — Citation was corrected by referee (The behavior is real, but the citation points at a test, not the code that does the work. EvalMinifierTest.java:75-102 (testNameGenerator) only checks results. It confirms the first three names are 'a','b','c' (lines 78-80). After 30 skipped draws the 34th is 'H' (lines 81-82): a-z are names 1-26 and A-H are 27-34. In the next 1000 draws 'as', 'do', 'if' and 'in…

### RULE-028: Minifier must not substitute property names or partial numeric tokens
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/EvalMinifier.java:76-118`
**Also cited:** (token scan + ':' key exclusion at 109-110), with isLetterOrNumberChar at 234-239; behavior asserted by src/test/java/com/google/json/EvalMinifierTest.java:58-72
**Plain English:** A string used as an object key (followed by ':') is never replaced by a variable. A number is only pooled as a whole token, so 123456 is never substituted inside 123456.789 or 123456e-3.
**Specification:**
  Given An object whose long string value also appears as a key, and an array mixing 123456, 123456.789 and 123456e-3
  When  EvalMinifier.minify runs
  Then  The key stays literal (`{"A very...":a, ...}`) while value occurrences become `a`. 123456.789 and 123456e-3 stay verbatim while standalone 123456 becomes `a`
**Parameters:** Token characters = digits, ASCII letters, '_', '$', '-', '.' (EvalMinifier.java:234-239); key detection = next non-whitespace char is ':' (EvalMinifier.java:109)
**Edge cases handled:** The unquoted key 'a' is quoted to "a" by the sanitizer and then kept as a key; An elided array element ',,' is sanitized to null; Whitespace (space, tab, CR, LF) between a key and ':' is skipped when detecting a key
**Confidence:** Medium — Citation was corrected by referee (The behaviour is real and the rule describes it correctly, but the cited lines are a unit test, not the code that does the work. EvalMinifierTest.java:58-72 only asserts the expected output. Lines 58-64 check that a long string used as a key stays literal while its value occurrences become 'a'. Lines 66-72 check that 123456.789 and 123456e-3 stay verbatim while…

### RULE-029: Pooling eligibility: exclude object keys and short tokens
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/EvalMinifier.java:97-118`
**Plain English:** A token can be replaced by a variable only if it is at least 4 characters long (quotes count) and is not an object property name, i.e. the next non-whitespace character is not ':'.
**Specification:**
  Given Sanitized JSON {"name":"name","id":7}
  When  each token is checked for pool eligibility
  Then  The first "name" is followed by ':' and is excluded, because a key stays literal: {a:1} means {"a":1} whatever a resolves to. The second "name" (6 chars) is eligible. "id" (4 chars) is a key and excluded. 7 (1 char) is too short.
**Parameters:** Minimum token length = 4 characters (hardcoded, line 110). Whitespace skipped before the ':' check: tab, LF, CR, space.
**Edge cases handled:** A 2-letter string such as "ab" is 4 characters with its quotes, so it is eligible.; A token at the very end of input (nextNonWhitespace == n) is eligible.; Equal tokens are chained (newest links to previous) in a map ordered by first occurrence. Equality means an exact character match, hash 31*h+ch (lines 241-287).; After a token, scanning resumes at the next non-whitespace character.
**Confidence:** Medium — P0 panel split on whether this is critical to the system's core purpose or a costly-if-wrong rule (Faithfulness: the rule matches the code. In /Users/jmanico/Dropbox/github/json-sanitizer/legacy/json-sanitizer/src/main/java/com/google/json/EvalMinifier.java at lines 97-103, the code skips only tab, LF, CR and space after a token. At lines 109-110, a token joins the pool only if the next non-white…

### RULE-030: Leading plus sign is stripped from numbers; minus sign is kept
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:1012-1025`
**Plain English:** JSON does not allow a '+' in front of a number, so a leading '+' is removed while a leading '-' is kept as-is.
**Specification:**
  Given An unquoted token in value position that begins with a digit, '.', '+' or '-' (classified as a number at lines 508-509)
  When  The token is normalized
  Then  '+12' is output as '12', '-12' stays '-12', and '+.5' becomes '0.5' (the empty integer part is then filled in, see the empty-part rule)
**Parameters:** Sign characters: '+' is removed, '-' is kept. Only one leading sign is inspected.
**Edge cases handled:** '+' on its own becomes '0'; '-' on its own becomes '-0' (empty integer part is filled with 0); '+-5': '+' is removed, then '-5' has an empty integer part, so the result is '0' and '-5' is dropped as trailing junk; A '+' inside the exponent ('1e+5') is allowed by JSON and kept for values (lines 1107-1110)
**Confidence:** High

### RULE-031: Empty integer, fraction, or exponent digits are filled with zero
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:1027-1119`
**Plain English:** JSON requires at least one digit before the decimal point, after the decimal point, and after an exponent marker, so the sanitizer inserts a '0' wherever JavaScript allowed one of these to be empty.
**Specification:**
  Given A number token in value position with a missing digit run
  When  The token is normalized
  Then  '.5' becomes '0.5', '-.5' becomes '-0.5', '1.' becomes '1.0', '1e' becomes '1e0', '1e+' becomes '1e+0', and '1.e5' becomes '1.0e5'
**Parameters:** Fill character '0'. Exponent marker 'e' or 'E', case-insensitive. An optional exponent sign of '+' or '-' is kept. Leading zeros in the exponent are allowed and not stripped. Fraction length is unlimited (no truncation).
**Edge cases handled:** Value-position numbers are not otherwise canonicalized: '1E5', '1.50', '-0' and '1e007' are passed through unchanged apart from the zero filling; Only ASCII 0-9 count as digits (endOfDigitRun, lines 1395-1401); Unicode digits do not
**Confidence:** High

### RULE-032: Characters after a valid number are dropped
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:1120-1122`
**Plain English:** After the sign, integer, optional fraction and optional exponent are read, anything else left in the same unquoted token is deleted, so only a well-formed number remains.
**Specification:**
  Given An unquoted token that starts like a number (digit, '.', '+' or '-') and continues with letters, digits, '.', '+', '-', '_' or '$' (token scan at lines 491-499)
  When  Normalization reaches the end of the recognized number before the end of the token
  Then  '1.2.3' becomes '1.2', '1abc' becomes '1', '1e5e6' becomes '1e5', '1-2' becomes '1', and '0x1G' becomes '1'
**Parameters:** No numeric parameters. The token boundary is set by the caller's run scan of [A-Za-z0-9+-._$].
**Edge cases handled:** '-Infinity' becomes '-0' and '+Infinity' becomes '0' (empty integer part filled, then 'Infinity' dropped), while bare 'Infinity' and 'NaN' do not start like numbers and are quoted as strings instead; '-' followed by letters becomes '-0'
**Suspected defect:** '-Infinity' and '+Infinity' are silently turned into '-0' and '0', which changes the meaning, while unsigned 'Infinity' becomes the string "Infinity". This inconsistent handling of non-finite values may not be intended.
**Confidence:** High

### RULE-033: Backslash-run parity decides whether the trigger character is escaped
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:1534-1540`
**Plain English:** Counting the consecutive backslashes immediately before a character tells whether that character is escaped: an odd count means it is escaped, an even count means the backslashes cancel out in pairs.
**Specification:**
  Given Raw string text --\> (two dashes, one backslash, '>')
  When  The '>' check counts the backslash run that ends just before the '>'
  Then  The run length is 1 (odd), so the '>' is treated as the escaped form \> and the look-behind starts before the backslash. Two dashes are found, so the '>' is rewritten to \u003e. This matters because the normalizer would otherwise drop the backslash and produce a literal -->. With --\\> the run is 2 (even), the previous logical character is a backslash, and no escaping happens.
**Parameters:** Odd parity means escaped (checked with &1 in the caller at line 698). The count runs backwards to the start of the whole input, not just the current token.
**Edge cases handled:** Position below 0 gives a count of 0; A run of 0 or 2 backslashes means the '>' is unescaped; The count is not limited to the current string token, but in practice the opening quote ends the run
**Confidence:** High

### RULE-034: Byte streams are read and written as UTF-8; malformed bytes become U+FFFD; output is flushed
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:243-249`
**Plain English:** The byte-stream API always decodes input as UTF-8 and encodes output as UTF-8. Invalid UTF-8 bytes are replaced with the Unicode replacement character, and the output is flushed but not closed.
**Specification:**
  Given An InputStream holding bytes 7B 22 61 22 3A 22 FF 22 7D ({"a":"<invalid 0xFF>"})
  When  sanitize(InputStream, OutputStream) is called
  Then  0xFF is decoded as U+FFFD. The sanitized JSON {"a":"�-equivalent"} is written to the OutputStream as UTF-8, the writer is flushed, and neither stream is closed
**Parameters:** Charset = UTF-8 (line 251), fixed with no override. The malformed-input action is replacement (the default for InputStreamReader built with a Charset), as stated in the Javadoc at line 219. The default depth of 64 is applied by the overload at lines 229-232.
**Edge cases handled:** A leading UTF-8 BOM (EF BB BF) is not stripped by the Java UTF-8 decoder. It reaches the sanitizer as U+FEFF.; There is no option to choose another input or output charset.; The output is flushed so that buffered bytes reach the caller's stream even though it is not closed.
**Confidence:** Medium — Should a leading UTF-8 byte-order mark be stripped or rejected in the byte-stream API, and is UTF-8 always the required encoding, or must other charsets (e.g. UTF-16) be supported?

### RULE-035: Missing (null) input is treated as JSON null
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:322-328`
**Plain English:** If the caller passes no content at all (a null reference), the sanitizer treats it as the JSON literal null and does not fail.
**Specification:**
  Given A caller passes a null String or a null CharSequence
  When  sanitize(String) or sanitize(CharSequence) is called
  Then  The result is the 4-character string "null" and no exception is thrown
**Parameters:** Substitute literal: "null" (line 327). The CharSequence overload sends null to the String path at lines 169-170, so the caller gets a String "null".
**Edge cases handled:** Null CharSequence: the String overload handles it (lines 169-170) and returns a new String "null".; Input that is only whitespace is also turned into "null", by a separate check at lines 564-567 (outside the focus range).; The Reader and InputStream overloads take no null content. A null Reader or stream would fail with NullPointerException, which is not a business rule.
**Confidence:** High

### RULE-036: Whole-input normalization: empty input, trailing content, comments and grouping parentheses
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:327`
**Also cited:** 358-375,397-400,427-428,468-475,558-568
**Plain English:** Null, empty or whitespace-only input becomes the JSON value null. Only the first top-level value is kept. JavaScript comments are removed, and wrapping parentheses are dropped.
**Specification:**
  Given Input false,true
  When  The input is sanitized
  Then  Output is false. The second top-level value is discarded
**Parameters:** Placeholder for no value = null
**Edge cases handled:** null reference and "" both give "null" (lines 51-52); CharSequence null gives "null" (line 411); Leading and trailing whitespace around a valid value is kept as is (lines 56-58); // line comments and /* block */ comments are removed, including unterminated ones such as 'false/* comment ' (lines 84-90); '/*/true**/false' gives false. The '/*/' opens a comment and does not close it (line 90); ({}) gives {} (line 133); A stray trailing '>' after a complete value is dropped: [[0]]> gives [[0]] (line 73); A leading BOM (U+FEFF) and a stray ']' are dropped (line 247)
**Confidence:** Medium — Citation was corrected by referee (The behavior is real, but the citation points at the wrong place. JsonSanitizerTest.java:47-90 is a JUnit test, not the code that does the work. It also misses parts of the rule. Lines 51-52 show null and "" becoming "null". Line 59 shows "false,true" becoming "false". Lines 84-90 show comments being removed. But nothing in lines 47-90 tests wrapping parentheses…

### RULE-037: Close-bracket repair: fill missing values, drop trailing commas, fix mismatched bracket type
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:397-426`
**Also cited:** src/main/java/com/google/json/JsonSanitizer.java:397-426
**Plain English:** When an array or object closes, a dangling key gets ':null', a dangling colon gets 'null', a trailing comma is removed, and the closing bracket is rewritten to match whichever bracket opened the container.
**Specification:**
  Given Inputs {"a"}, {"a":}, {"a":0,}, [1,2,3,], [1}, {"a":1]
  When  the closing bracket is processed
  Then  outputs are {"a":null}, {"a":null}, {"a":0}, [1,2,3], [1], {"a":1}
**Parameters:** Bracket type remembered per level in isMap[] (array sized to maximumNestingDepth)
**Edge cases handled:** Empty containers [] and {} pass unchanged (START_ARRAY/START_MAP need no repair); Source comment at lines 403-404 says [1,2,3,] -> [1,2,3,] (typo); executable code strips the comma via elideTrailingComma (lines 966-979)
**Confidence:** High

### RULE-038: Structural repair of malformed arrays and objects
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:403-465`
**Also cited:** (bracket/comma/colon repair), 585-600 (end-of-input fill and bracket closing), 816 (unclosed quote), 819-848 (requireValueState: insert commas/colons/"" keys), 966-995 (elideTrailingComma)
**Plain English:** Missing punctuation is filled in and extra punctuation is removed: trailing commas are dropped, empty slots become null, missing commas and colons are inserted, missing keys become "", missing values become null, and open brackets and quotes are closed.
**Specification:**
  Given Input [1,,3,]
  When  The input is sanitized
  Then  Output is [1,null,3]. The empty slot becomes null and the trailing comma is removed
**Parameters:** Fill value for a missing element or value = null; fill value for a missing key = "" (empty string)
**Edge cases handled:** [1 2 3] gives [1 ,2 ,3]. The comma is inserted before the following value and the whitespace is kept (line 77); [1,-1,0.0,-0.5,1e2, (unclosed) gives [1,-1,0.0,-0.5,1e2] (line 74); {"foo","bar"} gives {"foo":"bar"}. A comma in colon position becomes a colon (line 80); { foo: 'bar gives { "foo": "bar"}. The string is closed, then the object (line 82); [,] and [null,] both give [null] (lines 143-144); {"a":0,false"x":{...}} gives {"a":0,"false":"x","":{...}}. A value with no key gets an empty key (lines 145-146); {"a":"b","c": gives {"a":"b","c":null} (line 150); Fuzzer inputs: [{{},ä gives [{"":{}}] with no exception (lines 162-166); Long fuzzer inputs at lines 301-316 and 358-364 must produce the exact well-formed outputs shown
**Suspected defect:** The repaired output can contain duplicate keys in one object, for example {"":{},...,"":{}} (lines 305, 313). JSON parsers handle duplicate keys in different ways (last key wins, first key wins, or an error), so downstream consumers may disagree about the value.
**Confidence:** Medium — Citation was corrected by referee (The behaviour is real, but the citation points at the tests, not at the code that does the work. JsonSanitizerTest.java:73-83 is a list of assertSanitized calls. They check the expected repairs: [1,2,3,] becomes [1,2,3]; [1,,3,] becomes [1,null,3]; [1 2 3] becomes [1 ,2 ,3]; a trailing comma in an object is dropped; {"foo","bar"} becomes {"foo":"bar"}; an unquot…

### RULE-039: Separator repair: array elisions become null, stray commas and colons are dropped
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:427-466`
**Plain English:** A comma with no preceding array element inserts null (JS array-hole semantics), a comma after a key with no value inserts null, commas that make no sense in an object are dropped, and a colon is only kept directly after a key.
**Specification:**
  Given Inputs [1,,3], [,1], {"a":,"b":1}, {,"a":1}, {"a",1}, {"a"::1}, [1:2]
  When  the ',' or ':' is processed
  Then  outputs are [1,null,3], [null,1], {"a":null,"b":1}, {"a":1}, {"a":1}, {"a":1}, [1,2]
**Parameters:** none
**Edge cases handled:** AFTER_ELEMENT comma -> BEFORE_ELEMENT; AFTER_VALUE comma -> BEFORE_KEY; START_MAP/BEFORE_KEY/AFTER_KEY comma is elided without state change; Colon in any state other than AFTER_KEY is elided; Top-level comma truncates output (see top-level rule)
**Confidence:** High

### RULE-040: Bare words and single-quoted text become double-quoted JSON strings
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:477-555`
**Also cited:** (bare-word quoting in the default branch), src/main/java/com/google/json/JsonSanitizer.java:1334-1343 (case-sensitive isKeyword for true/false/null), src/main/java/com/google/json/JsonSanitizer.java:361-366,620-650 (single-quoted strings changed to double quotes in sanitizeString)
**Plain English:** Any unquoted word that is not exactly the lowercase keyword true, false or null, and any single-quoted text, is output as a double-quoted JSON string. This stops identifiers such as eval from becoming code.
**Specification:**
  Given Input eval (an unquoted identifier)
  When  The input is sanitized
  Then  Output is "eval", a string literal and not a reference to a variable
**Parameters:** Recognized keywords (case-sensitive): true, false, null
**Edge cases handled:** Misspelled or wrong-case keywords are quoted: fasle gives "fasle", FALSE gives "FALSE" (lines 174-175); dev/comment keeps the slash: "dev/comment" (line 176); An invalid backslash escape in a bare word drops the backslash: dev\comment gives "devcomment"; dev\ncomment keeps \n (lines 177-178); [dev\, comment] gives ["dev", "comment"] (line 179); 'foo' gives "foo" (line 61); a bare key foo: gives "foo": (line 81); y\4 gives "y\u0004". An octal escape is converted to a unicode escape (line 297); {a\b"c gives {"a\b":"c"} (line 149)
**Confidence:** Medium — Citation was corrected by referee (The behaviour is real, but the citation points at a test, not the code that does it. JsonSanitizerTest.java:168-180 (testIssue4) only checks expected outputs: dev -> "dev", eval -> "eval", fasle -> "fasle", FALSE -> "FALSE", and a few backslash and slash cases. The test does not cover single-quoted input at all. The behaviour itself lives in src/main/java/com/go…

### RULE-041: Bare token classification: number, keyword, or unquoted string
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:486-523`
**Plain English:** An unquoted run of letters, digits, _, $, +, - and . is a number if it starts with a digit, '.', '+' or '-'; it is a keyword only if it is exactly 'true', 'false' or 'null' (lower case); anything else is treated as a string and widened up to the next JSON-structural character, then quoted.
**Specification:**
  Given Inputs [True], [undefined], [NaN], [foo bar], [true]
  When  the bare token is classified
  Then  outputs are ["True"], ["undefined"], ["NaN"], ["foo","bar"], [true]
**Parameters:** Keyword list: false (5 chars), null, true (4 chars), case-sensitive exact match (isKeyword lines 1334-1342). Structural terminators for unquoted strings: any char <= U+0020, and " , : [ ] { } (isJsonSpecialChar lines 1368-1378)
**Edge cases handled:** Unquoted strings extend past ( ) / ' and other non-structural chars, e.g. foo(bar) -> "foo(bar)"; If an unquoted string ends at a '"', that quote is absorbed into the token (lines 520-522); Value-position numbers go to normalizeNumber (hex/octal to decimal, empty integer/fraction filled) - outside this module's line range; Tokens like -foo start with '-' so are routed to number normalization rather than string quoting
**Confidence:** High

### RULE-042: Character escaping and elision for XML and JavaScript safety
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:620-631`
**Also cited:** 789-814 (in-string escaping in sanitizeString); src/main/java/com/google/json/JsonSanitizer.java:487-503 (elision outside strings, default branch of the main loop)
**Plain English:** Inside strings, control characters, isolated UTF-16 surrogates, the noncharacters U+FFFE and U+FFFF, and the JS line separators U+2028 and U+2029 are written as \uXXXX escapes. Outside strings, these characters are removed.
**Specification:**
  Given A single-quoted string containing U+0000, U+0008 and U+001F
  When  The input is sanitized
  Then  Output is "\u0000\u0008\u001f"
**Parameters:** Characters always escaped in strings: U+0000-U+001F, lone surrogates U+D800-U+DFFF, U+FFFE, U+FFFF, U+2028 (line 441). Kept as is: U+FFFD, U+00A0, U+1234 and valid surrogate pairs.
**Edge cases handled:** A valid surrogate pair U+D800 U+DC00 is kept. Lone surrogates that follow it are escaped (lines 136-137); U+FFFD is kept raw; U+FFFE and U+FFFF are escaped (line 138); Outside a literal, U+FFEF, U+0000, U+0008, lone surrogates and U+FFFF are removed, so the input around 42 gives 42 (line 141), and input with only such characters gives null (line 142); [" ሴ"] passes through unchanged (line 148); U+2028 inside a string read from a UTF-8 stream is escaped as \u2028 (lines 439-441)
**Confidence:** Medium — Citation was corrected by referee (The behavior is real, but the citation points at unit tests, which check the behavior rather than implement it. JsonSanitizerTest.java:134-142 asserts that U+0000, U+0008 and U+001F inside a single-quoted string become "\u0000\u0008\u001f" (line 135). It also asserts that lone surrogates are escaped while a valid pair is kept (136-137), that U+FFFD is kept while…

### RULE-043: Escape JavaScript line terminators and tab inside strings
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:625-631`
**Plain English:** Raw tabs, line feeds, carriage returns, and the Unicode line and paragraph separators are not allowed to appear unescaped inside a string. Each one is replaced with its escape sequence.
**Specification:**
  Given A string value that contains a raw TAB, LF, CR, U+2028 or U+2029 character
  When  The string is sanitized
  Then  TAB -> \t, LF -> \n, CR -> \r, U+2028 -> \u2028, U+2029 -> \u2029. Example: a string with a real newline between 'a' and 'b' becomes "a\nb"
**Parameters:** Characters with short escapes: U+0009, U+000A, U+000D. JavaScript-only line terminators escaped as \uXXXX: U+2028, U+2029
**Edge cases handled:** U+2028 and U+2029 are legal in JSON but break older JavaScript eval, so they are always escaped; Other control characters fall through to the generic hex escape (see the control-character rule)
**Confidence:** High

### RULE-044: Escaped double-dash before '>' is still treated as an HTML comment close
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:693-713`
**Plain English:** If a '>' would follow two dashes in the decoded string, it is escaped as > even when one dash or the '>' was written as an escape sequence, and sanitizing the result again changes nothing.
**Specification:**
  Given Input '->??-\-> (single-quoted string where the second dash is written as \-)
  When  The input is sanitized, then the output is sanitized again
  Then  Both passes return "->??-->" (the stray backslash escape is dropped and the closing '>' is escaped)
**Parameters:** Escape for '>' = >
**Edge cases handled:** '.-->' (dash written as a unicode escape) becomes ".-->" and keeps the - escape as written (lines 385-391); Bare word x--\> becomes "x-->" (lines 395-401); A single '->' with only one dash is not escaped (line 377 output starts with ->)
**Confidence:** Medium — Citation was corrected by referee (The behavior is real, but the cited lines are where it is tested, not where it is implemented. JsonSanitizerTest.java:374-402 holds three JUnit tests: testDashDashGtEscaped, testDashDashGtUEscaped and testEscHtmlCommentClose. Each one sanitizes its input, sanitizes the result again, and asserts both outputs are the same. They cover a dash written as \-, a dash w…

### RULE-045: Escape control characters, lone surrogates and non-characters that XML disallows
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:788-813`
**Plain English:** Inside strings, any code unit that ECMA-404 JSON or XML 1.0 does not allow raw (control characters below U+0020, unpaired surrogate halves, U+FFFE and U+FFFF) is replaced with a \uXXXX escape.
**Specification:**
  Given A string value containing U+0001, an unpaired high surrogate U+D800, or U+FFFF
  When  The string is sanitized
  Then  Each such code unit becomes \u followed by 4 lowercase hex digits: U+0001 -> \u0001, U+001B -> \u001b, lone U+D800 -> \ud800, U+FFFF -> ￿. A valid high+low surrogate pair is kept unchanged.
**Parameters:** Pass-through ranges: U+0020-U+D7FF, U+E000-U+FFFD, and valid surrogate pairs (matching the XML Char production). Escaped: U+0000-U+001F (other than TAB, LF and CR, which have short escapes), unpaired U+D800-U+DFFF, U+FFFE-U+FFFF. Hex output uses the lowercase HEX_DIGITS table (line 1423).
**Edge cases handled:** A low surrogate with no high surrogate before it is escaped; A high surrogate at the end of the range (no following character) is escaped; DEL (U+007F) and C1 controls (U+0080-U+009F) are not escaped. XML 1.0 allows them, so they pass through raw.; Backspace and form feed in raw form are escaped as \u0008 and \u000c, not \b and \f
**Confidence:** High

### RULE-046: JS line comments are stripped up to and including the line terminator
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:883-898`
**Plain English:** A '//' outside a string starts a comment that runs to the next line break (LF, CR, U+2028 or U+2029). The comment and that one break are removed from the output, and if no break follows, everything to the end of the input is removed.
**Specification:**
  Given The input `false// comment\n`, or `[1,// note\n2]`
  When  The sanitizer reaches the '/' at structural level, i.e. outside any string
  Then  Everything from '//' up to and including the '\n' is removed, so the output is `false` (and `[1,2]` for the second input). For `// comment\nfalse` the output is `false`.
**Parameters:** Line terminators: '\n' (U+000A), '\r' (U+000D), U+2028 LINE SEPARATOR, U+2029 PARAGRAPH SEPARATOR. Exactly one terminator character is consumed (end = j + 1). Caller: sanitize() case '/' at lines 468-475 elides [i, endOfComment(i)) and resumes scanning after it.
**Edge cases handled:** Unterminated line comment ('false// comment'): end = input length, so the rest of the input is dropped and the output is 'false'; CRLF: only the '\r' is consumed. The following '\n' stays and is later treated as ordinary whitespace; The removed comment is not replaced with a space. Tokens on either side become adjacent, and the state machine then decides the separator (e.g. a comma inside an array); U+2028/U+2029 end the comment because they are JS line terminators
**Confidence:** High

### RULE-047: JS block comments are stripped up to the first closing '*/'; an unterminated block comment swallows the rest of the inp…
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:899-910`
**Plain English:** A '/*' outside a string starts a comment that ends at the first '*/' whose '*' comes after the opening '/*'. The opening '*' can never also close the comment, so '/*/' does not count as a closed comment. If the comment is never closed, everything to the end of the input is removed.
**Specification:**
  Given The input `/*/true**/false`
  When  The sanitizer reaches the leading '/' at structural level
  Then  The comment runs from '/*' through the first valid '*/' (the one after `true*`), so `true` is discarded and the output is `false`. Likewise, `false/* comment */` becomes `false`, and both `false/* comment *` and `false/* comment ` become `false` (unterminated: removed to the end of the input).
**Parameters:** The earliest possible closing '/' is at offset start+3, so `/**/` is the shortest closed comment. If fewer than 4 characters remain from start (start + 3 >= n), the comment is unterminated and end = n. Block comments do not nest: the first '*/' closes the comment.
**Edge cases handled:** '/*/' is not self-closing because the search for '/' starts at start+3; Input ending in '/*' or '/*x' is treated as unterminated, so the rest of the input is dropped; Nested '/* /* */ */': the first '*/' closes the comment, and the trailing '*/' is then processed as a lone '*' (elided as junk) followed by a '/' (a lone slash, also elided); Removing a comment between two values in an array, as in '[1/**/2]', gives '[1,2]' because the state machine inserts a comma. At the top level, '1/**/2' triggers the unbracketed-comma rule, the rest…
**Confidence:** High

### RULE-048: Quoted string boundaries: single or double quotes, backslash-parity termination, auto-close
**Category:** Validation
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:950-963`
**Plain English:** A string may open with ' or "; it ends at the next matching quote preceded by an even number of backslashes, and if no such quote exists the string runs to end of input and a closing quote is added.
**Specification:**
  Given Inputs ['a\\'b'] and ["abc
  When  the string literal is scanned
  Then  the first string ends at the quote after two backslashes (even count) and the second is closed automatically, giving ["abc"]
**Parameters:** Termination test: (number of consecutive preceding backslashes) & 1 == 0
**Edge cases handled:** Single-quoted strings are re-quoted with double quotes by sanitizeString (lines 361-366, 816); Strings are key-capable tokens (requireValueState canBeKey=true)
**Confidence:** High

### RULE-049: Reserved-keyword table integrity (length-indexed, strictly sorted)
**Category:** Validation
**Priority:** P1
**Source:** `src/test/java/com/google/json/EvalMinifierTest.java:104-117`
**Plain English:** Each row of the reserved-word table may only contain words whose length equals the row index, and each row must be in strictly ascending order with no duplicates, so the binary-search lookup used during name generation is reliable.
**Specification:**
  Given RESERVED_KEYWORDS row 4 = {case, else, enum, eval, from, null, this, true, void, with}
  When  The table integrity test runs
  Then  Every word has length 4, each word sorts strictly after the previous one, and Arrays.binarySearch finds each word. Adding an out-of-order or wrong-length keyword fails the build
**Parameters:** Table rows 0..10 (EvalMinifier.java:291-303); ordering = String.compareTo strictly < 0
**Edge cases handled:** Rows 0 and 1 are empty, so no 1-character name is ever reserved
**Confidence:** High

### RULE-050: A slash that does not start a comment is discarded as junk
**Category:** Validation
**Priority:** P2
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:883-886`
**Plain English:** A '/' outside a string that is not followed by '/' or '*', or that is the last character of the input, is not valid JSON, so it is removed by itself and scanning continues at the next character.
**Specification:**
  Given The input `[1/2]`
  When  The sanitizer reaches the '/' between 1 and 2
  Then  endOfComment returns start+1, so only the '/' is removed. The 2 then needs a separator, so a comma is inserted and the output is `[1,2]`. The division expression is not evaluated.
**Parameters:** Default result: end = start + 1 (lines 885, 913). The switch covers only '/' and '*' as the second character.
**Edge cases handled:** Trailing '/' at end of input (start + 1 >= n): only that character is removed; A '/' inside a quoted string never reaches this rule. Slashes inside strings are handled by sanitizeString (e.g. '</' escaping)
**Confidence:** High

## Lifecycle

### RULE-051: End-of-input completion and whitespace-only input
**Category:** Lifecycle
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:564-601`
**Plain English:** Input that is empty or only whitespace becomes null. At the end of the input, any half-finished entry is completed, and every open bracket is closed in reverse order. Input that needed no changes is returned unchanged with no copy.
**Specification:**
  Given Inputs '   ' ; {"a":[1, ; {"a" ; {"a":
  When  The end of input is reached
  Then  Outputs are '   null' ; {"a":[1]} ; {"a":null} ; {"a":null}
**Parameters:** Missing-value fillers: AFTER_KEY → ":null"; BEFORE_VALUE → "null"; BEFORE_ELEMENT/BEFORE_KEY → remove trailing comma. Closers come from the isMap stack: '}' for a map, ']' for an array.
**Edge cases handled:** Input with only comments or parentheses ends in START_ARRAY at depth 0, so null is appended; Fast path: if no edits were made and no brackets are open, the original string is returned as is (lines 576-577 and 1414-1421)
**Confidence:** High

### RULE-052: End-of-input completion: finish dangling key/value and close all open brackets
**Category:** Lifecycle
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:576-601`
**Plain English:** When input ends mid-structure, a dangling key gets ':null', a dangling colon gets 'null', a trailing comma is removed, and every still-open array/object is closed in reverse order with the right bracket type; if no repair was ever needed the original input is returned untouched.
**Specification:**
  Given Inputs [1,2 , {"a":[1,{"b" , {"a":, [1,
  When  end of input is reached
  Then  outputs are [1,2], {"a":[1,{"b":null}]}, {"a":null}, [1]
**Parameters:** none
**Edge cases handled:** If sanitizedJson was never created and no brackets are open, toString returns the original string unchanged (lines 1419-1421); Closing order uses isMap stack from innermost outwards
**Confidence:** High

### RULE-053: Missing punctuation is inserted between adjacent tokens
**Category:** Lifecycle
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:819-850`
**Also cited:** src/main/java/com/google/json/JsonSanitizer.java:819-849
**Plain English:** When a value appears where JSON needs punctuation first, the sanitizer adds it. It inserts a comma between adjacent values, a colon between a key and its value, and an empty-string key "" when an object or array shows up where a map key should be.
**Specification:**
  Given Inputs [1 2] ; {"a" 1} ; {[1]} ; {"a":1 "b":2}
  When  A value token (string, number, keyword, bare word or open bracket) is processed
  Then  Outputs are [1,2] ; {"a":1} ; {"":[1]} ; {"a":1,"b":2}
**Parameters:** Inserted strings: "," ; ":" ; "\"\":" ; ",\"\":". Strings and bare words may be keys (canBeKey = true), but brackets may not.
**Edge cases handled:** At depth 0 in AFTER_ELEMENT, a second value triggers top-level truncation instead of an inserted comma; Numbers used as keys are canonicalized and quoted ({.5e-1:1} gives {"0.05":1}, per lines 525-541)
**Confidence:** High

### RULE-054: Copy-on-write output: input that needs no fixes is returned unchanged
**Category:** Lifecycle
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:860-866`
**Also cited:** src/main/java/com/google/json/JsonSanitizer.java:167-176 (identity return at line 175); test: src/test/java/com/google/json/JsonSanitizerTest.java:405-408; src/main/java/com/google/json/JsonSanitizer.java:1414-1421
**Plain English:** The sanitizer only creates an output buffer when it makes its first edit. If the input needs no edits, the caller gets back the exact original text (the same object) instead of a copy.
**Specification:**
  Given A sanitizer run that starts with no output buffer (sanitizedJson = null, cleaned = 0, reset at lines 336-337)
  When  The first edit (insert, replace or elide) happens
  Then  A buffer with capacity input length + 16 is created, the input prefix [0, start) is copied into it, and the run is 'dirty' from then on. sanitize(CharSequence) (line 175), toCharSequence() (line 1415) and toString() (line 1420) then return the buffer. If no edit ever happens, they return the original input as-is.
  And   At end of input (lines 576-582) the run also counts as dirty, and a buffer is created if needed (capacity n + bracketDepth), if cleaned != 0, if the buffer is non-empty, or if brackets are still open (bracketDepth != 0). That way, closing brackets and null placeholders still get added when no mid-stream edit happened.
**Parameters:** Initial buffer capacity = jsonish.length() + 16 (line 862). This is a performance hint and has no effect on behavior. End-of-input capacity = n + bracketDepth (line 579). States: CLEAN (sanitizedJson == null) and DIRTY (sanitizedJson != null). The only transition is CLEAN to DIRTY, triggered by the first elide, replace or insert or by the end-of-input check. Every sanitize() call resets to CLEAN.
**Edge cases handled:** Valid, embeddable input such as '{"a":1}': no edit, so the same CharSequence instance is returned (documented contract at lines 164-165).; Null input: the constructor replaces it with the literal "null" (line 327). With no edits, the result is the string "null".; Input '[1' with no mid-stream edits: bracketDepth = 1 at the end, so a buffer is created and the result is '[1]'.; An elision at position 0 that writes nothing (empty buffer, cleaned > 0) still counts as dirty at the end, because of the 'cleaned != 0' check.; Callers of the CharSequence overload may get back a mutable StringBuilder (line 175). Later changes to it are visible through the returned reference.
**Suspected defect:** The Javadoc at lines 147-148 says the content is copied once so that later changes to jsonish do not affect the result. But line 175 returns the caller's original CharSequence when no changes were needed. If the caller passes a StringBuilder and changes it later, the returned value changes too. The documentation and the behavior disagree.
**Confidence:** Medium — Citation was corrected by referee (The behavior is real, but the citation points to the wrong code. The cited lines, JsonSanitizerTest.java:35-41, are a test helper for String input only. It calls sanitize(String, int) and asserts identity (assertSame) whenever the output equals the input. It contains no StringBuilder, and it is a test assertion, not the code that implements the behavior. The rul…

### RULE-055: Fuzz corpus lifecycle: fresh basis versus mutated variant
**Category:** Lifecycle
**Priority:** P1
**Source:** `src/test/java/com/google/json/FuzzyTest.java:146-177`
**Plain English:** The generator keeps a current 'basis' document. When there is none, it creates a new random JSON document and emits it unchanged. Otherwise it emits a random mutation of the basis, and after each emission there is a 1-in-16 chance the basis is thrown away.
**Specification:**
  Given The generator state is NO_BASIS (basis == null)
  When  next() is called
  Then  A new random JSON document becomes the basis and is emitted unmodified (state becomes HAS_BASIS). Later calls emit mutate(basis). After each emission, with probability 1/16, the state goes back to NO_BASIS
**Parameters:** Basis reset probability = 1/16 (rnd.nextInt(16) == 0, line 161); the iterator never ends (hasNext always true), and the caller limits it to 10,000
**Edge cases handled:** The reset check runs even right after a fresh basis is emitted, so a basis may be emitted with zero mutations; On average a basis yields about 16 emissions (1 original plus about 15 mutants); remove() is unsupported and throws UnsupportedOperationException
**Confidence:** High

## Policy

### RULE-056: Sanitize before minifying, with optional custom nesting limit
**Category:** Policy
**Priority:** P0
**Source:** `src/main/java/com/google/json/EvalMinifier.java:50-66`
**Plain English:** Any JSON-like input is first passed through the JSON sanitizer, using the default or a caller-supplied maximum nesting depth, and only the sanitized text is considered for constant pooling. The result can therefore be passed to JavaScript eval without free variables or side effects.
**Specification:**
  Given A caller supplies JSON-like text such as {'a':1} (single quotes, not strict JSON), optionally with a maximum nesting depth such as 64
  When  minify(jsonish) or minify(jsonish, maximumNestingDepth) is called
  Then  JsonSanitizer(jsonish[, depth]).sanitize() runs first, producing well-formed JSON such as {"a":1}. The minifier then works only on that sanitized text, and the result is either the sanitized JSON unchanged or a pooled function expression.
**Parameters:** maximumNestingDepth: caller-supplied; the default comes from JsonSanitizer when it is omitted. Values nested too deeply are handled by JsonSanitizer, which per recent commits replaces them with null.
**Edge cases handled:** The output is an expression, not a statement. If it is an object literal, the caller must still wrap it, e.g. eval('(' + s + ')') or eval('0,' + s). The minifier does not do this.; The javadoc claims the output is safe to embed in HTML <script> and XML CDATA. The minifier adds no escaping of its own, so that guarantee rests entirely on JsonSanitizer.
**Confidence:** High

### RULE-057: Neutralize HTML script and XML CDATA breakout sequences in strings
**Category:** Policy
**Priority:** P0
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:662-726`
**Also cited:** src/main/java/com/google/json/JsonSanitizer.java:654-726
**Plain English:** So the output can be placed safely inside an HTML <script> element or an XML CDATA section, the sequences <!--, <script, </script, --> and ]]> are broken up by Unicode-escaping one key character.
**Specification:**
  Given A string value containing "</script>", "<!--", "-->" or "]]>"
  When  The string is sanitized
  Then  '<' becomes \u003c when the next three decoded characters are '!--', or 'scr' (any case), or '/sc' (any case, for the s and c). '>' becomes \u003e when the two decoded characters before it are '--'. The first ']' of ']]>' becomes \u005d. Examples: "</script>" -> "\u003c/script>"; "<!--x" -> "\u003c!--x"; "a-->" -> "a--\u003e"; "]]>" -> "\u005d]>"
**Parameters:** Lookahead patterns after '<': "!--", "scr" (case-insensitive via c|32), "/sc" (s and c case-insensitive). Lookbehind before '>': "--". Lookahead after ']': "]>". Replacement escapes: \u003c, \u003e, \u005d. Case-insensitivity (c|32) applies to the letters only; ! - / ] > are compared exactly. Escape-aware decoding uses unescapedChar and unescapedCharRev (lines 1439-1532), so escaped forms such as <\x73cript or \x2d\x2d> are also caught.
**Edge cases handled:** The match is on a prefix ("<scr" or "</sc"), so harmless text such as "<scroll" or "</scope" is also escaped. This is conservative and acceptable.; '<' is checked only when at least three more characters remain in the range (i+3 < end). ']' is checked only when i+2 < end.; '-->' is checked only when '>' has at least two characters before it in the range (i-2 >= start). An escaped '>' (an odd run of backslashes) is stepped over before looking back.; Look-ahead and look-behind decode \xHH, \uHHHH, octal and single-character escapes, so obfuscated sequences are still caught; '<![CDATA[' is NOT escaped: after '<' the lookahead is '![c', which matches none of the patterns. JsonSanitizerTest.java:343 asserts the output never contains '<![cdata[', but that holds only because…
**Confidence:** Medium — Citation was corrected by referee (The cited lines are a JUnit test, not the code that does the escaping. JsonSanitizerTest.java:318-345 (testDisallowedSubstrings) runs 13 fuzzer-found inputs through JsonSanitizer.sanitize. It lower-cases each output with Locale.ROOT (line 337) and asserts that none of the six substrings appear (lines 338-343). That checks the property for those inputs only. It d…

### RULE-058: Reader and byte-stream entry points match the string sanitizer
**Category:** Policy
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:190-251`
**Also cited:** (with DEFAULT_NESTING_DEPTH at :112). The tests at src/test/java/com/google/json/JsonSanitizerTest.java:414-442 serve as supporting evidence
**Plain English:** Sanitizing from a character Reader or a UTF-8 byte stream gives the same output as sanitizing the same text as a string. The output is appended after any existing content in the destination, and byte streams are UTF-8 on both input and output.
**Specification:**
  Given A destination already holding prefix: and a Reader supplying {a:'b'}
  When  The Reader is sanitized into the destination
  Then  The destination holds prefix:{"a":"b"}
**Parameters:** Read buffer 8192 chars (JsonSanitizer.java:206); stream encoding UTF-8 both ways; default depth 64
**Edge cases handled:** About 200,000 characters of input (more than one read buffer) give the same result as String sanitize (lines 421-427); The depth limit applies: Reader [[[ at depth 2 gives [[null]] (lines 429-431); UTF-8 input ['café','<raw U+2028>'] gives ["café","\u2028"]. The non-ASCII é is kept raw and U+2028 is escaped (lines 438-441); The whole input is read into memory first (JsonSanitizer.java javadoc), so callers must limit the size of untrusted input
**Confidence:** Medium — Citation was corrected by referee (The behavior is real, but the cited lines are tests. They check the rule and do not implement it. JsonSanitizerTest.java:414-432 (testSanitizeReader) asserts three things. A StringBuilder holding "prefix:" ends up as prefix:{"a":"b"} after sanitize(new StringReader("{a:'b'}"), out). Reader input bigger than the read buffer (50000 repetitions of 'x',) gives the s…

### RULE-059: Stream input is fully buffered with no size limit; caller-owned streams are not closed
**Category:** Policy
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:203-214`
**Plain English:** The Reader/Appendable API reads the whole input into memory before sanitizing, with no maximum size. Limiting size is left to the caller, and the caller's streams are never closed.
**Specification:**
  Given A Reader that supplies 50 MB of JSON-like text
  When  sanitize(Reader, Appendable[, depth]) is called
  Then  All 50 MB is read into memory in 8,192-character chunks, the whole text is sanitized as one document, the result is appended to the output in one call, and neither the Reader nor the Appendable is closed
**Parameters:** Read chunk size = 8192 chars (line 206). There is no maximum input size. The default depth of 64 is applied by the overload at lines 190-192.
**Edge cases handled:** Empty Reader: the input is empty and the output is "null" (whitespace-only rule).; An IOException while reading or writing goes to the caller. Nothing is written to out if the read fails.; The Javadoc (lines 182-184) puts size limits for untrusted input on the caller.
**Suspected defect:** There is no built-in cap on input size, so untrusted streams can use unlimited memory unless the caller limits them. This is documented, but it is a risk to carry into the rewrite.
**Confidence:** High

### RULE-060: Maximum nesting depth is configurable but clamped to 1..4096 (default 64)
**Category:** Policy
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:318-332`
**Also cited:** src/main/java/com/google/json/JsonSanitizer.java:322-323; src/main/java/com/google/json/JsonSanitizer.java:322-323 (clamp in constructor; ceiling constant at line 115; getter at 330-332)
**Plain English:** Callers can choose how deeply arrays and objects may nest. If they don't choose, the limit is 64. Any requested value is forced into the range 1 to 4096.
**Specification:**
  Given A caller asks to sanitize input with maximumNestingDepth = 10000 (or 0, -5, or no value)
  When  The sanitizer is constructed
  Then  The effective limit is 4096 for 10000, 1 for 0 or -5, and 64 when no depth is given. getMaximumNestingDepth() reports the clamped value.
**Parameters:** DEFAULT_NESTING_DEPTH = 64 (line 112); MAXIMUM_NESTING_DEPTH = 4096 (line 115); minimum = 1 (hardcoded at line 323); formula: effective = min(max(1, requested), 4096)
**Edge cases handled:** A requested value of 0 or a negative number silently becomes 1, so the outermost bracket is always allowed; Values above 4096 are capped without warning; A null input string is treated as the literal text "null" (line 327); The depth-tracking array isMap is sized to the clamped limit, so the clamp also bounds memory per sanitizer
**Confidence:** Medium — Citation was corrected by referee (The rule is correct, but the citation points at a test instead of the code that does the clamping. JsonSanitizerTest.java:238-242 is the JUnit test testMaximumNestingLevelAssignment. It only checks the result: new JsonSanitizer("", Integer.MIN_VALUE).getMaximumNestingDepth() returns 1, and the same call with Integer.MAX_VALUE returns JsonSanitizer.MAXIMUM_NESTIN…

### RULE-061: Non-JSON cruft removal: grouping parentheses, JS comments and unrecognised characters
**Category:** Policy
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:358-375`
**Also cited:** (whitespace, parens), 468-475 (JS comments, via endOfComment 883-914), 477-503 (unrecognised character elision)
**Plain English:** Whitespace is passed through, eval-style grouping parentheses are deleted, JavaScript line and block comments are deleted, and any other character that cannot start a token (such as a byte-order mark) is deleted.
**Specification:**
  Given Input ({ /* c */ "a": 1 // tail
})
  When  sanitized
  Then  output is {  "a": 1 } (parens and both comments removed, whitespace kept)
**Parameters:** Whitespace set: tab, LF, CR, space. Line comment ends after CR, LF, U+2028 or U+2029 (endOfComment lines 883-914); block comment ends at the first '*/'
**Edge cases handled:** Comment handling at lines 468-475; unrecognised single characters elided at lines 501-504; Unterminated // or /* comment consumes the rest of input; A lone '/' not followed by '/' or '*' is removed as a single character; Parentheses inside a bare word (e.g. foo(bar)) are not removed; they become part of a quoted string; Input that is only cruft, e.g. () or //x, yields null
**Confidence:** Medium — Citation was corrected by referee (The behaviour is real, but the cited lines 358-375 cover only half of it. Those lines pass tab, LF, CR and space through unchanged (358-359) and delete '(' and ')' with elide(i, i+1) (368-375). The comment removal is in the '/' case at 468-475, which calls endOfComment and elides up to the returned end. The deletion of characters that cannot start a token, such…

### RULE-062: Empty or null input becomes the JSON value null
**Category:** Policy
**Priority:** P1
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:564-568`
**Plain English:** If the input contains no tokens (empty, whitespace only, or only removed cruft) the output is the literal null; a Java null input is treated as the text "null".
**Specification:**
  Given Input "   " (spaces only), or Java null
  When  sanitized
  Then  output is "   null" (whitespace preserved, null appended) or "null"
**Parameters:** Null input substitution at line 327
**Edge cases handled:** Input consisting only of comments or parentheses also yields null
**Confidence:** High

### RULE-063: Fuzz run budget and reproducible seed
**Category:** Policy
**Priority:** P1
**Source:** `src/test/java/com/google/json/FuzzyTest.java:39-59`
**Also cited:** src/test/java/com/google/json/FuzzyTest.java:39-59
**Plain English:** Each fuzz session checks 10,000 generated inputs from a random seed. The seed is logged in hex so a failure can be replayed exactly by passing it back as a system property.
**Specification:**
  Given No system property fuzz.seed is set
  When  The fuzz test starts
  Then  It picks a random 64-bit seed, prints `Fuzzing with -Dfuzz.seed=<hex>` to stderr, and runs 10,000 iterations. If -Dfuzz.seed=1a2b is supplied, the seed 0x1a2b is used and the same input sequence is reproduced.
**Parameters:** nRuns = 10000 (line 39); seed property name `fuzz.seed`, parsed as base-16 long (line 46); default seed = new Random().nextLong() (line 50); loop stops when --nRuns <= 0 (line 99)
**Edge cases handled:** A non-hex fuzz.seed value causes NumberFormatException before any fuzzing runs.; Seeds are not cryptographically strong. That is fine here because they are only used for coverage.
**Suspected defect:** Long.toHexString prints negative seeds as unsigned values (e.g. 'ffffffffffffffff'), and Long.parseLong(seedString, 16) rejects those, so about half of logged seeds fail with NumberFormatException when replayed. Long.parseUnsignedLong would round-trip.
**Confidence:** Medium — Should replaying a negative (top-bit-set) seed work? If yes, should the parser switch to Long.parseUnsignedLong?

### RULE-064: Escaped characters use lowercase four-digit hex
**Category:** Policy
**Priority:** P2
**Source:** `src/main/java/com/google/json/JsonSanitizer.java:1423-1426`
**Plain English:** When a character inside a string has to be escaped, it is written as \u followed by exactly four lowercase hex digits.
**Specification:**
  Given A string containing the character U+2028 or a lone surrogate U+D800
  When  The string is sanitized (lines 809-812)
  Then  The output contains \u2028 or \ud800 with lowercase hex
**Parameters:** HEX_DIGITS = 0-9, a-f (lowercase); width = 4
**Edge cases handled:** A valid surrogate pair is kept as is and not escaped
**Confidence:** High

## Rules requiring SME confirmation

- **RULE-002** (Medium): Citation was corrected by referee (The rule is accurate, but the cited lines don't implement it. EvalMinifierTest.java:36-56 is a JUnit golden-output test. It shows the behavior from outside: 13 nulls stay unchanged at lines 43-45, and 26 nulls become the '(function(a){return[a,...]}(null))' wrapper at lines 46-50. It contains no calculation logic. The executable rule is in EvalMinifier.java, and…
- **RULE-006** (Medium): Citation was corrected by referee (The behavior is real, but the cited lines are the wrong place to point. src/test/java/com/google/json/JsonSanitizerTest.java:91-110 holds only test assertions. They check the spec examples: line 99 ("6.0221412927e" -> "6.0221412927e0"), line 104 ("1." -> "1.0"), line 105 (".5" -> "0.5") and line 108 ("+.5e2" -> "0.5e2"). Tests show the behavior but don't impleme…
- **RULE-007** (Medium): Citation was corrected by referee (The behavior is real and the rule describes it accurately, but the cited lines only hold part of it. Lines 1428-1432 contain just the constant table DIGITS_BY_BASE_THAT_FIT_IN_63B. Its values match the rule's Parameters exactly: index 2=63, 3=39, 4=31, 5=27, 6=24, 7=22, 8=21, 9=19, 10=18, 11=18, 12=17, 13=17, 14=16, 15=16, 16=15. Indices 0 and 1 are -1. The re-e…
- **RULE-012** (Medium): For numeric keys whose exponent overflows a double, should the sanitizer keep the original digits (current behavior) or produce the JavaScript-equivalent key "Infinity"? Also, was the duplicate assertion at lines 129-130 meant to test a different input?
- **RULE-020** (Medium): Is the missing comma plus trailing comma in generated arrays and objects meant to exercise the sanitizer's repair logic, or was 'i != 0' intended so that the basis is valid JSON before mutation? / Is the comma-placement rule 'i != 1' (trailing comma, missing separator before the last element) intended to exercise malformed-list repair, or is it a bug where valid lists were meant?
- **RULE-022** (Medium): Is choosing the delimiter per character (so an unescaped '"' can break out of a double-quoted string) intentional fuzz pressure, or was the delimiter meant to be chosen once per string and used for the surrounding quotes?
- **RULE-024** (Medium): Citation was corrected by referee (The rule is accurate, but the citation points at the unit test that checks it, not the code that does it. JsonSanitizerTest.java:194-230 (testExcessiveNestingIsReplacedWithNull) asserts the stated cases. Line 213: with depth 2, "[[[1]],2]" becomes "[[null],2]". Line 210: input exactly at the limit (d '[' then d ']') comes back unchanged. Lines 214-215 cover the…
- **RULE-025** (Medium): Citation was corrected by referee (The behavior is real, but the cited lines don't implement it. JsonSanitizer.java:1389-1393 only declares the private static final class UnbracketedComma extends Exception. It is a marker type with a serialVersionUID and no logic ("No members. Used for nominal type."), and the singleton with no stack trace is at 1408-1412. The rule is enforced elsewhere. At :428,…
- **RULE-027** (Medium): Citation was corrected by referee (The behavior is real, but the citation points at a test, not the code that does the work. EvalMinifierTest.java:75-102 (testNameGenerator) only checks results. It confirms the first three names are 'a','b','c' (lines 78-80). After 30 skipped draws the 34th is 'H' (lines 81-82): a-z are names 1-26 and A-H are 27-34. In the next 1000 draws 'as', 'do', 'if' and 'in…
- **RULE-028** (Medium): Citation was corrected by referee (The behaviour is real and the rule describes it correctly, but the cited lines are a unit test, not the code that does the work. EvalMinifierTest.java:58-72 only asserts the expected output. Lines 58-64 check that a long string used as a key stays literal while its value occurrences become 'a'. Lines 66-72 check that 123456.789 and 123456e-3 stay verbatim while…
- **RULE-029** (Medium): P0 panel split on whether this is critical to the system's core purpose or a costly-if-wrong rule (Faithfulness: the rule matches the code. In /Users/jmanico/Dropbox/github/json-sanitizer/legacy/json-sanitizer/src/main/java/com/google/json/EvalMinifier.java at lines 97-103, the code skips only tab, LF, CR and space after a token. At lines 109-110, a token joins the pool only if the next non-white…
- **RULE-034** (Medium): Should a leading UTF-8 byte-order mark be stripped or rejected in the byte-stream API, and is UTF-8 always the required encoding, or must other charsets (e.g. UTF-16) be supported?
- **RULE-036** (Medium): Citation was corrected by referee (The behavior is real, but the citation points at the wrong place. JsonSanitizerTest.java:47-90 is a JUnit test, not the code that does the work. It also misses parts of the rule. Lines 51-52 show null and "" becoming "null". Line 59 shows "false,true" becoming "false". Lines 84-90 show comments being removed. But nothing in lines 47-90 tests wrapping parentheses…
- **RULE-038** (Medium): Citation was corrected by referee (The behaviour is real, but the citation points at the tests, not at the code that does the work. JsonSanitizerTest.java:73-83 is a list of assertSanitized calls. They check the expected repairs: [1,2,3,] becomes [1,2,3]; [1,,3,] becomes [1,null,3]; [1 2 3] becomes [1 ,2 ,3]; a trailing comma in an object is dropped; {"foo","bar"} becomes {"foo":"bar"}; an unquot…
- **RULE-040** (Medium): Citation was corrected by referee (The behaviour is real, but the citation points at a test, not the code that does it. JsonSanitizerTest.java:168-180 (testIssue4) only checks expected outputs: dev -> "dev", eval -> "eval", fasle -> "fasle", FALSE -> "FALSE", and a few backslash and slash cases. The test does not cover single-quoted input at all. The behaviour itself lives in src/main/java/com/go…
- **RULE-042** (Medium): Citation was corrected by referee (The behavior is real, but the citation points at unit tests, which check the behavior rather than implement it. JsonSanitizerTest.java:134-142 asserts that U+0000, U+0008 and U+001F inside a single-quoted string become "\u0000\u0008\u001f" (line 135). It also asserts that lone surrogates are escaped while a valid pair is kept (136-137), that U+FFFD is kept while…
- **RULE-044** (Medium): Citation was corrected by referee (The behavior is real, but the cited lines are where it is tested, not where it is implemented. JsonSanitizerTest.java:374-402 holds three JUnit tests: testDashDashGtEscaped, testDashDashGtUEscaped and testEscHtmlCommentClose. Each one sanitizes its input, sanitizes the result again, and asserts both outputs are the same. They cover a dash written as \-, a dash w…
- **RULE-054** (Medium): Citation was corrected by referee (The behavior is real, but the citation points to the wrong code. The cited lines, JsonSanitizerTest.java:35-41, are a test helper for String input only. It calls sanitize(String, int) and asserts identity (assertSame) whenever the output equals the input. It contains no StringBuilder, and it is a test assertion, not the code that implements the behavior. The rul…
- **RULE-057** (Medium): Citation was corrected by referee (The cited lines are a JUnit test, not the code that does the escaping. JsonSanitizerTest.java:318-345 (testDisallowedSubstrings) runs 13 fuzzer-found inputs through JsonSanitizer.sanitize. It lower-cases each output with Locale.ROOT (line 337) and asserts that none of the six substrings appear (lines 338-343). That checks the property for those inputs only. It d…
- **RULE-058** (Medium): Citation was corrected by referee (The behavior is real, but the cited lines are tests. They check the rule and do not implement it. JsonSanitizerTest.java:414-432 (testSanitizeReader) asserts three things. A StringBuilder holding "prefix:" ends up as prefix:{"a":"b"} after sanitize(new StringReader("{a:'b'}"), out). Reader input bigger than the read buffer (50000 repetitions of 'x',) gives the s…
- **RULE-060** (Medium): Citation was corrected by referee (The rule is correct, but the citation points at a test instead of the code that does the clamping. JsonSanitizerTest.java:238-242 is the JUnit test testMaximumNestingLevelAssignment. It only checks the result: new JsonSanitizer("", Integer.MIN_VALUE).getMaximumNestingDepth() returns 1, and the same call with Integer.MAX_VALUE returns JsonSanitizer.MAXIMUM_NESTIN…
- **RULE-061** (Medium): Citation was corrected by referee (The behaviour is real, but the cited lines 358-375 cover only half of it. Those lines pass tab, LF, CR and space through unchanged (358-359) and delete '(' and ')' with elide(i, i+1) (368-375). The comment removal is in the '/' case at 468-475, which calls endOfComment and elides up to the returned end. The deletion of characters that cannot start a token, such…
- **RULE-063** (Medium): Should replaying a negative (top-bit-set) seed work? If yes, should the parser switch to Long.parseUnsignedLong?

## Rules folded into another

These rules described the same behavior as another rule in a different place, so they were merged into it (the kept rule lists their locations under "Also cited"):

- Brackets inside comments and strings are ignored when finding the end of an over-deep value (src/main/java/com/google/json/JsonSanitizer.java:922-943) into Finding the end of an over-deep value (bracket matching that ignores strings and comments)
- Octal/hex integer re-encoding to decimal and overflow-safe digit limits (src/main/java/com/google/json/JsonSanitizer.java:1030-1088 (re-encoding logic), with the parameter…) into Hexadecimal and legacy-octal integer literals are converted to decimal
- Numeric property names are quoted and converted to JavaScript's canonical string form (src/main/java/com/google/json/JsonSanitizer.java:1130-1206) into Numeric object keys are canonicalized to JavaScript number-to-string form
- Random JSON corpus generation shape (src/test/java/com/google/json/FuzzyTest.java:179-260) into Random JSON document shape (depth, breadth, value kinds, whitespace)
- Mutation policy for fuzz variants (src/test/java/com/google/json/FuzzyTest.java:325-377) into Mutation algorithm: 1-16 sorted insert/replace/remove edits
- Short variable-name sequence avoiding reserved words (src/main/java/com/google/json/EvalMinifier.java:291-345) into Generated short variable names skip JavaScript reserved words
- Close-bracket repair: stray, mismatched and incomplete closers (src/main/java/com/google/json/JsonSanitizer.java:397-426) into Close-bracket repair: fill missing values, drop trailing commas, fix mismatched bracket type
- Already-safe input is returned unchanged without copying (src/main/java/com/google/json/JsonSanitizer.java:167-176 (identity return at line 175); test: src/t…) into Copy-on-write output: input that needs no fixes is returned unchanged
- Already-valid input is returned unchanged (no-copy passthrough) (src/main/java/com/google/json/JsonSanitizer.java:1414-1421) into Copy-on-write output: input that needs no fixes is returned unchanged
- Grammar state machine: auto-insert missing colons, commas and empty keys before a value (src/main/java/com/google/json/JsonSanitizer.java:819-849) into Missing punctuation is inserted between adjacent tokens
- Fuzz run reproducibility via logged seed (src/test/java/com/google/json/FuzzyTest.java:39-59) into Fuzz run budget and reproducible seed
- Effective maximum nesting depth (default 64, clamped to 1..4096) (src/main/java/com/google/json/JsonSanitizer.java:322-323) into Maximum nesting depth is configurable but clamped to 1..4096 (default 64)
- Maximum nesting depth parameter is clamped to 1..4096 (src/main/java/com/google/json/JsonSanitizer.java:322-323 (clamp in constructor; ceiling constant at…) into Maximum nesting depth is configurable but clamped to 1..4096 (default 64)
- Nesting depth limit replaces over-deep values with null (src/main/java/com/google/json/JsonSanitizer.java:377-395 (depth check and null replacement at 382-3…) into Over-deep arrays/objects are replaced with null instead of failing
- Nesting depth limit: configurable cap, over-deep values become null (src/main/java/com/google/json/JsonSanitizer.java:377-395) into Over-deep arrays/objects are replaced with null instead of failing
- Output must never contain HTML/XML breakout sequences (src/main/java/com/google/json/JsonSanitizer.java:654-726) into Neutralize HTML script and XML CDATA breakout sequences in strings
- Only the first top-level value is kept; trailing top-level content is discarded (src/main/java/com/google/json/JsonSanitizer.java:428 (throw on top-level comma), :845 (throw on sec…) into Output is exactly one top-level value; trailing extra content is discarded
- Global minification threshold (src/main/java/com/google/json/EvalMinifier.java:147-149) into Eval-minifier pooling profitability threshold
