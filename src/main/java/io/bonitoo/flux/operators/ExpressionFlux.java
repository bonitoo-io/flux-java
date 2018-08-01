package io.bonitoo.flux.operators;

import javax.annotation.Nonnull;

import io.bonitoo.flux.Flux;
import io.bonitoo.flux.FluxChain;
import io.bonitoo.flux.utils.Preconditions;

/**
 * The custom Flux expression.
 *
 * <h3>Example</h3>
 * <pre>
 *     Flux.from("telegraf")
 *          .expression("map(fn: (r) =&gt; r._value * r._value)")
 *          .expression("sum()")
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (27/06/2018 11:21)
 * @since 3.0.0
 */
public final class ExpressionFlux extends AbstractFluxWithUpstream {

    private final String expression;

    public ExpressionFlux(@Nonnull final Flux source, @Nonnull final String expression) {
        super(source);

        Preconditions.checkNonEmptyString(expression, "Expression");

        this.expression = expression;
    }

    @Override
    void appendAfterUpstream(@Nonnull final FluxChain fluxChain) {
        fluxChain.append(expression);
    }
}
