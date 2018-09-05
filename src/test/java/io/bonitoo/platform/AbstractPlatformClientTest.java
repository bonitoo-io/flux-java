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
package io.bonitoo.platform;

import java.io.IOException;
import javax.annotation.Nonnull;

import io.bonitoo.AbstractTest;
import io.bonitoo.platform.options.PlatformOptions;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 12:29)
 */
public class AbstractPlatformClientTest extends AbstractTest {

    MockWebServer platformServer;
    PlatformClient platformClient;

    @BeforeEach
    protected void setUp() {

        platformServer = new MockWebServer();
        try {
            platformServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PlatformOptions platformOptions = PlatformOptions.builder()
                .url(platformServer.url("/").url().toString())
                .build();

        platformClient = PlatformClientFactory.connect(platformOptions);
    }

    @AfterEach
    protected void after() throws IOException {
        platformServer.shutdown();
    }

    @Nonnull
    protected MockResponse createResponse(final String data) {
        return createResponse(data, "application/json", false);
    }
}