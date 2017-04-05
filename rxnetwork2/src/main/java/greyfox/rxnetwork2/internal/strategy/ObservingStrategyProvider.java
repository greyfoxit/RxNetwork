package greyfox.rxnetwork2.internal.strategy;

import greyfox.rxnetwork2.internal.strategy.network.NetworkObservingStrategy;

/**
 * @author Radek Kozak
 */

public interface ObservingStrategyProvider<T extends ObservingStrategy> {

    /**
     * Implement this method to determine under what condition your provider can {@link #provide()}
     * concrete {@link NetworkObservingStrategy}.
     *
     * @return {@code true} if concrete {@link NetworkObservingStrategy} can be provided
     * for given criteria, {@code false} if not
     */
    boolean canProvide();

    /**
     * Implement this to return concrete {@link NetworkObservingStrategy}.
     *
     * @return {@link NetworkObservingStrategy}
     */
    T provide();
}
