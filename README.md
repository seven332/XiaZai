# XiaZai

这是一个 Android 平台上基于 [OkHttp](https://github.com/square/okhttp) 的简单下载库。

It's a simple Android download library based on [OkHttp](https://github.com/square/okhttp).


# Usage

在最外面的 `build.gradle` 里加上 jitpack，别加到 buildscript 里了。

Add jitpack repository in top `build.gradle`, DO **NOT** ADD IT TO buildscript.

    allprojects {
        repositories {
            ...
            maven { url "https://jitpack.io" }
        }
    }

在项目 `build.gradle` 里添加 XiaZai 依赖。

Add XiaZai as dependency in project `build.gradle`.

    dependencies {
        ...
        compile 'com.github.xiazai:xiazai:0.1.0'
        // 往下看
        // See below
        compile 'com.github.xiazai:xiazai-rx:0.1.0'
    }


## xiazai

这一部分只提供了单线程且线程阻塞的方法，`xiazai()`。

This part only provides thread-blocking methods，`xiazai()`.

    OkHttpClient client;
    String url;
    File file;
    XiaZai xiaZai = new XiaZai(client);
    int state = xiaZai.xiazai(url, file);

`xiazai()` 返回值为 `@XiaZai.State int`，来表示下载状态。如果你希望更详细的信息，请使用 `XiaZaiCallback`。


### XiaZaiController

这是用来在其他线程取消下载的。

To cancel downloading in other thread.

### XiaZaiCallback

下载状态的回调函数，包括 `onStart()`，`onProgress(int, long, long)`，`onCancelled()`，`onCompleted()`，`onError(Throwable)`。`onCancelled()`，`onCompleted()`，`onError(Throwable)` 只有一个会被调用。

Download callback, including `onStart()`, `onProgress(int, long, long)`, `onCancelled()`, `onCompleted()`, `onError(Throwable)`. Only one of `onCancelled()`, `onCompleted()`, `onError(Throwable)` will be called.

### XiaZaiDestination

这是用来存储下载数据，需要实现 `OutputStream open(HttpUrl, int, Headers)`。建议使用 `BaseDestination`，它会帮你从返回首部中获取文件名。

Implement `OutputStream open(HttpUrl, int, Headers)` to return `OutputStream` to store downloaded data. You'd better use `BaseDestination` which can get filename from respond headers.


## xiazai-rx

这一部分使用 RxJava 1 来提供更方便的下载功能。

This part uses RxJava 1 to provide convenient downloading method.

    OkHttpClient client;
    String url;
    File file;
    // 创建一个 XiaZai
    // Create a XiaZai
    XiaZai xiaZai = new XiaZai(client);
    // 创建一个 XiaZaiTask
    // Create a XiaZaiTask
    XiaZaiTask task = new XiaZaiTask.Builder(xiaZai)
        .url(url)
        .file(file)
        // 这个可以保证 Observable 以一定的速率发射数据
        // This can keep emitting rate of Observable
        .interval(2, TimeUnit.SECONDS)
        .build();
    // 创建 Observable<XiaZaiProgress>
    // Create Observable<XiaZaiProgress>
    Observable<XiaZaiProgress> observable = task.xiazai();
    // 调整线程
    // Set thread
    observable = observable
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    // 订阅并开始下载
    // Subscribe and start downloading
    Subscription subscription = observable.subscribe(new Subscriber<XiaZaiProgress>() {
        @Override
        public void onCompleted() {
          // ...
        }
        @Override
        public void onError(Throwable e) {
          // ...
        }
        @Override
        public void onNext(XiaZaiProgress progress) {
          // ...
        }
    });
    // 如果不想继续下载，可以取消订阅
    // Unsubscribe if you don't want to download anymore
    subscription.unsubscribe();


### XiaZaiProgress

简单的下载进度封装。

A simple download progress wrapper.

### XiaZaiIntervalOperator

用来控制发射速率。

To keep emitting rate.


# License

    Copyright (C) 2016 Hippo Seven

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
