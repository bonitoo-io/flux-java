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
import javax.annotation.Nullable;

import io.bonitoo.flux.Flux;
import io.bonitoo.flux.FluxChain;

/**
 * @author Jakub Bednar (bednar@github) (27/06/2018 14:03)
 */
public abstract class AbstractParametrizedFlux extends AbstractFluxWithUpstream {

    protected AbstractParametrizedFlux() {
        super();
    }

    protected AbstractParametrizedFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Override
    protected final void appendAfterUpstream(@Nonnull final FluxChain fluxChain) {

        StringBuilder operator = new StringBuilder();
        //
        // see JoinFlux
        beforeAppendOperatorName(operator, fluxChain);
        //

        //
        // operator(
        //
        operator.append(operatorName()).append("(");
        //
        //
        // parameters: false
        boolean wasAppended = false;

        for (String name : operatorProperties.keys()) {

            String propertyValue = operatorProperties.get(name, fluxChain.getParameters());

            wasAppended = appendParameterTo(name, propertyValue, operator, wasAppended);
        }
        //
        // )
        //
        operator.append(")");

        fluxChain.append(operator);
    }

    /**
     * @return name of operator
     */
    @Nonnull
    protected abstract String operatorName();

    /**
     * For value property it is ": ", but for function it is "=&gt;".
     *
     * @param operatorName operator name
     * @return property value delimiter
     * @see AbstractParametrizedFlux#propertyDelimiter(String)
     */
    @Nonnull
    protected String propertyDelimiter(@Nonnull final String operatorName) {
        return ": ";
    }

    /**
     * Possibility to customize operator.
     *
     * @param operator  current Flux operator
     * @param fluxChain the incoming {@link FluxChain}, never null
     * @see JoinFlux
     */
    protected void beforeAppendOperatorName(@Nonnull final StringBuilder operator, @Nonnull final FluxChain fluxChain) {
    }

    /**
     * @return {@link Boolean#TRUE} if was appended parameter
     */
    private boolean appendParameterTo(@Nonnull final String operatorName,
                                      @Nullable final String propertyValue,
                                      @Nonnull final StringBuilder operator,
                                      final boolean wasAppendProperty) {

        if (propertyValue == null) {
            return wasAppendProperty;
        }

        // delimit previously appended parameter
        if (wasAppendProperty) {
            operator.append(", ");
        }

        // n: 5
        operator
                .append(operatorName)
                .append(propertyDelimiter(operatorName))
                .append(propertyValue);

        return true;
    }
}
