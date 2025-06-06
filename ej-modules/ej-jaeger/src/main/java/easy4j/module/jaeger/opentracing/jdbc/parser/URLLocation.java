/*
 * Copyright 2017-2021 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package easy4j.module.jaeger.opentracing.jdbc.parser;

/**
 * URLLocation
 *
 * @author bokun.li
 * @date 2025-05
 */
public class URLLocation {
  private final int startIndex;
  private final int endIndex;

  public URLLocation(int startIndex, int endIndex) {
    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }

  public int startIndex() {
    return startIndex;
  }

  public int endIndex() {
    return endIndex;
  }
}