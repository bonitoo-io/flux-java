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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import io.bonitoo.flux.utils.Preconditions;

/**
 * The now option is a function that returns a time value to be used as a proxy for the current system time.
 *
 * @author Jakub Bednar (bednar@github) (26/07/2018 12:14)
 * @since 1.0.0
 */
public final class NowOption extends AbstractOption {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn'Z'")
            .withZone(ZoneId.of("UTC"));

    private NowOption(@Nonnull final Builder builder) {

        super("now");

        Objects.requireNonNull(builder, "NowOption.Builder is required");

        this.value = builder.function;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 1.0.0
     */
    @Nonnull
    public static NowOption.Builder builder() {
        return new NowOption.Builder();
    }

    /**
     * A builder for {@code NowOption}.
     *
     * @since 1.0.0
     */
    @NotThreadSafe
    public static final class Builder {

        private String function;

        /**
         * Set the static time.
         *
         * @param time static time
         * @return {@code this}
         * @since 1.0.0
         */
        @Nonnull
        public NowOption.Builder time(@Nonnull final Instant time) {

            Objects.requireNonNull(time, "Time is required");

            return function("() => " + DATE_FORMATTER.format(time));
        }

        /**
         * Set the function that return now.
         *
         * @param function the function that return now
         * @return {@code this}
         * @since 1.0.0
         */
        @Nonnull
        public NowOption.Builder function(@Nonnull final String function) {

            Preconditions.checkNonEmptyString(function, "Function");

            this.function = function;

            return this;
        }

        /**
         * Build an instance of NowOption.
         *
         * @return {@link NowOption}
         */
        @Nonnull
        public NowOption build() {

            if (function == null) {
                throw new IllegalStateException("function or time has to be defined");
            }

            return new NowOption(this);
        }
    }
}
