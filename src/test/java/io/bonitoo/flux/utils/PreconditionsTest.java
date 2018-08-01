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
package io.bonitoo.flux.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (01/08/2018 15:29)
 */
@SuppressWarnings("ConstantConditions")
@RunWith(JUnitPlatform.class)
class PreconditionsTest {

    @Test
    void checkNonEmptyString() {

        Preconditions.checkNonEmptyString("valid", "property");
    }

    @Test
    void checkNonEmptyStringEmpty() {

        Assertions.assertThatThrownBy(() -> Preconditions.checkNonEmptyString("", "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a non-empty string for property");
    }

    @Test
    void checkNonEmptyStringNull() {

        Assertions.assertThatThrownBy(() -> Preconditions.checkNonEmptyString(null, "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a non-empty string for property");
    }

    @Test
    void checkPositiveNumber() {

        Preconditions.checkPositiveNumber(10, "property");
    }

    @Test
    void checkPositiveNumberZero() {

        Assertions.assertThatThrownBy(() -> Preconditions.checkPositiveNumber(0, "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a positive number for property");
    }

    @Test
    void checkPositiveNumberZeroNegative() {

        Assertions.assertThatThrownBy(() -> Preconditions.checkPositiveNumber(-12L, "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a positive number for property");
    }

    @Test
    void checkNotNegativeNumber() {

        Preconditions.checkNotNegativeNumber(0, "valid");
    }

    @Test
    void checkNotNegativeNumberNegative() {

        Assertions.assertThatThrownBy(() -> Preconditions.checkNotNegativeNumber(-12L, "property"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a positive or zero number for property");
    }
}