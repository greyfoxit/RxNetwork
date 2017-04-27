package greyfox.rxnetwork.internal.strategy;

import io.reactivex.Observable;

/**
 * @author Radek Kozak
 */
public interface ObservingStrategy<T> {

    Observable<T> observe();
}
