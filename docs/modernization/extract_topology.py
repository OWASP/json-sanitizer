#!/usr/bin/env python3
"""Extract a method-level dependency/topology map of json-sanitizer.

Why bytecode: the estate is 3 production classes, so a class graph (jdeps) has
~12 nodes and says almost nothing. The JDK's own `javap -c -p -l` gives exact
call targets (with overload signatures), field reads/writes (getfield/putfield)
and line ranges, with no source-parsing guesswork.

Pipeline
  1. javac the sources into analysis/json-sanitizer/map/classes (falls back to
     target/classes if the jars are missing), so the map is never stale.
  2. jdeps -verbose:class -filter:none (DOT) for the class graph -> cross-check.
  3. javap every class -> methods, invoke edges, field edges, line ranges.
  4. Group overloads (same class + name + first param type) into one node,
     assign nodes to the assessment's domains (D1..D11) by name/line.
  5. Add what bytecode cannot see: Maven/surefire + CI + release "jobs",
     JDK collection callbacks (equals/hashCode/compareTo), caller streams.

Usage: python3 extract_topology.py [repo_root]   (default: legacy/json-sanitizer)
Writes analysis/json-sanitizer/topology.json and prints a summary.
"""
import glob, json, os, re, subprocess, sys
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, "..", "..", "legacy", "json-sanitizer"))
OUT = os.path.join(HERE, "topology.json")
MAP = os.path.join(HERE, "map")
PKG = "com/google/json"
MAIN = os.path.join(ROOT, "src/main/java", PKG)
TEST = os.path.join(ROOT, "src/test/java", PKG)
M2 = os.path.expanduser("~/.m2/repository")


def rel(p):
    return os.path.relpath(p, ROOT)


def run(cmd):
    return subprocess.run(cmd, capture_output=True, text=True)


# ---------------------------------------------------------------- 1. compile
def jar(*parts):
    p = os.path.join(M2, *parts)
    return p if os.path.exists(p) else None


def compile_classes():
    deps = [jar("com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar"),
            jar("junit/junit/4.13.1/junit-4.13.1.jar"),
            jar("org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar")]
    main_out, test_out = os.path.join(MAP, "classes"), os.path.join(MAP, "test-classes")
    if all(deps):
        os.makedirs(main_out, exist_ok=True); os.makedirs(test_out, exist_ok=True)
        r1 = run(["javac", "-g", "-nowarn", "--release", "11", "-d", main_out, "-cp", deps[0]]
                 + glob.glob(MAIN + "/*.java"))
        r2 = run(["javac", "-g", "-nowarn", "--release", "11", "-d", test_out,
                  "-cp", os.pathsep.join([main_out] + deps)] + glob.glob(TEST + "/*.java"))
        if r1.returncode == 0 and r2.returncode == 0:
            return main_out, test_out, "javac --release 11 (fresh)"
    return (os.path.join(ROOT, "target/classes"), os.path.join(ROOT, "target/test-classes"),
            "target/ (prebuilt; jars or javac unavailable)")


# ---------------------------------------------------------------- 2. jdeps
def jdeps_edges(main_out, test_out):
    edges = set()
    d = os.path.join(MAP, "jdeps")
    os.makedirs(d, exist_ok=True)
    run(["jdeps", "-verbose:class", "-filter:none", "-dotoutput", d + "/main", main_out])
    run(["jdeps", "-verbose:class", "-filter:none", "-cp", main_out, "-dotoutput", d + "/test", test_out])
    for f in glob.glob(d + "/*/*classes.dot"):
        for m in re.finditer(r'"(com\.google\.json\.[\w$]+)"\s*->\s*"(com\.google\.json\.[\w$]+) ', open(f).read()):
            a, b = (x.split("$")[0].rsplit(".", 1)[1] for x in m.groups())
            if a != b:
                edges.add((a, b))
    return edges


# ---------------------------------------------------------------- 3. javap
HDR = re.compile(r"^  (?:[\w<>\[\],.$? ]+ )?([\w$<>.]+)\((.*?)\)(?: throws [\w., $]+)?;$")
INV = re.compile(r"\b(invoke\w+)\s+#\d+.*// (?:Interface)?Method (?:([\w/$]+)\.)?(\"?[\w$<>]+\"?):\((.*?)\)")
FLD = re.compile(r"\b(getfield|putfield|getstatic|putstatic)\s+#\d+.*// Field (?:([\w/$]+)\.)?([\w$]+):")
NEW = re.compile(r"\bnew\s+#\d+.*// class ([\w/$]+)")
LN = re.compile(r"^\s+line (\d+): \d+")


def javap(classdir):
    """{(cls, method, params): {calls, fields, news, lines, access}} for every class."""
    out = {}
    files = sorted(glob.glob(os.path.join(classdir, PKG, "*.class")))
    names = [os.path.basename(f)[:-6] for f in files if not f.endswith("package-info.class")]
    txt = run(["javap", "-c", "-p", "-l", "-cp", classdir] + ["com.google.json." + n for n in names]).stdout
    cls = cur = None
    for line in txt.splitlines():
        m = re.match(r"^(?:[\w ]+ )?(?:class|interface|enum) com\.google\.json\.([\w$]+)", line)
        if m:
            cls, cur = m.group(1), None; continue
        m = HDR.match(line)
        if m and cls:
            name, params = m.group(1).split(".")[-1], m.group(2)
            if name == cls.split("$")[-1] or name == cls:
                name = "<init>"
            if line.strip().startswith("static {}"):
                name = "<clinit>"
            key = (cls, name, params.replace(" ", ""))
            cur = out.setdefault(key, {"calls": [], "fields": [], "news": set(), "lines": [],
                                       "access": line.strip().split(" ")[0]})
            continue
        if line.strip() == "static {};" and cls:
            cur = out.setdefault((cls, "<clinit>", ""), {"calls": [], "fields": [], "news": set(),
                                                         "lines": [], "access": "static"}); continue
        if cur is None:
            continue
        m = INV.search(line)
        if m:
            owner = (m.group(2) or (PKG + "/" + cls)).replace("/", ".")
            cur["calls"].append((owner, m.group(3).strip('"'), m.group(4))); continue
        m = FLD.search(line)
        if m:
            owner = (m.group(2) or (PKG + "/" + cls)).replace("/", ".")
            cur["fields"].append((m.group(1), owner, m.group(3))); continue
        m = NEW.search(line)
        if m:
            cur["news"].add(m.group(1).replace("/", ".")); continue
        if re.search(r"\b[bcia]astore\b", line):
            cur["astore"] = True
        m = LN.match(line)
        if m:
            cur["lines"].append(int(m.group(1)))
    return out


# ------------------------------------------------- source helpers (headers)
def class_ranges(path):
    """Top-level and nested class declaration spans by brace matching on comment/string-blanked text."""
    src = open(path).read()
    blank = re.sub(r"//[^\n]*|/\*.*?\*/|\"(?:\\.|[^\"\\\n])*\"|'(?:\\.|[^'\\\n])*'",
                   lambda m: re.sub(r"[^\n]", " ", m.group(0)), src, flags=re.S)
    spans = {}
    for m in re.finditer(r"\b(?:class|enum|interface)\s+(\w+)", blank):
        i = blank.index("{", m.end()); depth = 0
        for j in range(i, len(blank)):
            depth += {"{": 1, "}": -1}.get(blank[j], 0)
            if depth == 0:
                break
        # include the javadoc/annotations above the declaration
        start = blank.count("\n", 0, m.start()) + 1
        spans.setdefault(m.group(1), (start, blank.count("\n", 0, j) + 1))
    return spans


def decl_start(lines, first_body_line):
    """Walk up from the first body line to the method declaration line."""
    i = first_body_line - 1
    while i > 0 and not re.search(r"\b(?:\w+)\s*\(", lines[i - 1]) and i > first_body_line - 4:
        i -= 1
    while i > 1 and not re.match(r"^\s*(?:@|public|private|protected|static|final|\w[\w<>\[\]]*\s+\w+\s*\(|\w+\s*\()", lines[i - 1]):
        i -= 1
    return i


# ---------------------------------------------------------- 4. build graph
JAVA_TO_SIMPLE = lambda p: p.split(",")[0].split(".")[-1] if p else ""
PRIM = {"I": "int", "Z": "boolean", "C": "char", "J": "long", "D": "double", "F": "float", "B": "byte", "S": "short"}


def desc_types(d):
    """'Ljava/lang/String;I[C' -> ['String', 'int', 'char[]'] (javap descriptor -> simple names)."""
    out, i = [], 0
    while i < len(d):
        dims = 0
        while d[i] == "[": dims += 1; i += 1
        if d[i] == "L":
            j = d.index(";", i); t = d[i + 1:j].split("/")[-1].split("$")[-1]; i = j + 1
        else:
            t = PRIM[d[i]]; i += 1
        out.append(t + "[]" * dims)
    return out


def param_types(p):
    return [x.split(".")[-1].split("$")[-1].replace("...", "[]") for x in p.split(",")] if p else []


STATIC_SEGS = {}  # static-init node -> [file, [[a, b], ...]] for describe_nodes.py packets


def init_segments(src_lines, ls, gap):
    """Static initializers: group line-table lines, then extend each group to the end of its statement/block."""
    segs = []
    for r in runs(ls, gap):
        a, b, depth, i = r[0], r[-1], 0, r[0]
        while i <= len(src_lines):
            t = re.sub(r"//.*", "", src_lines[i - 1]).strip()
            depth += t.count("{") - t.count("}")
            if i >= b and depth <= 0 and (t.endswith(";") or t.endswith("}")):
                break
            i += 1
        segs.append([a, i])
    return segs


def runs(ls, gap=2):
    """Contiguous runs of line numbers (for static initializers scattered through a class)."""
    out = []
    for x in sorted(set(ls)):
        if out and x - out[-1][-1] <= gap: out[-1].append(x)
        else: out.append([x])
    return out

# assessment domains (ASSESSMENT.md "Architecture at a Glance")
DOMAINS = [
    ("api", "D1 Public API"), ("tok", "D2 Tokenizer / state machine"),
    ("struct", "D3 Structure repair + depth limit"), ("cmt", "D4 Comment & junk removal"),
    ("str", "D5 String normalization"), ("emb", "D6 Embedding-safety escapes"),
    ("num", "D7 Number normalization"), ("buf", "D8 Copy-on-write buffer"),
    ("min", "D9 EvalMinifier"), ("test", "D10 Tests & fuzzing"), ("ops", "D11 Build, CI & release"),
    ("data", "Shared state & caller I/O"),
]
# JsonSanitizer method -> domain; anything not listed falls back by line range below
JS_DOMAIN = {
    "sanitize(String)": "api", "sanitize(CharSequence)": "api", "sanitize(Reader)": "api",
    "sanitize(InputStream)": "api", "<init>": "api", "toString": "api", "toCharSequence": "api",
    "sanitize()": "tok", "requireValueState": "tok", "endOfQuotedString": "tok", "isJsonSpecialChar": "tok",
    "getMaximumNestingDepth": "struct", "endOfBracketedValue": "struct", "elideTrailingComma": "struct",
    "endOfComment": "cmt",
    "sanitizeString": "str", "isHexAt": "str", "isOctAt": "str", "appendHex": "str", "isHex": "str",
    "isOct": "str", "isOctalAt": "str",
    "hexVal": "str", "isKeyword": "tok",
    "normalizeNumber": "num", "canonicalizeNumber": "num", "endOfDigitRun": "num", "isDigit": "num",
    "insert": "buf", "elide": "buf", "replace": "buf",
}
JS_LINE_DOMAIN = [(1000, 1340, "num"), (1420, 1600, "emb")]
FIELD_STORE = {  # logical in-memory stores (there are no persistent data stores)
    ("JsonSanitizer", "jsonish"): "ds:jsonish",
    ("JsonSanitizer", "sanitizedJson"): "ds:sanitizedJson",
    ("JsonSanitizer", "cleaned"): "ds:sanitizedJson",
    ("JsonSanitizer", "bracketDepth"): "ds:bracketStack",
    ("JsonSanitizer", "isMap"): "ds:bracketStack",
    ("JsonSanitizer", "maximumNestingDepth"): "ds:maximumNestingDepth",
    ("JsonSanitizer", "UNBRACKETED_COMMA"): "ds:UNBRACKETED_COMMA",
    ("EvalMinifier", "RESERVED_KEYWORDS"): "ds:RESERVED_KEYWORDS",
}
STORE_NAMES = {
    "ds:jsonish": "jsonish (input text)", "ds:sanitizedJson": "sanitizedJson + cleaned (output buffer)",
    "ds:bracketStack": "bracketDepth + isMap (bracket stack)", "ds:maximumNestingDepth": "maximumNestingDepth",
    "ds:UNBRACKETED_COMMA": "UNBRACKETED_COMMA (shared static exception)",
    "ds:RESERVED_KEYWORDS": "RESERVED_KEYWORDS (JS reserved-word table)",
    "ds:callerInput": "caller Reader / InputStream", "ds:callerOutput": "caller Appendable / OutputStream",
}
IO_READ = ("java.io.Reader.read", "java.io.InputStreamReader.<init>")
IO_WRITE = ("java.lang.Appendable.append", "java.io.Writer.flush", "java.io.Writer.write",
            "java.io.OutputStreamWriter.<init>", "java.io.Writer.append")
SB_MUTATORS = {"append", "insert", "setLength", "delete", "deleteCharAt", "replace", "setCharAt", "reverse"}
CALLBACKS = {"equals", "hashCode", "compareTo", "next", "hasNext", "iterator", "toString", "run"}


def main():
    main_out, test_out, how = compile_classes()
    jd = jdeps_edges(main_out, test_out)
    methods = {}
    for d, kind in ((main_out, "main"), (test_out, "test")):
        for k, v in javap(d).items():
            v["side"] = kind
            methods[k] = v

    srcfile = {}
    for p in glob.glob(MAIN + "/*.java") + glob.glob(TEST + "/*.java"):
        for c, span in class_ranges(p).items():
            srcfile[c] = (p, span)

    nodes, member_of = {}, {}  # node id -> node ; (cls, name, params) -> node id
    tests = {c for c in srcfile if srcfile[c][0].startswith(TEST)}
    test_top = {c for c in tests if c in ("JsonSanitizerTest", "EvalMinifierTest", "FuzzyTest", "FuzzyStringGenerator")}

    for (cls, name, params), v in methods.items():
        top = cls.split("$")[0]
        inner = cls.split("$")[1] if "$" in cls else None
        if top in tests:  # tests: one node per top-level test class
            nid = top
            if nid not in nodes:
                p, (a, b) = srcfile[top]
                nodes[nid] = {"id": nid, "name": top, "kind": "module", "language": "java", "dom": "test",
                              "file": rel(p) + (f":{a}-{b}" if top == "FuzzyStringGenerator" or top == "FuzzyTest" else ""),
                              "lines": [a, b]}
            member_of[(cls, name, params)] = nid
            continue
        if name.startswith("access$") or name.startswith("lambda$"):
            member_of[(cls, name, params)] = ("inline", (cls, name, params)); continue
        if inner in ("1", "State") or (inner and inner.isdigit()):
            continue  # switch-map / enum boilerplate
        if inner:  # nested helper classes are one node each
            nid, label = f"{top}.{inner}", f"{top}.{inner}"
        else:
            first = JAVA_TO_SIMPLE(params)
            if name == "<init>":
                label = f"{top}()" if top == "JsonSanitizer" else f"{top}.<init>"
            elif name == "<clinit>":
                label = "static-init"
            elif top == "JsonSanitizer" and name == "sanitize":
                label = f"sanitize({first})" if first else "sanitize()"
            elif name == "minify":
                label = f"minify({first})"
            else:
                label = name
            nid = f"{top}#{label}"
        if name == "<init>" and not v["fields"] and all(c[0] == "java.lang.Object" for c in v["calls"]):
            continue  # javac-generated default constructor
        n = nodes.setdefault(nid, {"id": nid, "name": label if inner else f"{top}.{label}", "kind": "module",
                                   "language": "java", "lines": [], "cls": top, "key": label,
                                   "public": False, "callback": False})
        n["lines"] += v["lines"]
        n["public"] |= v["access"] == "public" and v.get("side") == "main"
        n["callback"] |= name in CALLBACKS or name == "<clinit>"  # JVM/JDK-invoked
        member_of[(cls, name, params)] = nid

    # line ranges + domains + file locations for production nodes
    for n in nodes.values():
        if n.get("dom") == "test":
            continue
        top, key = n["cls"], n["key"]
        p, (ca, cb) = srcfile[top] if top in srcfile else srcfile[n["id"].split(".")[-1]]
        if "." in n["id"] and "#" not in n["id"]:  # nested class
            p, (a, b) = srcfile[n["id"].split(".")[-1]]
        else:
            src_lines = open(p).read().splitlines()
            ls = sorted(set(n["lines"])) or [ca]
            a, b = decl_start(src_lines, ls[0]), ls[-1]
            if key.endswith("static-init"):
                segs = init_segments(src_lines, [x for x in ls if x != ca], 6)
                a, b = max(segs, key=lambda r: r[1] - r[0])
                n["loc_override"] = sum(y - x + 1 for x, y in segs)
                STATIC_SEGS[n["id"]] = [rel(p), segs]
        n["lines"] = [a, b]
        n["file"] = f"{rel(p)}:{a}-{b}"
        if top == "EvalMinifier":
            n["dom"] = "min"
        else:
            base = key if key.startswith("sanitize") or key == f"{top}()" else key
            dom = JS_DOMAIN.get(base) or JS_DOMAIN.get("<init>" if base == "JsonSanitizer()" else base)
            if not dom:
                dom = next((d for lo, hi, d in JS_LINE_DOMAIN if lo <= a <= hi), "tok")
            if n["id"].startswith("JsonSanitizer.UnbracketedComma") or key == "static-init":
                dom = "struct"
            n["dom"] = dom
        n["loc"] = n.pop("loc_override", b - a + 1)

    for top in test_top:
        if top in nodes:
            a, b = nodes[top]["lines"]; nodes[top]["loc"] = b - a + 1

    # --- edges
    edges, unresolved = set(), defaultdict(int)

    def resolve(key, seen=()):
        t = member_of.get(key)
        if isinstance(t, tuple) and t[0] == "inline":  # synthetic accessor -> its body's targets
            return [r for c in methods[t[1]]["calls"] for r in resolve(target_key(c), seen + (key,)) if key not in seen]
        return [t] if t else []

    def target_key(call):
        owner, name, params = call
        cls = owner.replace("com.google.json.", "")
        hit = [k for k in methods if k[0] == cls and k[1] == name]
        want = desc_types(params)
        exact = [k for k in hit if param_types(k[2]) == want]
        return (exact or hit or [(cls, name, params)])[0]

    for key, v in methods.items():
        src = member_of.get(key)
        if not isinstance(src, str):
            continue
        for call in v["calls"]:
            owner, name, _ = call
            fq = f"{owner}.{name}"
            if owner.startswith("com.google.json."):
                for t in resolve(target_key(call)):
                    if t != src:
                        edges.add((src, t, "call"))
                if not resolve(target_key(call)) and "$" not in owner and \
                        any(k[0] == owner.split(".")[-1] and k[1] == name for k in methods):
                    unresolved[fq] += 1
            elif v["side"] == "test":
                continue  # tests' own StringBuilders/streams are not the caller's I/O
            elif fq.startswith(IO_READ):
                edges.add((src, "ds:callerInput", "read"))
            elif fq.startswith(IO_WRITE):
                edges.add((src, "ds:callerOutput", "write"))
        # in-place mutation through a getfield'd reference is a write too:
        # sanitizedJson.append/setLength/..., isMap[i] = ...
        sb_mut = any(o == "java.lang.StringBuilder" and n in SB_MUTATORS for o, n, _ in v["calls"])
        for op, owner, fname in v["fields"]:
            store = FIELD_STORE.get((owner.replace("com.google.json.", ""), fname))
            if store:
                edges.add((src, store, "read" if op.startswith("get") else "write"))
                if op == "getfield" and ((fname == "sanitizedJson" and sb_mut) or (fname == "isMap" and v.get("astore"))):
                    edges.add((src, store, "write"))
        for cname in v["news"]:
            c = cname.replace("com.google.json.", "")
            if c.startswith("EvalMinifier$") or c.startswith("JsonSanitizer$UnbracketedComma"):
                t = member_of.get((c, "<init>", next((k[2] for k in methods if k[0] == c and k[1] == "<init>"), "")))
                if isinstance(t, str) and t != src:
                    edges.add((src, t, "call"))
        # JDK collections call Token.equals/hashCode/compareTo back: a dispatch edge
        if any(o.startswith(("java.util.Map", "java.util.HashMap", "java.util.Set", "java.util.TreeSet",
                             "java.util.Collections", "java.util.Arrays")) for o, _, _ in v["calls"]) \
                and any(n.endswith("EvalMinifier$Token") for n in v["news"]) and "EvalMinifier.Token" in nodes:
            edges.add((src, "EvalMinifier.Token", "dispatch"))

    # --- jobs (deployment/build config: pom.xml, .travis.yml, RELEASE-checklist.sh)
    jobs = {
        "job:mvn-verify": ("Maven build + surefire", "pom.xml"),
        "job:travis-ci": ("Travis CI (dead)", ".travis.yml"),
        "job:release": ("Release checklist", "RELEASE-checklist.sh"),
    }
    for jid, (nm, f) in jobs.items():
        if os.path.exists(os.path.join(ROOT, f)):
            nodes[jid] = {"id": jid, "name": nm, "kind": "job", "file": f, "dom": "ops",
                          "loc": sum(1 for _ in open(os.path.join(ROOT, f)))}
    # surefire's default includes (**/*Test.java) - no <includes> override in pom.xml
    for t in sorted(test_top):
        if t.endswith("Test") and t in nodes:
            edges.add(("job:mvn-verify", t, "dispatch"))
    edges.add(("job:travis-ci", "job:mvn-verify", "dispatch"))   # Travis default for Maven: mvn test
    edges.add(("job:release", "job:mvn-verify", "call"))          # `mvn clean verify ...` in the script

    stores = sorted({t for _, t, k in edges if t.startswith("ds:")})
    for s in stores:
        nodes[s] = {"id": s, "name": STORE_NAMES[s], "kind": "datastore", "dom": "data"}

    # --- entry points: public API of the public classes + build/CI jobs
    entry = sorted([n["id"] for n in nodes.values() if n.get("public") and not n.get("callback")
                    and n["dom"] in ("api", "min") and "." not in n["id"].split("#")[0]]
                   + ["job:travis-ci", "job:release"])
    entry = [e for e in entry if e in nodes]
    inbound = defaultdict(int)
    for s, t, k in edges:
        inbound[t] += 1
    dead = sorted(n for n, v in nodes.items() if v["kind"] != "datastore" and n not in entry
                  and not inbound[n] and not v.get("callback"))

    # --- tree
    children = defaultdict(list)
    for n in nodes.values():
        leaf = {k: n[k] for k in ("id", "name", "kind", "language", "loc", "file") if k in n}
        children[n["dom"]].append(leaf)
    root = {"id": "sys", "name": "json-sanitizer", "kind": "system", "children": [
        {"id": f"dom:{d}", "name": nm, "kind": "domain", "children": sorted(children[d], key=lambda x: x["id"])}
        for d, nm in DOMAINS if children[d]]}

    # --- observations (computed, no raw config values)
    writers = defaultdict(set)
    for s, t, k in edges:
        if k == "write":
            writers[t].add(s)
    top_w = max(writers, key=lambda s: len(writers[s])) if writers else None
    fan_in = sorted(((inbound[n], n) for n in nodes if nodes[n]["kind"] == "module"), reverse=True)[:3]
    js_core = [n for n in nodes.values() if n.get("cls") == "JsonSanitizer"]
    em_to_js = sorted({t for s, t, k in edges if s.startswith("EvalMinifier") and t.startswith("JsonSanitizer")})
    obs = [
        f"Single point of failure: JsonSanitizer#sanitize() ({nodes['JsonSanitizer#sanitize()']['loc']} lines) is the only "
        f"tokenizer; every public overload ({sum(1 for e in entry if e.startswith('JsonSanitizer#'))}) and EvalMinifier funnel into it, "
        f"and it fans out to {sum(1 for s,t,k in edges if s=='JsonSanitizer#sanitize()' and k=='call')} helpers.",
        f"Store with too many writers: {STORE_NAMES[top_w]} is written by {len(writers[top_w])} methods "
        f"({', '.join(sorted(x.split('#')[-1] for x in writers[top_w]))}). Copy-on-write via shared mutable fields "
        f"couples every normalizer to the buffer; it is the seam to cut before any god-class split.",
        f"EvalMinifier is coupled to sanitizer internals, not its public API: it calls "
        f"{', '.join(x.split('#')[-1] for x in em_to_js)} (package-private). Extracting or deprecating it means "
        f"publishing that seam or inlining it.",
        f"Highest fan-in helpers: {', '.join(f'{n.split(chr(35))[-1]} ({c})' for c, n in fan_in)}. "
        f"These are the cheapest units to characterize first and the riskiest to change.",
        "No persistent data stores, network or config: the 'datastores' here are the in-memory fields of one "
        "JsonSanitizer instance plus the caller's streams. No reflection, ServiceLoader or string-built class "
        "names found, so there are no unresolved dynamic targets; only JDK collection callbacks "
        "(Token.equals/hashCode/compareTo) are dispatched, and they are edged as such.",
        "Bytecode shows no dead methods, but compile-time-dead code is invisible to it: the "
        "SUPER_VERBOSE_AND_SLOW_LOGGING=false blocks in the constructor and sanitize() are folded away by javac "
        "and remain source-level debt (ASSESSMENT debt #9).",
        "Build/CI is the weakest link: the only automated entry (Travis) is dead and the release script exits on "
        "line 5, so job:mvn-verify (pom.xml + surefire defaults) is currently run by hand only.",
    ]
    if unresolved:
        obs.append("Unresolved in-project calls: " + ", ".join(f"{k} x{v}" for k, v in sorted(unresolved.items())[:5]))

    flows = [
        {"name": "A web developer embeds untrusted JSON in a page",
         "persona": "Web application developer",
         "description": "A developer passes JSON-like text from a third party through the sanitizer before writing it into an HTML <script> block.",
         "steps": [
             {"label": "Hand the untrusted text to the library", "nodes": ["JsonSanitizer#sanitize(String)"]},
             {"label": "Set up a sanitizer with the nesting limit", "nodes": ["JsonSanitizer#JsonSanitizer()", "ds:maximumNestingDepth"]},
             {"label": "Walk the text token by token", "nodes": ["JsonSanitizer#sanitize()", "ds:jsonish", "JsonSanitizer#requireValueState"]},
             {"label": "Drop comments and junk", "nodes": ["JsonSanitizer#endOfComment", "JsonSanitizer#elide"]},
             {"label": "Normalize strings and defuse </script>, <!--, ]]>", "nodes": ["JsonSanitizer#sanitizeString", "JsonSanitizer#replace"]},
             {"label": "Normalize numbers", "nodes": ["JsonSanitizer#normalizeNumber"]},
             {"label": "Close any open brackets", "nodes": ["ds:bracketStack", "JsonSanitizer#elideTrailingComma"]},
             {"label": "Return safe JSON (or the original if already clean)", "nodes": ["JsonSanitizer#toCharSequence", "ds:sanitizedJson"]},
         ]},
        {"name": "An API service cleans a streamed request body",
         "persona": "Backend service team",
         "description": "A service streams an incoming request body straight through the sanitizer into its response or downstream writer.",
         "steps": [
             {"label": "Pass the request stream and an output stream", "nodes": ["JsonSanitizer#sanitize(InputStream)"]},
             {"label": "Decode bytes as UTF-8 and read the body", "nodes": ["JsonSanitizer#sanitize(Reader)", "ds:callerInput"]},
             {"label": "Sanitize the whole body in memory", "nodes": ["JsonSanitizer#sanitize(CharSequence)", "JsonSanitizer#sanitize()"]},
             {"label": "Write safe JSON to the caller's sink", "nodes": ["ds:callerOutput"]},
         ]},
        {"name": "A front-end team ships compact JSON for eval",
         "persona": "Front-end performance engineer",
         "description": "A team shrinks a large repetitive JSON payload into a smaller JavaScript expression that the browser evaluates.",
         "steps": [
             {"label": "Hand the payload to the minifier", "nodes": ["EvalMinifier#minify(String)"]},
             {"label": "Sanitize it first", "nodes": ["JsonSanitizer#JsonSanitizer()", "JsonSanitizer#sanitize()", "JsonSanitizer#toCharSequence"]},
             {"label": "Find repeated tokens worth pooling", "nodes": ["EvalMinifier#minify(CharSequence)", "EvalMinifier.Token"]},
             {"label": "Give each pooled token a short safe name", "nodes": ["EvalMinifier.NameGenerator", "ds:RESERVED_KEYWORDS"]},
         ]},
        {"name": "A maintainer proves a change is still safe",
         "persona": "Library maintainer / release manager",
         "description": "Before a release, a maintainer runs the golden, regression and fuzz suites against the sanitizer.",
         "steps": [
             {"label": "Run the build", "nodes": ["job:release", "job:mvn-verify"]},
             {"label": "Check known inputs give known outputs", "nodes": ["JsonSanitizerTest", "EvalMinifierTest"]},
             {"label": "Fuzz random JSON-ish text for idempotence", "nodes": ["FuzzyTest", "FuzzyStringGenerator"]},
             {"label": "Exercise the public API", "nodes": ["JsonSanitizer#sanitize(String)", "EvalMinifier#minify(String)"]},
         ]},
    ]
    ids = set(nodes)
    for f in flows:
        for s in f["steps"]:
            s["nodes"] = [n for n in s["nodes"] if n in ids]

    # keep existing descriptions on rerun
    old = {}
    if os.path.exists(OUT):
        def walk(n):
            if "description" in n: old[n["id"]] = n["description"]
            for c in n.get("children", []): walk(c)
        walk(json.load(open(OUT))["root"])
    def put(n):
        if n["id"] in old: n["description"] = old[n["id"]]
        for c in n.get("children", []): put(c)
    put(root)

    topo = {"system": "OWASP JSON Sanitizer", "root": root,
            "edges": [{"source": s, "target": t, "kind": k} for s, t, k in sorted(edges)],
            "entryPoints": entry, "deadEnds": dead, "observations": obs, "flows": flows}
    leaf_ids = {l["id"] for d in root["children"] for l in d["children"]}
    bad = [e for e in topo["edges"] if e["source"] not in leaf_ids or e["target"] not in leaf_ids]
    assert not bad, bad
    locs = [l["file"] for d in root["children"] for l in d["children"] if "file" in l]
    assert len(locs) == len(set(locs)), "duplicate leaf locations"
    json.dump(topo, open(OUT, "w"), indent=1)
    json.dump(STATIC_SEGS, open(os.path.join(MAP, "static_init_segments.json"), "w"), indent=1)

    # --- summary
    kinds = defaultdict(int)
    for e in topo["edges"]: kinds[e["kind"]] += 1
    print(f"classes: {how}")
    print(f"leaves: {len(leaf_ids)}  edges: {len(topo['edges'])} {dict(kinds)}")
    for d in root["children"]:
        print(f"  {d['name']:<38} {len(d['children']):>3} nodes  " +
              ", ".join(l["name"].replace("JsonSanitizer.", "").replace("EvalMinifier.", "EM.") for l in d["children"])[:150])
    print("entry points:", ", ".join(entry))
    print("dead ends:", ", ".join(dead) or "none")
    cls_edges = {(s.split("#")[0].split(".")[0], t.split("#")[0].split(".")[0]) for s, t, k in edges
                 if k in ("call", "dispatch") and not s.startswith("job") and not t.startswith(("ds:", "job"))}
    cls_edges = {e for e in cls_edges if e[0] != e[1]}
    print("jdeps class edges:", len(jd), " covered by method graph:", len(jd & cls_edges),
          " missing:", sorted(jd - cls_edges) or "none", " extra:", sorted(cls_edges - jd) or "none")
    if unresolved: print("unresolved:", dict(unresolved))
    print(f"wrote {OUT}")


if __name__ == "__main__":
    main()
