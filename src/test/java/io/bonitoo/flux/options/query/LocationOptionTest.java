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
package io.bonitoo.flux.options.query;

import java.time.temporal.ChronoUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (20/08/2018 14:18)
 */
@RunWith(JUnitPlatform.class)
class LocationOptionTest {

    @Test
    void offset() {

        LocationOption option = LocationOption.builder().offset("10s").build();

        Assertions.assertThat(option.toString()).isEqualTo("option location = fixedZone(offset: 10s)");
    }

    @Test
    void offsetChronoUnit() {

        LocationOption option = LocationOption.builder().offset(-15L, ChronoUnit.MONTHS).build();

        Assertions.assertThat(option.toString()).isEqualTo("option location = fixedZone(offset: -15mo)");
    }

    @Test
    void locationName() {

        LocationOption option = LocationOption.builder().location("America/Denver").build();

        Assertions.assertThat(option.toString()).isEqualTo("option location = loadLocation(name: \"America/Denver\")");
    }

    @Test
    void requiredFunction() {

        Assertions.assertThatThrownBy(() -> LocationOption.builder().build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("location has to be defined");

    }
}