package greyfox.rxnetwork2.internal.strategy;

/**
 * @author Radek Kozak
 */

public interface ObservingStrategyFactory<T extends ObservingStrategy> {

    T get();
}
