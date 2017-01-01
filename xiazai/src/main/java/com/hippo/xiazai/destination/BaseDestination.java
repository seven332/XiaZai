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

package com.hippo.xiazai.destination;

/*
 * Created by Hippo on 12/31/2016.
 */

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.hippo.xiazai.XiaZaiDestination;
import com.hippo.xiazai.exception.BadCodeException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.Headers;
import okhttp3.HttpUrl;

/**
 * A base XiaZaiDestination with handling status code and filename from respond headers.
 */
public abstract class BaseDestination implements XiaZaiDestination {

  private static final Pattern PATTERN_FILENAME = Pattern.compile("filename=\"([^\"]+)\"");

  /**
   * Get filename from respond headers.
   * The filename could be unaccepted for the file system.
   */
  @Nullable
  public static String getFilename(Headers headers) {
    String contentDisposition = headers.get("Content-Disposition");
    if (contentDisposition == null) return null;
    Matcher matcher = PATTERN_FILENAME.matcher(contentDisposition);
    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return null;
    }
  }

  /**
   * Return true if the code is acceptable.
   */
  public boolean acceptCode(int code) {
    return code >= 200 && code < 300;
  }

  @NonNull
  @Override
  public OutputStream open(HttpUrl url, int code, Headers headers) throws IOException {
    if (!acceptCode(code)) throw new BadCodeException(code);
    String filename = getFilename(headers);
    return open(url, filename);
  }

  /**
   * Open a {@code OutputStream} to store downloaded data.
   * <p>
   * Do NOT return null. If you don't want to download any more, throw IOException.
   */
  @NonNull
  public abstract OutputStream open(HttpUrl url, String filename) throws IOException;
}
