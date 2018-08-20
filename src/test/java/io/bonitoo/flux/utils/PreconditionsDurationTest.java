package io.bonitoo.flux.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (20/08/2018 12:31)
 */
@SuppressWarnings("ConstantConditions")
@RunWith(JUnitPlatform.class)
class PreconditionsDurationTest {

    @Test
    void literals() {

        Preconditions.checkDuration("1s", "duration");
        Preconditions.checkDuration("10d", "duration");
        Preconditions.checkDuration("1h15m", "duration");
        Preconditions.checkDuration("5w", "duration");
        Preconditions.checkDuration("1mo5d", "duration");
        Preconditions.checkDuration("-1mo5d", "duration");
    }

    @Test
    void literalNull() {

        Assertions.assertThatThrownBy(() -> Preconditions.checkDuration(null, "duration"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a duration string for duration. But got: null");
    }

    @Test
    void literalEmpty() {

        Assertions.assertThatThrownBy(() -> Preconditions.checkDuration("", "duration"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a duration string for duration. But got: ");
    }
    @Test
    void literalNotDuration() {

        Assertions.assertThatThrownBy(() -> Preconditions.checkDuration("x", "duration"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a duration string for duration. But got: x");
    }
}