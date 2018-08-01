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

import java.time.Instant;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/07/2018 13:07)
 */
@RunWith(JUnitPlatform.class)
class NowOptionTest {

    @Test
    void byInstant() {

        NowOption nowOption = NowOption.builder()
                .time(Instant.ofEpochSecond(10_000))
                .build();

        Assertions.assertThat(nowOption.toString()).isEqualTo("option now = () => 1970-01-01T02:46:40.000000000Z");
    }

    @Test
    void byFunction() {

        NowOption nowOption = NowOption.builder()
                .function("giveMeTime()")
                .build();

        Assertions.assertThat(nowOption.toString()).isEqualToIgnoringWhitespace("option now = giveMeTime()");
    }

    @Test
    void withoutValue() {

        Assertions.assertThatThrownBy(() -> NowOption.builder().build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("function or time has to be defined");
    }
}