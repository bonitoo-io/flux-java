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
package io.bonitoo.flux.option.query;

import java.time.temporal.ChronoUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/07/2018 13:42)
 */
@RunWith(JUnitPlatform.class)
class TaskOptionTest {

    @Test
    void every() {

        TaskOption taskOption = TaskOption.builder("foo")
                .every(1L, ChronoUnit.HOURS)
                .build();

        Assertions.assertThat(taskOption.toString()).isEqualToIgnoringWhitespace("option task = {name: \"foo\", every: 1h}");
    }

    @Test
    void delay() {

        TaskOption taskOption = TaskOption.builder("foo")
                .delay(10L, ChronoUnit.MINUTES)
                .build();

        Assertions.assertThat(taskOption.toString()).isEqualToIgnoringWhitespace("option task = {name: \"foo\", delay: 10m}");
    }

    @Test
    void cron() {

        TaskOption taskOption = TaskOption.builder("foo")
                .cron("0 2 * * *")
                .build();

        Assertions.assertThat(taskOption.toString()).isEqualToIgnoringWhitespace("option task = {name: \"foo\", cron: \"0 2 * * *\"}");
    }

    @Test
    void retry() {

        TaskOption taskOption = TaskOption.builder("foo")
                .retry(5)
                .build();

        Assertions.assertThat(taskOption.toString()).isEqualToIgnoringWhitespace("option task = {name: \"foo\", retry: 5}");
    }

    @Test
    void full() {

        TaskOption taskOption = TaskOption.builder("foo")
                .every(1L, ChronoUnit.HOURS)
                .delay(10L, ChronoUnit.MINUTES)
                .cron("0 2 * * *")
                .retry(5)
                .build();

        Assertions.assertThat(taskOption.toString())
                .isEqualToIgnoringWhitespace("option task = {name: \"foo\", every: 1h, delay: 10m, cron: \"0 2 * * *\", retry: 5}");
    }
}