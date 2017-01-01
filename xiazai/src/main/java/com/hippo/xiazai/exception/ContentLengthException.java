/*
 * Copyright 2016 Hippo Seven
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

package com.hippo.xiazai.exception;

/*
 * Created by Hippo on 12/31/2016.
 */

import java.io.IOException;

/**
 * Thrown if read length doesn't meet content length.
 */
public class ContentLengthException extends IOException {

  private long content;
  private long read;

  public ContentLengthException(long content, long read) {
    super("Content length is " + content + ", but read length is " + read);
    this.content = content;
    this.read = read;
  }

  public long contentLength() {
    return content;
  }

  public long readLength() {
    return read;
  }
}
