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
package io.bonitoo.flux.operator;

import javax.annotation.Nonnull;

import io.bonitoo.core.Preconditions;
import io.bonitoo.flux.Flux;

/**
 * <a href="https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md#distinct">distinct</a> - Distinct
 * produces the unique values for a given column.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>column</b> - The column on which to track unique values [string]
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux
 *     .from("telegraf")
 *     .groupBy("_measurement")
 *     .distinct("_measurement");
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (17/07/2018 12:08)
 * @since 1.0.0
 */
public final class DistinctFlux extends AbstractParametrizedFlux {

    public DistinctFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "distinct";
    }

    /**
     * @param column The column on which to track unique values.
     * @return this
     */
    @Nonnull
    public DistinctFlux withColumn(@Nonnull final String column) {

        Preconditions.checkNonEmptyString(column, "Column");

        this.withPropertyValueEscaped("column", column);

        return this;
    }
}
