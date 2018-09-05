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
package io.bonitoo.flux.options.query;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import io.bonitoo.Preconditions;
import io.bonitoo.flux.operators.properties.TimeInterval;

/**
 * The location option is used to set the default time zone of all times in the script.
 * The location maps the UTC offset in use at that location for a given time.
 * The default value is set using the time zone of the running process.
 *
 * @author Jakub Bednar (bednar@github) (20/08/2018 14:10)
 * @since 1.0.0
 */
public final class LocationOption extends AbstractOption {

    private LocationOption(@Nonnull final Builder builder) {
        super("location");

        Objects.requireNonNull(builder, "LocationOption.Builder is required");

        this.value = builder.function;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 1.0.0
     */
    @Nonnull
    public static LocationOption.Builder builder() {
        return new LocationOption.Builder();
    }

    /**
     * A builder for {@code LocationOption}.
     *
     * @since 1.0.0
     */
    @NotThreadSafe
    public static final class Builder {

        private String function;

        /**
         * Set the timezone offset time.
         *
         * @param offset timezone offset
         * @return {@code this}
         * @since 1.0.0
         */
        @Nonnull
        public LocationOption.Builder offset(@Nonnull final String offset) {

            Preconditions.checkDuration(offset, "offset");

            return function(String.format("fixedZone(offset: %s)", offset));
        }

        /**
         * Set the timezone offset time.
         *
         * @param offset     timezone offset
         * @param offsetUnit a {@code ChronoUnit} determining how to interpret the {@code offset}
         *
         * @return {@code this}
         * @since 1.0.0
         */
        @Nonnull
        public LocationOption.Builder offset(@Nonnull final Long offset,
                                             @Nonnull final ChronoUnit offsetUnit) {

            Objects.requireNonNull(offset, "Offset");
            Objects.requireNonNull(offsetUnit, "Offset unit");

            return offset(new TimeInterval(offset, offsetUnit).toString());
        }

        /**
         * Set the location to specified value ("America/Denver").
         *
         * @param location location to set
         * @return {@code this}
         * @since 1.0.0
         */
        @Nonnull
        public LocationOption.Builder location(@Nonnull final String location) {

            Preconditions.checkNonEmptyString(location, "Location name");

            return function(String.format("loadLocation(name: \"%s\")", location));
        }

        /**
         * Set the function that return location.
         *
         * @param function the function that return location
         * @return {@code this}
         * @since 1.0.0
         */
        @Nonnull
        private LocationOption.Builder function(@Nonnull final String function) {

            Preconditions.checkNonEmptyString(function, "Function");

            this.function = function;

            return this;
        }

        /**
         * Build an instance of LocationOption.
         *
         * @return {@link LocationOption}
         */
        @Nonnull
        public LocationOption build() {

            if (function == null) {
                throw new IllegalStateException("location has to be defined");
            }

            return new LocationOption(this);
        }
    }
}
