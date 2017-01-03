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

import com.hippo.xiazai.destination.FileDestination;
import java.io.File;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.Request;
import rx.Observable;
import rx.Scheduler;

public final class XiaZaiTask {

  private boolean occupied;

  private XiaZai xiazai;

  private Request request;
  private XiaZaiDestination dest;

  private boolean enableInterval;
  private long interval;
  private TimeUnit unit;
  private Scheduler scheduler;

  private XiaZaiController controller;

  private XiaZaiTask(Builder builder) {
    this.xiazai = builder.xiazai;
    this.request = builder.request;
    this.dest = builder.dest;
    this.enableInterval = builder.enableInterval;
    this.interval = builder.interval;
    this.unit = builder.unit;
    this.scheduler = builder.scheduler;
    controller = new XiaZaiController();
  }

  // Occupy this XiaZaiTask
  // Throw IllegalStateException if occupy it twice
  synchronized void occupy() throws IllegalStateException {
    if (!occupied) {
      occupied = true;
    } else {
      throw new IllegalStateException("The XiaZaiTask is already occupied.");
    }
  }

  Request request() {
    return request;
  }

  XiaZaiDestination destination() {
    return dest;
  }

  XiaZaiController controller() {
    return controller;
  }

  /**
   * Cancel this task.
   */
  public void cancel() {
    controller.cancel();
  }

  /**
   * Return a {@code Observable<XiaZaiProgress>}, subscribe it to download.
   * {@link com.hippo.xiazai.exception.CancelledException} in
   * {@link rx.Observer#onError(Throwable)} represents
   * {@link XiaZaiCallback#onCancelled()}.
   * <p>
   * You may call {@link rx.Observable#subscribeOn(Scheduler)} and
   * {@link rx.Observable#observeOn(Scheduler)}.
   * <p>
   * Note: Do <b>NOT</b> call it twice or throw IllegalStateException.
   */
  public Observable<XiaZaiProgress> xiazai() throws IllegalStateException {
    occupy();
    Observable<XiaZaiProgress> observable = Observable.create(new XiaZaiOnSubscribe(xiazai, this));
    if (enableInterval) {
      observable = observable.lift(new XiaZaiIntervalOperator(interval, unit, scheduler));
    }
    return observable;
  }

  public static class Builder {

    private XiaZai xiazai;

    private Request request;
    private XiaZaiDestination dest;

    private boolean enableInterval;
    private long interval;
    private TimeUnit unit;
    private Scheduler scheduler;

    public Builder(XiaZai xiazai) {
      this.xiazai = xiazai;
    }

    public Builder url(String url) {
      return request(new Request.Builder().get().url(url).build());
    }

    public Builder url(HttpUrl url) {
      return request(new Request.Builder().get().url(url).build());
    }

    public Builder request(Request request) {
      this.request = request;
      return this;
    }

    public Builder file(File file) {
      return destination(new FileDestination(file));
    }

    public Builder destination(XiaZaiDestination dest) {
      this.dest = dest;
      return this;
    }

    /**
     * Set emitting interval of {@link XiaZaiProgress} in milliseconds.
     * Keep emitting {@code XiaZaiProgress(0, 0, -1)} before actually starting downloading.
     */
    public Builder interval(long interval, TimeUnit unit, Scheduler scheduler) {
      this.enableInterval = true;
      this.interval = interval;
      this.unit = unit;
      this.scheduler = scheduler;
      return this;
    }

    public XiaZaiTask build() throws IllegalStateException {
      if (request == null || dest == null) {
        throw new IllegalStateException("request == null || dest == null");
      }
      if (enableInterval && scheduler == null) {
        throw new IllegalStateException("enableInterval && scheduler == null");
      }
      return new XiaZaiTask(this);
    }
  }
}
