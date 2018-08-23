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
package io.bonitoo.flux.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The settings for customize parsing the Flux CSV response.
 *
 * TODO every constant can be renamed
 *
 * @author Jakub Bednar (bednar@github) (16/07/2018 12:42)
 * @since 1.0.0
 */
@ThreadSafe
public final class FluxCsvParserOptions {

    /**
     * Default FluxCsvParser settings.
     */
    public static final FluxCsvParserOptions DEFAULTS = FluxCsvParserOptions.builder().build();

    private final List<String> valueDestinations;

    private FluxCsvParserOptions(@Nonnull final Builder builder) {
        Objects.requireNonNull(builder, "FluxCsvParserOptions.Builder is required");

        valueDestinations = builder.valueDestinations;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 1.0.0
     */
    @Nonnull
    public static FluxCsvParserOptions.Builder builder() {
        return new FluxCsvParserOptions.Builder();
    }

    /**
     * @return the column names of the record where result will be placed
     * @see Builder#valueDestinations(String...)
     * @since 1.0.0
     */
    @Nonnull
    public List<String> getValueDestinations() {
        return valueDestinations;
    }

    /**
     * A builder for {@code FluxCsvParserOptions}.
     *
     * @since 1.0.0
     */
    @NotThreadSafe
    public static final class Builder {

        private List<String> valueDestinations = new ArrayList<>();

        private Builder() {
            valueDestinations.add("_value");
        }


        /**
         * Set the column names of the record where result will be placed.
         * <p>
         * Map function can produce multiple value columns:
         * <pre>
         * from(db:"foo")
         *     |&gt; filter(fn: (r) =&gt; r["_measurement"]=="cpu" AND
         *                 r["_field"] == "usage_system" AND
         *                 r["service"] == "app-server")
         *     |&gt; range(start:-12h)
         *     // Square the value and keep the original value
         *     |&gt; map(fn: (r) =&gt; ({value: r._value, value2:r._value * r._value}))
         * </pre>
         *
         * @param valueDestinations the column names of the record where result will be placed. Defaults "_value".
         * @return {@code this}
         * @since 1.0.0
         */
        @Nonnull
        public FluxCsvParserOptions.Builder valueDestinations(@Nonnull final String... valueDestinations) {
            Objects.requireNonNull(valueDestinations, "ValueDestinations is required");

            if (valueDestinations.length != 0) {
                this.valueDestinations = Arrays.asList(valueDestinations);
            }

            return this;
        }

        /**
         * Build an instance of FluxCsvParserOptions.
         *
         * @return {@link FluxCsvParserOptions}
         */
        @Nonnull
        public FluxCsvParserOptions build() {

            return new FluxCsvParserOptions(this);
        }
    }
}
