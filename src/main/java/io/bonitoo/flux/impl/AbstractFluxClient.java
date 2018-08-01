package io.bonitoo.flux.impl;

import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.flux.Flux;
import io.bonitoo.flux.FluxChain;
import io.bonitoo.flux.mapper.FluxResultMapper;
import io.bonitoo.flux.options.FluxConnectionOptions;
import io.bonitoo.flux.options.FluxOptions;
import io.bonitoo.flux.utils.GzipRequestInterceptor;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * @param <T> type of Retrofit Service
 * @author Jakub Bednar (bednar@github) (30/07/2018 14:01)
 */
public abstract class AbstractFluxClient<T> {

    protected final FluxResultMapper mapper = new FluxResultMapper();
    protected final FluxConnectionOptions fluxConnectionOptions;
    final T fluxService;
    final HttpLoggingInterceptor loggingInterceptor;
    final GzipRequestInterceptor gzipRequestInterceptor;

    AbstractFluxClient(@Nonnull final FluxConnectionOptions options,
                       @Nonnull final Class<T> serviceType, @Nullable final T service) {

        Objects.requireNonNull(options, "FluxConnectionOptions are required");
        Objects.requireNonNull(serviceType, "Flux service type are required");

        this.fluxConnectionOptions = options;
        this.loggingInterceptor = new HttpLoggingInterceptor();
        this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        this.gzipRequestInterceptor = new GzipRequestInterceptor();

        if (service != null) {
            this.fluxService = service;
        } else {

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
}
