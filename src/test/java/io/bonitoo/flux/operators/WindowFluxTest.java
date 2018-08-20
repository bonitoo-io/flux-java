/*
 * The MIT License
 * Copyright © 2018
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.bonitoo.flux.operators;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import io.bonitoo.flux.Flux;
import io.bonitoo.flux.FluxChain;
import io.bonitoo.flux.operators.properties.TimeInterval;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (27/06/2018 12:42)
 */
@RunWith(JUnitPlatform.class)
class WindowFluxTest {

    @Test
    void windowEveryChronoUnit() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> window(every: 15m)");
    }

    @Test
    void unSupportedChronoUnit() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.DECADES);

        Assertions.assertThatThrownBy(flux::print)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unit must be one of: NANOS, MICROS, MILLIS, SECONDS, MINUTES, HOURS, HALF_DAYS, DAYS, WEEKS, MONTHS, YEARS");
    }

    @Test
    void windowEveryPeriodChronoUnit() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES, 20L, ChronoUnit.SECONDS);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> window(every: 15m, period: 20s)");
    }

    @Test
    void windowEveryPeriodStartChronoUnit() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.HALF_DAYS, 20L, ChronoUnit.SECONDS, -50L, ChronoUnit.DAYS);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> window(every: 180h, period: 20s, start: -50d)");
    }

    @Test
    void windowEveryPeriodStartInstant() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES, 20L, ChronoUnit.SECONDS, Instant.ofEpochSecond(1_750_000));

        String expected = "from(db:\"telegraf\") |> window(every: 15m, period: 20s, start: 1970-01-21T06:06:40.000000000Z)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void windowEveryPeriodStartRoundChronoUnit() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES,
                        20L, ChronoUnit.SECONDS,
                        -50L, ChronoUnit.WEEKS,
                        1L, ChronoUnit.SECONDS);

        String expected = "from(db:\"telegraf\") |> window(every: 15m, period: 20s, start: -50w, round: 1s)";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void windowEveryPeriodStartRoundInstant() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES,
                        20L, ChronoUnit.SECONDS,
                        Instant.ofEpochSecond(1_750_000),
                        1L, ChronoUnit.SECONDS);

        String expected = "from(db:\"telegraf\") |> "
                + "window(every: 15m, period: 20s, start: 1970-01-21T06:06:40.000000000Z, round: 1s)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void windowEveryPeriodStartString() {

        Flux flux = Flux
                .from("telegraf")
                .window()
                .withEvery("10s").withPeriod("30m").withStart("-1d");

        String expected = "from(db:\"telegraf\") |> "
                + "window(every: 10s, period: 30m, start: -1d)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void windowEveryPeriodStartRoundChronoUnitColumns() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES,
                        20L, ChronoUnit.SECONDS,
                        -50L, ChronoUnit.DAYS,
                        1L, ChronoUnit.HOURS,
                        "time", "superStart", "totalEnd");

        String expected = "from(db:\"telegraf\") |> "
                + "window(every: 15m, period: 20s, start: -50d, round: 1h, column: \"time\", "
                + "startCol: \"superStart\", stopCol: \"totalEnd\")";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void windowEveryPeriodStartRoundInstantColumns() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES,
                        20L, ChronoUnit.SECONDS,
                        Instant.ofEpochSecond(1_750_000),
                        1L, ChronoUnit.SECONDS,
                        "time", "superStart", "totalEnd");

        String expected = "from(db:\"telegraf\") |> "
                + "window(every: 15m, period: 20s, start: 1970-01-21T06:06:40.000000000Z, round: 1s, column: \"time\", "
                + "startCol: \"superStart\", stopCol: \"totalEnd\")";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void namedParameters() {

        Flux flux = Flux
                .from("telegraf")
                .window()
                .withPropertyNamed("every")
                .withPropertyNamed("period")
                .withPropertyNamed("start")
                .withPropertyNamed("round");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("every", new TimeInterval(15L, ChronoUnit.MINUTES));
        parameters.put("period", new TimeInterval(20L, ChronoUnit.SECONDS));
        parameters.put("start", new TimeInterval(-50L, ChronoUnit.DAYS));
        parameters.put("round", new TimeInterval(1L, ChronoUnit.HOURS));

        String expected = "from(db:\"telegraf\") |> "
                + "window(every: 15m, period: 20s, start: -50d, round: 1h)";

        Assertions.assertThat(flux.print(new FluxChain().addParameters(parameters))).isEqualToIgnoringWhitespace(expected);
    }
}