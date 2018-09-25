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
package io.bonitoo.flux.operator.restriction;

import java.util.regex.Pattern;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (28/06/2018 13:16)
 */
@RunWith(JUnitPlatform.class)
class RestrictionTest {

    @Test
    void isEqualTo() {

        Assertions.assertThat(Restrictions.measurement().equal("mem"))
                .hasToString("r[\"_measurement\"] == \"mem\"");

        Assertions.assertThat(Restrictions.field()
                .equal(10)).hasToString("r[\"_field\"] == 10");

        Assertions.assertThat(Restrictions.tag("location")
                .equal(Pattern.compile("/var/"))).hasToString("r[\"location\"] == /var/");
    }

    @Test
    void isNotEqualTo() {

        Assertions.assertThat(Restrictions.measurement().notEqual("mem"))
                .hasToString("r[\"_measurement\"] != \"mem\"");
    }

    @Test
    void less() {
        Assertions.assertThat(Restrictions.start().less(12L)).hasToString("r[\"_start\"] < 12");
    }

    @Test
    void greater() {
        Assertions.assertThat(Restrictions.stop().greater(15)).hasToString("r[\"_stop\"] > 15");
    }

    @Test
    void lessOrEqual() {
        Assertions.assertThat(Restrictions.time().lessOrEqual(20)).hasToString("r[\"_time\"] <= 20");
    }

    @Test
    void greaterOrEqual() {
        Assertions.assertThat(Restrictions.value().greaterOrEqual(20D)).hasToString("r[\"_value\"] >= 20.0");
    }

    @Test
    void columnRestrictions() {
        Assertions.assertThat(Restrictions.column("custom_column").equal(20D))
                .hasToString("r[\"custom_column\"] == 20.0");
    }

    @Test
    void custom() {
        Assertions.assertThat(Restrictions.value().custom(15L, "=~")).hasToString("r[\"_value\"] =~ 15");
    }
}