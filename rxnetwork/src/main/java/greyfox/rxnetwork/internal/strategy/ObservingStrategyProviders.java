package greyfox.rxnetwork.internal.strategy;

import java.util.Collection;

/**
 * @author Radek Kozak
 */

public interface ObservingStrategyProviders<T extends ObservingStrategyProvider> {

    Collection<T> get();
}
