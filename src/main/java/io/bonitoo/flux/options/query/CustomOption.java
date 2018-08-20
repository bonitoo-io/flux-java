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

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;

import io.bonitoo.flux.utils.Preconditions;

/**
 * The custom option is prepare for option specified by user.
 *
 * @author Jakub Bednar (bednar@github) (27/07/2018 07:47)
 * @since 1.0.0
 */
public final class CustomOption extends AbstractOption {

    private CustomOption(@Nonnull final Builder builder) {
        super(builder.optionName);

        Objects.requireNonNull(builder, "CustomOption.Builder is required");

        this.value = builder.optionValue;
    }

    /**
     * Creates a builder instance.
     *
     * @param optionName name of the option
     * @return a builder
     * @since 1.0.0
     */
    @Nonnull
    public static CustomOption.Builder builder(@Nonnull final String optionName) {

        Preconditions.checkNonEmptyString(optionName, "Option name");

        return new CustomOption.Builder(optionName);
    }

    /**
     * A builder for {@code CustomOption}.
     *
     * @since 1.0.0
     */
    @NotThreadSafe
    public static final class Builder {

        private final String optionName;
        private String optionValue;

        public Builder(@Nonnull final String optionName) {

            Preconditions.checkNonEmptyString(optionName, "Option name");

            this.optionName = optionName;
        }

        /**
         * Set the option value.
         *
         * @param optionValue option value
         * @return {@code this}
         * @since 1.0.0
         */
        @Nonnull
        public CustomOption.Builder value(@Nonnull final String optionValue) {

            Preconditions.checkNonEmptyString(optionValue, "Option value");

            this.optionValue = optionValue;

            return this;
        }

        /**
         * Build an instance of CustomOption.
         *
         * @return {@link CustomOption}
         */
        @Nonnull
        public CustomOption build() {

            if (optionValue == null) {
                throw new IllegalStateException("Option value has to be defined");
            }

            return new CustomOption(this);
        }
    }
}
