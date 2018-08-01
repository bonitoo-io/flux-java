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

import io.bonitoo.flux.mapper.FluxResult;
import io.bonitoo.flux.options.FluxConnectionOptions;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 13:12)
 */
@RunWith(JUnitPlatform.class)
class FluxClientFactoryTest {

    @Test
    void connect() {

        FluxConnectionOptions options = FluxConnectionOptions.builder()
                .url("http://localhost:8093")
                .orgID("0")
                .build();

        FluxClient fluxClient = FluxClientFactory.connect(options);

        Assertions.assertThat(fluxClient).isNotNull();
    }

    @Test
    void f() {

        FluxConnectionOptions options = FluxConnectionOptions.builder()
                .url("http://localhost:8093")
                .orgID("0")
                .build();

// Results
        FluxClient fluxClient = FluxClientFactory.connect(options);

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .difference();

        fluxClient.flux(flux, fluxResult -> {

            logFluxResult(fluxResult);
        });

        fluxClient.close();
    }


    private void logFluxResult(FluxResult fluxResult) {
        //To change body of created methods use File | Settings | File Templates.
    }
}