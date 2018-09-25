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

import java.util.Objects;
import javax.annotation.Nonnull;

import io.bonitoo.flux.Flux;
import io.bonitoo.flux.operator.restriction.Restrictions;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#filter">filter</a> -
 * Filters the results using an expression.
 *
 * <h3>Options</h3>
 * <ul>
 * <li>
 * <b>fn</b> - Function to when filtering the records. The function must accept a single parameter
 * which will be the records and return a boolean value. Records which evaluate to true,
 * will be included in the results. [function(record) bool]
 * </li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 *  Restrictions restriction = Restrictions.and(
 *          Restrictions.measurement().equal("mem"),
 *          Restrictions.field().equal("usage_system"),
 *          Restrictions.tag("service").equal("app-server")
 * );
 *
 * Flux flux = Flux
 *          .from("telegraf")
 *          .filter(restriction)
 *          .range(-4L, ChronoUnit.HOURS)
 *          .count();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (28/06/2018 14:12)
 * @since 1.0.0
 */
public final class FilterFlux extends AbstractParametrizedFlux {

    public FilterFlux(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "filter";
    }

    @Nonnull
    @Override
    protected String propertyDelimiter(@Nonnull final String operatorName) {
        return " => ";
    }

    /**
     * @param restrictions filter restrictions
     * @return this
     */
    @Nonnull
    public FilterFlux withRestrictions(@Nonnull final Restrictions restrictions) {

        Objects.requireNonNull(restrictions, "Restrictions are required");

        this.withPropertyValue("fn: (r)", restrictions);

        return this;
    }
}
