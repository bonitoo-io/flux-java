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
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (03/09/2018 12:10)
 */
class ITPlatformClientTest {

    private static final Logger LOG = Logger.getLogger(ITPlatformClientTest.class.getName());

    private String influxdURL;

    @BeforeEach
    void setUp() {

        String influxdIP = System.getenv().getOrDefault("INFLUXD_IP", "127.0.0.1");
        String influxdPort = System.getenv().getOrDefault("INFLUXD_PORT_API", "9999");

        influxdURL = "http://" + influxdIP + ":" + influxdPort;
        LOG.log(Level.FINEST, "Influxd URL: {0}", influxdURL);
    }

    @Test
    void connectToPlatform() throws IOException {

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        Request request = new Request.Builder()
                .url(influxdURL + "/v1/tasks")
                .addHeader("accept", "application/json")
                .build();

        Response response = okHttpClient.newCall(request).execute();

        Assertions.assertThat(response.code()).isEqualTo(200);
    }
}
