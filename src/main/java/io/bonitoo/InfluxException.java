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
package io.bonitoo;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import retrofit2.HttpException;
import retrofit2.Response;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 11:53)
 * @since 1.0.0
 */
public class InfluxException extends RuntimeException {

    public InfluxException(@Nullable final String message) {
        super(message);
    }

    private InfluxException(@Nullable final Throwable cause) {
        super(cause);
    }

    private InfluxException(@Nullable final String message, @Nullable final Throwable cause) {
        super(message, cause);
    }

    @Nonnull
    public static InfluxException fromCause(@Nullable final Throwable cause) {

        if (cause instanceof InfluxException) {
            return (InfluxException) cause;
        }

        if (cause instanceof HttpException) {
            Response<?> response = ((HttpException) cause).response();

            String errorHeader = getErrorMessage(response);

            if (errorHeader != null && !errorHeader.isEmpty()) {
                return new InfluxException(errorHeader, cause);
            }
        }

        return new InfluxException(cause);
    }

    @Nullable
    public static String getErrorMessage(@Nonnull final Response<?> response) {

        Objects.requireNonNull(response, "Response is required");

        return response.headers().get("X-Influx-Error");
    }
}
