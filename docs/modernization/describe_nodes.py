#!/usr/bin/env python3
"""Build per-node description packets and merge the analysts' paragraphs back.

  python3 describe_nodes.py packets [N]      # N largest leaves by loc (default 40)
      -> map/packets/<safe-id>.txt and map/packets/index.json
  python3 describe_nodes.py check answers.json
      -> prints nodes whose paragraph names a number/identifier absent from its packet
  python3 describe_nodes.py merge answers.json
      -> writes passing paragraphs into topology.json (failures are left without one)

answers.json: {"<node id>": "<paragraph>"}
"""
import json, os, re, sys
from collections import defaultdict

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", "..", "legacy", "json-sanitizer"))
TOPO = os.path.join(HERE, "topology.json")
PK = os.path.join(HERE, "map", "packets")
MAX_SRC = 150


def leaves(t):
    out = []
    def walk(n, dom=None):
        if n.get("children"):
            for c in n["children"]:
                walk(c, n["name"] if n["kind"] == "domain" else dom)
        elif n["kind"] != "system":
            out.append(dict(n, domain=dom))
    walk(t["root"])
    return out


def safe(nid):
    return re.sub(r"[^\w.-]+", "_", nid).strip("_")


def source_slice(file):
    path, _, rng = file.partition(":")
    lines = open(os.path.join(ROOT, path), errors="replace").read().splitlines()
    a, b = (int(x) for x in rng.split("-")) if rng else (1, len(lines))
    # header comment: the javadoc/comment block directly above the declaration
    h = a - 1
    while h > 0 and re.match(r"^\s*(\*|/\*\*|/\*|//|@)", lines[h - 1]):
        h -= 1
    header = lines[h:a - 1]
    body = lines[a - 1:b]
    note = ""
    if len(body) > MAX_SRC:
        note = f"(excerpt: first {MAX_SRC} of {len(body)} lines)"
        body = body[:MAX_SRC]
    num = lambda start, ls: "\n".join(f"{start + i:5d}  {l}" for i, l in enumerate(ls))
    return path, num(h + 1, header) if header else "(none)", num(a, body), note


def packets(n):
    t = json.load(open(TOPO))
    ls = leaves(t)
    names = {l["id"]: l["name"] for l in ls}
    out_e, in_e = defaultdict(list), defaultdict(list)
    for e in t["edges"]:
        out_e[e["source"]].append(e); in_e[e["target"]].append(e)
    chosen = sorted((l for l in ls if l.get("loc")), key=lambda l: -l["loc"])[:n]
    os.makedirs(PK, exist_ok=True)
    index = {}
    segs_f = os.path.join(HERE, "map", "static_init_segments.json")
    segs = json.load(open(segs_f)) if os.path.exists(segs_f) else {}
    for l in chosen:
        path, header, body, note = source_slice(l["file"])
        if l["id"] in segs:  # static initializers are scattered: show every segment
            f, rs = segs[l["id"]]
            parts = [source_slice(f"{f}:{a}-{b}") for a, b in rs]
            header = "\n".join(p[1] for p in parts if p[1] != "(none)") or "(none)"
            body = "\n   ...\n".join(p[2] for p in parts)
            note = f"(all {len(rs)} static-initializer segments)"
        conn = [f"- {e['kind']} -> {names[e['target']]} ({e['target']})" for e in out_e[l["id"]]] + \
               [f"- {e['kind']} <- {names[e['source']]} ({e['source']})" for e in in_e[l["id"]]]
        flags = []
        if l["id"] in t["entryPoints"]: flags.append("entry point (public API or deployment/build config)")
        txt = (f"NODE: {l['name']}\nID: {l['id']}\nKIND: {l['kind']}\nDOMAIN: {l['domain']}\n"
               f"LOCATION: {l['file']}\nLINES: {l['loc']}\n" + (f"FLAGS: {'; '.join(flags)}\n" if flags else "") +
               f"\nCONNECTIONS (from the dependency map):\n" + ("\n".join(conn) or "- none") +
               f"\n\nHEADER COMMENT:\n{header}\n\nSOURCE {note}:\n{body}\n")
        f = os.path.join(PK, safe(l["id"]) + ".txt")
        open(f, "w").write(txt)
        index[l["id"]] = os.path.relpath(f, os.path.join(HERE, "..", ".."))
    json.dump(index, open(os.path.join(PK, "index.json"), "w"), indent=1)
    print(f"{len(index)} packets in {os.path.relpath(PK)}")
    for k, v in index.items():
        print(f"{k}\t{v}")


GENERIC = {"JSON", "HTML", "XML", "API", "JDK", "JVM", "CI", "JS", "UTF", "ASCII", "Unicode", "JavaScript",
           "Java", "Maven", "JUnit", "I/O", "CDATA", "OK"}
TOKEN = re.compile(r"[A-Za-z_$][\w$]*(?:[.#][A-Za-z_$][\w$]*)*(?:\(\))?|\d[\d,.]*")


def suspicious(par):
    for tok in TOKEN.findall(par):
        tok = tok.rstrip(".,")
        if not tok or tok in GENERIC:
            continue
        if tok[0].isdigit():
            yield tok.replace(",", "")
        elif (re.search(r"[a-z][A-Z]|_|[.#(]", tok) or (tok.isupper() and len(tok) > 1)
              or (tok[0].isupper() and re.search(r"[A-Z]", tok[1:]))):
            yield tok


def check(answers):
    index = json.load(open(os.path.join(PK, "index.json")))
    bad = {}
    for nid, par in answers.items():
        pkt = open(os.path.join(HERE, "..", "..", index[nid])).read()
        miss = sorted({t for t in suspicious(par) if t.rstrip("()") not in pkt})
        words = len(par.split())
        if miss or not 55 <= words <= 90:
            bad[nid] = {"missing": miss, "words": words}
    return bad


if __name__ == "__main__":
    cmd = sys.argv[1]
    if cmd == "packets":
        packets(int(sys.argv[2]) if len(sys.argv) > 2 else 40)
    else:
        answers = json.load(open(sys.argv[2]))
        bad = check(answers)
        for k, v in bad.items():
            print(f"FAIL {k}: missing={v['missing']} words={v['words']}")
        print(f"{len(answers) - len(bad)}/{len(answers)} pass")
        if cmd == "merge":
            t = json.load(open(TOPO))
            def put(n):
                if n["id"] in answers and n["id"] not in bad:
                    n["description"] = answers[n["id"]].strip()
                for c in n.get("children", []): put(c)
            put(t["root"])
            json.dump(t, open(TOPO, "w"), indent=1)
            print("merged into topology.json")
