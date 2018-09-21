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
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (21/09/2018 09:14)
 */
@RunWith(JUnitPlatform.class)
class GzipRequestInterceptorTest {

    private MockWebServer platformServer;
    private String url;

    @BeforeEach
    void setUp() {

        platformServer = new MockWebServer();
        try {
            platformServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        url = platformServer.url("/").url().toString();
    }

    @AfterEach
    void after() throws IOException {
        platformServer.shutdown();
    }

    @Test
    void disabledGzip() throws InterruptedException, IOException {

        RecordedRequest recordedRequest = newCall(new GzipRequestInterceptor());

        Assertions.assertThat(recordedRequest.getHeader("Content-Encoding")).isNull();
    }

    @Test
    void enabledGzip() throws InterruptedException, IOException {

        GzipRequestInterceptor interceptor = new GzipRequestInterceptor();
        interceptor.enable();

        RecordedRequest recordedRequest = newCall(interceptor);

        Assertions.assertThat(recordedRequest.getHeader("Content-Encoding")).isEqualTo("gzip");
    }

    @Test
    void enabledGzipUrl() throws IOException, InterruptedException {

        Pattern pattern = Pattern.compile(".*/write", Pattern.CASE_INSENSITIVE);
        GzipRequestInterceptor interceptor = new GzipRequestInterceptor(pattern);
        interceptor.enable();

        RecordedRequest recordedRequest = newCall(interceptor);

        Assertions.assertThat(recordedRequest.getHeader("Content-Encoding")).isEqualTo("gzip");
    }

    @Test
    void enabledGzipNotMatch() throws IOException, InterruptedException {

        Pattern pattern = Pattern.compile(".*/query", Pattern.CASE_INSENSITIVE);
        GzipRequestInterceptor interceptor = new GzipRequestInterceptor(pattern);
        interceptor.enable();

        RecordedRequest recordedRequest = newCall(interceptor);

        Assertions.assertThat(recordedRequest.getHeader("Content-Encoding")).isNull();
    }

    @Nonnull
    private RecordedRequest newCall(@Nonnull final GzipRequestInterceptor interceptor) throws IOException, InterruptedException {

        platformServer.enqueue(new MockResponse());

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        JSONObject json = new JSONObject();
        json.put("name", "Tom Dnif");

        Request request = new Request.Builder()
                .url(url + "write")
                .addHeader("accept", "application/json")
                .post(RequestBody.create(MediaType.parse("application/json"), json.toString()))
                .build();

        okHttpClient.newCall(request).execute();

        return platformServer.takeRequest();
    }
}