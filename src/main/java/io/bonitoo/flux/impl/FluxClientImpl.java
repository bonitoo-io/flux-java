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
package io.bonitoo.flux.impl;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.flux.Flux;
import io.bonitoo.flux.FluxClient;
import io.bonitoo.flux.FluxException;
import io.bonitoo.flux.events.AbstractFluxEvent;
import io.bonitoo.flux.events.FluxErrorEvent;
import io.bonitoo.flux.events.FluxSuccessEvent;
import io.bonitoo.flux.events.UnhandledErrorEvent;
import io.bonitoo.flux.mapper.FluxResult;
import io.bonitoo.flux.options.FluxConnectionOptions;
import io.bonitoo.flux.options.FluxOptions;
import io.bonitoo.flux.utils.Preconditions;

import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Jakub Bednar (bednar@github) (30/07/2018 13:56)
 */
public class FluxClientImpl extends AbstractFluxClient<FluxService> implements FluxClient {

    private static final Logger LOG = Logger.getLogger(FluxClientImpl.class.getName());

    private final Map<Class<?>, Set<Consumer>> subscribers = new ConcurrentHashMap<>();

    public FluxClientImpl(@Nonnull final FluxConnectionOptions options) {
        super(options, FluxService.class, null);
    }

    @Nonnull
    @Override
    public FluxResult flux(@Nonnull final Flux query) {

        Objects.requireNonNull(query, "Flux is required");

        return flux(query, FluxOptions.DEFAULTS);
    }

    @Override
    public void flux(@Nonnull final Flux query, @Nonnull final Consumer<FluxResult> callback) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(callback, "Callback consumer is required");

        flux(query, FluxOptions.DEFAULTS, callback);
    }

    @Nonnull
    @Override
    public FluxResult flux(@Nonnull final Flux query, @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return flux(query, new HashMap<>(), options);
    }

    @Override
    public void flux(@Nonnull final Flux query,
                     @Nonnull final FluxOptions options,
                     @Nonnull final Consumer<FluxResult> callback) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(callback, "Callback consumer is required");

        flux(query, new HashMap<>(), options, callback);
    }

    @Nonnull
    @Override
    public FluxResult flux(@Nonnull final Flux query, @Nonnull final Map<String, Object> properties) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");

        return flux(query, properties, FluxOptions.DEFAULTS);
    }

    @Override
    public void flux(@Nonnull final Flux query,
                     @Nonnull final Map<String, Object> properties,
                     @Nonnull final Consumer<FluxResult> callback) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(callback, "Callback consumer is required");

        flux(query, properties, FluxOptions.DEFAULTS, callback);
    }

    @Nonnull
    @Override
    public FluxResult flux(@Nonnull final Flux query,
                           @Nonnull final Map<String, Object> properties,
                           @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");

        FluxResult fluxResult = flux(query, properties, options, false, result -> {
        });

        if (fluxResult == null) {
            throw new IllegalStateException("Result is null");
        }

        return fluxResult;
    }

    @Override
    public void flux(@Nonnull final Flux query,
                     @Nonnull final Map<String, Object> properties,
                     @Nonnull final FluxOptions options,
                     @Nonnull final Consumer<FluxResult> callback) {

        Objects.requireNonNull(query, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(callback, "Callback consumer is required");

        flux(query, properties, options, true, callback);
    }

    @Nullable
    private FluxResult flux(@Nonnull final Flux flux,
                            @Nonnull final Map<String, Object> properties,
                            @Nonnull final FluxOptions options,
                            @Nonnull final Boolean async,
                            @Nonnull final Consumer<FluxResult> callback) {

        Objects.requireNonNull(flux, "Flux is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(async, "Async configuration is required");
        Objects.requireNonNull(callback, "Callback consumer is required");

        String orgID = this.fluxConnectionOptions.getOrgID();
        String query = toFluxString(flux, properties, options);

        Call<ResponseBody> request = fluxService.query(query, orgID);
        if (async) {
            request.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@Nonnull final Call<ResponseBody> call,
                                       @Nonnull final Response<ResponseBody> response) {

                    if (!response.isSuccessful()) {
                        errorResponse(query, response);
                        callback.accept(FluxResult.empty());
                        return;
                    }

                    ResponseBody body = response.body();
                    if (body == null) {
                        return;
                    }

                    try {
                        BufferedSource source = body.source();

                        //
                        // Source has data => parse
                        //
                        while (!source.exhausted()) {

                            FluxResult fluxResult = mapper.toFluxResult(source, options.getParserOptions());
                            if (fluxResult != null) {

                                callback.accept(fluxResult);
                            }
                        }

                        publish(new FluxSuccessEvent(fluxConnectionOptions, query));

                    } catch (IOException e) {

                        //
                        // Socket closed by remote server or end of data
                        //
                        if (e.getMessage().equals("Socket closed") || e instanceof EOFException) {
                            LOG.log(Level.FINEST, "Socket closed by remote server or end of data", e);
                        } else {
                            publish(new UnhandledErrorEvent(e));
                        }
                    }

                    publish(new FluxSuccessEvent(fluxConnectionOptions, query));

                    body.close();
                }

                @Override
                public void onFailure(@Nonnull final Call<ResponseBody> call,
                                      @Nonnull final Throwable t) {

                    publish(new UnhandledErrorEvent(t));
                }
            });
        } else {
            try {

                Response<ResponseBody> response = request.execute();
                if (response.isSuccessful()) {

                    ResponseBody body = response.body();
                    if (body == null) {
                        return FluxResult.empty();
                    }

                    BufferedSource source = body.source();
                    FluxResult fluxResult = mapper.toFluxResult(source, options.getParserOptions());

                    publish(new FluxSuccessEvent(fluxConnectionOptions, query));

                    return fluxResult;
                } else {

                    errorResponse(query, response);
                }

            } catch (Exception e) {

                publish(new UnhandledErrorEvent(FluxException.fromCause(e)));
            }
        }

        return FluxResult.empty();
    }

    @Override
    public <T extends AbstractFluxEvent> void subscribeEvents(@Nonnull final Class<T> eventType,
                                                              @Nonnull final Consumer<T> listener) {

        Objects.requireNonNull(eventType, "Event type is required");
        Objects.requireNonNull(listener, "Consumer is required");

        Set<Consumer> listeners = subscribers.get(eventType);
        if (listeners == null) {
            listeners = new CopyOnWriteArraySet<>();
            subscribers.put(eventType, listeners);
        }

        listeners.add(listener);
    }

    @Override
    public <T extends AbstractFluxEvent> void unsubscribeEvents(@Nonnull final Consumer<T> listener) {

        Objects.requireNonNull(listener, "Consumer is required");

        subscribers.values().forEach(listeners -> listeners.remove(listener));
    }

    @Nonnull
    @Override
    public FluxClient enableGzip() {

        this.gzipRequestInterceptor.enable();

        return this;
    }

    @Nonnull
    @Override
    public FluxClient disableGzip() {

        this.gzipRequestInterceptor.disable();

        return this;
    }

    @Override
    public boolean isGzipEnabled() {
        return this.gzipRequestInterceptor.isEnabled();
    }

    @Nonnull
    @Override
    public Boolean ping() {

        try {
            return fluxService.ping().execute().isSuccessful();
        } catch (Exception e) {
            publish(new UnhandledErrorEvent(e));
        }

        return false;
    }

    @Nonnull
    @Override
    public HttpLoggingInterceptor.Level getLogLevel() {

        return this.loggingInterceptor.getLevel();
    }

    @Nonnull
    @Override
    public FluxClient setLogLevel(@Nonnull final HttpLoggingInterceptor.Level logLevel) {

        Objects.requireNonNull(logLevel, "Log level is required");

        this.loggingInterceptor.setLevel(logLevel);

        return this;
    }

    @Nonnull
    @Override
    public FluxClient close() {

        LOG.log(Level.INFO, "Dispose all event listeners before shutdown.");

        subscribers.clear();

        return this;
    }

    private void errorResponse(@Nonnull final String query, @Nonnull final Response<ResponseBody> response) {

        Preconditions.checkNonEmptyString(query, "Query");
        Objects.requireNonNull(response, "Response is required");

        String error = FluxException.getErrorMessage(response);

        FluxException exception;
        if (error != null) {
            exception = new FluxException(error);
        } else {
            exception = new FluxException("Unsuccessful response: " + response);
        }

        publish(new FluxErrorEvent(fluxConnectionOptions, query, exception));
    }

    private void publish(@Nonnull final AbstractFluxEvent event) {

        Objects.requireNonNull(event, "Event is required");

        event.logEvent();

        Class<?> eventType = event.getClass();

        //noinspection unchecked
        subscribers.keySet().stream()
                .filter(type -> type.isAssignableFrom(eventType))
                .flatMap(type -> subscribers.get(type).stream())
                .forEach(consumer -> consumer.accept(event));
    }
}
