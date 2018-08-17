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

import java.util.HashMap;
import javax.annotation.Nonnull;

import io.bonitoo.flux.mapper.FluxResult;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 07:05)
 */
@RunWith(JUnitPlatform.class)
class FluxClientQueryTest extends AbstractFluxClientTest {

    @Test
    void query() {

        fluxServer.enqueue(createResponse());

        FluxResult result = fluxClient.flux(Flux.from("flux_database"));

        assertSuccessResult(result);
    }

    @Test
    void queryParameters() throws InterruptedException {

        fluxServer.enqueue(createResponse());

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("n", 5);

        fluxClient.flux(Flux.from("flux_database").limit().withPropertyNamed("n"), properties);

        Assertions.assertThat(fluxServer.takeRequest().getRequestUrl().queryParameter("query"))
                .isEqualTo("from(db:\"flux_database\")|> limit(n: 5)");
    }

    @Test
    void queryError() {

        fluxServer.enqueue(createErrorResponse("Flux query is not valid"));

        FluxResult result = fluxClient.flux(Flux.from("flux_database"));

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getTables()).hasSize(0);
    }

    @Test
    void queryCallback() {

        fluxServer.enqueue(createResponse());

        fluxClient.flux(Flux.from("flux_database"), result -> {
            assertSuccessResult(result);

            countDownLatch.countDown();
        });

        waitToCallback();
    }

    @Test
    void queryCallbackParameters() throws InterruptedException {

        fluxServer.enqueue(createResponse());

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("n", 5);

        Flux query = Flux.from("flux_database").limit().withPropertyNamed("n");
        fluxClient.flux(query, properties, result -> countDownLatch.countDown());

        waitToCallback();

        Assertions.assertThat(fluxServer.takeRequest().getRequestUrl().queryParameter("query"))
                .isEqualTo("from(db:\"flux_database\")|> limit(n: 5)");
    }

    @Test
    void queryCallbackError() {

        fluxServer.enqueue(createErrorResponse("Flux query is not valid", true));

        fluxClient.flux(Flux.from("flux_database"), result -> {

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getTables()).hasSize(0);

            countDownLatch.countDown();
        });

        waitToCallback();
    }

    @Test
    void queryString() {

        fluxServer.enqueue(createResponse());

        String query = "from(db:\"telegraf\") |> " +
                "filter(fn: (r) => r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\") |> sum()";

        FluxResult result = fluxClient.flux(query);

        assertSuccessResult(result);
    }

    @Test
    void queryStringCallback() {

        fluxServer.enqueue(createResponse());

        String query = "from(db:\"telegraf\") |> " +
                "filter(fn: (r) => r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\") |> sum()";

        fluxClient.flux(query, result -> {
            assertSuccessResult(result);

            countDownLatch.countDown();
        });

        waitToCallback();
    }

    @Test
    void queryStringRequestQueryParameter() throws InterruptedException {

        fluxServer.enqueue(createResponse());

        String query = "from(db:\"telegraf\") |> " +
                "filter(fn: (r) => r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\") |> sum()";

        fluxClient.flux(query);

        Assertions.assertThat(fluxServer.takeRequest().getRequestUrl().queryParameter("query")).isEqualTo(query);
    }

    private void assertSuccessResult(@Nonnull final FluxResult result) {

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getTables()).hasSize(1);
        Assertions.assertThat(result.getTables().get(0).getRecords()).hasSize(4);
    }
}