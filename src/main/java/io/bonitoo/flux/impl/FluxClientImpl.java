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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import io.bonitoo.flux.dto.FluxRecord;
import io.bonitoo.flux.dto.FluxTable;
import io.bonitoo.flux.events.AbstractFluxEvent;
import io.bonitoo.flux.events.FluxErrorEvent;
import io.bonitoo.flux.events.FluxSuccessEvent;
import io.bonitoo.flux.events.UnhandledErrorEvent;
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
        super(options, FluxService.class);
    }

    @Nonnull
    @Override
    public List<FluxTable> flux(@Nonnull final String query) {

        Objects.requireNonNull(query, "Flux query is required");

        return flux(query, FluxOptions.DEFAULTS);
    }

    @Nonnull
    @Override
    public List<FluxTable> flux(@Nonnull final String query, @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return flux(new StringFlux(query), options);
    }

    @Override
    public Cancellable flux(@Nonnull final String query, @Nonnull final Consumer<FluxRecord> onNext) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(onNext, "Callback consumer is required");

        return flux(query, onNext, EMPTY_ON_COMPLETE);
    }

    @Override
    public Cancellable flux(@Nonnull final String query,
                            @Nonnull final Consumer<FluxRecord> onNext,
                            @Nonnull final Consumer<Boolean> onComplete) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(onNext, "onNext consumer is required");
        Objects.requireNonNull(onComplete, "onComplete consumer is required");

        return flux(query, onNext, onComplete, EMPTY_ON_ERROR);
    }

    @Override
    public Cancellable flux(@Nonnull final String query,
                            @Nonnull final Consumer<FluxRecord> onNext,
                            @Nonnull final Consumer<Boolean> onComplete,
                            @Nonnull final Consumer<? super Throwable> onError) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(onNext, "onNext consumer is required");
        Objects.requireNonNull(onComplete, "onComplete consumer is required");
        Objects.requireNonNull(onError, "onError consumer is required");

        return flux(query, FluxOptions.DEFAULTS, onNext, onComplete, onError);
    }

    @Override
    public Cancellable flux(@Nonnull final String query,
                            @Nonnull final FluxOptions options,
                            @Nonnull final Consumer<FluxRecord> onNext) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(onNext, "Callback consumer is required");

        return flux(query, options, onNext, EMPTY_ON_COMPLETE);
    }

    @Override
    public Cancellable flux(@Nonnull final String query,
                            @Nonnull final FluxOptions options,
                            @Nonnull final Consumer<FluxRecord> onNext,
                            @Nonnull final Consumer<Boolean> onComplete) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(onNext, "onNext consumer is required");
        Objects.requireNonNull(onComplete, "onComplete consumer is required");

        return flux(query, options, onNext, onComplete, EMPTY_ON_ERROR);
    }

    @Override
    public Cancellable flux(@Nonnull final String query,
                            @Nonnull final FluxOptions options,
                            @Nonnull final Consumer<FluxRecord> onNext,
                            @Nonnull final Consumer<Boolean> onComplete,
                            @Nonnull final Consumer<? super Throwable> onError) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(onNext, "onNext consumer is required");
        Objects.requireNonNull(onComplete, "onComplete consumer is required");
        Objects.requireNonNull(onError, "onError consumer is required");

        return flux(new StringFlux(query), options, onNext, onComplete, onError);
    }

    @Nonnull
    @Override
    public Response<ResponseBody> fluxRaw(@Nonnull final String query) {

        Objects.requireNonNull(query, "Flux query is required");

        return fluxRaw(query, FluxOptions.DEFAULTS);
    }

    @Nonnull
    @Override
    public Response<ResponseBody> fluxRaw(@Nonnull final String query, @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return fluxRaw(new StringFlux(query), options);
    }

    @Override
    public void fluxRaw(@Nonnull final String query, @Nonnull final Consumer<Response<ResponseBody>> onResponse) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(onResponse, "Callback consumer is required");

        fluxRaw(query, FluxOptions.DEFAULTS, onResponse);
    }

    @Override
    public void fluxRaw(@Nonnull final String query,
                        @Nonnull final Consumer<Response<ResponseBody>> onResponse,
                        @Nonnull final Consumer<? super Throwable> onFailure) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(onResponse, "onResponse consumer is required");
        Objects.requireNonNull(onFailure, "onFailure consumer is required");

        fluxRaw(query, FluxOptions.DEFAULTS, onResponse, onFailure);
    }

    @Override
    public void fluxRaw(@Nonnull final String query,
                        @Nonnull final FluxOptions options,
                        @Nonnull final Consumer<Response<ResponseBody>> onResponse) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(onResponse, "Callback consumer is required");

        fluxRaw(new StringFlux(query), options, onResponse);
    }

    @Override
    public void fluxRaw(@Nonnull final String query,
                        @Nonnull final FluxOptions options,
                        @Nonnull final Consumer<Response<ResponseBody>> onResponse,
                        @Nonnull final Consumer<? super Throwable> onFailure) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(onResponse, "onResponse consumer is required");
        Objects.requireNonNull(onFailure, "onFailure consumer is required");

        fluxRaw(new StringFlux(query), options, onResponse, onFailure);
    }

    @Nonnull
    @Override
    public List<FluxTable> flux(@Nonnull final Flux query) {

        Objects.requireNonNull(query, "Flux query is required");

        return flux(query, FluxOptions.DEFAULTS);
    }

    @Override
    public Cancellable flux(@Nonnull final Flux query, @Nonnull final Consumer<FluxRecord> onNext) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(onNext, "Callback consumer is required");

        return flux(query, onNext, EMPTY_ON_COMPLETE, EMPTY_ON_ERROR);
    }

    @Override
    public Cancellable flux(@Nonnull final Flux query,
                            @Nonnull final Consumer<FluxRecord> onNext,
                            @Nonnull final Consumer<Boolean> onComplete) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(onNext, "onNext consumer is required");
        Objects.requireNonNull(onComplete, "onComplete consumer is required");

        return flux(query, onNext, onComplete, EMPTY_ON_ERROR);
    }

    @Override
    public Cancellable flux(@Nonnull final Flux query,
                            @Nonnull final Consumer<FluxRecord> onNext,
                            @Nonnull final Consumer<Boolean> onComplete,
                            @Nonnull final Consumer<? super Throwable> onError) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(onNext, "onNext consumer is required");
        Objects.requireNonNull(onComplete, "onComplete consumer is required");
        Objects.requireNonNull(onError, "onError consumer is required");

        return flux(query, new HashMap<>(), FluxOptions.DEFAULTS, onNext, onComplete, onError);
    }

    @Nonnull
    @Override
    public List<FluxTable> flux(@Nonnull final Flux query, @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return flux(query, new HashMap<>(), options);
    }

    @Override
    public Cancellable flux(@Nonnull final Flux query,
                            @Nonnull final FluxOptions options,
                            @Nonnull final Consumer<FluxRecord> onNext) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(onNext, "Callback consumer is required");

        return flux(query, options, onNext, EMPTY_ON_COMPLETE);
    }

    @Override
    public Cancellable flux(@Nonnull final Flux query,
                            @Nonnull final FluxOptions options,
                            @Nonnull final Consumer<FluxRecord> onNext,
                            @Nonnull final Consumer<Boolean> onComplete) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(onNext, "onNext consumer is required");
        Objects.requireNonNull(onComplete, "onComplete consumer is required");

        return flux(query, options, onNext, onComplete, EMPTY_ON_ERROR);
    }

    @Override
    public Cancellable flux(@Nonnull final Flux query,
                            @Nonnull final FluxOptions options,
                            @Nonnull final Consumer<FluxRecord> onNext,
                            @Nonnull final Consumer<Boolean> onComplete,
                            @Nonnull final Consumer<? super Throwable> onError) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(onNext, "onNext consumer is required");
        Objects.requireNonNull(onComplete, "onComplete consumer is required");
        Objects.requireNonNull(onError, "onError consumer is required");

        return flux(query, new HashMap<>(), options, onNext, onComplete, onError);
    }

    @Nonnull
    @Override
    public List<FluxTable> flux(@Nonnull final Flux query, @Nonnull final Map<String, Object> properties) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");

        return flux(query, properties, FluxOptions.DEFAULTS);
    }

    @Override
    public Cancellable flux(@Nonnull final Flux query,
                            @Nonnull final Map<String, Object> properties,
                            @Nonnull final Consumer<FluxRecord> onNext) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(onNext, "Callback consumer is required");

        return flux(query, properties, onNext, EMPTY_ON_COMPLETE);
    }

    @Override
    public Cancellable flux(@Nonnull final Flux query,
                            @Nonnull final Map<String, Object> properties,
                            @Nonnull final Consumer<FluxRecord> onNext,
                            @Nonnull final Consumer<Boolean> onComplete) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(onNext, "onNext consumer is required");
        Objects.requireNonNull(onComplete, "onComplete consumer is required");

        return flux(query, properties, onNext, onComplete, EMPTY_ON_ERROR);
    }

    @Override
    public Cancellable flux(@Nonnull final Flux query,
                            @Nonnull final Map<String, Object> properties,
                            @Nonnull final Consumer<FluxRecord> onNext,
                            @Nonnull final Consumer<Boolean> onComplete,
                            @Nonnull final Consumer<? super Throwable> onError) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(onNext, "onNext consumer is required");
        Objects.requireNonNull(onComplete, "onComplete consumer is required");
        Objects.requireNonNull(onError, "onError consumer is required");

        return flux(query, properties, FluxOptions.DEFAULTS, onNext, onComplete, onError);
    }

    @Nonnull
    @Override
    public List<FluxTable> flux(@Nonnull final Flux query,
                                @Nonnull final Map<String, Object> properties,
                                @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return flux(query, properties, options, false,
                EMPTY_ON_NEXT, EMPTY_ON_COMPLETE, EMPTY_ON_ERROR, new DefaultCancellable());
    }

    @Override
    public Cancellable flux(@Nonnull final Flux query,
                            @Nonnull final Map<String, Object> properties,
                            @Nonnull final FluxOptions options,
                            @Nonnull final Consumer<FluxRecord> onNext) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(onNext, "Callback consumer is required");

        return flux(query, properties, options, onNext, EMPTY_ON_COMPLETE, EMPTY_ON_ERROR);
    }

    @Override
    public Cancellable flux(@Nonnull final Flux query,
                            @Nonnull final Map<String, Object> properties,
                            @Nonnull final FluxOptions options,
                            @Nonnull final Consumer<FluxRecord> onNext,
                            @Nonnull final Consumer<Boolean> onComplete) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(onNext, "onNext consumer is required");
        Objects.requireNonNull(onComplete, "onComplete consumer is required");

        return flux(query, properties, options, onNext, onComplete, EMPTY_ON_ERROR);
    }

    @Override
    public Cancellable flux(@Nonnull final Flux query,
                            @Nonnull final Map<String, Object> properties,
                            @Nonnull final FluxOptions options,
                            @Nonnull final Consumer<FluxRecord> onNext,
                            @Nonnull final Consumer<Boolean> onComplete,
                            @Nonnull final Consumer<? super Throwable> onError) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(onNext, "onNext consumer is required");
        Objects.requireNonNull(onComplete, "onComplete consumer is required");
        Objects.requireNonNull(onError, "onError consumer is required");

        DefaultCancellable cancellable = new DefaultCancellable();

        flux(query, properties, options, true, onNext, onComplete, onError, cancellable);

        return cancellable;
    }

    @Nonnull
    @Override
    public Response<ResponseBody> fluxRaw(@Nonnull final Flux query) {

        Objects.requireNonNull(query, "Flux query is required");

        return fluxRaw(query, FluxOptions.DEFAULTS);
    }

    @Override
    public void fluxRaw(@Nonnull final Flux query, @Nonnull final Consumer<Response<ResponseBody>> onResponse) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(onResponse, "Callback consumer is required");

        fluxRaw(query, onResponse, EMPTY_ON_ERROR);
    }

    @Override
    public void fluxRaw(@Nonnull final Flux query,
                        @Nonnull final Consumer<Response<ResponseBody>> onResponse,
                        @Nonnull final Consumer<? super Throwable> onFailure) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(onResponse, "onResponse consumer is required");
        Objects.requireNonNull(onFailure, "onFailure consumer is required");

        fluxRaw(query, FluxOptions.DEFAULTS, onResponse, onFailure);
    }

    @Nonnull
    @Override
    public Response<ResponseBody> fluxRaw(@Nonnull final Flux query, @Nonnull final FluxOptions options) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return fluxRaw(query, new HashMap<>(), options);
    }

    @Override
    public void fluxRaw(@Nonnull final Flux query,
                        @Nonnull final FluxOptions options,
                        @Nonnull final Consumer<Response<ResponseBody>> onResponse) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(onResponse, "Callback consumer is required");

        fluxRaw(query, options, onResponse, EMPTY_ON_ERROR);
    }

    @Override
    public void fluxRaw(@Nonnull final Flux query,
                        @Nonnull final FluxOptions options,
                        @Nonnull final Consumer<Response<ResponseBody>> onResponse,
                        @Nonnull final Consumer<? super Throwable> onFailure) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(onResponse, "onResponse consumer is required");
        Objects.requireNonNull(onFailure, "onFailure consumer is required");

        fluxRaw(query, new HashMap<>(), options, onResponse, onFailure);
    }

    @Nonnull
    @Override
    public Response<ResponseBody> fluxRaw(@Nonnull final Flux query,
                                          @Nonnull final Map<String, Object> properties) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");

        return fluxRaw(query, properties, FluxOptions.DEFAULTS);
    }

    @Override
    public void fluxRaw(@Nonnull final Flux query,
                        @Nonnull final Map<String, Object> properties,
                        @Nonnull final Consumer<Response<ResponseBody>> onResponse) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(onResponse, "Callback consumer is required");

        fluxRaw(query, properties, FluxOptions.DEFAULTS, onResponse);
    }

    @Override
    public void fluxRaw(@Nonnull final Flux query,
                        @Nonnull final Map<String, Object> properties,
                        @Nonnull final Consumer<Response<ResponseBody>> onResponse,
                        @Nonnull final Consumer<? super Throwable> onFailure) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(onResponse, "onResponse consumer is required");
        Objects.requireNonNull(onFailure, "onFailure consumer is required");

        fluxRaw(query, properties, FluxOptions.DEFAULTS, onResponse, onFailure);
    }

    @Nonnull
    @Override
    public Response<ResponseBody> fluxRaw(@Nonnull final Flux flux,
                                          @Nonnull final Map<String, Object> properties,
                                          @Nonnull final FluxOptions options) {

        Objects.requireNonNull(flux, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");

        String query = toFluxString(flux, properties, options);

        Response<ResponseBody> response = fluxRaw(query, properties, options, false, result -> {
        }, EMPTY_ON_ERROR, new DefaultCancellable());

        if (response == null) {
            throw new IllegalStateException("Response is null");
        }

        return response;
    }

    @Override
    public void fluxRaw(@Nonnull final Flux query,
                        @Nonnull final Map<String, Object> properties,
                        @Nonnull final FluxOptions options,
                        @Nonnull final Consumer<Response<ResponseBody>> onResponse) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(onResponse, "Callback consumer is required");

        fluxRaw(query, properties, options, onResponse, EMPTY_ON_ERROR);
    }

    @Override
    public void fluxRaw(@Nonnull final Flux query,
                        @Nonnull final Map<String, Object> properties,
                        @Nonnull final FluxOptions options,
                        @Nonnull final Consumer<Response<ResponseBody>> onResponse,
                        @Nonnull final Consumer<? super Throwable> onFailure) {

        Objects.requireNonNull(query, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(onResponse, "onResponse consumer is required");
        Objects.requireNonNull(onFailure, "onFailure consumer is required");

        String queryString = toFluxString(query, properties, options);

        fluxRaw(queryString, properties, options, true, onResponse, onFailure, new DefaultCancellable());
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

    @Nonnull
    private List<FluxTable> flux(@Nonnull final Flux flux,
                                 @Nonnull final Map<String, Object> properties,
                                 @Nonnull final FluxOptions options,
                                 @Nonnull final Boolean async,
                                 @Nonnull final Consumer<FluxRecord> onNext,
                                 @Nonnull final Consumer<Boolean> onComplete,
                                 @Nonnull final Consumer<? super Throwable> onError,
                                 @Nonnull final DefaultCancellable cancellable) {

        Objects.requireNonNull(flux, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(async, "Async configuration is required");
        Objects.requireNonNull(onNext, "onNext consumer is required");
        Objects.requireNonNull(onComplete, "onComplete consumer is required");
        Objects.requireNonNull(onError, "onError consumer is required");
        Objects.requireNonNull(cancellable, "FluxClient.Cancellable is required");

        String query = toFluxString(flux, properties, options);
        Response<ResponseBody> response = fluxRaw(query, properties, options, async, asyncResponse -> {

            if (!asyncResponse.isSuccessful()) {
                errorResponse(query, asyncResponse, onError, cancellable, true);
                return;
            }

            ResponseBody body = asyncResponse.body();
            if (body == null) {
                return;
            }

            try {
                BufferedSource source = body.source();

                //
                // Source has data => parse
                //
                while (!source.exhausted() && !cancellable.wasCancelled) {

                    mapper.toFluxRecords(source, onNext, () -> !cancellable.wasCancelled);
                }

                cancellable.wasDone = true;
                onComplete.accept(!cancellable.wasCancelled);
                publish(new FluxSuccessEvent(fluxConnectionOptions, query));

            } catch (Exception e) {

                //
                // Socket closed by remote server or end of data
                //
                if (e.getMessage().equals("Socket closed") || e instanceof EOFException) {
                    LOG.log(Level.FINEST, "Socket closed by remote server or end of data", e);
                } else {
                    cancellable.wasError = true;
                    onError.accept(e);
                    publish(new UnhandledErrorEvent(e));
                }
            } finally {

                body.close();
            }

        }, onError, cancellable);

        if (!async && response != null) {
            try {

                if (response.isSuccessful()) {

                    ResponseBody body = response.body();
                    if (body == null) {
                        return new ArrayList<>();
                    }

                    BufferedSource source = body.source();
                    List<FluxTable> tables = mapper.toFluxTables(source);

                    publish(new FluxSuccessEvent(fluxConnectionOptions, query));

                    return tables;
                } else {

                    errorResponse(query, response, onError, cancellable, false);
                }

            } catch (Exception e) {

                FluxException exception = FluxException.fromCause(e);
                publish(new UnhandledErrorEvent(exception));

                throw exception;
            }
        }

        return new ArrayList<>();
    }

    @Nullable
    private Response<ResponseBody> fluxRaw(@Nonnull final String query,
                                           @Nonnull final Map<String, Object> properties,
                                           @Nonnull final FluxOptions options,
                                           @Nonnull final Boolean async,
                                           @Nonnull final Consumer<Response<ResponseBody>> callback,
                                           @Nonnull final Consumer<? super Throwable> onError,
                                           @Nonnull final DefaultCancellable cancellable) {

        Preconditions.checkNonEmptyString(query, "Flux query");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");
        Objects.requireNonNull(async, "Async configuration is required");
        Objects.requireNonNull(callback, "Callback consumer is required");
        Objects.requireNonNull(onError, "onError consumer is required");
        Objects.requireNonNull(cancellable, "cancellable is required");

        String orgID = this.fluxConnectionOptions.getOrgID();

        Call<ResponseBody> request = fluxService.query(orgID, createBody(query, options));

        if (async) {
            request.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@Nonnull final Call<ResponseBody> call,
                                       @Nonnull final Response<ResponseBody> response) {

                    callback.accept(response);
                }

                @Override
                public void onFailure(@Nonnull final Call<ResponseBody> call,
                                      @Nonnull final Throwable throwable) {

                    propagateError(onError, cancellable, throwable, new UnhandledErrorEvent(throwable));
                }
            });
        } else {
            try {

                return request.execute();

            } catch (Exception e) {

                FluxException throwable = FluxException.fromCause(e);

                publish(new UnhandledErrorEvent(throwable));
                onError.accept(e);

                throw throwable;
            }
        }

        return null;
    }

    private void errorResponse(@Nonnull final String query,
                               @Nonnull final Response<ResponseBody> response,
                               @Nonnull final Consumer<? super Throwable> onError,
                               @Nonnull final DefaultCancellable cancellable,
                               @Nonnull final Boolean async) {

        Preconditions.checkNonEmptyString(query, "Query");
        Objects.requireNonNull(response, "Response is required");
        Objects.requireNonNull(async, "Async is required");

        String error = FluxException.getErrorMessage(response);

        FluxException exception;
        if (error != null) {
            exception = new FluxException(error);
        } else {
            exception = new FluxException("Unsuccessful response: " + response);
        }

        FluxErrorEvent errorEvent = new FluxErrorEvent(fluxConnectionOptions, query, exception);
        propagateError(onError, cancellable, exception, errorEvent);

        if (!async) {
            throw exception;
        }
    }

    private void propagateError(@Nonnull final Consumer<? super Throwable> onError,
                                @Nonnull final DefaultCancellable cancellable,
                                @Nonnull final Throwable throwable,
                                @Nonnull final AbstractFluxEvent errorEvent) {

        cancellable.wasError = true;
        onError.accept(throwable);
        publish(errorEvent);
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

    private class DefaultCancellable implements Cancellable {
        private volatile boolean wasCancelled = false;
        private volatile boolean wasDone = false;
        private volatile boolean wasError = false;

        @Override
        public boolean cancel() {
            if (!isDone()) {
                wasCancelled = true;
            }
            return wasCancelled;
        }

        @Override
        public boolean isCancelled() {
            return wasCancelled;
        }

        @Override
        public boolean isDone() {
            return wasDone || wasError || wasCancelled;
        }
    }
}
