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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

/**
 * Implementation of a intercepter to compress http's body using GZIP.
 * <p>
 * Copied from InfluxDB Java - <a href="https://github.com/influxdata/influxdb-java/">thanks</a>
 *
 * @author fujian1115 [at] gmail.com
 */
public final class GzipRequestInterceptor implements Interceptor {

    private final Pattern pattern;

    private AtomicBoolean enabled = new AtomicBoolean(false);

    public GzipRequestInterceptor() {
        this(null);
    }

    /**
     * The pattern that is applied to URL if GZIP is enabled.
     *
     * @param pattern with acceptable url
     */
    public GzipRequestInterceptor(@Nullable final Pattern pattern) {
        this.pattern = pattern;
    }

    public void enable() {
        enabled.set(true);
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void disable() {
        enabled.set(false);
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        if (!enabled.get()) {
            return chain.proceed(chain.request());
        }

        Request request = chain.request();
        RequestBody body = request.body();
        if (body == null || request.header("Content-Encoding") != null || !isEnabledGzip(request.url())) {
            return chain.proceed(request);
        }

        Request compressedRequest = request.newBuilder().header("Content-Encoding", "gzip")
                .method(request.method(), gzip(body)).build();
        return chain.proceed(compressedRequest);
    }

    private boolean isEnabledGzip(final HttpUrl url) {

        if (pattern == null) {
            return true;
        }

        return pattern.matcher(url.encodedPath()).matches();
    }

    private RequestBody gzip(final RequestBody body) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return body.contentType();
            }

            @Override
            public long contentLength() {
                return -1; // We don't know the compressed length in advance!
            }

            @Override
            public void writeTo(final BufferedSink sink) throws IOException {
                BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                body.writeTo(gzipSink);
                gzipSink.close();
            }
        };
    }
}
