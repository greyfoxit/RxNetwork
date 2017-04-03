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
package greyfox.rxnetwork2.internal.strategy.network.impl;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.net.ConnectivityManager;
import greyfox.rxnetwork2.BuildConfig;
import greyfox.rxnetwork2.internal.net.RxNetworkInfo;
import io.reactivex.observers.TestObserver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * @author Radek Kozak
 */
@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = LOLLIPOP)
public class LollipopNetworkObservingStrategyTest {

    @Rule public MockitoRule rule = MockitoJUnit.rule();

    @Mock Context context;
    @Mock ConnectivityManager connectivityManager;

    BuiltInNetworkObservingStrategy sut;
    TestObserver<RxNetworkInfo> testObserver = new TestObserver<>();

    @Before
    public void setUp() {
        when(context.getSystemService(CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);
        sut = spy(new LollipopNetworkObservingStrategy(context));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToInstantiateWithoutContext() {
        new LollipopNetworkObservingStrategy(null);
    }

    @Test
    public void shouldSubscribeCorrectly() {
        sut.observe().subscribeWith(testObserver);

        testObserver.assertSubscribed().assertEmpty();
    }

    @Test
    public void shouldDisposeCorrectly() {
        sut.observe().subscribeWith(testObserver).assertSubscribed();

        testObserver.dispose();

        verify(sut).dispose();
        testObserver.isDisposed();
    }
}
