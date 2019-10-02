// Copyright (C) 2012 Google Inc.
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

package com.google.json;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;

import javax.annotation.Nullable;

import junit.framework.TestCase;
import org.junit.Test;


/**
 * Tries a series of pseudo-random variants of a string of JSON to suss out
 * boundary conditions in the JSON parser.
 */
@SuppressWarnings("javadoc")
public final class FuzzyTest extends TestCase {
  @Test
  public static final void testSanitizerLikesFuzzyWuzzyInputs()
  throws Throwable {
    int nRuns = 10000;
    long seed;
    {
      // Try to fetch a seed from a system property so that we can replay failed
      // tests.
      String seedString = System.getProperty("fuzz.seed", null);
      if (seedString != null) {
        seed = Long.parseLong(seedString, 16);
      } else {
        // Use java.util.Random's default constructor to generate a seed since
        // it does a pretty good job of making a good non-crypto-strong seed.
        seed = new Random().nextLong();
      }
    }

    // Dump the seed so that failures can be reproduced with only this line
    // from the test log.
    System.err.println("Fuzzing with -Dfuzz.seed=" + Long.toHexString(seed));
    System.err.flush();

    Random rnd = new Random(seed);
    for (String fuzzyWuzzyString : new FuzzyStringGenerator(rnd)) {
      try {
        String sanitized0 = JsonSanitizer.sanitize(fuzzyWuzzyString);
        String sanitized1 = JsonSanitizer.sanitize(sanitized0);
        // Test idempotence.
        assertEquals(fuzzyWuzzyString + "  =>  " + sanitized0, sanitized0,
                     sanitized1);
      } catch (Throwable th) {
        System.err.println("Failed on `" + fuzzyWuzzyString + "`");
        hexDump(fuzzyWuzzyString.getBytes("UTF16"), System.err);
        System.err.println("");
        throw th;
      }
      if (--nRuns <= 0) { break; }
    }
  }


  private static void hexDump(byte[] bytes, Appendable app)
    throws IOException {
    for (int i = 0; i < bytes.length; ++i) {
      if ((i % 16) == 0) {
        if (i != 0) {
          app.append('\n');
        }
      } else {
        app.append(' ');
      }
      byte b = bytes[i];
      app.append("0123456789ABCDEF".charAt((b >>> 4) & 0xf));
      app.append("0123456789ABCDEF".charAt((b >>> 0) & 0xf));
    }
  }
}

final class FuzzyStringGenerator implements Iterable<String> {
  final Random rnd;

  FuzzyStringGenerator(Random rnd) {
    this.rnd = rnd;
  }

  @Override
  public Iterator<String> iterator() {
    return new Iterator<String>() {
      private @Nullable String basis;
      private @Nullable String pending;
      @Override
      public boolean hasNext() {
        return true;
      }
      @Override
      public String next() {
        if (pending == null) {
          fuzz();
        }
        String s = pending;
        pending = null;
        if (0 == rnd.nextInt(16)) { basis = null; }
        return s;
      }
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
      @SuppressWarnings("synthetic-access")
      private void fuzz() {
        if (basis == null) {
          pending = basis = makeRandomJson();
          return;
        }
        pending = mutate(basis);
      }
    };
  }

  private String makeRandomJson() {
    int maxDepth = 1 + rnd.nextInt(8);
    int maxBreadth = 4 + rnd.nextInt(16);
    StringBuilder sb = new StringBuilder();
    appendWhitespace(sb);
    appendRandomJson(maxDepth, maxBreadth, sb);
    appendWhitespace(sb);
    return sb.toString();
  }

  private static final String[] FLOAT_FORMAT_STRING = {
    "%g", "%G", "%e", "%E", "%f"
  };

  private static final String[] INT_FORMAT_STRING = {
    "%x", "%X", "%d"
  };


  private void appendRandomJson(
      int maxDepth, int maxBreadth, StringBuilder sb) {
    int r = rnd.nextInt(maxDepth > 0 ? 8 : 6);

    switch (r) {
      case 0: sb.append("null"); break;
      case 1: sb.append("true"); break;
      case 2: sb.append("false"); break;
      case 3: {
        String fmt = FLOAT_FORMAT_STRING
          [rnd.nextInt(FLOAT_FORMAT_STRING.length)];
        sb.append(String.format(Locale.ROOT, fmt, 1.0 / rnd.nextGaussian()));
        break;
      }
      case 4: {
        switch (rnd.nextInt(3)) {
          case 0: break;
          case 1: sb.append('-'); break;
          case 2: sb.append('+'); break;
        }
        String fmt = INT_FORMAT_STRING
          [rnd.nextInt(INT_FORMAT_STRING.length)];
        BigInteger num = new BigInteger(randomDecimalDigits(maxBreadth * 2));
        sb.append(String.format(Locale.ROOT, fmt, num));
        break;
      }
      case 5:
        appendRandomString(maxBreadth, sb);
        break;
      case 6:
        sb.append('[');
        appendWhitespace(sb);
        for (int i = rnd.nextInt(maxBreadth); --i >= 0;) {
          appendWhitespace(sb);
          appendRandomJson(maxDepth - 1, Math.max(1, maxBreadth - 1), sb);
          if (i != 1) {
            appendWhitespace(sb);
            sb.append(',');
          }
        }
        appendWhitespace(sb);
        sb.append(']');
        break;
      case 7:
        sb.append('{');
        appendWhitespace(sb);
        for (int i = rnd.nextInt(maxBreadth); --i >= 0;) {
          appendWhitespace(sb);
          appendRandomString(maxBreadth, sb);
          appendWhitespace(sb);
          sb.append(':');
          appendWhitespace(sb);
          appendRandomJson(maxDepth - 1, Math.max(1, maxBreadth - 1), sb);
          if (i != 1) {
            appendWhitespace(sb);
            sb.append(',');
          }
        }
        appendWhitespace(sb);
        sb.append('}');
        break;
    }
  }

  private void appendRandomString(int maxBreadth, StringBuilder sb) {
    sb.append('"');
    appendRandomChars(rnd.nextInt(maxBreadth * 4), sb);
    sb.append('"');
  }

  private void appendRandomChars(int nChars, StringBuilder sb) {
    for (int i = nChars; --i >= 0;) {
      appendRandomChar(sb);
    }
  }

  private void appendRandomChar(StringBuilder sb) {
    char delim = rnd.nextInt(8) == 0 ? '\'' : '"';
    int cpMax;
    switch (rnd.nextInt(7)) {
      case 0: case 1: case 2: case 3: cpMax = 0x100; break;
      case 4: case 5: cpMax = 0x10000; break;
      default: cpMax = Character.MAX_CODE_POINT; break;
    }
    int cp = rnd.nextInt(cpMax);
    boolean encode = false;
    if (cp == delim || cp < 0x20 || cp == '\\') {
      encode = true;
    }
    if (!encode && 0 == rnd.nextInt(8)) {
      encode = true;
    }
    if (encode) {
      if (rnd.nextBoolean()) {
        for (char cu : Character.toChars(cp)) {
          sb.append("\\u").append(String.format("%04x", (int) cu));
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

  private void appendWhitespace(StringBuilder sb) {
    if (rnd.nextInt(4) == 0) {
      for (int i = rnd.nextInt(4); --i >= 0;) {
        sb.append(" \t\r\n".charAt(rnd.nextInt(4)));
      }
    }
  }

  private String randomDecimalDigits(int maxDigits) {
    int nDigits = Math.max(1, rnd.nextInt(maxDigits));
    StringBuilder sb = new StringBuilder(nDigits);
    for (int i = nDigits; --i >= 0;) {
      sb.append((char) ('0' + rnd.nextInt(10)));
    }
    return sb.toString();
  }

  private String mutate(String s) {
    int n = rnd.nextInt(16) + 1;  // Number of changes.
    int len = s.length();
    // Pick the places where we mutate, so we can sort, de-dupe, and then
    // derive s' in a left-to-right pass.
    int[] locations = new int[n];
    for (int i = n; --i >= 0;) {
      locations[i] = rnd.nextInt(len);
    }
    Arrays.sort(locations);

    // Dedupe.
    {
      int k = 1;
      for (int i = 1; i < n; ++i) {
        if (locations[i] != locations[i - 1]) {
          locations[k++] = locations[i];
        }
      }
      n = k;  // Skip any duped ones.
    }

    // Walk left-to-right and perform modifications.
    int left = 0;
    StringBuilder delta = new StringBuilder(len);
    for (int i = 0; i < n; ++i) {
      int loc = locations[i];
      int nextLoc = i + 1 == n ? len : locations[i + 1];
      int size = nextLoc - loc;
      int rndSliceLen = 1;
      if (size > 1) {
        rndSliceLen = rnd.nextInt(size);
      }

      delta.append(s, left, loc);
      left = loc;

      switch (rnd.nextInt(3)) {
        case 0:  // insert
          appendRandomChars(rndSliceLen, delta);
          break;
        case 1:  // replace
          appendRandomChars(rndSliceLen, delta);
          left += rndSliceLen;
          break;
        case 2:  // remove
          left += rndSliceLen;
          break;
      }
    }
    delta.append(s, left, len);
    return delta.toString();
  }
}
