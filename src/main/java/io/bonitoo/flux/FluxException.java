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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import retrofit2.HttpException;
import retrofit2.Response;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 11:53)
 * @since 3.0.0
 */
public class FluxException extends RuntimeException {

    public FluxException(@Nullable final String message) {
        super(message);
    }

    private FluxException(@Nullable final Throwable cause) {
        super(cause);
    }

    private FluxException(@Nullable final String message, @Nullable final Throwable cause) {
        super(message, cause);
    }

    @Nonnull
    public static FluxException fromCause(@Nullable final Throwable cause) {
        if (cause instanceof HttpException) {
            Response<?> response = ((HttpException) cause).response();

            String errorHeader = getErrorMessage(response);

            if (errorHeader != null && !errorHeader.isEmpty()) {
                return new FluxException(errorHeader, cause);
            }
        }

        return new FluxException(cause);
    }

    @Nullable
    public static String getErrorMessage(@Nullable final Response<?> response) {

        if (response == null) {
            return null;
        }

        return response.headers().get("X-Influx-Error");
    }
}
