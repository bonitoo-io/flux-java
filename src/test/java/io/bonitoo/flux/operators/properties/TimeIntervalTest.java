package io.bonitoo.flux.operators.properties;

import java.time.temporal.ChronoUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (20/08/2018 11:56)
 */
@RunWith(JUnitPlatform.class)
class TimeIntervalTest {

    @Test
    void supportMonth() {
        TimeInterval timeInterval = new TimeInterval(10L, ChronoUnit.MONTHS);

        Assertions.assertThat("10mo").isEqualTo(timeInterval.toString());
    }

    @Test
    void supportYear() {
        TimeInterval timeInterval = new TimeInterval(15L, ChronoUnit.YEARS);

        Assertions.assertThat("15y").isEqualTo(timeInterval.toString());
    }
}
