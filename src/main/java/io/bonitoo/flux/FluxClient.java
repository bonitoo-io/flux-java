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

import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import io.bonitoo.flux.events.AbstractFluxEvent;
import io.bonitoo.flux.mapper.FluxResult;
import io.bonitoo.flux.options.FluxOptions;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * The client for the Flux service.
 * <p>
 * TODO simple string, documentation - first simple
 *
 * @author Jakub Bednar (bednar@github) (30/07/2018 10:55)
 * @since 3.0.0
 */
public interface FluxClient {

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query the flux query to execute
     * @return {@link FluxResult}  which are matched the query
     */
    @Nonnull
    FluxResult flux(@Nonnull final Flux query);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query    the flux query to execute
     * @param callback callback to consume result which are matched the query
     */
    void flux(@Nonnull final Flux query, @Nonnull final Consumer<FluxResult> callback);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query   the flux query to execute
     * @param options the options for the query
     * @return {@link FluxResult}  which are matched the query
     */
    @Nonnull
    FluxResult flux(@Nonnull final Flux query, @Nonnull final FluxOptions options);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query    the flux query to execute
     * @param options  the options for the query
     * @param callback callback to consume result which are matched the query
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final FluxOptions options,
              @Nonnull final Consumer<FluxResult> callback);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @return {@link FluxResult}  which are matched the query
     */
    @Nonnull
    FluxResult flux(@Nonnull final Flux query, @Nonnull final Map<String, Object> properties);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param callback   callback to consume result which are matched the query
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final Map<String, Object> properties,
              @Nonnull final Consumer<FluxResult> callback);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param options    the options for the query
     * @return {@link FluxResult}  which are matched the query
     */
    @Nonnull
    FluxResult flux(@Nonnull final Flux query,
                    @Nonnull final Map<String, Object> properties,
                    @Nonnull final FluxOptions options);

    /**
     * Execute a Flux against the Flux service.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param options    the options for the query
     * @param callback   callback to consume result which are matched the query
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final Map<String, Object> properties,
              @Nonnull final FluxOptions options,
              @Nonnull final Consumer<FluxResult> callback);

    /**
     * Listen the events produced by {@link FluxClient}.
     *
     * @param eventType type of event to listen
     * @param <T>       type of event to listen
     * @param listener  listener to consume events
     */
    <T extends AbstractFluxEvent> void subscribeEvents(@Nonnull final Class<T> eventType,
                                                       @Nonnull final Consumer<T> listener);

    /**
     * Listen the events produced by {@link FluxClient}.
     *
     * @param <T>      type of event to listen
     * @param listener listener to unsubscribe to events
     */
    <T extends AbstractFluxEvent> void unsubscribeEvents(@Nonnull final Consumer<T> listener);

    /**
     * Enable Gzip compress for http request body.
     *
     * @return the FluxClientReactive instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClient enableGzip();

    /**
     * Disable Gzip compress for http request body.
     *
     * @return the FluxClientReactive instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClient disableGzip();

    /**
     * Returns whether Gzip compress for http request body is enabled.
     *
     * @return true if gzip is enabled.
     */
    boolean isGzipEnabled();

    /**
     * Check the status of Flux Server.
     *
     * @return {@link Boolean#TRUE} if server is healthy otherwise return {@link Boolean#FALSE}
     */
    @Nonnull
    Boolean ping();

    /**
     * @return the {@link HttpLoggingInterceptor.Level} that is used for logging requests and responses
     */
    @Nonnull
    HttpLoggingInterceptor.Level getLogLevel();

    /**
     * Set the log level for the request and response information.
     *
     * @param logLevel the log level to set.
     * @return the FluxClientReactive instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClient setLogLevel(@Nonnull final HttpLoggingInterceptor.Level logLevel);

    /**
     * Dispose all event listeners before shutdown.
     *
     * @return the FluxClientReactive instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClient close();
}
