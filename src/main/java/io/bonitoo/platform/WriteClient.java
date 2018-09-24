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

import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Write time-series data into InfluxDB.
 * <p>
 * The data are formatted in <a href="https://bit.ly/2QL99fu">InfluxDB Line Protocol</a>.
 * TODO write by Point, Pojo
 *
 * @author Jakub Bednar (bednar@github) (20/09/2018 10:58)
 */
public interface WriteClient {

    /**
     * Write time-series data into InfluxDB.
     *
     * @param bucket       specifies the destination bucket name for writes
     * @param organization specifies the destination organization name for writes
     * @param token        the token used to authorize write to bucket
     * @param records      specifies the records in InfluxDB Line Protocol
     */
    void write(@Nonnull final String bucket,
               @Nonnull final String organization,
               @Nonnull final String token,
               @Nonnull final List<String> records);

    /**
     * Write time-series data into InfluxDB.
     *
     * @param bucket       specifies the destination bucket name for writes
     * @param organization specifies the destination organization name for writes
     * @param token        the token used to authorize write to bucket
     * @param precision    specifies the precision for the unix timestamps within the body line-protocol.
     *                     Available values : {@link TimeUnit#NANOSECONDS}, {@link TimeUnit#MICROSECONDS},
     *                     {@link TimeUnit#MILLISECONDS}, {@link TimeUnit#SECONDS}.
     *                     Default value : {@link TimeUnit#NANOSECONDS}.
     * @param records      specifies the records in InfluxDB Line Protocol
     */
    void write(@Nonnull final String bucket,
               @Nonnull final String organization,
               @Nonnull final String token,
               @Nonnull final TimeUnit precision,
               @Nonnull final List<String> records);

    /**
     * Write time-series data into InfluxDB.
     *
     * @param bucket       specifies the destination bucket name for writes
     * @param organization specifies the destination organization name for writes
     * @param token        the token used to authorize write to bucket
     * @param records      specifies the records in InfluxDB Line Protocol
     */
    void write(@Nonnull final String bucket,
               @Nonnull final String organization,
               @Nonnull final String token,
               @Nullable final String records);


    /**
     * Write time-series data into InfluxDB.
     *
     * @param bucket       specifies the destination bucket name for writes
     * @param organization specifies the destination organization name for writes
     * @param token        the token used to authorize write to bucket
     * @param precision    specifies the precision for the unix timestamps within the body line-protocol.
     *                     Available values : {@link TimeUnit#NANOSECONDS}, {@link TimeUnit#MICROSECONDS},
     *                     {@link TimeUnit#MILLISECONDS}, {@link TimeUnit#SECONDS}.
     *                     Default value : {@link TimeUnit#NANOSECONDS}.
     * @param records      specifies the records in InfluxDB Line Protocol
     */
    void write(@Nonnull final String bucket,
               @Nonnull final String organization,
               @Nonnull final String token,
               @Nonnull final TimeUnit precision,
               @Nullable final String records);

    /**
     * Enable Gzip compress for http request body.
     *
     * @return the {@link WriteClient} instance to be able to use it in a fluent manner.
     */
    @Nonnull
    WriteClient enableGzip();

    /**
     * Disable Gzip compress for http request body.
     *
     * @return the {@link WriteClient} instance to be able to use it in a fluent manner.
     */
    @Nonnull
    WriteClient disableGzip();

    /**
     * Returns whether Gzip compress for http request body is enabled.
     *
     * @return true if gzip is enabled.
     */
    boolean isGzipEnabled();

    /**
     * Close threads for asynchronous batch writing.
     *
     * @return the {@link WriteClient} instance to be able to use it in a fluent manner.
     */
    @Nonnull
    WriteClient close();
}