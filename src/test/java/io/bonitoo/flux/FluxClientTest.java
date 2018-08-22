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

import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 09:24)
 */
class FluxClientTest extends AbstractFluxClientTest {

    @Test
    void gzip() {

        // default disabled
        Assertions.assertThat(fluxClient.isGzipEnabled()).isEqualTo(false);

        // enable
        fluxClient.enableGzip();
        Assertions.assertThat(fluxClient.isGzipEnabled()).isEqualTo(true);

        // disable
        fluxClient.disableGzip();
        Assertions.assertThat(fluxClient.isGzipEnabled()).isEqualTo(false);
    }

    @Test
    void gzipHeader() throws InterruptedException {

        fluxServer.enqueue(new MockResponse());
        fluxServer.enqueue(new MockResponse());

        // Disabled GZIP
        fluxClient.disableGzip();
        fluxClient.flux(Flux.from("flux_database"));
        // GZIP header IS NOT set
        Assertions.assertThat(fluxServer.takeRequest().getHeader("Content-Encoding")).isNull();

        // Enabled GZIP
        fluxClient.enableGzip();
        fluxClient.flux(Flux.from("flux_database"));
        // GZIP header IS set
        Assertions.assertThat(fluxServer.takeRequest().getHeader("Content-Encoding")).isEqualTo("gzip");
    }

    @Test
    void logLevel() {

        // default NONE
        Assertions.assertThat(fluxClient.getLogLevel()).isEqualTo(HttpLoggingInterceptor.Level.NONE);

        // set HEADERS
        fluxClient.setLogLevel(HttpLoggingInterceptor.Level.HEADERS);

        Assertions.assertThat(fluxClient.getLogLevel()).isEqualTo(HttpLoggingInterceptor.Level.HEADERS);
    }
}