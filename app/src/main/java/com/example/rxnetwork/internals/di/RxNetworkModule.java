package com.example.rxnetwork.internals.di;

import android.app.Application;
import greyfox.rxnetwork2.RxNetwork;
import greyfox.rxnetwork2.internal.strategy.network.factory.BuiltInStrategyFactory;
import toothpick.config.Module;

/**
 * DI module for all {@link RxNetwork} - related injections.
 *
 * @author radekkozak
 */
public class RxNetworkModule extends Module {

    public RxNetworkModule(Application application) {

        RxNetwork rxNetwork = RxNetwork.builder()
                .networkObservingStrategyFactory(BuiltInStrategyFactory.create(null))
                .init(application);

        bind(RxNetwork.class).toInstance(rxNetwork);
    }
}
