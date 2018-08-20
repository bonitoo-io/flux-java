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

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import io.bonitoo.flux.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (03/07/2018 15:05)
 */
@RunWith(JUnitPlatform.class)
class DerivativeFluxTest {

    @Test
    void derivative() {

        Flux flux = Flux
                .from("telegraf")
                .derivative(1L, ChronoUnit.MINUTES);

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> derivative(unit: 1m)");
    }

    @Test
    void derivativeByParameters() {

        Flux flux = Flux
                .from("telegraf")
                .derivative()
                .withUnit(10L, ChronoUnit.DAYS)
                .withNonNegative(true)
                .withColumns(new String[]{"time1", "time2"})
                .withTimeSrc("_timeMy");

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> derivative(unit: 10d, nonNegative: true, columns: [\"time1\", \"time2\"], timeSrc: \"_timeMy\")");
    }

    @Test
    void derivativeByColumnsCollection() {

        List<String> columns = new ArrayList<>();
        columns.add("time");
        columns.add("century");

        Flux flux = Flux
                .from("telegraf")
                .derivative()
                .withUnit(15L, ChronoUnit.DAYS)
                .withColumns(columns);

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> derivative(unit: 15d, columns: [\"time\", \"century\"])");
    }

    @Test
    void derivativeByString() {

        Flux flux = Flux
                .from("telegraf")
                .derivative()
                    .withUnit("15s");

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> derivative(unit: 15s)");
    }
}