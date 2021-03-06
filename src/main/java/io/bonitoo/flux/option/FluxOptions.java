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
package io.bonitoo.flux.option;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import io.bonitoo.flux.option.query.AbstractOption;
import io.bonitoo.flux.option.query.NowOption;
import io.bonitoo.flux.option.query.TaskOption;

/**
 * The settings for customize Flux query.
 *
 * @author Jakub Bednar (bednar@github) (16/07/2018 13:50)
 * @since 1.0.0
 */
@ThreadSafe
public final class FluxOptions {

    /**
     * Default FluxOptions settings.
     */
    public static final FluxOptions DEFAULTS = FluxOptions.builder().build();

    private final FluxDialect dialect;
    private final List<AbstractOption> queryOptions = new ArrayList<>();

    private FluxOptions(@Nonnull final Builder builder) {

        Objects.requireNonNull(builder, "FluxOptions.Builder is required");

        this.dialect = builder.dialect;
        this.queryOptions.addAll(builder.variables);
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 1.0.0
     */
    @Nonnull
    public static FluxOptions.Builder builder() {
        return new FluxOptions.Builder();
    }

    /**
     * @return the Flux dialect
     * @see Builder#dialect(FluxDialect)
     */
    @Nonnull
    public FluxDialect getDialect() {
        return dialect;
    }

    /**
     * @return the Flux query options that define variables
     * @see Builder#addOption(AbstractOption)
     */
    @Nonnull
    public List<AbstractOption> getQueryOptions() {
        return queryOptions;
    }

    /**
     * A builder for {@code FluxOptions}.
     *
     * @since 1.0.0
     */
    @NotThreadSafe
    public static class Builder {

        private FluxDialect dialect = FluxDialect.DEFAULTS;
        private List<AbstractOption> variables = new ArrayList<>();

        /**
         * Set {@link FluxDialect} that defining the options to use when encoding the response.
         *
         * @param fluxDialect defining the options to use when encoding the response
         * @return {@code this}
         * @since 1.0.0
         */
        @Nonnull
        public Builder dialect(@Nonnull final FluxDialect fluxDialect) {
            Objects.requireNonNull(fluxDialect, "FluxDialect is required");

            this.dialect = fluxDialect;
            return this;
        }

        /**
         * Add option that define variables of Flux query.
         *
         * @param option option that define variables of Flux query
         * @param <O>    type of options
         * @return {@code this}
         * @see NowOption
         * @see TaskOption
         */
        @Nonnull
        public <O extends AbstractOption> FluxOptions.Builder addOption(@Nonnull final O option) {

            Objects.requireNonNull(option, "Option is required");

            this.variables.add(option);

            return this;
        }

        /**
         * Build an instance of FluxOptions.
         *
         * @return {@link FluxOptions}
         */
        @Nonnull
        public FluxOptions build() {

            return new FluxOptions(this);
        }
    }
}
