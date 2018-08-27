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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import okhttp3.ResponseBody;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import retrofit2.Response;

/**
 * @author Jakub Bednar (bednar@github) (03/08/2018 12:14)
 */
@RunWith(JUnitPlatform.class)
class FluxClientQueryRawTest extends AbstractFluxClientTest {

    @Test
    void queryRaw() {

        fluxServer.enqueue(createResponse());

        Response<ResponseBody> result = fluxClient.fluxRaw(Flux.from("telegraf"));

        assertSuccessResult(result);
    }

    @Test
    void queryRawProperties() {

        fluxServer.enqueue(createResponse());

        Map<String, Object> properties = new HashMap<>();
        properties.put("n", 10);

        Flux query = Flux
                .from("telegraf")
                .count()
                    .withPropertyNamed("n");

        Response<ResponseBody> result = fluxClient.fluxRaw(query, properties);

        assertSuccessResult(result);
    }

    @Test
    void queryRawCallback() {

        fluxServer.enqueue(createResponse());

        Map<String, Object> properties = new HashMap<>();
        properties.put("n", 10);

        Flux query = Flux
                .from("telegraf")
                .count()
                .withPropertyNamed("n");

        fluxClient.fluxRaw(query, properties, result -> {
            assertSuccessResult(result);

            countDownLatch.countDown();
        });

        waitToCallback();
    }

    @Test
    void queryRawCallbackProperties() {

        fluxServer.enqueue(createResponse());

        fluxClient.fluxRaw(Flux.from("telegraf"), result -> {
            assertSuccessResult(result);

            countDownLatch.countDown();
        });

        waitToCallback();
    }

    @Test
    void queryRawString() {

        fluxServer.enqueue(createResponse());

        String query = "from(db:\"telegraf\") |> " +
                "filter(fn: (r) => r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\") |> sum()";

        Response<ResponseBody> result = fluxClient.fluxRaw(query);

        assertSuccessResult(result);
    }

    @Test
    void queryRawStringCallback() {

        fluxServer.enqueue(createResponse());

        String query = "from(db:\"telegraf\") |> " +
                "filter(fn: (r) => r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\") |> sum()";

        fluxClient.fluxRaw(query, result -> {
            assertSuccessResult(result);

            countDownLatch.countDown();
        });

        waitToCallback();
    }

    @Test
    void queryRawError() {

        fluxServer.enqueue(createErrorResponse("Flux query is not valid"));

        Response<ResponseBody> fluxQuery = fluxClient.fluxRaw(Flux.from("flux_database"));

        Assertions.assertThat(fluxQuery.isSuccessful()).isFalse();
    }

    @Test
    void queryRawDialect() {

        fluxServer.enqueue(createResponse());

        fluxClient.fluxRaw(Flux.from("telegraf"));

        Assertions.assertThat(getObjectFromBody("dialect"))
                .isEqualToIgnoringWhitespace("{\"quoteChar\":\"\\\"\",\"commentPrefix\":\"#\",\"delimiter\":\",\",\"header\":true,\"annotations\":[\"datatype\",\"group\",\"default\"]}");
    }

    private void assertSuccessResult(@Nullable final Response<ResponseBody> result) {

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.isSuccessful()).isTrue();
        Assertions.assertThat(result.code()).isEqualTo(200);
        Assertions.assertThat(result.body()).isNotNull();
        try {
            Assertions.assertThat(createResponse().getBody().readUtf8()).contains(result.body().string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}