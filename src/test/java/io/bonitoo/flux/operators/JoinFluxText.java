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
import io.bonitoo.flux.operators.restriction.Restrictions;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (19/07/2018 12:59)
 */
@RunWith(JUnitPlatform.class)
class JoinFluxText {

    @Test
    void join() {

        Flux cpu = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("cpu"), Restrictions.field().equal("usage_user")))
                .range(-30L, ChronoUnit.MINUTES);

        Flux mem = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))
                .range(-30L, ChronoUnit.MINUTES);

        Flux flux = Flux
                .join("cpu", cpu, "mem", mem, "host", "tables.cpu[\"_value\"] + tables.mem[\"_value\"]");

        String expected = "cpu = from(db:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\")) |> range(start: -30m) "
                + "mem = from(db:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"used_percent\")) |> range(start: -30m) "
                + "join(tables: {cpu:cpu, mem:mem}, on: [\"host\"], fn: (tables) => tables.cpu[\"_value\"] + tables.mem[\"_value\"])";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void joinByParameters() {

        Flux cpu = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("cpu"), Restrictions.field().equal("usage_user")))
                .range(-30L, ChronoUnit.MINUTES);

        Flux mem = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))
                .range(-30L, ChronoUnit.MINUTES);

        List<String> tags = new ArrayList<>();
        tags.add("host");
        tags.add("production");

        Flux flux = Flux.join()
                .withTable("cpu", cpu)
                .withTable("mem", mem)
                .withOn(tags)
                .withFunction("tables.cpu[\"_value\"] + tables.mem[\"_value\"]");

        String expected = "cpu = from(db:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\")) |> range(start: -30m) "
                + "mem = from(db:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"used_percent\")) |> range(start: -30m) "
                + "join(tables: {cpu:cpu, mem:mem}, on: [\"host\", \"production\"], fn: (tables) => tables.cpu[\"_value\"] + tables.mem[\"_value\"])";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }
}