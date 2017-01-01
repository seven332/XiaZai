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

import okhttp3.Call;

/**
 * A controller to cancel download.
 * <p>
 * It's ok to call {@link #cancel()} before pass it to {@code xiazai()}.
 * <br>
 * Do NOT pass one XiaZaiController to {@code xiazai()} twice
 * or throw IllegalStateException.
 */
public class XiaZaiController {

  private boolean occupied;
  private boolean cancelled;
  private Call call;

  // Occupy this XiaZaiController
  // Throw IllegalStateException if occupy it twice
  synchronized void occupy() throws IllegalStateException {
    if (!occupied) {
      occupied = true;
    } else {
      throw new IllegalStateException("The XiaZaiController is already occupied.");
    }
  }

  // Set call, return cancelled
  synchronized boolean setCall(Call call) {
    if (!cancelled) {
      this.call = call;
    }
    return cancelled;
  }

  /**
   * Cancel downloading.
   * <p>
   * Can be called in any thread.
   */
  public synchronized void cancel() {
    cancelled = true;

    if (call != null) {
      call.cancel();
      call = null;
    }
  }

  /**
   * Return true if {@link #cancel()} has been called.
   */
  public synchronized boolean isCancelled() {
    return cancelled;
  }
}
