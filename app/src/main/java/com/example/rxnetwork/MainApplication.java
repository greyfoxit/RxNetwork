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
package com.example.rxnetwork;

import static toothpick.registries.FactoryRegistryLocator.setRootRegistry;
import static toothpick.registries.MemberInjectorRegistryLocator.setRootRegistry;

import android.app.Application;
import com.example.rxnetwork.internals.di.RxNetworkModule;
import greyfox.rxnetwork.RxNetwork;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.configuration.Configuration;

/**
 * Entry point for the whole application.
 *
 * @author Radek Kozak
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        setupDi();
    }

    private void setupDi() {

        // If not using the reflection free configuration, the next 3 lines can be omitted
        Toothpick.setConfiguration(Configuration.forProduction().disableReflection());
        setRootRegistry(new com.example.rxnetwork.MemberInjectorRegistry());
        setRootRegistry(new com.example.rxnetwork.FactoryRegistry());

        Scope statusScope = Toothpick.openScope(RxNetwork.class);
        statusScope.installModules(new RxNetworkModule(this));
    }
}
