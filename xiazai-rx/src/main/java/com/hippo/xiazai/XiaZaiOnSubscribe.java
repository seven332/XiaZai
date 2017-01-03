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

import com.hippo.xiazai.exception.CancelledException;
import java.util.concurrent.atomic.AtomicBoolean;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

final class XiaZaiOnSubscribe implements Observable.OnSubscribe<XiaZaiProgress> {

  private XiaZai xiazai;
  private XiaZaiTask task;

  public XiaZaiOnSubscribe(XiaZai xiazai, XiaZaiTask task) {
    this.xiazai = xiazai;
    this.task = task;
  }

  @Override
  public void call(final Subscriber<? super XiaZaiProgress> subscriber) {
    subscriber.add(new XiaZaiTaskSubscription());
    xiazai.xiazai(task.request(), task.destination(),
        task.controller(), new InnerXiaZaiCallback(subscriber));
  }

  private class InnerXiaZaiCallback implements XiaZaiCallback {

    private Subscriber<? super XiaZaiProgress> subscriber;

    public InnerXiaZaiCallback(Subscriber<? super XiaZaiProgress> subscriber) {
      this.subscriber = subscriber;
    }

    @Override
    public void onStart() {
      if (!subscriber.isUnsubscribed()) {
        // Emit a dump XiaZaiProgress to represent XiaZaiCallback.onStart().
        // n and read will never be 0, so it is unique.
        subscriber.onNext(new XiaZaiProgress(0, 0, -1));
      }
    }

    @Override
    public void onProgress(int n, long read, long content) {
      if (!subscriber.isUnsubscribed()) {
        subscriber.onNext(new XiaZaiProgress(n, read, content));
      }
    }

    @Override
    public void onCancelled() {
      if (!subscriber.isUnsubscribed()) {
        subscriber.onError(new CancelledException());
      }
    }

    @Override
    public void onCompleted() {
      if (!subscriber.isUnsubscribed()) {
        subscriber.onCompleted();
      }
    }

    @Override
    public void onError(final Throwable e) {
      if (!subscriber.isUnsubscribed()) {
        subscriber.onError(e);
      }
    }
  }

  private class XiaZaiTaskSubscription implements Subscription {

    private AtomicBoolean unsubscribed = new AtomicBoolean();

    @Override
    public void unsubscribe() {
      if (unsubscribed.compareAndSet(false, true)) {
        task.cancel();
      }
    }

    @Override
    public boolean isUnsubscribed() {
      return unsubscribed.get();
    }
  }
}
