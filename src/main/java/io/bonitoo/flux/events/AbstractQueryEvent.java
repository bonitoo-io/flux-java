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
