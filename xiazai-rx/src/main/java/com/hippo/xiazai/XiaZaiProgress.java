/*
 * Copyright 2017 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.xiazai;

/*
 * Created by Hippo on 1/1/2017.
 */

/**
 * Simple download progress wrapper.
 */
public class XiaZaiProgress {

  private long n;
  private long read;
  private long content;

  public XiaZaiProgress(long n, long read, long content) {
    this.n = n;
    this.read = read;
    this.content = content;
  }

  public long n() {
    return n;
  }

  public long read() {
    return read;
  }

  public long content() {
    return content;
  }
}
