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

/**
 * Functions for parameter validation.
 * <p>
 * Copied from InfluxDB Java - <a href="https://github.com/influxdata/influxdb-java/">thanks</a>
 *
 * @author Simon Legner
 */
public final class Preconditions {

    private Preconditions() {
    }

    /**
     * Enforces that the string is {@linkplain String#isEmpty() not empty}.
     *
     * @param string the string to test
     * @param name   variable name for reporting
     * @return {@code string}
     * @throws IllegalArgumentException if the string is empty
     */
    public static String checkNonEmptyString(final String string, final String name) throws IllegalArgumentException {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException("Expecting a non-empty string for " + name);
        }
        return string;
    }

    /**
     * Enforces that the number is larger than 0.
     *
     * @param number the number to test
     * @param name   variable name for reporting
     * @throws IllegalArgumentException if the number is less or equal to 0
     */
    public static void checkPositiveNumber(final Number number, final String name) throws IllegalArgumentException {
        if (number == null || number.doubleValue() <= 0) {
            throw new IllegalArgumentException("Expecting a positive number for " + name);
        }
    }

    /**
     * Enforces that the number is not negative.
     *
     * @param number the number to test
     * @param name   variable name for reporting
     * @throws IllegalArgumentException if the number is less or equal to 0
     */
    public static void checkNotNegativeNumber(final Number number, final String name) throws IllegalArgumentException {
        if (number == null || number.doubleValue() < 0) {
            throw new IllegalArgumentException("Expecting a positive or zero number for " + name);
        }
    }

    /**
     * Enforces that the duration is a valid influxDB duration.
     *
     * @param duration the duration to test
     * @param name     variable name for reporting
     * @throws IllegalArgumentException if the given duration is not valid.
     */
    public static void checkDuration(final String duration, final String name) throws IllegalArgumentException {
        if (!duration.matches("(\\d+[wdmhs])+")) {
            throw new IllegalArgumentException("Invalid InfluxDB duration: " + duration
                    + "for " + name);
        }
    }
}
