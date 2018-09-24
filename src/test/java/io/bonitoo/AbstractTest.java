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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 10:21)
 */
public abstract class AbstractTest {

    protected CountDownLatch countDownLatch;

    @BeforeEach
    protected void prepare() {
        countDownLatch = new CountDownLatch(1);
    }

    protected void waitToCallback() {
        waitToCallback(10);
    }

    protected void waitToCallback(final int seconds) {
        waitToCallback(countDownLatch, seconds);
    }

    protected void waitToCallback(@Nonnull final CountDownLatch countDownLatch, final int seconds) {
        try {
            Assertions.assertThat(countDownLatch.await(seconds, TimeUnit.SECONDS))
                    .overridingErrorMessage("The countDown wasn't counted to zero. Before elapsed: %s seconds.", seconds)
                    .isTrue();
        } catch (InterruptedException e) {
            Assertions.fail("Unexpected exception", e);
        }
    }

    protected void holdTheProcessing() {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    protected MockResponse createResponse(final String data, final String contentType, final boolean chunked) {

        MockResponse response = new MockResponse()
                .setHeader("Content-Type", contentType + "; charset=utf-8")
                .setHeader("Date", "Tue, 26 Jun 2018 13:15:01 GMT");

        if (chunked) {
            response.setChunkedBody(data, data.length());
        } else {
            response.setBody(data);
        }

        return response;
    }

    @Nonnull
    protected String getRequestBody(@Nonnull final MockWebServer server) {

        Assertions.assertThat(server).isNotNull();

        RecordedRequest recordedRequest = null;
        try {
            recordedRequest = server.takeRequest(10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Assertions.fail("Unexpected exception", e);
        }
        Assertions.assertThat(recordedRequest).isNotNull();

        return recordedRequest.getBody().readUtf8();
    }

    @Nonnull
    protected JSONObject getRequestBodyAsJSON(@Nonnull final MockWebServer server) {

        String body = getRequestBody(server);

        return new JSONObject(body);
    }

    @Nonnull
    protected MockResponse createErrorResponse(@Nullable final String influxDBError) {
        return createErrorResponse(influxDBError, false);
    }

    @Nonnull
    protected MockResponse createErrorResponse(@Nullable final String influxDBError, final boolean chunked) {

        String body = String.format("{\"error\":\"%s\"}", influxDBError);

        MockResponse mockResponse = new MockResponse()
                .setResponseCode(500)
                .addHeader("X-Influx-Error", influxDBError);

        if (chunked) {
            return mockResponse.setChunkedBody(body, body.length());
        }

        return mockResponse.setBody(body);
    }
}