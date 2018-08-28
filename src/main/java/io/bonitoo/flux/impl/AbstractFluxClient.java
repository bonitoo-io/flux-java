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

import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

import io.bonitoo.flux.Flux;
import io.bonitoo.flux.FluxChain;
import io.bonitoo.flux.mapper.impl.FluxResultMapper;
import io.bonitoo.flux.options.FluxConnectionOptions;
import io.bonitoo.flux.options.FluxOptions;
import io.bonitoo.flux.utils.GzipRequestInterceptor;
import io.bonitoo.flux.utils.Preconditions;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.json.JSONObject;
import retrofit2.Retrofit;

/**
 * @param <T> type of Retrofit Service
 * @author Jakub Bednar (bednar@github) (30/07/2018 14:01)
 */
public abstract class AbstractFluxClient<T> {

    private static final MediaType CONTENT_TYPE_JSON = MediaType.parse("application/json");

    protected final FluxResultMapper mapper = new FluxResultMapper();
    protected final FluxConnectionOptions fluxConnectionOptions;
    final T fluxService;
    final HttpLoggingInterceptor loggingInterceptor;
    final GzipRequestInterceptor gzipRequestInterceptor;

    AbstractFluxClient(@Nonnull final FluxConnectionOptions options,
                       @Nonnull final Class<T> serviceType) {

        Objects.requireNonNull(options, "FluxConnectionOptions are required");
        Objects.requireNonNull(serviceType, "Flux service type are required");

        this.fluxConnectionOptions = options;
        this.loggingInterceptor = new HttpLoggingInterceptor();
        this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        this.gzipRequestInterceptor = new GzipRequestInterceptor();

        OkHttpClient okHttpClient = fluxConnectionOptions.getOkHttpClient()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(gzipRequestInterceptor)
                    .build();

            Retrofit.Builder serviceBuilder = new Retrofit.Builder()
                    .baseUrl(fluxConnectionOptions.getUrl())
                    .client(okHttpClient);

            configure(serviceBuilder);

            this.fluxService = serviceBuilder
                    .build()
                    .create(serviceType);
    }

    /**
     * Configure Retrofit Service Builder.
     *
     * @param serviceBuilder builder
     */
    protected void configure(@Nonnull final Retrofit.Builder serviceBuilder) {
    }

    @Nonnull
    protected String toFluxString(@Nonnull final Flux flux,
                                  @Nonnull final Map<String, Object> properties,
                                  @Nonnull final FluxOptions options) {

        Objects.requireNonNull(flux, "Flux query is required");
        Objects.requireNonNull(properties, "Properties are required");
        Objects.requireNonNull(options, "FluxOptions are required");

        return flux.print(new FluxChain().addParameters(properties).addOptions(options.getQueryOptions()));
    }

    @Nonnull
    protected RequestBody createBody(@Nonnull final String query, @Nonnull final FluxOptions options) {

        Preconditions.checkNonEmptyString(query, "query");
        Objects.requireNonNull(options, "FluxOptions are required");

        String body = new JSONObject()
                .put("query", query)
                .put("dialect", options.getDialect().getJson())
                .toString();

        return RequestBody.create(CONTENT_TYPE_JSON, body);
    }

    class StringFlux extends Flux {

        private final String fluxQuery;

        StringFlux(@Nonnull final String fluxQuery) {

            Preconditions.checkNonEmptyString(fluxQuery, "Flux query");

            this.fluxQuery = fluxQuery;
        }

        @Override
        protected void appendActual(@Nonnull final FluxChain fluxChain) {
            fluxChain.append(fluxQuery);
        }
    }
}
