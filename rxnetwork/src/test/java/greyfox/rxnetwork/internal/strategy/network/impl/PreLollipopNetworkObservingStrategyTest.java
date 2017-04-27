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

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import greyfox.rxnetwork.BuildConfig;
import greyfox.rxnetwork.internal.net.RxNetworkInfo;
import greyfox.rxnetwork.internal.net.RxNetworkInfoHelper;
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

@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class PreLollipopNetworkObservingStrategyTest {

    @Rule public MockitoRule rule = MockitoJUnit.rule();

    Context context;

    BaseNetworkObservingStrategy sut;
    TestObserver<RxNetworkInfo> testObserver = new TestObserver<>();

    RxNetworkInfo VALID_RXNETWORK_INFO;

    @Before
    public void setUp() {
        context = spy(RuntimeEnvironment.application.getApplicationContext());
        sut = spy(new PreLollipopNetworkObservingStrategy(context));
        VALID_RXNETWORK_INFO = RxNetworkInfoHelper.getNetworkInfoFrom(context);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToInstantiateWithoutContext() {
        new PreLollipopNetworkObservingStrategy(null);
    }

    @Test
    public void shouldReceiveCorrectValue_whenConnectivityChanges() {
        sut.observe().subscribeWith(testObserver);

        RuntimeEnvironment.application.sendBroadcast(new Intent(CONNECTIVITY_ACTION));
        testObserver.assertSubscribed().assertValue(VALID_RXNETWORK_INFO);
    }

    @Test
    public void shouldDisposeCorrectly_whenDisposed() {
        sut.observe().subscribeWith(testObserver).assertSubscribed();

        testObserver.dispose();

        verify(sut).dispose();
        testObserver.isDisposed();
    }

    @Test
    public void shouldDisposeWithException_whenDisposed() {
        doThrow(Exception.class).when(context).unregisterReceiver(any(BroadcastReceiver.class));
        sut.observe().subscribeWith(testObserver).assertSubscribed();

        testObserver.dispose();

        verify(sut).dispose();
        verify(sut).onError(anyString(), any(Exception.class));
        testObserver.isDisposed();
    }
}
