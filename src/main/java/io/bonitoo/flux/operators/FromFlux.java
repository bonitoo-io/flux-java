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
package io.bonitoo.flux.operators;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import io.bonitoo.Preconditions;
import io.bonitoo.flux.Flux;
import io.bonitoo.flux.FluxChain;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#from">from</a> - starting point
 * for all queries. Get data from the specified bucket.
 *
 * <h3>Options</h3>
 * <ul>
 * <li><b>bucket</b> - The name of the bucket to query [string]</li>
 * <li><b>hosts</b> - array of strings from(bucket:"telegraf", hosts:["host1", "host2"])</li>
 * </ul>
 *
 * <h3>Example</h3>
 * <pre>
 * Flux flux = Flux.from("telegraf");
 *
 * Flux flux = Flux
 *      .from("telegraf", new String[]{"192.168.1.200", "192.168.1.100"})
 *      .last();
 * </pre>
 *
 * @author Jakub Bednar (bednar@github) (22/06/2018 10:20)
 * @since 1.0.0
 */
public final class FromFlux extends Flux {

    private String bucket;
    private Collection<String> hosts;

    public FromFlux(@Nonnull final String bucket) {
        Preconditions.checkNonEmptyString(bucket, "Bucket name");

        this.bucket = bucket;
    }

    public FromFlux(@Nonnull final String bucket, @Nonnull final String[] hosts) {
        Preconditions.checkNonEmptyString(bucket, "Bucket name");
        Objects.requireNonNull(hosts, "Hosts are required");

        this.bucket = bucket;
        this.hosts = Arrays.asList(hosts);
    }

    public FromFlux(@Nonnull final String bucket, @Nonnull final Collection<String> hosts) {
        Preconditions.checkNonEmptyString(bucket, "Bucket name");
        Objects.requireNonNull(hosts, "Hosts are required");

        this.bucket = bucket;
        this.hosts = hosts;
    }

    @Override
    protected void appendActual(@Nonnull final FluxChain fluxChain) {

        //
        // from(bucket:"telegraf"
        //
        StringBuilder fromBuilder = new StringBuilder()
                .append("from(bucket:\"")
                .append(bucket)
                .append("\"");

        //
        // , hosts:["host1", "host2"]
        //
        if (hosts != null && !hosts.isEmpty()) {

            String concatenatedHosts = hosts.stream().map(host -> "\"" + host + "\"")
                    .collect(Collectors.joining(", "));

            fromBuilder.append(", hosts:[");
            fromBuilder.append(concatenatedHosts);
            fromBuilder.append("]");
        }

        //
        // )
        //
        fromBuilder.append(")");

        fluxChain.append(fromBuilder);
    }
}
