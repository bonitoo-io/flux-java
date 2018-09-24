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

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.AbstractTest;
import io.bonitoo.flux.impl.FluxClientImpl;
import io.bonitoo.flux.options.FluxConnectionOptions;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 07:06)
 */
public abstract class AbstractFluxClientTest extends AbstractTest {

    MockWebServer fluxServer;
    FluxClientImpl fluxClient;

    @BeforeEach
    protected void setUp() {

        fluxServer = new MockWebServer();
        try {
            fluxServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FluxConnectionOptions fluxConnectionOptions = FluxConnectionOptions.builder()
                .url(fluxServer.url("/").url().toString())
                .orgID("0")
                .build();

        fluxClient = new FluxClientImpl(fluxConnectionOptions);
    }

    @AfterEach
    protected void after() throws IOException {
        fluxClient.close();
        fluxServer.shutdown();
    }

    @Nonnull
    MockResponse createResponse() {

        String data =
                "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,string\n"
                        + "#group,false,false,false,false,false,false,false,false,false,true\n"
                        + "#default,_result,,,,,,,,,\n"
                        + ",result,table,_start,_stop,_time,_value,_field,_measurement,host,region\n"
                        + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,west\n"
                        + ",,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,20,free,mem,B,west\n"
                        + ",,0,1970-01-01T00:00:20Z,1970-01-01T00:00:30Z,1970-01-01T00:00:20Z,11,free,mem,A,west\n"
                        + ",,0,1970-01-01T00:00:20Z,1970-01-01T00:00:30Z,1970-01-01T00:00:20Z,22,free,mem,B,west\n";

        return createResponse(data);
    }

    @Nonnull
    MockResponse createResponse(final String data) {
        return createResponse(data, "text/csv", true);
    }

    @Nullable
    String getObjectFromBody(@Nonnull final String key) {
        return getRequestBodyAsJSON(fluxServer).get(key).toString();
    }
}