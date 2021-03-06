/*
 * Copyright (C) 2017 Greyfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.rxnetwork.internals.di;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.NetworkRequest;
import greyfox.rxnetwork.RxNetwork;
import greyfox.rxnetwork.internal.strategy.internet.InternetObservingStrategy;
import greyfox.rxnetwork.internal.strategy.internet.impl.HttpOkInternetObservingStrategy;
import toothpick.config.Module;

/**
 * DI module for all {@link RxNetwork} - related injections.
 *
 * @author radekkozak
 */
public class RxNetworkModule extends Module {

  @SuppressLint("NewApi")
  public RxNetworkModule(Context context) {

    InternetObservingStrategy customStrategy =
        HttpOkInternetObservingStrategy.builder().endpoint("http://captive.apple.com").delay(1000)
            .interval(5000).build();

    RxNetwork custom = RxNetwork.builder().internetObservingStrategy(customStrategy)
        .defaultNetworkRequest(new NetworkRequest.Builder().build()).init();

    bind(RxNetwork.class).toInstance(RxNetwork.init(context)); // default RxNetwork
    bind(RxNetwork.class).withName("custom").toInstance(custom);
  }
}
