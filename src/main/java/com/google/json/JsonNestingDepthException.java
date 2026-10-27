// Copyright (C) 2026 OWASP
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

/**
 * Thrown when JSON-like content nests brackets more deeply than the maximum
 * nesting depth allowed by {@link JsonSanitizer}.
 * <p>
 * Callers that sanitize untrusted input should catch this and reject the
 * input.
 * <p>
 * This extends {@link ArrayIndexOutOfBoundsException} because earlier versions
 * threw that type for over-deep input, so existing handlers keep working.
 */
public final class JsonNestingDepthException
    extends ArrayIndexOutOfBoundsException {
  private static final long serialVersionUID = -6230548613540127914L;

  private final int maximumNestingDepth;

  JsonNestingDepthException(int maximumNestingDepth, int offset) {
    super("JSON nesting depth exceeds maximum of " + maximumNestingDepth
        + " at offset " + offset);
    this.maximumNestingDepth = maximumNestingDepth;
  }

  /**
   * @return the maximum nesting depth that the input exceeded.
   */
  public int getMaximumNestingDepth() {
    return maximumNestingDepth;
  }
}
