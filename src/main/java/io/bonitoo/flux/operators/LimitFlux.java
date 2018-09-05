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

import javax.annotation.Nonnull;

import io.bonitoo.Preconditions;
import io.bonitoo.flux.Flux;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#limit">limit</a> - Restricts the number of rows
 * returned in the results.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>n</b> - The maximum number of records to output [int].
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .limit(5);
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (25/06/2018 11:22)
 * @since 1.0.0
 */
public final class LimitFlux extends AbstractParametrizedFlux {

    public LimitFlux(@Nonnull final Flux flux) {
        super(flux);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "limit";
    }

    /**
     * @param numberOfResults The number of results
     * @return this
     */
    @Nonnull
    public LimitFlux withN(final int numberOfResults) {

        Preconditions.checkPositiveNumber(numberOfResults, "Number of results");

        this.withPropertyValue("n", numberOfResults);

        return this;
    }
}
