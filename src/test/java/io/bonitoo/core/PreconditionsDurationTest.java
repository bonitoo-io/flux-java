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
package io.bonitoo.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (20/08/2018 12:31)
 */
@SuppressWarnings("ConstantConditions")
@RunWith(JUnitPlatform.class)
class PreconditionsDurationTest {

    @Test
    void literals() {

        Preconditions.checkDuration("1s", "duration");
        Preconditions.checkDuration("10d", "duration");
        Preconditions.checkDuration("1h15m", "duration");
        Preconditions.checkDuration("5w", "duration");
        Preconditions.checkDuration("1mo5d", "duration");
        Preconditions.checkDuration("-1mo5d", "duration");
    }

    @Test
    void literalNull() {

        Assertions.assertThatThrownBy(() -> Preconditions.checkDuration(null, "duration"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a duration string for duration. But got: null");
    }

    @Test
    void literalEmpty() {

        Assertions.assertThatThrownBy(() -> Preconditions.checkDuration("", "duration"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a duration string for duration. But got: ");
    }

    @Test
    void literalNotDuration() {

        Assertions.assertThatThrownBy(() -> Preconditions.checkDuration("x", "duration"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a duration string for duration. But got: x");
    }

    @Test
    void notRequiredValid() {

        Preconditions.checkDurationNotRequired(null, "duration");
        Preconditions.checkDurationNotRequired("", "duration");
        Preconditions.checkDurationNotRequired("1s", "duration");
    }
}