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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.AbstractRestClient;
import io.bonitoo.GzipRequestInterceptor;
import io.bonitoo.platform.WriteClient;
import io.bonitoo.platform.options.WriteOptions;

/**
 * @author Jakub Bednar (bednar@github) (21/09/2018 11:02)
 */
final class WriteClientImpl extends AbstractRestClient implements WriteClient {

    private final PlatformService platformService;
    private final GzipRequestInterceptor interceptor;

    WriteClientImpl(@Nonnull final WriteOptions writeOptions,
                    @Nonnull final PlatformService platformService,
                    @Nonnull final GzipRequestInterceptor interceptor) {

        this.platformService = platformService;
        this.interceptor = interceptor;
    }

    @Override
    public void write(@Nonnull final String bucket,
                      @Nonnull final String organization,
                      @Nonnull final String token,
                      @Nonnull final TimeUnit precision,
                      @Nonnull final List<String> records) {

        Objects.requireNonNull(records, "records are required");

        write(bucket, organization, token, precision, String.join("\n", records));
    }

    @Override
    public void write(@Nonnull final String bucket,
                      @Nonnull final String organization,
                      @Nonnull final String token,
                      @Nonnull final TimeUnit precision,
                      @Nullable final String records) {


    }

    @Nonnull
    @Override
    public WriteClient enableGzip() {
        interceptor.enable();
        return this;
    }

    @Nonnull
    @Override
    public WriteClient disableGzip() {
        interceptor.disable();
        return this;
    }

    @Override
    public boolean isGzipEnabled() {
        return interceptor.isEnabled();
    }
}