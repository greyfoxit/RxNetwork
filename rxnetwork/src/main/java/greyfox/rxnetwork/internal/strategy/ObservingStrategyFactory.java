package greyfox.rxnetwork.internal.strategy;

/**
 * @author Radek Kozak
 */

public interface ObservingStrategyFactory<T extends ObservingStrategy> {

    T get();
}
