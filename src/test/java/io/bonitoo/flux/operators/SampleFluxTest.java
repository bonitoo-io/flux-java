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

import io.bonitoo.flux.Flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static io.bonitoo.flux.operators.restriction.Restrictions.and;
import static io.bonitoo.flux.operators.restriction.Restrictions.field;
import static io.bonitoo.flux.operators.restriction.Restrictions.measurement;

/**
 * @author Jakub Bednar (bednar@github) (29/06/2018 07:37)
 */
@RunWith(JUnitPlatform.class)
class SampleFluxTest {

    @Test
    void sampleN() {

        Flux flux = Flux.from("telegraf")
                .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
                .range(-1L, ChronoUnit.DAYS)
                .sample(10);

        String expected = "from(db:\"telegraf\") |> "
                + "filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\")) |> "
                + "range(start: -1d) |> sample(n: 10)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void sampleNPosition() {

        Flux flux = Flux.from("telegraf")
                .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
                .range(-1L, ChronoUnit.DAYS)
                .sample(5, 1);

        String expected = "from(db:\"telegraf\") |> "
                + "filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\")) |> "
                + "range(start: -1d) |> sample(n: 5, pos: 1)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void sampleWith() {

        Flux flux = Flux
                .from("telegraf")
                .sample()
                .withN(5);

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> sample(n: 5)");
    }

    @Test
    void samplePositionGreaterThanN() {

        Flux flux = Flux.from("telegraf")
                .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
                .range(-1L, ChronoUnit.DAYS);


        Assertions.assertThatThrownBy(() -> flux.sample(5, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("pos must be less than n");
    }

    @Test
    void samplePositionSameThanN() {

        Flux flux = Flux.from("telegraf")
                .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
                .range(-1L, ChronoUnit.DAYS);

        Assertions.assertThatThrownBy(() -> flux.sample(10, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("pos must be less than n");
    }
}