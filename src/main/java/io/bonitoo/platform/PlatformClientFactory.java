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

import java.util.Objects;
import javax.annotation.Nonnull;

import io.bonitoo.platform.impl.PlatformClientImpl;
import io.bonitoo.platform.option.PlatformOptions;

/**
 * The Factory that create a instance of a Platform client.
 *
 * @author Jakub Bednar (bednar@github) (05/09/2018 10:04)
 * @since 1.0.0
 */
public final class PlatformClientFactory {

    private PlatformClientFactory() {
    }

    /**
     * Create a instance of the Platform client.
     *
     * @param url the url to connect to Platform.
     * @return client
     * @see PlatformOptions.Builder#url(String)
     */
    @Nonnull
    public static PlatformClient connect(@Nonnull final String url) {

        PlatformOptions options = PlatformOptions.builder()
                .url(url)
                .build();

        return connect(options);
    }

    /**
     * Create a instance of the Platform client.
     *
     * @param options the connection configuration
     * @return client
     */
    @Nonnull
    public static PlatformClient connect(@Nonnull final PlatformOptions options) {

        Objects.requireNonNull(options, "PlatformOptions are required");

        return new PlatformClientImpl(options);
    }
}