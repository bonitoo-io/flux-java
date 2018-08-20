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
package io.bonitoo.flux;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import io.bonitoo.flux.options.query.AbstractOption;
import io.bonitoo.flux.options.query.LocationOption;
import io.bonitoo.flux.options.query.NowOption;
import io.bonitoo.flux.options.query.TaskOption;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (22/06/2018 10:27)
 */
@RunWith(JUnitPlatform.class)
class FluxTest {

    @Test
    void flux1() {

        String flux = Flux
                .from("telegraf")
                .count()
                .print();

        Assertions.assertThat(flux).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> count()");
    }

    @Test
    void fluxWithOneOptions() {

        NowOption now = NowOption.builder()
                .function("giveMeTime()")
                .build();

        List<AbstractOption> options = new ArrayList<>();
        options.add(now);

        String flux = Flux
                .from("telegraf")
                .count()
                .print(new FluxChain().addOptions(options));

        Assertions.assertThat(flux).isEqualToIgnoringWhitespace("option now = giveMeTime() from(db:\"telegraf\") |> count()");
    }

    @Test
    void fluxWithTwoOptions() {

        NowOption now = NowOption.builder()
                .function("giveMeTime()")
                .build();

        TaskOption task = TaskOption.builder("foo")
                .every(1L, ChronoUnit.HOURS)
                .delay(10L, ChronoUnit.MINUTES)
                .cron("0 2 * * *")
                .retry(5)
                .build();

        List<AbstractOption> options = new ArrayList<>();
        options.add(now);
        options.add(task);

        String flux = Flux
                .from("telegraf")
                .count()
                .print(new FluxChain().addOptions(options));

        String expected = "option now = giveMeTime() "
                + "option task = {name: \"foo\", every: 1h, delay: 10m, cron: \"0 2 * * *\", retry: 5} "
                + "from(db:\"telegraf\") |> count()";

        Assertions.assertThat(flux).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void fluxWithLocationOptions() {

        NowOption now = NowOption.builder()
                .function("giveMeTime()")
                .build();

        LocationOption location = LocationOption.builder()
                .offset("10d")
                .build();

        List<AbstractOption> options = new ArrayList<>();
        options.add(now);
        options.add(location);

        String flux = Flux
                .from("telegraf")
                .count()
                .print(new FluxChain().addOptions(options));

        Assertions.assertThat(flux).isEqualToIgnoringWhitespace("option now = giveMeTime() option location = fixedZone(offset: 10d) from(db:\"telegraf\") |> count()");
    }

    @Test
    void propertyValueEscapedNull() {

        Flux flux = Flux
                .from("telegraf")
                .count()
                    .withPropertyValueEscaped("unused", null);

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> count()");
    }

    @Test
    void propertyValueNull() {

        Flux flux = Flux
                .from("telegraf")
                .count()
                    .withPropertyValue("unused", null);

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> count()");
    }

    @Test
    void propertyValueAmountNull() {

        Flux flux = Flux
                .from("telegraf")
                .count()
                    .withPropertyValue("unused", null, ChronoUnit.HOURS);

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> count()");
    }

    @Test
    void propertyValueUnitNull() {

        Flux flux = Flux
                .from("telegraf")
                .count()
                    .withPropertyValue("unused", 10L, null);

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> count()");
    }
}