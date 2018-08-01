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
package io.bonitoo.flux.events;

import java.util.Objects;
import javax.annotation.Nonnull;

import io.bonitoo.flux.options.FluxConnectionOptions;
import io.bonitoo.flux.utils.Preconditions;

/**
 * @author Jakub Bednar (bednar@github) (30/07/2018 14:59)
 */
public abstract class AbstractQueryEvent extends AbstractFluxEvent {

    private final FluxConnectionOptions options;
    private final String fluxQuery;

    AbstractQueryEvent(@Nonnull final FluxConnectionOptions options, @Nonnull final String fluxQuery) {

        Objects.requireNonNull(options, "FluxConnectionOptions are required");
        Preconditions.checkNonEmptyString(fluxQuery, "Flux query");

        this.options = options;
        this.fluxQuery = fluxQuery;
    }

    /**
     * @return {@link FluxConnectionOptions} that was used in query
     */
    @Nonnull
    public FluxConnectionOptions getOptions() {
        return options;
    }

    /**
     * @return Flux query sent to Flux server
     */
    @Nonnull
    public String getFluxQuery() {
        return fluxQuery;
    }
}
