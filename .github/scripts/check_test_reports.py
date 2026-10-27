#!/usr/bin/env python3
# Copyright (C) 2026 OWASP json-sanitizer contributors
# SPDX-License-Identifier: Apache-2.0
"""Checks a leg's surefire reports: every report ran on the expected Java
version, every test named in the required list ran and was not skipped, and at
least the given number of Jazzer seed inputs were replayed.

usage: check_test_reports.py <java.specification.version> <required-tests.txt>
                             <min-seed-replays> <surefire-reports-dir>
"""

import glob
import os
import sys
import xml.etree.ElementTree as ET

SEED_CLASS = "com.google.json.JsonSanitizerFuzzTest"
SEED_PREFIX = "sanitizeProperties("


def main(java, required_file, min_seeds, reports):
    with open(required_file, encoding="utf-8") as f:
        required = [l.strip() for l in f if l.strip() and not l.startswith("#")]
    files = sorted(glob.glob(os.path.join(reports, "TEST-*.xml")))
    if not files:
        print("no surefire reports in " + reports)
        return 1
    problems = []
    ran, skipped = set(), set()
    executed = seeds = 0
    for path in files:
        suite = ET.parse(path).getroot()
        props = {p.get("name"): p.get("value") for p in suite.iter("property")}
        if props.get("java.specification.version") != java:
            problems.append("%s ran on Java %s, not %s"
                            % (os.path.basename(path), props.get("java.specification.version"), java))
        for case in suite.iter("testcase"):
            key = case.get("classname") + "#" + case.get("name")
            if case.find("skipped") is not None:
                skipped.add(key)
                continue
            ran.add(key)
            executed += 1
            if case.get("classname") == SEED_CLASS and case.get("name").startswith(SEED_PREFIX):
                seeds += 1
    missing = [key for key in required if key not in ran]
    for key in missing:
        problems.append(("skipped: " if key in skipped else "did not run: ") + key)
    if seeds < min_seeds:
        problems.append("Jazzer seed replays: %d, expected at least %d" % (seeds, min_seeds))
    print("Java %s: %d tests executed, %d skipped; %d of %d required tests ran; %d seed replays"
          % (java, executed, len(skipped), len(required) - len(missing), len(required), seeds))
    for p in problems:
        print("  " + p)
    return 1 if problems else 0


if __name__ == "__main__":
    if len(sys.argv) != 5:
        sys.exit(__doc__)
    sys.exit(main(sys.argv[1], sys.argv[2], int(sys.argv[3]), sys.argv[4]))
