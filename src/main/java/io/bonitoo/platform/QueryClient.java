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
package io.bonitoo.platform;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

import io.bonitoo.flux.FluxClient;
import io.bonitoo.flux.dto.FluxRecord;
import io.bonitoo.flux.dto.FluxTable;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * The client of the InfluxData Platform for Time Series that implement Query HTTP API endpoint.
 *
 * @author Jakub Bednar (bednar@github) (26/09/2018 13:01)
 */
public interface QueryClient {

    /**
     * Execute a Flux query against the Platform and synchronously map whole response to {@link FluxTable}s.
     *
     * @param query   the flux query to execute
     * @param dialect the flux dialect
     * @param token   the token used to authorize query
     * @return {@code List<FluxTable>} which are matched the query
     */
    @Nonnull
    List<FluxTable> query(@Nonnull final String query, @Nonnull final String dialect, @Nonnull final String token);

    /**
     * Execute a Flux query against the Platform and asynchronous stream {@link FluxRecord}s to {@code onNext}.
     *
     * @param query      the flux query to execute
     * @param dialect    the flux dialect
     * @param token      the token used to authorize query
     * @param onNext     callback to consume result which are matched the query
     *                   with capability to discontinue a streaming query
     * @param onComplete callback to consume a notification about successfully end of stream,
     * @param onError    callback to consume any error notification
     */
    void query(@Nonnull final String query,
               @Nonnull final String dialect,
               @Nonnull final String token,
               @Nonnull final BiConsumer<FluxClient.Cancellable, FluxRecord> onNext,
               @Nonnull final Runnable onComplete,
               @Nonnull final Consumer<? super Throwable> onError);

    /**
     * Execute a Flux query against the Platform and synchronously return the server HTTP response.
     *
     * @param query   the flux query to execute
     * @param dialect the flux dialect
     * @param token   the token used to authorize query
     * @return {@code Response<ResponseBody>} raw response which matched the query
     */
    @Nonnull
    Response<ResponseBody> raw(@Nonnull final String query,
                               @Nonnull final String dialect,
                               @Nonnull final String token);

    /**
     * Execute a Flux query against the Platform and asynchronous stream raw response to {@code onResponse}.
     * <p>
     * The callback is call only once.
     *
     * @param query      the flux query to execute
     * @param dialect    the flux dialect
     * @param token      the token used to authorize query
     * @param onResponse callback to consume the raw response which are matched the query
     * @param onFailure  callback to consume the error notification invoked when a network exception occurred
     */
    void raw(@Nonnull final String query,
             @Nonnull final String dialect,
             @Nonnull final String token,
             @Nonnull final Consumer<Response<ResponseBody>> onResponse,
             @Nonnull final Consumer<? super Throwable> onFailure);
}