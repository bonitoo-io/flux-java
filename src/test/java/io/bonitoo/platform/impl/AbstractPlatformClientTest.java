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
package io.bonitoo.platform.impl;

import java.io.IOException;
import java.util.Optional;
import javax.annotation.Nonnull;

import io.bonitoo.AbstractTest;
import io.bonitoo.GzipRequestInterceptor;
import io.bonitoo.platform.PlatformClient;
import io.bonitoo.platform.PlatformClientFactory;
import io.bonitoo.platform.WriteClient;
import io.bonitoo.platform.options.PlatformOptions;
import io.bonitoo.platform.options.WriteOptions;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 12:29)
 */
public class AbstractPlatformClientTest extends AbstractTest {

    protected MockWebServer platformServer;
    protected PlatformClient platformClient;

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

    @Nonnull
    protected WriteClient createWriteClient(@Nonnull final WriteOptions writeOptions,
                                            @Nonnull final GzipRequestInterceptor interceptor,
                                            @Nonnull final Scheduler batchScheduler,
                                            @Nonnull final Scheduler jitterScheduler,
                                            @Nonnull final Scheduler retryScheduler) {

        Optional<Object> platformService = ReflectionUtils.readFieldValue(PlatformClientImpl.class, "platformService",
                (PlatformClientImpl) platformClient);

        if (!platformService.isPresent()) {
            Assertions.fail();
        }

        return new WriteClientImpl(writeOptions,
                (PlatformService) platformService.get(),
                interceptor,
                Schedulers.trampoline(),
                batchScheduler,
                jitterScheduler,
                retryScheduler);
    }
}