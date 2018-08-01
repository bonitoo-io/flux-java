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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.flux.options.query.AbstractOption;
import io.bonitoo.flux.options.query.NowOption;
import io.bonitoo.flux.options.query.TaskOption;

/**
 * The utility for chaining Flux operators {@link io.bonitoo.flux.operators}.
 *
 * @author Jakub Bednar (bednar@github) (22/06/2018 11:14)
 */
public final class FluxChain {

    private final StringBuilder builder = new StringBuilder();

    private Map<String, Object> parameters = new HashMap<>();
    private List<AbstractOption> options = new ArrayList<>();

    public FluxChain() {
    }

    /**
     * Add the Flux parameters.
     *
     * @param parameters parameters
     * @return the current {@link FluxChain}
     */
    @Nonnull
    public FluxChain addParameters(@Nonnull final Map<String, Object> parameters) {

        Objects.requireNonNull(parameters, "Parameters are required");

        this.parameters.putAll(parameters);

        return this;
    }

    /**
     * @return get bound parameters
     */
    @Nonnull
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Add the Flux query options.
     *
     * @param options Flux query options
     * @return the current {@link FluxChain}
     * @see TaskOption
     * @see NowOption
     */
    @Nonnull
    public FluxChain addOptions(@Nonnull final List<AbstractOption> options) {

        Objects.requireNonNull(options, "Options are required");

        this.options.addAll(options);

        return this;
    }

    /**
     * Appends the operator to the chain sequence.
     *
     * @param operator the incoming operator
     * @return the current {@link FluxChain}
     */
    @Nonnull
    public FluxChain append(@Nullable final CharSequence operator) {

        if (operator == null) {
            return this;
        }

        if (builder.length() != 0) {
            builder.append("\n");
            builder.append("\t|> ");
        }
        builder.append(operator);

        return this;
    }

    /**
     * Appends the {@code source} to the chain sequence.
     *
     * @param source the incoming {@link Flux} operator
     * @return the current {@link FluxChain}
     */
    @Nonnull
    public FluxChain append(@Nonnull final Flux source) {

        Objects.requireNonNull(source, "Flux source is required");

        source.appendActual(this);

        return this;
    }

    /**
     * @return operator chain
     */
    @Nonnull
    String print() {

        StringJoiner joiner = new StringJoiner("\n\n");

        options.forEach(option -> joiner.add(option.toString()));
        joiner.add(builder.toString());

        return joiner.toString();
    }

}
