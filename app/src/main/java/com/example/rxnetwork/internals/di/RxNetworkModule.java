package com.example.rxnetwork.internals.di;

import android.app.Application;
import greyfox.rxnetwork.RxNetwork;
import toothpick.config.Module;

/**
 * DI module for all {@link RxNetwork} - related injections.
 *
 * @author radekkozak
 */
public class RxNetworkModule extends Module {

    public RxNetworkModule(Application application) {
        bind(RxNetwork.class).toInstance(RxNetwork.init(application));
    }
}
