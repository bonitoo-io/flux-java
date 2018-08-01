package io.bonitoo.flux.custom;

import javax.annotation.Nonnull;

import io.bonitoo.flux.Flux;
import io.bonitoo.flux.operators.AbstractParametrizedFlux;
import io.bonitoo.flux.utils.Preconditions;

/**
 * The custom function.
 *
 * @author Jakub Bednar (bednar@github) (02/07/2018 14:23)
 */
public class FilterMeasurement extends AbstractParametrizedFlux {

    public FilterMeasurement(@Nonnull final Flux source) {
        super(source);
    }

    @Nonnull
    @Override
    protected String operatorName() {
        return "measurement";
    }

    /**
     * @param measurement the measurement name. Has to be defined.
     * @return this
     */
    @Nonnull
    public FilterMeasurement withName(@Nonnull final String measurement) {

        Preconditions.checkNonEmptyString(measurement, "Measurement name");

        withPropertyValueEscaped("m", measurement);

        return this;
    }
}