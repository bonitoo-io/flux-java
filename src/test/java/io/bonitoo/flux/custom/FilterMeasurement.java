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
package io.bonitoo.flux.custom;

import javax.annotation.Nonnull;

import io.bonitoo.core.Preconditions;
import io.bonitoo.flux.Flux;
import io.bonitoo.flux.operator.AbstractParametrizedFlux;

/**
 * The custom function.
 *
 * @author Jakub Bednar (bednar@github) (02/07/2018 14:23)
 */
public class FilterMeasurement extends AbstractParametrizedFlux {

    public FilterMeasurement(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "measurement";
    }

    /**
     * @param measurement the measurement name. Has to be defined.
     * @return this
     */
    @Nonnull
    public FilterMeasurement withName(@Nonnull final String measurement) {

        Preconditions.checkNonEmptyString(measurement, "Measurement name");

        withPropertyValueEscaped("m", measurement);

        return this;
    }
}