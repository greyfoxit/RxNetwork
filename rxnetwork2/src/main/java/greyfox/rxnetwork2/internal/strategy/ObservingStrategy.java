package greyfox.rxnetwork2.internal.strategy;

import io.reactivex.Observable;

/**
 * @author Radek Kozak
 */
public interface ObservingStrategy<T> {

    Observable<T> observe();
}
