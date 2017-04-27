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

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.support.annotation.RequiresApi;
import greyfox.rxnetwork.BuildConfig;
import greyfox.rxnetwork.helpers.robolectric.shadows.ShadowConnectivityManagerWithCallback;
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

/**
 * @author Radek Kozak
 */
@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
@RequiresApi(api = LOLLIPOP)
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = LOLLIPOP,
        shadows = ShadowConnectivityManagerWithCallback.class)
public class LollipopNetworkObservingStrategyTest {

    @Rule public MockitoRule rule = MockitoJUnit.rule();

    Context context;

    BaseNetworkObservingStrategy sut;
    TestObserver<RxNetworkInfo> testObserver = new TestObserver<>();

    @Before
    public void setUp() {
        context = spy(RuntimeEnvironment.application.getApplicationContext());
        sut = spy(new LollipopNetworkObservingStrategy(context));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToInstantiateWithoutContext() {
        new LollipopNetworkObservingStrategy(null);
    }

    @Test
    public void shouldSubscribeCorrectly() {
        sut.observe().subscribeWith(testObserver);

        testObserver.assertSubscribed().assertValueCount(1);
    }

    @Test
    public void shouldDisposeCorrectly() {
        sut.observe().subscribeWith(testObserver).assertSubscribed();

        testObserver.dispose();

        verify(sut).dispose();
        testObserver.isDisposed();
    }

    @Test
    public void shouldDisposeWithException_whenDisposed() {
        ConnectivityManager connectivityManager = mock(ConnectivityManager.class);
        doReturn(connectivityManager).when(context).getSystemService(Context.CONNECTIVITY_SERVICE);
        doThrow(Exception.class).when(connectivityManager)
                .unregisterNetworkCallback(any(NetworkCallback.class));
        sut = spy(new LollipopNetworkObservingStrategy(context));

        sut.observe().subscribeWith(testObserver).assertSubscribed();
        testObserver.dispose();

        verify(sut).dispose();
        verify(sut).onError(anyString(), any(Exception.class));
        testObserver.isDisposed();
    }
}
