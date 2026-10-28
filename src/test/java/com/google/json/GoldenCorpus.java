// Copyright (C) 2026 OWASP json-sanitizer contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// GoldenCorpus.java
//
// Deterministic golden corpus for json-sanitizer: records, or verifies against a recording,
// SHA-256 digests of the sanitizer's output over a fixed corpus of inputs:
//   1. every input used by the unit tests (JsonSanitizerTest, EvalMinifierTest);
//   2. a number corpus: hex, octal and decimal literals, long literals around the 1024-digit
//      boundary, fraction and exponent tails, each also as an object key and an array element;
//   3. every UTF-16 code unit in five syntactic positions, plus supplementary and lone-surrogate
//      samples;
//   4. 200,000 pseudo-random JSON-ish strings from a fixed-seed fuzzer, a port of the test
//      suite's FuzzyStringGenerator that never formats doubles, so the inputs are the same on
//      every JDK.
// For each input seven calls are made ("legs"): S = sanitize(String), C = sanitize(CharSequence),
// D1 = sanitize(String, 1), D3 = sanitize(String, 3), M = EvalMinifier.minify(String),
// R = sanitize(Reader, Appendable), B = sanitize(InputStream, OutputStream) over UTF-8 bytes.
// Inputs are digested in blocks of 4096: an "in" digest of the inputs and an "out" digest of the
// seven outputs, so a mismatch names the block and says whether inputs or outputs differ.
//
// Java 8 source level, no dependencies beyond the sanitizer classes. GoldenCorpusTest runs
// verify against src/test/resources/com/google/json/golden-digests.txt on every build; after
// `mvn test-compile` the tool also runs by hand:
//   java -cp target/test-classes:target/classes com.google.json.GoldenCorpus record <digests-file> [--dump <gz-file>] [--provenance <text>]
//   java -cp target/test-classes:target/classes com.google.json.GoldenCorpus verify <digests-file>
//   java -cp target/test-classes:target/classes com.google.json.GoldenCorpus dump <from-index> <to-index> <file>
// A re-recording must come from the legacy build's classes, never from the build under test.

package com.google.json;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

public final class GoldenCorpus {
  static final String FORMAT = "json-sanitizer golden corpus v1";
  static final long SEED = 0x6a736f6e2d73616eL;  // "json-san"
  static final int FUZZ_COUNT = 200000;
  static final int BLOCK = 4096;
  static final String[] LEG_NAMES = { "S", "C", "D1", "D3", "M", "R", "B" };

  public static void main(String[] args) throws Exception {
    if (args.length < 2) { usage(); }
    String cmd = args[0];
    if ("record".equals(cmd)) {
      String dump = null, provenance = null;
      for (int i = 2; i < args.length; ++i) {
        if ("--dump".equals(args[i]) && i + 1 < args.length) { dump = args[++i]; }
        else if ("--provenance".equals(args[i]) && i + 1 < args.length) { provenance = args[++i]; }
        else { usage(); }
      }
      record(args[1], dump, provenance);
    } else if ("verify".equals(cmd)) {
      System.exit(verify(args[1], LEGACY) ? 0 : 1);
    } else if ("dump".equals(cmd) && (args.length == 4 || args.length == 5)) {
      Legs impl = args.length == 5 && "legacy".equals(args[4]) ? LEGACY : CURRENT;
      dumpRange(Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3], impl);
    } else {
      usage();
    }
  }

  static void usage() {
    System.err.println("usage: GoldenCorpus record <digests-file> [--dump <gz-file>] [--provenance <text>]");
    System.err.println("       GoldenCorpus verify <digests-file>");
    System.err.println("       GoldenCorpus dump <from-index> <to-index> <file> [current|legacy]");
    System.exit(2);
  }

  // ---- the seven legs -------------------------------------------------------------------

  /** The seven calls on one input, by one implementation. */
  interface Legs {
    String[] legs(String input);
  }

  /** The library under test (src/main). */
  static final Legs CURRENT = new Legs() {
    public String[] legs(String input) {
      String[] out = new String[7];
      out[0] = JsonSanitizer.sanitize(input);
      out[1] = JsonSanitizer.sanitize(new StringBuilder(input)).toString();
      out[2] = JsonSanitizer.sanitize(input, 1);
      out[3] = JsonSanitizer.sanitize(input, 3);
      out[4] = EvalMinifier.minify(input);
      try {
        StringBuilder r = new StringBuilder();
        JsonSanitizer.sanitize(new StringReader(input), r);
        out[5] = r.toString();
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        JsonSanitizer.sanitize(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), b);
        out[6] = new String(b.toByteArray(), StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return out;
    }
  };

  /** The legacy release (d9b2768), kept verbatim in com.google.json.legacy: the oracle. */
  static final Legs LEGACY = new Legs() {
    public String[] legs(String input) {
      String[] out = new String[7];
      out[0] = com.google.json.legacy.JsonSanitizer.sanitize(input);
      out[1] = com.google.json.legacy.JsonSanitizer.sanitize(new StringBuilder(input)).toString();
      out[2] = com.google.json.legacy.JsonSanitizer.sanitize(input, 1);
      out[3] = com.google.json.legacy.JsonSanitizer.sanitize(input, 3);
      out[4] = com.google.json.legacy.EvalMinifier.minify(input);
      try {
        StringBuilder r = new StringBuilder();
        com.google.json.legacy.JsonSanitizer.sanitize(new StringReader(input), r);
        out[5] = r.toString();
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        com.google.json.legacy.JsonSanitizer.sanitize(
            new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), b);
        out[6] = new String(b.toByteArray(), StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return out;
    }
  };

  // ---- digests ----------------------------------------------------------------------------

  static void update(MessageDigest md, String s) {
    int n = s.length();
    byte[] buf = new byte[4 + 2 * n];
    buf[0] = (byte) (n >>> 24); buf[1] = (byte) (n >>> 16); buf[2] = (byte) (n >>> 8); buf[3] = (byte) n;
    for (int i = 0, j = 4; i < n; ++i) {
      char c = s.charAt(i);
      buf[j++] = (byte) (c >>> 8);
      buf[j++] = (byte) c;
    }
    md.update(buf);
  }

  static String hex(byte[] b) {
    StringBuilder sb = new StringBuilder(b.length * 2);
    for (byte x : b) {
      sb.append(Character.forDigit((x >>> 4) & 0xf, 16)).append(Character.forDigit(x & 0xf, 16));
    }
    return sb.toString();
  }

  static final class Block {
    final int index, first, last;
    final String in, out;
    Block(int index, int first, int last, String in, String out) {
      this.index = index; this.first = first; this.last = last; this.in = in; this.out = out;
    }
    String line() { return "block " + index + " " + first + " " + last + " in=" + in + " out=" + out; }
  }

  static final class Result {
    final List<Block> blocks = new ArrayList<Block>();
    int inputs;
    long inputChars, outputChars, maxInput;
    String total;
  }

  /** Sees each input and its legs as compute() digests them. */
  interface Observer {
    void observe(int index, String input, String[] legs);
  }

  static Result compute(Legs impl, Writer dump, int dumpFrom, int dumpTo) throws Exception {
    return compute(impl, dump, dumpFrom, dumpTo, null);
  }

  static Result compute(Legs impl, Writer dump, int dumpFrom, int dumpTo, Observer observer) throws Exception {
    MessageDigest din = MessageDigest.getInstance("SHA-256");
    MessageDigest dout = MessageDigest.getInstance("SHA-256");
    MessageDigest dtotal = MessageDigest.getInstance("SHA-256");
    Result r = new Result();
    Iterator<String> corpus = corpus();
    int i = 0, first = 0;
    while (corpus.hasNext()) {
      String input = corpus.next();
      String[] legs = impl.legs(input);
      if (observer != null) { observer.observe(i, input, legs); }
      update(din, input);
      r.inputChars += input.length();
      if (input.length() > r.maxInput) { r.maxInput = input.length(); }
      for (String leg : legs) { update(dout, leg); r.outputChars += leg.length(); }
      if (dump != null && i >= dumpFrom && i <= dumpTo) { writeRecord(dump, i, input, legs); }
      ++i;
      if (i % BLOCK == 0) { first = closeBlock(r, din, dout, dtotal, first, i); }
      if (dump != null && i > dumpTo) { break; }
    }
    if (i > first) { closeBlock(r, din, dout, dtotal, first, i); }
    r.inputs = i;
    r.total = hex(dtotal.digest());
    return r;
  }

  static int closeBlock(Result r, MessageDigest din, MessageDigest dout, MessageDigest dtotal, int first, int next) {
    Block b = new Block(r.blocks.size(), first, next - 1, hex(din.digest()), hex(dout.digest()));
    r.blocks.add(b);
    dtotal.update(b.line().getBytes(StandardCharsets.US_ASCII));
    return next;
  }

  static String header() {
    return "seed=" + Long.toHexString(SEED) + " fuzz=" + FUZZ_COUNT + " block=" + BLOCK + " legs=" + String.join(",", LEG_NAMES);
  }

  static long ms(long t0) { return (System.nanoTime() - t0) / 1000000L; }

  static void record(String file, String dumpFile, String provenance) throws Exception {
    Writer dump = null;
    if (dumpFile != null) {
      dump = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(dumpFile), 1 << 16), StandardCharsets.UTF_8);
    }
    long t0 = System.nanoTime();
    Result r;
    try { r = compute(LEGACY, dump, 0, Integer.MAX_VALUE); } finally { if (dump != null) { dump.close(); } }
    PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.US_ASCII));
    w.println("# " + FORMAT);
    w.println("# " + header());
    w.println("# recorded-on " + System.getProperty("java.vendor") + " " + System.getProperty("java.version")
        + " (" + System.getProperty("java.vm.name") + ")");
    if (provenance != null) { w.println("# provenance " + provenance); }
    for (Block b : r.blocks) { w.println(b.line()); }
    w.println("total " + r.inputs + " " + r.total);
    w.close();
    System.out.println("recorded " + r.inputs + " inputs (" + r.blocks.size() + " blocks, " + LEG_NAMES.length
        + " legs each, " + (long) r.inputs * LEG_NAMES.length + " calls) in " + ms(t0) + " ms -> " + file
        + (dumpFile != null ? " and " + dumpFile : ""));
    System.out.println("stats: input chars " + r.inputChars + ", max input " + r.maxInput + ", output chars " + r.outputChars);
  }

  static boolean verify(String file, Legs impl) throws Exception {
    List<String> expectedBlocks = new ArrayList<String>();
    String expectedTotal = null, expectedHeader = null;
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.US_ASCII));
    String line;
    boolean first = true;
    while ((line = br.readLine()) != null) {
      if (first) {
        first = false;
        if (!line.equals("# " + FORMAT)) { br.close(); System.err.println("not a golden corpus file: " + file); return false; }
        continue;
      }
      if (line.startsWith("# seed=")) { expectedHeader = line.substring(2); }
      else if (line.startsWith("block ")) { expectedBlocks.add(line); }
      else if (line.startsWith("total ")) { expectedTotal = line; }
    }
    br.close();
    if (expectedHeader == null || !expectedHeader.equals(header())) {
      System.err.println("corpus parameters differ: file says '" + expectedHeader + "', this tool is '" + header() + "'");
      return false;
    }
    long t0 = System.nanoTime();
    Result r = compute(impl, null, 0, -1);
    int mismatches = 0;
    List<String> report = new ArrayList<String>();
    int n = Math.max(expectedBlocks.size(), r.blocks.size());
    for (int i = 0; i < n; ++i) {
      String exp = i < expectedBlocks.size() ? expectedBlocks.get(i) : null;
      Block got = i < r.blocks.size() ? r.blocks.get(i) : null;
      if (exp == null || got == null) {
        ++mismatches;
        if (report.size() < 20) { report.add("block " + i + ": " + (exp == null ? "not in the baseline" : "not produced")); }
        continue;
      }
      if (exp.equals(got.line())) { continue; }
      ++mismatches;
      String[] e = exp.split(" ");
      boolean inDiffers = e.length < 6 || !e[4].equals("in=" + got.in);
      boolean outDiffers = e.length < 6 || !e[5].equals("out=" + got.out);
      if (report.size() < 20) {
        report.add("block " + i + " (inputs " + got.first + "-" + got.last + "): "
            + (inDiffers ? "INPUTS differ (the generator diverged on this JDK) " : "")
            + (outDiffers ? "OUTPUTS differ" : ""));
      }
    }
    String totalLine = "total " + r.inputs + " " + r.total;
    System.out.println("inputs executed: " + r.inputs + " (" + LEG_NAMES.length + " legs each, "
        + (long) r.inputs * LEG_NAMES.length + " calls), blocks: " + r.blocks.size() + ", " + ms(t0) + " ms, "
        + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
    if (mismatches == 0 && totalLine.equals(expectedTotal)) {
      System.out.println("GOLDEN CORPUS: byte-identical to " + file);
      return true;
    }
    System.out.println("GOLDEN CORPUS: " + mismatches + " of " + n + " blocks differ from " + file);
    for (String s : report) { System.out.println("  " + s); }
    if (mismatches > report.size()) { System.out.println("  ... " + (mismatches - report.size()) + " more"); }
    System.out.println("  to see a block's inputs and outputs: GoldenCorpus dump <first> <last> <file>, on both builds, then diff");
    return false;
  }

  static void dumpRange(int from, int to, String file, Legs impl) throws Exception {
    Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
    try { compute(impl, w, from, to); } finally { w.close(); }
  }

  // ---- comparing the build under test with the legacy oracle -------------------------------

  /** Names the approved class of one output difference, or returns null if none explains it. */
  interface Classifier {
    String classify(String input, int leg, String legacy, String current);
  }

  /** One input and leg whose output differs from the legacy oracle's. */
  static final class Difference {
    final int index, leg;
    final String input, legacy, current;
    Difference(int index, int leg, String input, String legacy, String current) {
      this.index = index; this.leg = leg; this.input = input; this.legacy = legacy; this.current = current;
    }
    @Override public String toString() {
      return "input " + index + " leg " + LEG_NAMES[leg] + ": " + clip(escape(input))
          + "\n    legacy:  " + clip(escape(legacy)) + "\n    current: " + clip(escape(current));
    }
    private static String clip(String s) { return s.length() <= 300 ? s : s.substring(0, 300) + "...(" + s.length() + " chars)"; }
  }

  static final class Comparison {
    /** The legacy oracle's digests, to check against the recorded baseline. */
    Result legacy;
    /** Differing (input, leg) pairs by approved class. */
    final Map<String, Integer> counts = new TreeMap<String, Integer>();
    /** The first unexplained differences, and how many there were. */
    final List<Difference> unexplained = new ArrayList<Difference>();
    int unexplainedCount;
  }

  /**
   * One pass over the corpus: digests the legacy oracle's outputs (as verify does) and
   * classifies every leg where the build under test differs from them.
   */
  static Comparison compare(final Classifier classifier) throws Exception {
    final Comparison c = new Comparison();
    c.legacy = compute(LEGACY, null, 0, -1, new Observer() {
      public void observe(int index, String input, String[] legacyLegs) {
        String[] current = CURRENT.legs(input);
        for (int leg = 0; leg < legacyLegs.length; ++leg) {
          if (legacyLegs[leg].equals(current[leg])) { continue; }
          String cls = classifier.classify(input, leg, legacyLegs[leg], current[leg]);
          if (cls == null) {
            if (c.unexplained.size() < 20) {
              c.unexplained.add(new Difference(index, leg, input, legacyLegs[leg], current[leg]));
            }
            ++c.unexplainedCount;
          } else {
            Integer n = c.counts.get(cls);
            c.counts.put(cls, n == null ? 1 : n + 1);
          }
        }
      }
    });
    return c;
  }

  /** The recorded digest lines (blocks and total) of a digests file, or null if it is not one. */
  static List<String> recordedLines(String file) throws Exception {
    List<String> lines = new ArrayList<String>();
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.US_ASCII));
    try {
      String line = br.readLine();
      if (line == null || !line.equals("# " + FORMAT)) { return null; }
      while ((line = br.readLine()) != null) {
        if (line.startsWith("# seed=")) {
          if (!line.substring(2).equals(header())) { return null; }
        } else if (line.startsWith("block ") || line.startsWith("total ")) {
          lines.add(line);
        }
      }
    } finally {
      br.close();
    }
    return lines;
  }

  /** The digest lines of a computed result, in the same form as recordedLines. */
  static List<String> lines(Result r) {
    List<String> lines = new ArrayList<String>();
    for (Block b : r.blocks) { lines.add(b.line()); }
    lines.add("total " + r.inputs + " " + r.total);
    return lines;
  }

  static void writeRecord(Writer w, int index, String input, String[] legs) throws IOException {
    w.write(index + "\tIN\t"); w.write(escape(input)); w.write('\n');
    for (int k = 0; k < legs.length; ++k) {
      w.write(index + "\t" + LEG_NAMES[k] + "\t"); w.write(escape(legs[k])); w.write('\n');
    }
  }

  // Lossless, line-safe, UTF-8-safe: control characters, every surrogate, BOM and noncharacters are \\uXXXX.
  static String escape(String s) {
    StringBuilder sb = new StringBuilder(s.length() + 16);
    for (int i = 0, n = s.length(); i < n; ++i) {
      char c = s.charAt(i);
      switch (c) {
        case '\\': sb.append("\\\\"); break;
        case '\t': sb.append("\\t"); break;
        case '\n': sb.append("\\n"); break;
        case '\r': sb.append("\\r"); break;
        default:
          if (c < 0x20 || (c >= 0x7f && c <= 0x9f) || (c >= 0xd800 && c <= 0xdfff) || c == 0xfeff || c == 0xfffe || c == 0xffff) {
            sb.append("\\u");
            String h = Integer.toHexString(c);
            for (int p = h.length(); p < 4; ++p) { sb.append('0'); }
            sb.append(h);
          } else {
            sb.append(c);
          }
      }
    }
    return sb.toString();
  }

  // ---- the corpus -------------------------------------------------------------------------

  static Iterator<String> corpus() {
    List<Iterator<String>> parts = new ArrayList<Iterator<String>>();
    parts.add(testInputs().iterator());
    parts.add(numberCorpus().iterator());
    parts.add(new CodeUnitSweep());
    parts.add(new Fuzzer(SEED, FUZZ_COUNT));
    return new Chain(parts);
  }

  static final class Chain implements Iterator<String> {
    final Iterator<Iterator<String>> parts;
    Iterator<String> cur;
    Chain(List<Iterator<String>> p) { parts = p.iterator(); cur = parts.hasNext() ? parts.next() : null; }
    public boolean hasNext() {
      while (cur != null) {
        if (cur.hasNext()) { return true; }
        cur = parts.hasNext() ? parts.next() : null;
      }
      return false;
    }
    public String next() { if (!hasNext()) { throw new NoSuchElementException(); } return cur.next(); }
    public void remove() { throw new UnsupportedOperationException(); }
  }

  static String repeat(String s, int n) {
    StringBuilder sb = new StringBuilder(s.length() * n);
    for (int i = 0; i < n; ++i) { sb.append(s); }
    return sb.toString();
  }

  // Every input of JsonSanitizerTest and EvalMinifierTest at commit d9b2768 (null is left out: it cannot be digested).
  static List<String> testInputs() {
    List<String> in = new ArrayList<String>(Arrays.asList(
      // JsonSanitizerTest#testSanitize
      "", "null", "false", "true", " false ", "  false", "false\n", "false,true", "\"foo\"", "'foo'",
      "\"<script>foo()</script>\"", "\"</SCRIPT\n>\"", "\"</ScRIpT\"", "\"</ScRİpT\"",
      "\"<b>Hello</b>\"", "\"<s>Hello</s>\"", "'<[[]]>'", "']]>'", "[[0]]>", "[1,-1,0.0,-0.5,1e2,",
      "[1,2,3,]", "[1,,3,]", "[1 2 3]", "{ \"foo\": \"bar\" }", "{ \"foo\": \"bar\", }", "{\"foo\",\"bar\"}",
      "{ foo: \"bar\" }", "{ foo: 'bar", "{ foo: ['bar", "// comment\nfalse", "false// comment",
      "false// comment\n", "false/* comment */", "false/* comment *", "false/* comment ", "/*/true**/false",
      "1", "-1", "1.0", "-1.0", "1.05", "427.0953333", "6.0221412927e+23", "6.0221412927e23",
      "6.0221412927e", "6.0221412927e-", "6.0221412927e+", "1.660538920287695E-24", "-6.02e-23",
      "1.", ".5", "-.5", "+.5", "+.5e2", "+1.5e+2", "+.5e-2",
      "{0:0}", "{-0:0}", "{+0:0}", "{1.0:0}", "{1.:0}", "{.5:0}", "{-.5:0}", "{+.5:0}", "{+.5e2:0}",
      "{+1.5e+2:0}", "{+.1:0}", "{+.01:0}", "{+.5e-2:0}", "{10e100:0}", "{10e-100:0}", "{10.5e-100:0}",
      "{10.500e-100:0}", "{12.34e100:0}", "{.01234e-100:0}", "{}", "({})",
      "'\u0000\u0008\u001f'", "'𐀀\udc00\ud800'", "'�￾￿'",
      "￯\u000042\u0008\ud800￿\udc00", "￯\u0000\u0008\ud800￿\udc00",
      "[,]", "[null,]", "{\"a\":0,false\"x\":{\"\":-1}}", "[true false]", "[\"\\u00a0\\u1234\"]",
      "{a\\b\"c", "{\"a\":\"b\",\"c\":", "{1e0001234567890123456789123456789123456789:0}", "-016923547559",
      // testIssue3
      "[{{},ä", "[{{ää},ä",
      // testIssue4
      "dev", "eval", "comment", "fasle", "FALSE", "dev/comment", "dev\\comment", "dev\\ncomment", "[dev\\, comment]",
      // testExcessiveNestingIsReplacedWithNull (the literal inputs; the built ones are added below)
      "[[[1]],2]", "{\"a\":{\"c\":2},\"b\":1}", "[[1],[2]]", "[[\"]\",']',/* ] */1],2]", "[[// ]\n1],2]",
      "[[1},2]", "{[1]}", "[[[",
      // testUnopenedArray, testIssue13
      "﻿-01742461140214282]", "[ { \"description\": \"aa##############aa\" }, 1 ]",
      // testHtmlParserStateChanges
      "\"<script\"", "\"<Script\"", "\"<ScRİpT\"", "\"<SCRIPT\n>\"", "<script", "\"<!--\"", "<!--",
      "\"-->\"", "-->", "\"<!--<script>\"",
      // CI Fuzz findings
      "-0888888888888888888888", "{{{{{{{\\x80{{([-053333333304233333333333", "y\\4",
      "?\u0000\u0000\u0000{{\u0000�\u0003]ve{R]\u00000�\u0016&e{\u0003]�a<!.b<!<!cc1x\u0000\u00005{281<\u0000.{t\u0001�5�{5��0�15\r�\u0000\u0000\u0000~~-0081273222428822883223759,55�\u0000�\t\u0000�",
      "?\u0000\u0000\u0000{{\u0000�\u0003]ve{R]\u00000��\u0016&e{\u0003]�a<!.b<!<!c\u00005{281<\u0000.{t\u0001�5�{515\r[\u0000\u0000\u0000~~-008127322242��������23759,551x\u0000\u00006{281<\u0000.{t\u0001�5�{5��0�15\r[\u0000\u0000\u0000~~-0081273222428822883223759,�",
      "x<\\script>", "x</\\script>", "x</sc\\ript>", "x<\\163cript>", "x</\\163cript>", "x<\\123cript>",
      "x</\\123cript>", "u\\u\\uu��\\u7u\\u\\u\\u�u<\\script>5", "z\\<\\!--", "z\\<!\\--",
      "z\\<!-\\-", "z\\<\\!--", "\"\\]]\\>",
      "x</\\script>u\\u\\uu��\\u7u\\u\\u\\u�u<\\script>5+alert(1)//",
      "\u0010{'\u0000\u0000'\"\u0000\"{.�-0X29295909049550970,\n\n0",
      "¶0x.\\蹃4\\À906",
      "'->??-\\->", "'.\\u002D->'", "x--\\>",
      // CharSequence / Reader / stream tests
      "{\"a\":[1,2]}", "{a:[1,2,]}", "{a:'b'}", "['café',' ']",
      // EvalMinifierTest#testMinify
      "[null,null]", "[null,null,null,null]", "[null,null,null,null,null,null,null]",
      "[null,null,null,null,null,null,null,null,null,null,null,null,null]",
      "[null,null,null,null,null,null,null,null,null,null,null,null,null, null,null,null,null,null,null,null,null,null,null,null,null,null]",
      "['foo','bar','foo','bar','foo','bar','foo','bar','foo','bar','foo','bar','foo','bar','foo','bar','foo','bar','foo','bar','foo','z']",
      "[123456,123456,123456,123456,123456.789,123456,123456,123456,123456,123456,123456,123456,123456,123456,123456e-3,123456,123456,123456]"
    ));
    String longStr = "\"A very very very very loooooooong string that can be pooled, but not when it appears as a property name.\"";
    in.add("{" + longStr + ": " + longStr + ", a:[" + longStr + "," + longStr + ",42,," + longStr + "]}");
    int d = JsonSanitizer.DEFAULT_NESTING_DEPTH;
    in.add(repeat("{", 66) + repeat("}", 66));
    in.add(repeat("[", 5000));
    in.add(repeat("[", d + 1) + repeat("]", d + 1));
    in.add(repeat("[{\"a\":", d / 2 + 1));
    in.add(repeat("[", d) + repeat("]", d));
    in.add(repeat("[{", 3000) + "1,2" + repeat("}]", 10));
    StringBuilder big = new StringBuilder("[");
    for (int i = 0; i < 50000; ++i) { big.append("'x',"); }
    in.add(big.toString());
    return in;
  }

  static List<String> numberCorpus() {
    List<String> out = new ArrayList<String>();
    String[] specials = {
      "0", "-0", "+0", "00", "-00", "0.", "-.", ".", "+", "-", "0x", "0X", "-0x", "0x.", "0e", "0e0", "1e", "1e+",
      "1e-", "1E", "1E+5", "1e05", "1e+05", "1e2147483647", "1e2147483648", "1e-2147483648", "1e-2147483649",
      "11e2147483647", "1e99999999999999999999", "1e-99999999999999999999", "0.1", "0.10", "1.00", "100", "1e2",
      "1.5e1", "15e-1", "0.15e2", "123456789012345678901234567890", "-123456789012345678901234567890",
      "9007199254740993", "18446744073709551616", "0x10", "-0x10", "0X1F", "0xdeadbeef", "0xDEADBEEF", "0x1p3",
      "0b101", "1_000", "١٢٣", "Infinity", "-Infinity", "+Infinity", "NaN", "-NaN", "017", "018",
      "-017", "019", "08", "09", "0777777777777777777777", "0x.5", "1.2.3", "1e2e3", "1-2", "--1", "+-1", "-+1",
      "1+", "1e+-2", "0X29295909049550970", "-0X29295909049550970", "1e0001234567890123456789123456789123456789",
      ".e5", "e5", "1.e5", ".5.", "1..", "1.e", "0x1.8p1"
    };
    for (String s : specials) {
      out.add(s); out.add("[" + s + "]"); out.add("{" + s + ":0}"); out.add("[" + s + "," + s + "]");
    }
    int[] lens = { 1, 2, 3, 8, 16, 19, 20, 21, 64, 100, 300, 1000, 1023, 1024, 1025, 1026, 1100, 2048 };
    String[] fills = { "7", "f", "F", "9", "0", "1" };
    String[] prefixes = { "", "0", "0x", "0X" };
    String[] signs = { "", "-", "+" };
    String[] tails = { "", ".5", "e3", "E3", "e+3", "e-3", ".", "e", "e+", ".e5" };
    for (int len : lens) {
      for (String fill : fills) {
        String digits = fill.equals("1") ? "1" + repeat("0", len - 1) : repeat(fill, len);
        for (String prefix : prefixes) {
          for (String sign : signs) {
            for (String tail : tails) {
              String n = sign + prefix + digits + tail;
              out.add(n); out.add("[" + n + "]"); out.add("{" + n + ":0}");
            }
          }
        }
      }
    }
    return out;
  }

  // Every UTF-16 code unit, then supplementary and lone-surrogate samples, each in five positions.
  static final class CodeUnitSweep implements Iterator<String> {
    static final String[] SAMPLES = {
      new String(Character.toChars(0x10000)), new String(Character.toChars(0x1D11E)),
      new String(Character.toChars(0x1F600)), new String(Character.toChars(0x20000)),
      new String(Character.toChars(0xE0001)), new String(Character.toChars(0x10FFFF)),
      "\udc00\ud800", "\ud800", "\udc00", "\ud800\ud800", "\udc00\udc00", "😀\ud83d", "a\ud800b", "a\udc00b"
    };
    static final int FORMS = 5;
    int cu = 0, form = 0, sample = 0;
    public boolean hasNext() { return cu <= 0xFFFF || sample < SAMPLES.length; }
    public String next() {
      if (!hasNext()) { throw new NoSuchElementException(); }
      String s = cu <= 0xFFFF ? String.valueOf((char) cu) : SAMPLES[sample];
      String r = form(s, form);
      if (++form == FORMS) {
        form = 0;
        if (cu <= 0xFFFF) { ++cu; } else { ++sample; }
      }
      return r;
    }
    static String form(String s, int form) {
      switch (form) {
        case 0: return "\"" + s + "\"";
        case 1: return s;
        case 2: return "[" + s + "]";
        case 3: return "\"\\" + s + "\"";
        default: return "'" + s + "'";
      }
    }
    public void remove() { throw new UnsupportedOperationException(); }
  }

  // A port of the test suite's FuzzyStringGenerator (FuzzyTest.java at d9b2768) with a fixed seed and a
  // bounded count. The one deliberate difference: random floating-point literals are built from integer
  // draws instead of String.format on a double, because double formatting differs between JDKs and the
  // corpus must be the same on every JDK. java.util.Random's sequence is specified and stable.
  static final class Fuzzer implements Iterator<String> {
    final Random rnd;
    final int count;
    int produced = 0;
    String basis, pending;

    Fuzzer(long seed, int count) { rnd = new Random(seed); this.count = count; }

    public boolean hasNext() { return produced < count; }

    public String next() {
      if (!hasNext()) { throw new NoSuchElementException(); }
      if (pending == null) {
        if (basis == null) { pending = basis = makeRandomJson(); } else { pending = mutate(basis); }
      }
      String s = pending;
      pending = null;
      if (0 == rnd.nextInt(16)) { basis = null; }
      ++produced;
      return s;
    }

    public void remove() { throw new UnsupportedOperationException(); }

    String makeRandomJson() {
      int maxDepth = 1 + rnd.nextInt(8);
      int maxBreadth = 4 + rnd.nextInt(16);
      StringBuilder sb = new StringBuilder();
      appendWhitespace(sb);
      appendRandomJson(maxDepth, maxBreadth, sb);
      appendWhitespace(sb);
      return sb.toString();
    }

    void appendRandomJson(int maxDepth, int maxBreadth, StringBuilder sb) {
      int r = rnd.nextInt(maxDepth > 0 ? 8 : 6);
      switch (r) {
        case 0: sb.append("null"); break;
        case 1: sb.append("true"); break;
        case 2: sb.append("false"); break;
        case 3: appendRandomFloat(sb); break;
        case 4: {
          switch (rnd.nextInt(3)) {
            case 0: break;
            case 1: sb.append('-'); break;
            case 2: sb.append('+'); break;
          }
          BigInteger num = new BigInteger(randomDecimalDigits(maxBreadth * 2));
          switch (rnd.nextInt(3)) {
            case 0: sb.append(num.toString(16)); break;
            case 1: sb.append(num.toString(16).toUpperCase(Locale.ROOT)); break;
            default: sb.append(num.toString()); break;
          }
          break;
        }
        case 5: appendRandomString(maxBreadth, sb); break;
        case 6: {
          sb.append('[');
          appendWhitespace(sb);
          for (int i = rnd.nextInt(maxBreadth); --i >= 0;) {
            appendWhitespace(sb);
            appendRandomJson(maxDepth - 1, Math.max(1, maxBreadth - 1), sb);
            if (i != 1) { appendWhitespace(sb); sb.append(','); }
          }
          appendWhitespace(sb);
          sb.append(']');
          break;
        }
        case 7: {
          sb.append('{');
          appendWhitespace(sb);
          for (int i = rnd.nextInt(maxBreadth); --i >= 0;) {
            appendWhitespace(sb);
            appendRandomString(maxBreadth, sb);
            appendWhitespace(sb);
            sb.append(':');
            appendWhitespace(sb);
            appendRandomJson(maxDepth - 1, Math.max(1, maxBreadth - 1), sb);
            if (i != 1) { appendWhitespace(sb); sb.append(','); }
          }
          appendWhitespace(sb);
          sb.append('}');
          break;
        }
      }
    }

    // Shapes of %g %G %e %E %f without formatting a double.
    void appendRandomFloat(StringBuilder sb) {
      int style = rnd.nextInt(5);
      if (rnd.nextBoolean()) { sb.append('-'); }
      sb.append(randomDecimalDigits(1 + rnd.nextInt(7)));
      sb.append('.');
      sb.append(randomDecimalDigits(1 + rnd.nextInt(6)));
      if (style < 4) {
        sb.append(style % 2 == 0 ? 'e' : 'E');
        sb.append(rnd.nextBoolean() ? '+' : '-');
        int exp = rnd.nextInt(400);
        if (exp < 10) { sb.append('0'); }
        sb.append(exp);
      }
    }

    void appendRandomString(int maxBreadth, StringBuilder sb) {
      sb.append('"');
      appendRandomChars(rnd.nextInt(maxBreadth * 4), sb);
      sb.append('"');
    }

    void appendRandomChars(int nChars, StringBuilder sb) {
      for (int i = nChars; --i >= 0;) { appendRandomChar(sb); }
    }

    void appendRandomChar(StringBuilder sb) {
      char delim = rnd.nextInt(8) == 0 ? '\'' : '"';
      int cpMax;
      switch (rnd.nextInt(7)) {
        case 0: case 1: case 2: case 3: cpMax = 0x100; break;
        case 4: case 5: cpMax = 0x10000; break;
        default: cpMax = Character.MAX_CODE_POINT; break;
      }
      int cp = rnd.nextInt(cpMax);
      boolean encode = cp == delim || cp < 0x20 || cp == '\\';
      if (!encode && 0 == rnd.nextInt(8)) { encode = true; }
      if (encode) {
        if (rnd.nextBoolean()) {
          for (char cu : Character.toChars(cp)) {
            sb.append("\\u");
            String h = Integer.toHexString(cu);
            for (int p = h.length(); p < 4; ++p) { sb.append('0'); }
            sb.append(h);
          }
        } else {
          sb.append('\\');
          switch (cp) {
            case 0xa: sb.append('\n'); break;
            case 0xd: sb.append('\r'); break;
            default: sb.appendCodePoint(cp); break;
          }
        }
      } else {
        sb.appendCodePoint(cp);
      }
    }

    void appendWhitespace(StringBuilder sb) {
      if (rnd.nextInt(4) == 0) {
        for (int i = rnd.nextInt(4); --i >= 0;) { sb.append(" \t\r\n".charAt(rnd.nextInt(4))); }
      }
    }

    String randomDecimalDigits(int maxDigits) {
      int nDigits = Math.max(1, rnd.nextInt(maxDigits));
      StringBuilder sb = new StringBuilder(nDigits);
      for (int i = nDigits; --i >= 0;) { sb.append((char) ('0' + rnd.nextInt(10))); }
      return sb.toString();
    }

    String mutate(String s) {
      int n = rnd.nextInt(16) + 1;
      int len = s.length();
      int[] locations = new int[n];
      for (int i = n; --i >= 0;) { locations[i] = rnd.nextInt(len); }
      Arrays.sort(locations);
      {
        int k = 1;
        for (int i = 1; i < n; ++i) {
          if (locations[i] != locations[i - 1]) { locations[k++] = locations[i]; }
        }
        n = k;
      }
      int left = 0;
      StringBuilder delta = new StringBuilder(len);
      for (int i = 0; i < n; ++i) {
        int loc = locations[i];
        int nextLoc = i + 1 == n ? len : locations[i + 1];
        int size = nextLoc - loc;
        int rndSliceLen = 1;
        if (size > 1) { rndSliceLen = rnd.nextInt(size); }
        delta.append(s, left, loc);
        left = loc;
        switch (rnd.nextInt(3)) {
          case 0: appendRandomChars(rndSliceLen, delta); break;
          case 1: appendRandomChars(rndSliceLen, delta); left += rndSliceLen; break;
          case 2: left += rndSliceLen; break;
        }
      }
      delta.append(s, left, len);
      return delta.toString();
    }
  }
}
