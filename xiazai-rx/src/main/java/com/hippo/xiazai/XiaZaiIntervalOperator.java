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
 * Created by Hippo on 1/3/2017.
 */

import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.internal.operators.OperatorBufferWithTime;
import rx.observers.SerializedSubscriber;

/**
 * This operation emits a sequential {@link XiaZaiProgress} every specified interval of time.
 * It keeps emitting {@code XiaZaiProgress(0, 0, -1)} before actually starting downloading.
 * <p>
 * Inspired by {@link OperatorBufferWithTime}.
 */
public class XiaZaiIntervalOperator implements
    Observable.Operator<XiaZaiProgress, XiaZaiProgress> {

  final long interval;
  final TimeUnit unit;
  final Scheduler scheduler;

  public XiaZaiIntervalOperator(long interval, TimeUnit unit, Scheduler scheduler) {
    this.interval = interval;
    this.unit = unit;
    this.scheduler = scheduler;
  }

  @Override
  public Subscriber<? super XiaZaiProgress> call(Subscriber<? super XiaZaiProgress> child) {
    final Scheduler.Worker inner = scheduler.createWorker();
    SerializedSubscriber<XiaZaiProgress> serialized = new SerializedSubscriber<>(child);

    ExactSubscriber parent = new ExactSubscriber(serialized, inner);
    parent.add(inner);
    child.add(parent);
    parent.scheduleExact();
    return parent;
  }

  final class ExactSubscriber extends Subscriber<XiaZaiProgress> {

    final Subscriber<? super XiaZaiProgress> child;
    final Scheduler.Worker inner;
    // Guarded by this
    long lastRead = 0;
    // Guarded by this
    long read = 0;
    // Guarded by this
    long content = -1;
    // Guarded by this
    boolean done;

    public ExactSubscriber(Subscriber<XiaZaiProgress> child, Scheduler.Worker inner) {
      this.child = child;
      this.inner = inner;
    }

    @Override
    public void onNext(XiaZaiProgress progress) {
      synchronized (this) {
        if (done) return;
        read = progress.read();
        content = progress.content();
      }
    }

    @Override
    public void onError(Throwable e) {
      synchronized (this) {
        if (done) return;
        done = true;
      }
      child.onError(e);
      unsubscribe();
    }

    @Override
    public void onCompleted() {
      try {
        inner.unsubscribe();
        long n, read, content;
        synchronized (this) {
          if (done) return;
          done = true;
          n = this.read - this.lastRead;
          read = this.read;
          content = this.content;
          this.lastRead = read;
        }
        // Only emit a item if progress report isn't done
        if (n != 0) child.onNext(new XiaZaiProgress(n, read, content));
      } catch (Throwable t) {
        Exceptions.throwOrReport(t, child);
        return;
      }
      child.onCompleted();
      unsubscribe();
    }

    private void scheduleExact() {
      inner.schedulePeriodically(new Action0() {
        @Override
        public void call() {
          emit();
        }
      }, 0, interval, unit);
    }

    private void emit() {
      long n, read, content;
      synchronized (this) {
        if (done) return;
        n = this.read - this.lastRead;
        read = this.read;
        content = this.content;
        this.lastRead = read;
      }
      XiaZaiProgress progress = new XiaZaiProgress(n, read, content);
      try {
        child.onNext(progress);
      } catch (Throwable t) {
        Exceptions.throwOrReport(t, this);
      }
    }
  }
}
