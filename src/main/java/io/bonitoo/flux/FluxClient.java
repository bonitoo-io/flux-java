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

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import io.bonitoo.core.event.AbstractInfluxEvent;
import io.bonitoo.flux.dto.FluxRecord;
import io.bonitoo.flux.dto.FluxTable;
import io.bonitoo.flux.option.FluxOptions;

import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;

/**
 * The client for the Flux service.
 *
 * @author Jakub Bednar (bednar@github) (30/07/2018 10:55)
 * @since 1.0.0
 */
public interface FluxClient {

    BiConsumer<Cancellable, FluxRecord> EMPTY_ON_NEXT = (cancellable, fluxRecord) -> {
    };

    // TODO log
    Runnable EMPTY_ON_COMPLETE = () -> {
    };
    // TODO throw errors
    Consumer<? super Throwable> EMPTY_ON_ERROR = (Consumer<Throwable>) throwable -> {
    };

    /**
     * Execute a Flux against the Flux service and synchronously map whole response to {@link FluxTable}s.
     *
     * @param query the flux query to execute
     * @return {@code List<FluxTable>} which are matched the query
     */
    @Nonnull
    List<FluxTable> flux(@Nonnull final String query);

    /**
     * Execute a Flux against the Flux service and synchronously map whole response to {@link FluxTable}s.
     *
     * @param options the options for the query
     * @param query   the flux query to execute
     * @return {@code List<FluxTable>} which are matched the query
     */
    @Nonnull
    List<FluxTable> flux(@Nonnull final String query, @Nonnull final FluxOptions options);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query  the flux query to execute
     * @param onNext callback to consume result which are matched the query
     *               with capability to discontinue a streaming query
     */
    void flux(@Nonnull final String query, @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param onNext     callback to consume result which are matched the query
     *                   with capability to discontinue a streaming query
     * @param onComplete callback to consume a notification about successfully end of stream
     */
    void flux(@Nonnull final String query,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
              @Nonnull final Runnable onComplete);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param onNext     callback to consume result which are matched the query
     *                   with capability to discontinue a streaming query
     * @param onComplete callback to consume a notification about successfully end of stream
     * @param onError    callback to consume any error notification
     */
    void flux(@Nonnull final String query,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
              @Nonnull final Runnable onComplete,
              @Nonnull final Consumer<? super Throwable> onError);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query   the flux query to execute
     * @param onNext  callback to consume result which are matched the query
     *                with capability to discontinue a streaming query
     * @param options the options for the query
     */
    void flux(@Nonnull final String query,
              @Nonnull final FluxOptions options,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param onNext     callback to consume result which are matched the query
     *                   with capability to discontinue a streaming query
     * @param options    the options for the query
     * @param onComplete callback to consume a notification about successfully end of stream
     */
    void flux(@Nonnull final String query,
              @Nonnull final FluxOptions options,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
              @Nonnull final Runnable onComplete);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param onNext     callback to consume result which are matched the query
     *                   with capability to discontinue a streaming query
     * @param options    the options for the query
     * @param onComplete callback to consume a notification about successfully end of stream,
     * @param onError    callback to consume any error notification
     *                   {@link Boolean#TRUE} if query successfully finish
     */
    void flux(@Nonnull final String query,
              @Nonnull final FluxOptions options,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
              @Nonnull final Runnable onComplete,
              @Nonnull final Consumer<? super Throwable> onError);

    /**
     * Execute a Flux against the Flux service and return the Flux server HTTP response.
     *
     * @param query the flux query to execute
     * @return {@code Response<ResponseBody>} raw response which are matched the query
     */
    @Nonnull
    Response<ResponseBody> fluxRaw(@Nonnull final String query);

    /**
     * Execute a Flux against the Flux service and return the Flux server HTTP response.
     *
     * @param options the options for the query
     * @param query   the flux query to execute
     * @return {@code Response<ResponseBody>} raw response which are matched the query
     */
    @Nonnull
    Response<ResponseBody> fluxRaw(@Nonnull final String query, @Nonnull final FluxOptions options);

    /**
     * Execute a Flux against the Flux service and asynchronous stream HTTP response to {@code onResponse}.
     * <p>
     * The callback is call only once.
     *
     * @param query      the flux query to execute
     * @param onResponse callback to consume raw response which are matched the query
     */
    void fluxRaw(@Nonnull final String query, @Nonnull final Consumer<Response<ResponseBody>> onResponse);

    /**
     * Execute a Flux against the Flux service and asynchronous stream HTTP response to {@code onResponse}.
     * <p>
     * The callback is call only once.
     *
     * @param query      the flux query to execute
     * @param onResponse callback to consume raw response which are matched the query
     * @param onFailure  callback to consume error notification invoked when a network exception occurred
     *                   talking to the server
     */
    void fluxRaw(@Nonnull final String query,
                 @Nonnull final Consumer<Response<ResponseBody>> onResponse,
                 @Nonnull final Consumer<? super Throwable> onFailure);

    /**
     * Execute a Flux against the Flux service.
     * <p>
     * The callback is call only once.
     *
     * @param query      the flux query to execute
     * @param onResponse callback to consume raw response which are matched the query
     * @param options    the options for the query
     */
    void fluxRaw(@Nonnull final String query,
                 @Nonnull final FluxOptions options,
                 @Nonnull final Consumer<Response<ResponseBody>> onResponse);

    /**
     * Execute a Flux against the Flux service.
     * <p>
     * The callback is call only once.
     *
     * @param query      the flux query to execute
     * @param onResponse callback to consume raw response which are matched the query
     * @param options    the options for the query
     * @param onFailure  callback to consume error notification invoked when a network exception occurred
     *                   talking to the server
     */
    void fluxRaw(@Nonnull final String query,
                 @Nonnull final FluxOptions options,
                 @Nonnull final Consumer<Response<ResponseBody>> onResponse,
                 @Nonnull final Consumer<? super Throwable> onFailure);

    /**
     * Execute a Flux against the Flux service and synchronously map whole response to {@link FluxTable}s.
     *
     * @param query the flux query to execute
     * @return {@code List<FluxTable>}  which are matched the query
     */
    @Nonnull
    List<FluxTable> flux(@Nonnull final Flux query);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query  the flux query to execute
     * @param onNext callback to consume result which are matched the query
     *               with capability to discontinue a streaming query
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param onNext     callback to consume result which are matched the query
     *                   with capability to discontinue a streaming query
     * @param onComplete callback to consume a notification about successfully end of stream
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
              @Nonnull final Runnable onComplete);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param onNext     callback to consume result which are matched the query
     * @param onComplete callback to consume a notification about successfully end of stream
     * @param onError    callback to consume any error notification
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
              @Nonnull final Runnable onComplete,
              @Nonnull final Consumer<? super Throwable> onError);

    /**
     * Execute a Flux against the Flux service and synchronously map whole response to {@link FluxTable}s.
     *
     * @param query   the flux query to execute
     * @param options the options for the query
     * @return {@code List<FluxTable>} which are matched the query
     */
    @Nonnull
    List<FluxTable> flux(@Nonnull final Flux query, @Nonnull final FluxOptions options);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query   the flux query to execute
     * @param options the options for the query
     * @param onNext  callback to consume result which are matched the query
     *                with capability to discontinue a streaming query
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final FluxOptions options,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param options    the options for the query
     * @param onNext     callback to consume result which are matched the query
     *                   with capability to discontinue a streaming query
     * @param onComplete callback to consume a notification about successfully end of stream
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final FluxOptions options,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
              @Nonnull final Runnable onComplete);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param options    the options for the query
     * @param onNext     callback to consume result which are matched the query
     *                   with capability to discontinue a streaming query
     * @param onComplete callback to consume a notification about successfully end of stream
     * @param onError    callback to consume any error notification
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final FluxOptions options,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
              @Nonnull final Runnable onComplete,
              @Nonnull final Consumer<? super Throwable> onError);

    /**
     * Execute a Flux against the Flux service and synchronously map whole response to {@link FluxTable}s.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @return {@code List<FluxTable>} which are matched the query
     */
    @Nonnull
    List<FluxTable> flux(@Nonnull final Flux query, @Nonnull final Map<String, Object> properties);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param onNext     callback to consume result which are matched the query
     *                   with capability to discontinue a streaming query
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final Map<String, Object> properties,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param onNext     callback to consume result which are matched the query
     *                   with capability to discontinue a streaming query
     * @param onComplete callback to consume a notification about successfully end of stream
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final Map<String, Object> properties,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
              @Nonnull final Runnable onComplete);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param onNext     callback to consume result which are matched the query
     *                   with capability to discontinue a streaming query
     * @param onComplete callback to consume a notification about successfully end of stream
     * @param onError    callback to consume any error notification
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final Map<String, Object> properties,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
              @Nonnull final Runnable onComplete,
              @Nonnull final Consumer<? super Throwable> onError);

    /**
     * Execute a Flux against the Flux service and synchronously map whole response to {@link FluxTable}s.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param options    the options for the query
     * @return {@code List<FluxTable>} which are matched the query
     */
    @Nonnull
    List<FluxTable> flux(@Nonnull final Flux query,
                         @Nonnull final Map<String, Object> properties,
                         @Nonnull final FluxOptions options);

    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param options    the options for the query
     * @param onNext     callback to consume result which are matched the query
     *                   with capability to discontinue a streaming query
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final Map<String, Object> properties,
              @Nonnull final FluxOptions options,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext);


    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param options    the options for the query
     * @param onNext     callback to consume result which are matched the query
     *                   with capability to discontinue a streaming query
     * @param onComplete callback to consume a notification about successfully end of stream
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final Map<String, Object> properties,
              @Nonnull final FluxOptions options,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
              @Nonnull final Runnable onComplete);


    /**
     * Execute a Flux against the Flux service and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param options    the options for the query
     * @param onNext     callback to consume result which are matched the query
     *                   with capability to discontinue a streaming query
     * @param onComplete callback to consume a notification about successfully end of stream
     * @param onError    callback to consume any error notification
     */
    void flux(@Nonnull final Flux query,
              @Nonnull final Map<String, Object> properties,
              @Nonnull final FluxOptions options,
              @Nonnull final BiConsumer<Cancellable, FluxRecord> onNext,
              @Nonnull final Runnable onComplete,
              @Nonnull final Consumer<? super Throwable> onError);

    /**
     * Execute a Flux against the Flux service and return the Flux server HTTP response.
     *
     * @param query the flux query to execute
     * @return {@code Response<ResponseBody>} raw response which are matched the query
     */
    @Nonnull
    Response<ResponseBody> fluxRaw(@Nonnull final Flux query);

    /**
     * Execute a Flux against the Flux service and asynchronous stream HTTP response to {@code onResponse}.
     * <p>
     * The callback is call only once.
     *
     * @param query      the flux query to execute
     * @param onResponse callback to consume raw response which are matched the query
     */
    void fluxRaw(@Nonnull final Flux query, @Nonnull final Consumer<Response<ResponseBody>> onResponse);

    /**
     * Execute a Flux against the Flux service and asynchronous stream HTTP response to {@code onResponse}.
     * <p>
     * The callback is call only once.
     *
     * @param query      the flux query to execute
     * @param onResponse callback to consume raw response which are matched the query
     * @param onFailure  callback to consume error notification invoked when a network exception occurred
     *                   talking to the server
     */
    void fluxRaw(@Nonnull final Flux query,
                 @Nonnull final Consumer<Response<ResponseBody>> onResponse,
                 @Nonnull final Consumer<? super Throwable> onFailure);

    /**
     * Execute a Flux against the Flux service and return the Flux server HTTP response.
     *
     * @param query   the flux query to execute
     * @param options the options for the query
     * @return {@code Response<ResponseBody>} raw response which are matched the query
     */
    @Nonnull
    Response<ResponseBody> fluxRaw(@Nonnull final Flux query, @Nonnull final FluxOptions options);

    /**
     * Execute a Flux against the Flux service and asynchronous stream HTTP response to {@code onResponse}.
     * <p>
     * The callback is call only once.
     *
     * @param query      the flux query to execute
     * @param options    the options for the query
     * @param onResponse callback to consume raw response which are matched the query
     */
    void fluxRaw(@Nonnull final Flux query,
                 @Nonnull final FluxOptions options,
                 @Nonnull final Consumer<Response<ResponseBody>> onResponse);


    /**
     * Execute a Flux against the Flux service and asynchronous stream HTTP response to {@code onResponse}.
     * <p>
     * The callback is call only once.
     *
     * @param query      the flux query to execute
     * @param options    the options for the query
     * @param onResponse callback to consume raw response which are matched the query
     * @param onFailure  callback to consume error notification invoked when a network exception occurred
     *                   talking to the server
     */
    void fluxRaw(@Nonnull final Flux query,
                 @Nonnull final FluxOptions options,
                 @Nonnull final Consumer<Response<ResponseBody>> onResponse,
                 @Nonnull final Consumer<? super Throwable> onFailure);

    /**
     * Execute a Flux against the Flux service and return the Flux server HTTP response.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @return {@code Response<ResponseBody>} raw response which are matched the query
     */
    @Nonnull
    Response<ResponseBody> fluxRaw(@Nonnull final Flux query, @Nonnull final Map<String, Object> properties);

    /**
     * Execute a Flux against the Flux service and asynchronous stream HTTP response to {@code onResponse}.
     * <p>
     * The callback is call only once.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param onResponse callback to consume raw response which are matched the query
     */
    void fluxRaw(@Nonnull final Flux query,
                 @Nonnull final Map<String, Object> properties,
                 @Nonnull final Consumer<Response<ResponseBody>> onResponse);

    /**
     * Execute a Flux against the Flux service and asynchronous stream HTTP response to {@code onResponse}.
     * <p>
     * The callback is call only once.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param onResponse callback to consume raw response which are matched the query
     * @param onFailure  callback to consume error notification invoked when a network exception occurred
     *                   talking to the server
     */
    void fluxRaw(@Nonnull final Flux query,
                 @Nonnull final Map<String, Object> properties,
                 @Nonnull final Consumer<Response<ResponseBody>> onResponse,
                 @Nonnull final Consumer<? super Throwable> onFailure);

    /**
     * Execute a Flux against the Flux service and return the Flux server HTTP response.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param options    the options for the query
     * @return {@code Response<ResponseBody>} raw response which are matched the query
     */
    @Nonnull
    Response<ResponseBody> fluxRaw(@Nonnull final Flux query,
                                   @Nonnull final Map<String, Object> properties,
                                   @Nonnull final FluxOptions options);

    /**
     * Execute a Flux against the Flux service and asynchronous stream HTTP response to {@code onResponse}.
     * <p>
     * The callback is call only once.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param options    the options for the query
     * @param onResponse callback to consume raw response which are matched the query
     */
    void fluxRaw(@Nonnull final Flux query,
                 @Nonnull final Map<String, Object> properties,
                 @Nonnull final FluxOptions options,
                 @Nonnull final Consumer<Response<ResponseBody>> onResponse);


    /**
     * Execute a Flux against the Flux service and asynchronous stream HTTP response to {@code onResponse}.
     * <p>
     * The callback is call only once.
     *
     * @param query      the flux query to execute
     * @param properties named properties
     * @param options    the options for the query
     * @param onResponse callback to consume raw response which are matched the query
     * @param onFailure  callback to consume error notification invoked when a network exception occurred
     *                   talking to the server
     */
    void fluxRaw(@Nonnull final Flux query,
                 @Nonnull final Map<String, Object> properties,
                 @Nonnull final FluxOptions options,
                 @Nonnull final Consumer<Response<ResponseBody>> onResponse,
                 @Nonnull final Consumer<? super Throwable> onFailure);

    /**
     * Listen the events produced by {@link FluxClient}.
     *
     * @param eventType type of event to listen
     * @param <T>       type of event to listen
     * @param listener  listener to consume events
     */
    <T extends AbstractInfluxEvent> void subscribeEvents(@Nonnull final Class<T> eventType,
                                                         @Nonnull final Consumer<T> listener);

    /**
     * Listen the events produced by {@link FluxClient}.
     *
     * @param <T>      type of event to listen
     * @param listener listener to unsubscribe to events
     */
    <T extends AbstractInfluxEvent> void unsubscribeEvents(@Nonnull final Consumer<T> listener);

    /**
     * Enable Gzip compress for http request body.
     *
     * @return the FluxClient instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClient enableGzip();

    /**
     * Disable Gzip compress for http request body.
     *
     * @return the FluxClient instance to be able to use it in a fluent manner.
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
     * @return the FluxClient instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClient setLogLevel(@Nonnull final HttpLoggingInterceptor.Level logLevel);

    /**
     * Dispose all event listeners before shutdown.
     *
     * @return the FluxClient instance to be able to use it in a fluent manner.
     */
    @Nonnull
    FluxClient close();

    /**
     * Cancellation is performed by the cancel method. Additional methods are provided to determine if the query
     * completed normally or was cancelled.
     */
    interface Cancellable {

        /**
         * Attempt to cancel execution of this query.
         */
        void cancel();

        /**
         * Returns {@link Boolean#TRUE} if query was cancelled.
         *
         * @return {@link Boolean#TRUE} if query was cancelled
         */
        boolean isCancelled();
    }
}
