# Data Objects — json-sanitizer

## JsonSanitizer: input and configuration (instance fields)
Source: `legacy/json-sanitizer/src/main/java/com/google/json/JsonSanitizer.java:109`

| Field | Type | Note |
|---|---|---|
| jsonish | String (final) | The raw JSON-like input. A null input is replaced with the literal "null" in the constructor (JsonSanitizer.java:327). Every scanner reads it: endOfQuotedStrin… |
| maximumNestingDepth | int (final) | Clamped to 1..MAXIMUM_NESTING_DEPTH by Math.min(Math.max(1, d), 4096) at :323. Also sets the length of isMap (:380). |
| DEFAULT_NESTING_DEPTH | public static final int = 64 | :112. Used by the overloads that take no depth argument (:125, :155, :191, :231). |
| MAXIMUM_NESTING_DEPTH | public static final int = 4096 | :115 |
| SUPER_VERBOSE_AND_SLOW_LOGGING | private static final boolean = false | :316. Compile-time debug switch. When true it prints the raw input to System.err (:325, :354). This is dead code today, but it would leak input if anyone turne… |

Used by: Missing (null) input is treated as JSON null, Empty or null input becomes the JSON value null, Maximum nesting depth is configurable but clamped to 1..4096 (default 64), Over-deep arrays/objects are replaced with null instead of failing, Finding the end of an over-deep value (bracket matching that ignores strings an…, Sanitize before minifying, with optional custom nesting limit, Quoted string boundaries: single or double quotes, backslash-parity termination…, Bare token classification: number, keyword, or unquoted string, Non-JSON cruft removal: grouping parentheses, JS comments and unrecognised char…, JS line comments are stripped up to and including the line terminator, JS block comments are stripped up to the first closing '*/'; an unterminated bl…, A slash that does not start a comment is discarded as junk, Whole-input normalization: empty input, trailing content, comments and grouping…, Escape-aware forward decoding for embedding look-ahead, Backward decoding to detect disguised '-->' before a '>', Backslash-run parity decides whether the trigger character is escaped, Reader and byte-stream entry points match the string sanitizer

## JsonSanitizer: bracket stack (instance fields)
Source: `legacy/json-sanitizer/src/main/java/com/google/json/JsonSanitizer.java:297`

| Field | Type | Note |
|---|---|---|
| bracketDepth | int | Number of brackets that are open. Reset to 0 at :336. Incremented at :393 and decremented at :419 and :599. A close bracket or comma at depth 0 ends the top-le… |
| isMap | boolean[] (lazily allocated, length = maximumNestingDepth) | :302, allocated at :380. isMap[i] is true when open bracket i was '{'. The sanitizer uses it to rewrite a mismatched close bracket (:420-423) and to emit the m… |

Used by: Close-bracket repair: fill missing values, drop trailing commas, fix mismatched…, Output is exactly one top-level value; trailing extra content is discarded, Separator repair: array elisions become null, stray commas and colons are dropp…, Missing punctuation is inserted between adjacent tokens, Maximum nesting depth is configurable but clamped to 1..4096 (default 64), Over-deep arrays/objects are replaced with null instead of failing, End-of-input completion: finish dangling key/value and close all open brackets, End-of-input completion and whitespace-only input, Empty or null input becomes the JSON value null, Structural repair of malformed arrays and objects

## JsonSanitizer: copy-on-write output buffer (instance fields)
Source: `legacy/json-sanitizer/src/main/java/com/google/json/JsonSanitizer.java:309`

| Field | Type | Note |
|---|---|---|
| sanitizedJson | StringBuilder (nullable) | Stays null until the first edit. While it is null, toString() and toCharSequence() return the original jsonish (:1414-1421), so clean input is returned without… |
| cleaned | int | Length of the prefix of jsonish that has already been copied into sanitizedJson (:314). elide() appends jsonish[cleaned:start] and then advances cleaned to end… |

Used by: Edit splice: copy the untouched input up to the edit, swap in the replacement,…, Copy-on-write output: input that needs no fixes is returned unchanged, Trailing comma removal before a close bracket or end of input, End-of-input completion: finish dangling key/value and close all open brackets, Close-bracket repair: fill missing values, drop trailing commas, fix mismatched…, Separator repair: array elisions become null, stray commas and colons are dropp…, Missing punctuation is inserted between adjacent tokens, Unquoted object keys are quoted; numeric keys canonicalised to JS string form, Bare words and single-quoted text become double-quoted JSON strings, String literal delimiter normalization and termination, Escape JavaScript line terminators and tab inside strings, Neutralize HTML script and XML CDATA breakout sequences in strings, Normalize JavaScript escape sequences to valid JSON escapes, Convert JavaScript octal escapes to JSON unicode escapes, Escape control characters, lone surrogates and non-characters that XML disallows, Character escaping and elision for XML and JavaScript safety, Number literal normalization to JSON grammar, Sanitizer output must be idempotent (sanitizing twice changes nothing), Sanitize before minifying, with optional custom nesting limit, Reader and byte-stream entry points match the string sanitizer

## State (parser state-machine enum)
Source: `legacy/json-sanitizer/src/main/java/com/google/json/JsonSanitizer.java:257`

| Field | Type | Note |
|---|---|---|
| START_ARRAY | enum constant | Also the initial state (:339). If the scan ends in START_ARRAY at depth 0, no token was seen and the sanitizer outputs null (:564-567). |
| BEFORE_ELEMENT | enum constant |  |
| AFTER_ELEMENT | enum constant | A second value while in this state at depth 0 throws UNBRACKETED_COMMA (:845). |
| START_MAP | enum constant |  |
| BEFORE_KEY | enum constant |  |
| AFTER_KEY | enum constant | When a bare token arrives in this state it is quoted as a property name (:525). |
| BEFORE_VALUE | enum constant |  |
| AFTER_VALUE | enum constant |  |

Used by: Close-bracket repair: fill missing values, drop trailing commas, fix mismatched…, Separator repair: array elisions become null, stray commas and colons are dropp…, Missing punctuation is inserted between adjacent tokens, Unquoted object keys are quoted; numeric keys canonicalised to JS string form, Bare token classification: number, keyword, or unquoted string, Bare words and single-quoted text become double-quoted JSON strings, Trailing comma removal before a close bracket or end of input, End-of-input completion: finish dangling key/value and close all open brackets, End-of-input completion and whitespace-only input, Empty or null input becomes the JSON value null, Output is exactly one top-level value; trailing extra content is discarded, Structural repair of malformed arrays and objects

## UnbracketedComma (control-flow exception, singleton)
Source: `legacy/json-sanitizer/src/main/java/com/google/json/JsonSanitizer.java:1390`

| Field | Type | Note |
|---|---|---|
| serialVersionUID | private static final long | The class has no members. It exists only as a nominal type. |
| UNBRACKETED_COMMA | private static final UnbracketedComma | Shared instance with an empty stack trace (:1408-1412). It is thrown at :428 and :845 and caught at :558, where the catch elides everything from i to the end o… |

Used by: Output is exactly one top-level value; trailing extra content is discarded, Whole-input normalization: empty input, trailing content, comments and grouping…, Separator repair: array elisions become null, stray commas and colons are dropp…

## Packed escape-decode result (int return of unescapedChar / unescapedCharRev)
Source: `legacy/json-sanitizer/src/main/java/com/google/json/JsonSanitizer.java:1439`

| Field | Type | Note |
|---|---|---|
| low 16 bits | char | The decoded code unit. It comes from a literal, a \b \f \n \r \t \v escape, \xHH, \uHHHH, or a 1-3 digit octal escape (:1450-1498). |
| high bits (>>> 16) | int | How many source characters the escape or literal consumed: 1 for a literal, 2 for a simple escape, 4 for \x, 6 for \u, and 2-4 for octal. A value of 0 means pa… |
| runSlashPreceding result | int | :1534. Length of the backslash run that ends at pos. Its parity decides whether '>' is escaped (:698). |

Used by: Escape-aware forward decoding for embedding look-ahead, Backward decoding to detect disguised '-->' before a '>', Legacy octal escape length and value, Backslash-run parity decides whether the trigger character is escaped, Neutralize HTML script and XML CDATA breakout sequences in strings, Escaped double-dash before '>' is still treated as an HTML comment close

## Number literal decomposition (local record in normalizeNumber / canonicalizeNumber)
Source: `legacy/json-sanitizer/src/main/java/com/google/json/JsonSanitizer.java:1163`

| Field | Type | Note |
|---|---|---|
| intStart, intEnd | int | Integer-part span in sanitizedJson, after any leading '-' (:1164-1168). |
| fractionStart, fractionEnd | int | :1169-1177 |
| expStart, expEnd | int | Exponent span, skipping a leading '+' (:1178-1185). Parsing it with Integer.parseInt can overflow. On overflow canonicalization is abandoned (:1198-1205) and t… |
| n | int | Decimal exponent as defined in ES5 9.8.1 (:1217). |
| k | int | Count of significant digits (:1272). The TODO at :1265 ('limit s to 21 digits?') is unresolved, so digits are never rounded. |
| zero, sawDecimal | boolean | When zero is true the output collapses to "0" and any sign is dropped (:1278-1283). |
| probableBase, maxDigVal, firstDigitIndex, reencoded | int / boolean | normalizeNumber locals (:1032-1035) that drive hex and octal re-encoding. If an "octal" literal contains an 8 or 9, the base falls back to 10 (:1076). |

Used by: Leading plus sign is stripped from numbers; minus sign is kept, Hexadecimal and legacy-octal integer literals are converted to decimal, Empty integer, fraction, or exponent digits are filled with zero, Characters after a valid number are dropped, Significant digits and decimal exponent are extracted without rounding, Canonical numeric key layout: plain, decimal, small-fraction, or exponent notat…, Numeric object keys are canonicalized to JavaScript number-to-string form, Unquoted object keys are quoted; numeric keys canonicalised to JS string form, Number literal normalization to JSON grammar

## DIGITS_BY_BASE_THAT_FIT_IN_63B (lookup table)
Source: `legacy/json-sanitizer/src/main/java/com/google/json/JsonSanitizer.java:1428`

| Field | Type | Note |
|---|---|---|
| [base] | int[17] | Maximum number of digits in a given base that fit in a signed long. If a literal has more digits than this, normalizeNumber uses BigInteger instead of Long.par… |

Used by: Hexadecimal and legacy-octal integer literals are converted to decimal, Number literal normalization to JSON grammar

## HEX_DIGITS (lookup table)
Source: `legacy/json-sanitizer/src/main/java/com/google/json/JsonSanitizer.java:1423`

| Field | Type | Note |
|---|---|---|
| [0..15] | char[16] | Lowercase digits '0'-'9' and 'a'-'f'. Used for 4-digit \uXXXX escapes of disallowed characters (:809-812). appendHex (:1382) builds lowercase hex on its own fo… |

Used by: Escaped characters use lowercase four-digit hex, Escape control characters, lone surrogates and non-characters that XML disallows, Character escaping and elision for XML and JavaScript safety, Convert JavaScript octal escapes to JSON unicode escapes

## Sanitize entry-point I/O contract (String / CharSequence / Reader+Appendable / InputStream+OutputSt…
Source: `legacy/json-sanitizer/src/main/java/com/google/json/JsonSanitizer.java:124`

| Field | Type | Note |
|---|---|---|
| jsonish (String or CharSequence) | String / CharSequence (nullable) | A null CharSequence is passed to the String path (:169-171). If no edits are needed, the CharSequence overload returns the caller's own object (:175). |
| in | java.io.Reader / java.io.InputStream | Read to EOF into an unbounded StringBuilder in 8192-char chunks (:205-209). It is never closed. A null 'in' is not checked and would throw NPE (inferred: there… |
| out | Appendable / java.io.OutputStream | Receives toCharSequence() (:213). It is not closed. The OutputStream variant wraps it in an OutputStreamWriter and flushes it (:246-248). |
| UTF_8 | private static final Charset | :251. The InputStreamReader and OutputStreamWriter constructors use their default REPLACE action, so malformed bytes become U+FFFD. |
| maximumNestingDepth | int | Optional argument, defaults to 64. It is passed to the constructor, which clamps it. |

Used by: Missing (null) input is treated as JSON null, Stream input is fully buffered with no size limit; caller-owned streams are not…, Byte streams are read and written as UTF-8; malformed bytes become U+FFFD; outp…, Reader and byte-stream entry points match the string sanitizer, Copy-on-write output: input that needs no fixes is returned unchanged, Maximum nesting depth is configurable but clamped to 1..4096 (default 64)

## EvalMinifier.Token (pooled-constant candidate)
Source: `legacy/json-sanitizer/src/main/java/com/google/json/EvalMinifier.java:241`

| Field | Type | Note |
|---|---|---|
| start | int (final) | Inclusive offset into seq. compareTo sorts tokens by start (:274-276). |
| end | int (final) | Exclusive offset. |
| hashCode | int (final) | 31-based hash of seq[start:end], computed in the constructor (:251-256). |
| seq | CharSequence (@Nonnull, final) | The sanitized JSON text. |
| prev | Token (@Nullable) | Links to the previous occurrence of the same token. The length of this chain is the occurrence count (:131-134). |
| name | String (@Nullable) | The generated short identifier assigned to the chain (:164). |

Used by: Identify candidate constant tokens, Pooling eligibility: exclude object keys and short tokens, Per-constant savings test, Assemble the pooled-constant function expression, Minifier must not substitute property names or partial numeric tokens, Eval-minifier pooling profitability threshold

## EvalMinifier constant pool (pool map + dupes list, locals of minify)
Source: `legacy/json-sanitizer/src/main/java/com/google/json/EvalMinifier.java:71`

| Field | Type | Note |
|---|---|---|
| pool | LinkedHashMap<Token, Token> | Maps each distinct token to its latest occurrence. Insertion order fixes both the parameter order and the argument order (:162, :214). Entries with one occurre… |
| dupes | ArrayList<Token> | :124. All occurrences of profitable tokens, sorted by start offset (:152) for the splice pass. |
| potentialSavings | int | :123. If it is <= BOILERPLATE_COST + SAVINGS_THRESHOLD, the input is returned unchanged (:147-149). |

Used by: Identify candidate constant tokens, Pooling eligibility: exclude object keys and short tokens, Per-constant savings test, Eval-minifier pooling profitability threshold, Assemble the pooled-constant function expression

## EvalMinifier envelope and cost constants
Source: `legacy/json-sanitizer/src/main/java/com/google/json/EvalMinifier.java:224`

| Field | Type | Note |
|---|---|---|
| ENVELOPE_P1..P4 | private static final String | "(function(", "){return", "}(", "))" (:224-227) |
| BOILERPLATE_COST | private static final int | Total length of the four envelope parts (:229-230). |
| MARGINAL_VAR_COST | private static final int = 2 | ",,".length() (:231) |
| SAVINGS_THRESHOLD | private static final int = 32 | Magic number with no rationale given (:232). |

Used by: Per-constant savings test, Eval-minifier pooling profitability threshold, Assemble the pooled-constant function expression

## EvalMinifier.NameGenerator
Source: `legacy/json-sanitizer/src/main/java/com/google/json/EvalMinifier.java:305`

| Field | Type | Note |
|---|---|---|
| sb | StringBuilder (final, initial "a") | Counter over identifier characters a-z, A-Z, _, $, and 0-9 for positions after the first (nextIdentChar :335). Any name found in RESERVED_KEYWORDS is skipped (… |

Used by: Generated short variable names skip JavaScript reserved words, Assemble the pooled-constant function expression

## EvalMinifier.RESERVED_KEYWORDS (length-indexed keyword table)
Source: `legacy/json-sanitizer/src/main/java/com/google/json/EvalMinifier.java:291`

| Field | Type | Note |
|---|---|---|
| [length][] | String[][] (static final, package-private) | Row i holds the reserved words of length i. Each row must stay sorted for Arrays.binarySearch (comment :290). EvalMinifierTest.java:106 checks this. Names long… |

Used by: Generated short variable names skip JavaScript reserved words, Reserved-keyword table integrity (length-indexed, strictly sorted)

## FuzzyStringGenerator (test-only fuzz corpus source)
Source: `legacy/json-sanitizer/src/test/java/com/google/json/FuzzyTest.java:138`

| Field | Type | Note |
|---|---|---|
| rnd | java.util.Random (final) | Seeded from the fuzz.seed system property (hex) or from new Random().nextLong() (FuzzyTest.java:44-51). |
| basis | String (@Nullable, iterator field) | :148. The current fresh document. Each next() has a 1-in-16 chance of resetting it (:161). |
| pending | String (@Nullable, iterator field) | :149. Holds either the fresh basis or a mutated copy of it (:169-175). |
| FLOAT_FORMAT_STRING | static final String[] | %g %G %e %E %f (:189) |
| INT_FORMAT_STRING | static final String[] | %x %X %d (:193). Hex output has no 0x prefix, so these appear as bare tokens or invalid numbers. |

Used by: Fuzz corpus lifecycle: fresh basis versus mutated variant, Random JSON document shape (depth, breadth, value kinds, whitespace), Random numeric literal formats (float and integer), Random string-character generation and escaping, Mutation algorithm: 1-16 sorted insert/replace/remove edits, Fuzz run budget and reproducible seed, Sanitizer output must be idempotent (sanitizing twice changes nothing)

## FuzzyTest run parameters (locals of testSanitizerLikesFuzzyWuzzyInputs)
Source: `legacy/json-sanitizer/src/test/java/com/google/json/FuzzyTest.java:39`

| Field | Type | Note |
|---|---|---|
| nRuns | int = 10000 | Iteration budget (:39, :99). |
| seed | long | Printed as -Dfuzz.seed=<hex> so a failing run can be replayed (:56). |
| sanitized0, sanitized1 | String | Results of the first and second sanitize passes. They must be equal (:62-92). |

Used by: Fuzz run budget and reproducible seed, Sanitizer output must be idempotent (sanitizing twice changes nothing)
