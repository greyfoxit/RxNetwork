package greyfox.rxnetwork2.internal.strategy.internet.impl;

import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(Parameterized.class)
public class BaseEndpointInternetObservingStrategyTest {

    @Rule public MockitoRule rule = MockitoJUnit.rule();

    @Parameterized.Parameter
    public int INVALID_PORT;

    @Parameterized.Parameters
    public static Iterable<? extends Integer> invalidPorts() {
        return Arrays.asList(-1, 0, 65536);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrow_whenTryingToBuildStrategyWithInvalidPort() {
        SocketInternetObservingStrategy.builder().port(INVALID_PORT);
    }
}
