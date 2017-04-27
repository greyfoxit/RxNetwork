package greyfox.rxnetwork.internal.strategy.internet.error;

/**
 * @author Radek Kozak
 */

public class InternetObservingStrategyException extends Exception {

    public InternetObservingStrategyException() {
        super();
    }

    public InternetObservingStrategyException(String message) {
        super(message);
    }

    public InternetObservingStrategyException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternetObservingStrategyException(Throwable cause) {
        super(cause);
    }
}
