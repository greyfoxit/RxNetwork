package greyfox.rxnetwork2.internal.strategy;

/**
 * @author radekkozak
 */
public interface NetworkObservingStrategyFactory<T extends NetworkObservingStrategy> {

    T get();
}
