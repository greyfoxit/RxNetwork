package greyfox.rxnetwork.internal.strategy.internet.impl;

import android.support.annotation.NonNull;
import greyfox.rxnetwork.internal.strategy.internet.error.InternetObservingStrategyException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Logger;

import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.util.logging.Logger.getLogger;

/**
 * Library's default for monitoring real internet connectivity changes.
 * <p>
 * <b>This strategy takes care of captive portals / walled-garden internet scenarios</b>
 * by using Android team's solution (as seen in {@code android.net.wifi.WifiWatchdogStateMachine})
 * <p>
 * This strategy uses {@linkplain HttpURLConnection#HTTP_NO_CONTENT} (Status-Code 204)
 * as default check against given endpoint.
 *
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
      return urlConnection.getResponseCode() == HTTP_NO_CONTENT;
    } catch (IOException ioe) {
      throw new InternetObservingStrategyException("Unable to check internet access", ioe);
    }
  }

  // @formatter:off

  @Override
  Logger logger() {
    return getLogger(WalledGardenInternetObservingStrategy.class.getSimpleName());
  }

  /** Build a new {@link WalledGardenInternetObservingStrategy}. */
  public static final class Builder extends
      UrlConnectionInternetObservingStrategy.Builder<WalledGardenInternetObservingStrategy,
          WalledGardenInternetObservingStrategy.Builder> {

    // @formatter:on

    private static final String DEFAULT_ENDPOINT = "http://google.cn/generate_204";
    private static final int DEFAULT_TIMEOUT_MS = 3000;

    Builder() {
      super();
      endpoint(DEFAULT_ENDPOINT);
      timeout(DEFAULT_TIMEOUT_MS);
    }

    /**
     * Create an immutable instance of {@link WalledGardenInternetObservingStrategy} using
     * configured values.
     */
    @NonNull
    @Override
    public WalledGardenInternetObservingStrategy build() {
      return new WalledGardenInternetObservingStrategy(this);
    }
  }
}
