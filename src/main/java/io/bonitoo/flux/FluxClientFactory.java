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
package io.bonitoo.flux;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

import io.bonitoo.flux.dto.FluxRecord;
import io.bonitoo.flux.impl.FluxClientImpl;
import io.bonitoo.flux.option.FluxConnectionOptions;

/**
 * The Factory that create a instance of a Flux client.
 *
 * @author Jakub Bednar (bednar@github) (31/07/2018 13:11)
 * @since 1.0.0
 */
public final class FluxClientFactory {

    private static final Logger LOG = Logger.getLogger(FluxClientFactory.class.getName());

    public static final BiConsumer<FluxClient.Cancellable, FluxRecord> EMPTY_ON_NEXT =
            (cancellable, fluxRecord) -> LOG.finest("Finished query");

    public static final Runnable EMPTY_ON_COMPLETE = () -> LOG.log(Level.FINEST, "successfully end of stream");

    public static final Consumer<? super Throwable> EMPTY_ON_ERROR = (Consumer<Throwable>) throwable -> {
        Thread currentThread = Thread.currentThread();
        Thread.UncaughtExceptionHandler handler = currentThread.getUncaughtExceptionHandler();
        handler.uncaughtException(currentThread, throwable);
    };

    private FluxClientFactory() {
    }

    /**
     * Create a instance of the Flux client.
     *
     * @param orgID the organization id required by Flux
     * @param url   the url to connect to Flux.
     * @return client
     * @see FluxConnectionOptions.Builder#orgID(String)
     * @see FluxConnectionOptions.Builder#url(String)
     */
    @Nonnull
    public static FluxClient connect(@Nonnull final String orgID, @Nonnull final String url) {

        FluxConnectionOptions options = FluxConnectionOptions.builder()
                .url(url)
                .orgID(orgID)
                .build();

        return connect(options);
    }

    /**
     * Create a instance of the Flux client.
     *
     * @param options the connection configuration
     * @return client
     */
    @Nonnull
    public static FluxClient connect(@Nonnull final FluxConnectionOptions options) {

        Objects.requireNonNull(options, "FluxConnectionOptions are required");

        return new FluxClientImpl(options);
    }
}
