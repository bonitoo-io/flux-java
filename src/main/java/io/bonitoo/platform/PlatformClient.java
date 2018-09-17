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

import javax.annotation.Nonnull;

import io.bonitoo.flux.FluxClient;
import io.bonitoo.platform.dto.Health;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * The client of the InfluxData Platform for Time Series that implement HTTP API defined by
 * <a href="https://github.com/influxdata/platform/blob/master/http/swagger.yml">Influx API Service swagger.yml</a>.
 *
 * @author Jakub Bednar (bednar@github) (05/09/2018 07:20)
 * @see FluxClient
 * @since 1.0.0
 */
public interface PlatformClient {

    /**
     * Get client for User API.
     *
     * @return the client for User API
     */
    @Nonnull
    UserClient getUserClient();

    /**
     * Get client for Organization API.
     *
     * @return the client for Organization API
     */
    @Nonnull
    OrganizationClient getOrganizationClient();

    /**
     * Get client for Bucket API.
     *
     * @return the client for Bucket API
     */
    @Nonnull
    BucketClient getBucketClient();

    /**
     * Get client for Task API.
     *
     * @return the client for Task API
     */
    @Nonnull
    TaskClient getTaskClient();

    /**
     * Get client for Authorization API.
     *
     * @return the client for Authorization API
     */
    @Nonnull
    AuthorizationClient getAuthorizationClient();

    /**
     * Check the status of Platform.
     *
     * @return {@link Boolean#TRUE} if server is healthy otherwise return {@link Boolean#FALSE}
     */
    @Nonnull
    Health health();

    /**
     * @return the {@link HttpLoggingInterceptor.Level} that is used for logging requests and responses
     */
    @Nonnull
    HttpLoggingInterceptor.Level getLogLevel();

    /**
     * Set the log level for the request and response information.
     *
     * @param logLevel the log level to set.
     * @return the PlatformClient instance to be able to use it in a fluent manner.
     */
    @Nonnull
    PlatformClient setLogLevel(@Nonnull final HttpLoggingInterceptor.Level logLevel);
}