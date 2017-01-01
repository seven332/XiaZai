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
import com.hippo.xiazai.XiaZaiDestination;
import com.hippo.xiazai.exception.BadCodeException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import okhttp3.Headers;
import okhttp3.HttpUrl;

public class FileDestination implements XiaZaiDestination {

  public File file;

  public FileDestination(File file) {
    this.file = file;
  }

  @NonNull
  @Override
  public OutputStream open(HttpUrl url, int code, Headers headers) throws IOException {
    if (code >= 200 && code < 300) {
      return new FileOutputStream(file);
    } else {
      throw new BadCodeException(code);
    }
  }
}
