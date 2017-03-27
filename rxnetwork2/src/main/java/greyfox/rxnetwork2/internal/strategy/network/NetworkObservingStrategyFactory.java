package greyfox.rxnetwork2.internal.strategy.network;

/**
 * Factory class for {@link NetworkObservingStrategy}.
 *
 * @author radekkozak
 */
public interface NetworkObservingStrategyFactory<T extends NetworkObservingStrategy> {

    T get();
}
