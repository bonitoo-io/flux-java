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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.annotation.Nonnull;

import io.bonitoo.core.InfluxException;
import io.bonitoo.flux.dto.FluxRecord;
import io.bonitoo.flux.dto.FluxTable;
import io.bonitoo.flux.event.FluxErrorEvent;

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

        List<FluxTable> result = fluxClient.flux(Flux.from("flux_database"));

        assertSuccessResult(result);
    }

    @Test
    void queryParameters() {

        fluxServer.enqueue(createResponse());

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("n", 5);

        fluxClient.flux(Flux.from("flux_database").limit().withPropertyNamed("n"), properties);

        Assertions.assertThat(getObjectFromBody("query"))
                .isEqualToIgnoringWhitespace("from(bucket:\"flux_database\") |> limit(n: 5)");
    }

    @Test
    void queryError() {

        fluxServer.enqueue(createErrorResponse("Flux query is not valid"));

        Assertions.assertThatThrownBy(() -> fluxClient.flux(Flux.from("flux_database")))
                .isInstanceOf(InfluxException.class)
                .hasMessage("Flux query is not valid");
    }

    @Test
    void queryErrorSuccessResponse() {

        String error =
                "#datatype,string,string\n"
                + "#group,true,true\n"
                + "#default,,\n"
                + ",error,reference\n"
                + ",failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time,897";

        fluxServer.enqueue(createResponse(error));

        Assertions.assertThatThrownBy(() -> fluxClient.flux(Flux.from("flux_database")))
                .isInstanceOf(InfluxException.class)
                .hasMessage("failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time [reference: 897]");
    }

    @Test
    void queryErrorSuccessResponseWithoutReference() {

        String error =
                "#datatype,string,string\n"
                        + "#group,true,true\n"
                        + "#default,,\n"
                        + ",error,reference\n"
                        + ",failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time,";

        fluxServer.enqueue(createResponse(error));

        Assertions.assertThatThrownBy(() -> fluxClient.flux(Flux.from("flux_database")))
                .isInstanceOf(InfluxException.class)
                .hasMessage("failed to create physical plan: invalid time bounds from procedure from: bounds contain zero time");
    }

    @Test
    void queryCallback() {

        countDownLatch = new CountDownLatch(4);

        fluxServer.enqueue(createResponse());

        List<FluxRecord> records = new ArrayList<>();
        fluxClient.flux(Flux.from("flux_database"), (cancellable, result) -> {
            records.add(result);

            countDownLatch.countDown();
        });

        waitToCallback();

        assertRecords(records);
    }

    @Test
    void queryCallbackOnComplete() {

        countDownLatch = new CountDownLatch(5);

        fluxServer.enqueue(createResponse());

        List<FluxRecord> records = new ArrayList<>();
        fluxClient.flux(Flux.from("flux_database"), (cancellable, result) -> {
            records.add(result);

            countDownLatch.countDown();
        }, () -> {

            countDownLatch.countDown();
        });

        waitToCallback();

        assertRecords(records);
    }

    @Test
    void queryCallbackParameters() {

        fluxServer.enqueue(createResponse());

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("n", 5);

        Flux query = Flux.from("flux_database").limit().withPropertyNamed("n");
        fluxClient.flux(query, properties, (cancellable, result) -> countDownLatch.countDown());

        waitToCallback();

        Assertions.assertThat(getObjectFromBody("query"))
                .isEqualToIgnoringWhitespace("from(bucket:\"flux_database\") |> limit(n: 5)");
    }

    @Test
    void queryCallbackError() {

        fluxServer.enqueue(createErrorResponse("Flux query is not valid", true));

        fluxClient.subscribeEvents(FluxErrorEvent.class, fluxErrorEvent -> countDownLatch.countDown());
        fluxClient.flux(Flux.from("flux_database"), (cancellable, result) -> {

            Assertions.fail("Unreachable");
        });

        waitToCallback();
    }


    @Test
    void queryCallbackErrorOnError() {

        countDownLatch = new CountDownLatch(2);

        fluxServer.enqueue(createErrorResponse("Flux query is not valid", true));

        fluxClient.subscribeEvents(FluxErrorEvent.class, fluxErrorEvent -> countDownLatch.countDown());
        fluxClient.flux(Flux.from("flux_database"), (cancellable, result) -> {

            Assertions.fail("Unreachable");
        }, FluxClientFactory.EMPTY_ON_COMPLETE, throwable -> countDownLatch.countDown());

        waitToCallback();
    }

    @Test
    void queryString() {

        fluxServer.enqueue(createResponse());

        String query = "from(bucket:\"telegraf\") |> " +
                "filter(fn: (r) => r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\") |> sum()";

        List<FluxTable> result = fluxClient.flux(query);

        assertSuccessResult(result);
    }

    @Test
    void queryStringCallback() {

        countDownLatch = new CountDownLatch(4);

        fluxServer.enqueue(createResponse());

        List<FluxRecord> records = new ArrayList<>();

        String query = "from(bucket:\"telegraf\") |> " +
                "filter(fn: (r) => r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\") |> sum()";

        fluxClient.flux(query, (cancellable, record) -> {
            records.add(record);

            countDownLatch.countDown();
        });

        waitToCallback();
        assertRecords(records);
    }

    @Test
    void queryStringRequestQueryParameter() {

        fluxServer.enqueue(createResponse());

        String query = "from(bucket:\"telegraf\") |> " +
                "filter(fn: (r) => r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\") |> sum()";

        fluxClient.flux(query);

        Assertions.assertThat(getObjectFromBody("query")).isEqualTo(query);
    }

    @Test
    void queryDialect() {

        fluxServer.enqueue(createResponse());

        fluxClient.flux(Flux.from("telegraf"));

        Assertions.assertThat(getObjectFromBody("dialect"))
                .isEqualToIgnoringWhitespace("{\"quoteChar\":\"\\\"\",\"commentPrefix\":\"#\",\"delimiter\":\",\",\"header\":true,\"annotations\":[\"datatype\",\"group\",\"default\"]}");
    }

    private void assertSuccessResult(@Nonnull final List<FluxTable> tables) {

        Assertions.assertThat(tables).isNotNull();
        Assertions.assertThat(tables).hasSize(1);
        List<FluxRecord> records = tables.get(0).getRecords();
        assertRecords(records);
    }

    private void assertRecords(@Nonnull final List<FluxRecord> records) {
        Assertions.assertThat(records).hasSize(4);
    }
}