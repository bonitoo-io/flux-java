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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.internal.http.RealResponseBody;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * @author Jakub Bednar (bednar@github) (02/08/2018 08:58)
 */
@RunWith(JUnitPlatform.class)
class InfluxExceptionTest {

    @Test
    void unExpectedError() {

        Assertions
                .assertThatThrownBy(() -> {
                    throw InfluxException.fromCause(new IllegalStateException("unExpectedError"));
                })
                .isInstanceOf(InfluxException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasMessage("java.lang.IllegalStateException: unExpectedError");
    }

    @Test
    void retrofitHttpException() {

        Assertions
                .assertThatThrownBy(() -> {

                    throw InfluxException.fromCause(new HttpException(errorResponse("Wrong query")));
                })
                .isInstanceOf(InfluxException.class)
                .hasCauseInstanceOf(HttpException.class)
                .hasMessage("Wrong query");
    }

    @Test
    void retrofitHttpExceptionEmptyError() {

        Assertions
                .assertThatThrownBy(() -> {

                    throw InfluxException.fromCause(new HttpException(errorResponse("")));
                })
                .isInstanceOf(InfluxException.class)
                .hasCauseInstanceOf(HttpException.class)
                .hasMessage("retrofit2.HttpException: HTTP 500 Response.error()");
    }

    @Test
    void retrofitHttpExceptionNullError() {

        Assertions
                .assertThatThrownBy(() -> {

                    throw InfluxException.fromCause(new HttpException(errorResponse("")));
                })
                .isInstanceOf(InfluxException.class)
                .hasCauseInstanceOf(HttpException.class)
                .hasMessage("retrofit2.HttpException: HTTP 500 Response.error()");
    }

    @Nonnull
    private Response<Object> errorResponse(@Nullable final String influxError) {

        okhttp3.Response.Builder builder = new okhttp3.Response.Builder() //
                .code(500)
                .message("Response.error()")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build());

        if (influxError != null) {
            builder.addHeader("X-Influx-Error", influxError);
        }

        return Response.error(new RealResponseBody(null, 0, null), builder.build());
    }
}