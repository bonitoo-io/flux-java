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
package io.bonitoo.flux.option;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import io.bonitoo.core.Preconditions;

import okhttp3.OkHttpClient;

/**
 * FluxConnectionOptions are used to configure queries to the Flux.
 *
 * @author Jakub Bednar (bednar@github) (26/06/2018 08:59)
 * @since 1.0.0
 */
@ThreadSafe
public final class FluxConnectionOptions {

    private final String url;
    private final String orgID;
    private OkHttpClient.Builder okHttpClient;

    private FluxConnectionOptions(@Nonnull final Builder builder) {
        Objects.requireNonNull(builder, "FluxConnectionOptions.Builder is required");

        url = builder.url;
        orgID = builder.orgID;
        okHttpClient = builder.okHttpClient;
    }

    /**
     * Creates a builder instance.
     *
     * @return a builder
     * @since 1.0.0
     */
    @Nonnull
    public static FluxConnectionOptions.Builder builder() {
        return new FluxConnectionOptions.Builder();
    }

    /**
     * @return the url to connect to Flux
     * @see FluxConnectionOptions.Builder#url(String)
     * @since 1.0.0
     */
    @Nonnull
    public String getUrl() {
        return url;
    }

    /**
     * @return the organization id required by Flux
     * @see FluxConnectionOptions.Builder#orgID(String)
     * @since 1.0.0
     */

    @Nonnull
    public String getOrgID() {
        return orgID;
    }

    /**
     * @return HTTP client to use for communication with Flux
     * @see FluxConnectionOptions.Builder#okHttpClient(OkHttpClient.Builder)
     * @since 1.0.0
     */
    @Nonnull
    public OkHttpClient.Builder getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * A builder for {@code FluxConnectionOptions}.
     *
     * @since 1.0.0
     */
    @NotThreadSafe
    public static class Builder {

        private String url;
        private String orgID;
        private OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();

        /**
         * Set the url to connect to Flux.
         *
         * @param url the url to connect to Flux. It must be defined.
         * @return {@code this}
         * @since 1.0.0
         */
        @Nonnull
        public Builder url(@Nonnull final String url) {
            Preconditions.checkNonEmptyString(url, "url");
            this.url = url;
            return this;
        }

        /**
         * Set the organization id required by Flux.
         *
         * @param orgID the organization id required by Flux. It must be defined.
         * @return {@code this}
         * @since 1.0.0
         */
        @Nonnull
        public Builder orgID(@Nonnull final String orgID) {
            Preconditions.checkNonEmptyString(orgID, "orgID");
            this.orgID = orgID;
            return this;
        }

        /**
         * Set the HTTP client to use for communication with Flux.
         *
         * @param okHttpClient the HTTP client to use.
         * @return {@code this}
         * @since 1.0.0
         */
        @Nonnull
        public Builder okHttpClient(@Nonnull final OkHttpClient.Builder okHttpClient) {
            Objects.requireNonNull(okHttpClient, "OkHttpClient.Builder is required");
            this.okHttpClient = okHttpClient;
            return this;
        }

        /**
         * Build an instance of FluxConnectionOptions.
         *
         * @return {@link FluxConnectionOptions}
         */
        @Nonnull
        public FluxConnectionOptions build() {

            if (url == null) {
                throw new IllegalStateException("The url to connect to Flux has to be defined.");
            }

            if (orgID == null) {
                throw new IllegalStateException("The organization id required by Flux has to be defined.");
            }

            return new FluxConnectionOptions(this);
        }
    }
}
