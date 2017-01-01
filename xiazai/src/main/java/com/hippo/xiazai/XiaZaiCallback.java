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

/**
 * Callback of download.
 * <br>
 * Only one of {@link #onCompleted()}, {@link #onCancelled()} ()}
 * and {@link #onError(Throwable)} will be called.
 */
public interface XiaZaiCallback {

  /**
   * Called at the start of downloading.
   */
  void onStart();

  /**
   * Update download progress.
   *
   * @param n read byte in this turn
   * @param read read byte in total
   * @param content content length, -1 for unknown
   */
  void onProgress(int n, long read, long content);

  /**
   * Called if download cancelled.
   */
  void onCancelled();

  /**
   * Called if download completed.
   */
  void onCompleted();

  /**
   * Called if get error.
   */
  void onError(Throwable e);
}
