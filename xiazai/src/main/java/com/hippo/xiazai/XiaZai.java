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

package com.hippo.xiazai;

/*
 * Created by Hippo on 12/31/2016.
 */

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.hippo.xiazai.destination.FileDestination;
import com.hippo.xiazai.exception.ContentLengthException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This class only provides thread-blocking methods to download file.
 */
public class XiaZai {

  @IntDef({STATE_COMPLETED, STATE_CANCELLED, STATE_ERROR})
  @Retention(RetentionPolicy.CLASS)
  public @interface State {}

  public static final int STATE_COMPLETED = 0;
  public static final int STATE_CANCELLED = 1;
  public static final int STATE_ERROR = 2;

  private final OkHttpClient client;

  public XiaZai(@NonNull OkHttpClient client) {
    this.client = client;
  }

  /**
   * A convenient way to call {@code xiazai(url, file, null, null)}.
   */
  @State
  public int xiazai(@NonNull String url, @NonNull File file) {
    return xiazai(url, file, null, null);
  }

  /**
   * Download the data get from the url to the file.
   */
  @State
  public int xiazai(@NonNull String url, @NonNull File file,
      @Nullable XiaZaiController controller, @Nullable XiaZaiCallback callback) {
    XiaZaiDestination dest = new FileDestination(file);
    return xiazai(url, dest, controller, callback);
  }

  /**
   * Download the data get from the url to the destination.
   */
  @State
  public int xiazai(@NonNull String url, @NonNull XiaZaiDestination dest,
      @Nullable XiaZaiController controller, @Nullable XiaZaiCallback callback) {
    Request request = new Request.Builder().get().url(url).build();
    return xiazai(request, dest, controller, callback);
  }

  /**
   * Download the data from the OkHttp request to the destination.
   *
   * @param request OkHttp Request
   * @param dest the destination to store downloaded date
   * @param controller the controller to cancel download
   * @param callback the callback of download, called in the same thread
   * @return one of {@link #STATE_COMPLETED}, {@link #STATE_CANCELLED} and {@link #STATE_ERROR}
   */
  @State
  public int xiazai(@NonNull Request request, @NonNull XiaZaiDestination dest,
      @Nullable XiaZaiController controller, @Nullable XiaZaiCallback callback) {
    // Occupy the XiaZaiController
    if (controller != null) {
      controller.occupy();
    }

    Call call = null;
    Response response = null;
    OutputStream os = null;
    InputStream is = null;
    Throwable exception = null;

    try {
      if (controller != null && controller.isCancelled()) {
        return STATE_CANCELLED;
      }
      if (callback != null) {
        callback.onStart();
      }

      call = client.newCall(request);
      if (controller != null && controller.setCall(call)) {
        return STATE_CANCELLED;
      }
      response = call.execute();
      if (controller != null && controller.setCall(null)) {
        return STATE_CANCELLED;
      }

      os = dest.open(response.request().url(), response.code(), response.headers());
      is = response.body().byteStream();
      if (controller != null && controller.isCancelled()) {
        return STATE_CANCELLED;
      }

      byte[] buffer = new byte[1024 * 4];
      long content = response.body().contentLength();
      long read = 0;
      int n;
      while ((n = is.read(buffer)) != -1) {
        os.write(buffer, 0, n);
        read += n;

        if (controller != null && controller.isCancelled()) {
          return STATE_CANCELLED;
        }

        if (callback != null) {
          callback.onProgress(n, read, content);
        }
      }

      // Check whether read length meets content length
      if (content != -1 && content != read) {
        throw new ContentLengthException(content, read);
      }

      return STATE_COMPLETED;
    } catch (Throwable e) {
      exception = e;
      return STATE_ERROR;
    } finally {
      if (controller != null) {
        controller.setCall(null);
      }
      if (call != null) {
        call.cancel();
      }
      if (response != null) {
        response.close();
      }
      if (os != null) {
        try {
          os.close();
        } catch (IOException e) {
          // Ignore
        }
      }
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // Ignore
        }
      }

      // Callback
      if (callback != null) {
        if (controller != null && controller.isCancelled()) {
          callback.onCancelled();
        } else if (exception != null) {
          callback.onError(exception);
        } else {
          callback.onCompleted();
        }
      }
    }
  }
}
