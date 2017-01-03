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

package com.hippo.xiazai.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.hippo.xiazai.XiaZai;
import com.hippo.xiazai.XiaZaiProgress;
import com.hippo.xiazai.XiaZaiTask;
import java.io.File;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final LogView logView = new LogView(this);
    setContentView(logView);

    OkHttpClient client = new OkHttpClient.Builder().build();
    XiaZai xiaZai = new XiaZai(client);
    new XiaZaiTask.Builder(xiaZai)
        .url("http://dl.coolapkmarket.com/down/apk_file/2016/0714/com.hippo.nimingban-1.2.28-42.apk?_upt=46a4b7a01483432154")
        .file(new File(getCacheDir(), "temp"))
        .interval(2, TimeUnit.SECONDS, Schedulers.computation())
        .build()
        .xiazai()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<XiaZaiProgress>() {
          @Override
          public void onCompleted() {
            logView.println(Thread.currentThread().getName() + " onCompleted");
          }

          @Override
          public void onError(Throwable e) {
            logView.println(Log.ERROR, Thread.currentThread().getName() + " onError", e);
          }

          @Override
          public void onNext(XiaZaiProgress progress) {
            logView.println(Thread.currentThread().getName() + " onNext n       " + progress.n());
            logView.println(Thread.currentThread().getName() + " onNext read    " + progress.read());
            logView.println(Thread.currentThread().getName() + " onNext content " + progress.content());
          }
        });
  }
}
