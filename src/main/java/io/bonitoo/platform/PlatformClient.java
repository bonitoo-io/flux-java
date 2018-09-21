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
import io.bonitoo.platform.options.WriteOptions;

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

    //TODO add to all clients createByPOJO

    /**
     * Get client for User API.
     *
     * @return the new client instance for User API
     */
    @Nonnull
    UserClient createUserClient();

    /**
     * Get client for Organization API.
     *
     * @return the new client instance for Organization API
     */
    @Nonnull
    OrganizationClient createOrganizationClient();

    /**
     * Get client for Bucket API.
     *
     * @return the new client instance for Bucket API
     */
    @Nonnull
    BucketClient createBucketClient();

    /**
     * Get client for Task API.
     *
     * @return the new client instance for Task API
     */
    @Nonnull
    TaskClient createTaskClient();

    /**
     * Get client for Authorization API.
     *
     * @return the new client instance for Authorization API
     */
    @Nonnull
    AuthorizationClient createAuthorizationClient();

    /**
     * Get client for Source API.
     *
     * @return the new client instance for Source API
     */
    @Nonnull
    SourceClient createSourceClient();

    /**
     * Get client for Write API.
     *
     * @return the new client instance for Write API
     */
    @Nonnull
    WriteClient createWriteClient();

    /**
     * Get client for Write API.
     *
     * @param writeOptions configure write options
     * @return the new client instance for Write API
     */
    @Nonnull
    WriteClient createWriteClient(@Nonnull final WriteOptions writeOptions);

    /**
     * Check the status of Platform.
     *
     * @return health of instance
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