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

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.core.InfluxException;
import io.bonitoo.core.Preconditions;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 14:06)
 */
public abstract class AbstractRestClient {

    private static final Logger LOG = Logger.getLogger(AbstractRestClient.class.getName());

    private static final MediaType CONTENT_TYPE_JSON = MediaType.parse("application/json");

    @Nonnull
    protected RequestBody createBody(@Nonnull final JSONObject json) {

        Objects.requireNonNull(json, "JSON is required");

        String content = json.toString();
        return createBody(content);
    }

    @Nonnull
    protected RequestBody createBody(@Nonnull final String content) {

        Preconditions.checkNonEmptyString(content, "");

        return RequestBody.create(CONTENT_TYPE_JSON, content);
    }

    protected <T> T execute(@Nonnull final Call<T> call) throws InfluxException {
        return execute(call, null);
    }

    protected <T> T execute(@Nonnull final Call<T> call, @Nullable final String nullError) throws InfluxException {

        Objects.requireNonNull(call, "call is required");

        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {

                String error = InfluxException.getErrorMessage(response);

                //
                // The error message signal not found on the server => return null
                //
                if (nullError != null && nullError.equals(error)) {

                    LOG.log(Level.WARNING, "Error is considered as null response: {0}", error);

                    return null;
                }

                throw new InfluxException(error);
            }
        } catch (IOException e) {
            throw InfluxException.fromCause(e);
        }
    }
}