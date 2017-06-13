/*
 * Copyright (C) 2017 Greyfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greyfox.rxnetwork.internal.strategy.network.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import greyfox.rxnetwork.BuildConfig;
import greyfox.rxnetwork.internal.net.RxNetworkInfo;
import io.reactivex.observers.TestObserver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static greyfox.rxnetwork.internal.net.RxNetworkInfoHelper.getRxNetworkInfoFrom;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class PreLollipopNetworkObservingStrategyTest {

  private final TestObserver<RxNetworkInfo> testObserver = new TestObserver<>();

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  private BaseNetworkObservingStrategy sut;
  private RxNetworkInfo validRxnetworkInfo;
  private Context context;

  @Before
  public void setUp() {
    context = spy(RuntimeEnvironment.application.getApplicationContext());
    sut = spy(new PreLollipopNetworkObservingStrategy(context));
    validRxnetworkInfo = getRxNetworkInfoFrom(context);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrow_whenTryingToInstantiateWithNullContext() {
    new PreLollipopNetworkObservingStrategy(null);
  }

  @Test
  public void shouldReceiveCorrectValue_whenConnectivityChanges() {
    sut.observe().subscribeWith(testObserver);

    RuntimeEnvironment.application.sendBroadcast(new Intent(CONNECTIVITY_ACTION));
    testObserver.assertSubscribed().assertValue(validRxnetworkInfo);
  }

  @Test
  public void shouldDisposeCorrectly_whenObserverDisposed() {
    sut.observe().subscribeWith(testObserver).assertSubscribed();

    testObserver.dispose();

    verify(sut).dispose();
    testObserver.isDisposed();
  }

  @Test
  public void shouldLogError_whenUnregisterException() {
    doThrow(Exception.class).when(context).unregisterReceiver(any(BroadcastReceiver.class));
    sut.observe().subscribeWith(testObserver).assertSubscribed();

    testObserver.dispose();

    verify(sut).dispose();
    testObserver.isDisposed();
    verify(sut).onError(anyString(), any(Exception.class));
  }
}
