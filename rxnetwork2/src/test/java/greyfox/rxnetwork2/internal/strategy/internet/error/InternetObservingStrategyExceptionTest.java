package greyfox.rxnetwork2.internal.strategy.internet.error;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;

/**
 * @author Radek Kozak
 */
@SuppressWarnings({"WeakerAccess", "ThrowableInstanceNeverThrown"})
public class InternetObservingStrategyExceptionTest {

    InternetObservingStrategyException sut;
    String VALID_MESSAGE = "valid message";
    Throwable VALID_CAUSE = new Throwable();

    @Test
    public void shouldHaveNoCause_andNoMessage() {
        sut = new InternetObservingStrategyException();

        assertThat(sut).hasNoCause().hasMessage(null);
    }

    @Test
    public void shouldHaveValidMessage() {
        sut = new InternetObservingStrategyException(VALID_MESSAGE);

        assertThat(sut).hasNoCause().hasMessage(VALID_MESSAGE);
    }

    @Test
    public void shouldHaveValidMessage_andValidCause() {
        sut = new InternetObservingStrategyException(VALID_MESSAGE, VALID_CAUSE);

        assertThat(sut).hasCause(VALID_CAUSE).hasMessage(VALID_MESSAGE);
    }

    @Test
    public void shouldHaveValidCause() {
        sut = new InternetObservingStrategyException(VALID_CAUSE);

        assertThat(sut).hasCause(VALID_CAUSE);
    }
}
