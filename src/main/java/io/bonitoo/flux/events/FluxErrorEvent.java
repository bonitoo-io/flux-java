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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import io.bonitoo.flux.FluxException;
import io.bonitoo.flux.options.FluxConnectionOptions;

/**
 * The event is published when arrived the error response from Flux server.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 15:35)
 * @since 1.0.0
 */
public class FluxErrorEvent extends AbstractQueryEvent {

    private static final Logger LOG = Logger.getLogger(FluxErrorEvent.class.getName());

    private final FluxException exception;

    public FluxErrorEvent(@Nonnull final FluxConnectionOptions options,
                          @Nonnull final String fluxQuery,
                          @Nonnull final FluxException exception) {

        super(options, fluxQuery);

        Objects.requireNonNull(exception, "FluxException is required");

        this.exception = exception;
    }

    /**
     * @return the exception that was throw
     */
    @Nonnull
    public FluxException getException() {
        return exception;
    }

    @Override
    public void logEvent() {
        LOG.log(Level.SEVERE, "Error response from InfluxDB: ", exception);
    }
}
