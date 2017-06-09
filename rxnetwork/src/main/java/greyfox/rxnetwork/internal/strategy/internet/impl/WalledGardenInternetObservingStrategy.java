package greyfox.rxnetwork.internal.strategy.internet.impl;

import android.support.annotation.NonNull;
import greyfox.rxnetwork.internal.strategy.internet.error.InternetObservingStrategyException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Logger;

import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;
import static java.util.logging.Logger.getLogger;

/**
 * @author Radek Kozak
 */
public final class WalledGardenInternetObservingStrategy
    extends UrlConnectionInternetObservingStrategy {

  WalledGardenInternetObservingStrategy(@NonNull Builder builder) {
    super(builder);
  }

  @NonNull
  public static WalledGardenInternetObservingStrategy create() {
    return builder().build();
  }

  @NonNull
  public static Builder builder() {
    return new WalledGardenInternetObservingStrategy.Builder();
  }

  @Override
  boolean isConnected(@NonNull HttpURLConnection urlConnection)
      throws InternetObservingStrategyException {

    checkNotNull(urlConnection, "urlConnection");

    try {
      return urlConnection.getResponseCode() == 204;
    } catch (IOException ioe) {
      throw new InternetObservingStrategyException("Unable to check internet access", ioe);
    }
  }

  // @formatter:off

  @Override
  Logger logger() {
    return getLogger(WalledGardenInternetObservingStrategy.class.getSimpleName());
  }

  public static final class Builder extends
      UrlConnectionInternetObservingStrategy.Builder<WalledGardenInternetObservingStrategy,
          WalledGardenInternetObservingStrategy.Builder> {

    // @formatter:on

    private static final String DEFAULT_ENDPOINT = "http://g.cn/generate_204";
    private static final int DEFAULT_TIMEOUT_MS = 3000;

    Builder() {
      super();
      endpoint(DEFAULT_ENDPOINT);
      timeout(DEFAULT_TIMEOUT_MS);
    }

    /**
     * Returns a {@code WalledGardenInternetObservingStrategy} built from the parameters
     * previously set.
     *
     * @return a {@code WalledGardenInternetObservingStrategy} built with parameters
     * of this {@code WalledGardenInternetObservingStrategy.Builder}
     */
    @NonNull
    @Override
    public WalledGardenInternetObservingStrategy build() {
      return new WalledGardenInternetObservingStrategy(this);
    }
  }
}
