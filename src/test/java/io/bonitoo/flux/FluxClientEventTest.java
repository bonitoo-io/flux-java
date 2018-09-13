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

import java.time.Instant;
import java.util.function.Consumer;

import io.bonitoo.InfluxException;
import io.bonitoo.flux.events.FluxErrorEvent;
import io.bonitoo.flux.events.FluxSuccessEvent;
import io.bonitoo.flux.events.UnhandledErrorEvent;
import io.bonitoo.flux.impl.FluxResultMapperException;
import io.bonitoo.flux.options.FluxOptions;
import io.bonitoo.flux.options.query.NowOption;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 07:05)
 */
@RunWith(JUnitPlatform.class)
class FluxClientEventTest extends AbstractFluxClientTest {

    @Test
    void fluxSuccessEvent() {

        fluxServer.enqueue(createResponse());

        fluxClient.subscribeEvents(FluxSuccessEvent.class, event -> {

            String expected = "option now = () => 1970-01-01T00:02:00.000000000Z from(bucket:\"flux_database\")";

            Assertions.assertThat(event).isNotNull();
            Assertions.assertThat(event.getFluxQuery()).isEqualToIgnoringWhitespace(expected);
            Assertions.assertThat(event.getOptions().getOrgID()).isEqualTo("0");
            Assertions.assertThat(event.getOptions().getUrl()).isEqualTo(fluxServer.url("/").toString());

            countDownLatch.countDown();
        });

        FluxOptions fluxOptions = FluxOptions.builder()
                .addOption(NowOption.builder().time(Instant.ofEpochSecond(120)).build())
                .build();

        fluxClient.flux(Flux.from("flux_database"), fluxOptions);

        waitToCallback();
    }

    @Test
    void fluxErrorEvent() {

        fluxServer.enqueue(createErrorResponse("rpc error: code = Unavailable desc = all SubConns are in Transie"));

        fluxClient.subscribeEvents(FluxErrorEvent.class, event -> {

            String expected = "option now = () => 1970-01-01T00:02:00.000000000Z from(bucket:\"flux_database\")";

            Assertions.assertThat(event).isNotNull();
            Assertions.assertThat(event.getFluxQuery()).isEqualToIgnoringWhitespace(expected);
            Assertions.assertThat(event.getException()).hasMessage("rpc error: code = Unavailable desc = all SubConns are in Transie");

            countDownLatch.countDown();
        });

        FluxOptions fluxOptions = FluxOptions.builder()
                .addOption(NowOption.builder().time(Instant.ofEpochSecond(120)).build())
                .build();

        Assertions.assertThatThrownBy(() -> fluxClient.flux(Flux.from("flux_database"), fluxOptions))
                .isInstanceOf(InfluxException.class)
                .hasMessage("rpc error: code = Unavailable desc = all SubConns are in Transie");

        waitToCallback();
    }

    @Test
    void fluxErrorEventAsync() {

        fluxServer.enqueue(createErrorResponse("rpc error: code = Unavailable desc = all SubConns are in Transie"));

        fluxClient.subscribeEvents(FluxErrorEvent.class, event -> {

            String expected = "option now = () => 1970-01-01T00:02:00.000000000Z from(bucket:\"flux_database\")";

            Assertions.assertThat(event).isNotNull();
            Assertions.assertThat(event.getFluxQuery()).isEqualToIgnoringWhitespace(expected);
            Assertions.assertThat(event.getException()).hasMessage("rpc error: code = Unavailable desc = all SubConns are in Transie");

            countDownLatch.countDown();
        });

        FluxOptions fluxOptions = FluxOptions.builder()
                .addOption(NowOption.builder().time(Instant.ofEpochSecond(120)).build())
                .build();

        fluxClient.flux(Flux.from("flux_database"), fluxOptions, (cancellable, fluxRecord) -> {
        });

        waitToCallback();
    }

    @Test
    void fluxUnhandledErrorEvent() {

        fluxServer.enqueue(createResponse("un-parsable"));

        fluxClient.subscribeEvents(UnhandledErrorEvent.class, event -> {

            Assertions.assertThat(event).isNotNull();
            Assertions.assertThat(event.getThrowable())
                    .isInstanceOf(InfluxException.class)
                    .hasCauseInstanceOf(FluxResultMapperException.class)
                    .hasMessageContaining("Unable to parse CSV response. FluxTable definition was not found. Record: 1");

            countDownLatch.countDown();
        });

        FluxOptions fluxOptions = FluxOptions.builder()
                .addOption(NowOption.builder().time(Instant.ofEpochSecond(120)).build())
                .build();

        Assertions.assertThatThrownBy(() -> fluxClient.flux(Flux.from("flux_database"), fluxOptions))
                .isInstanceOf(InfluxException.class)
                .hasMessage("io.bonitoo.flux.impl.FluxResultMapperException: Unable to parse CSV response. FluxTable definition was not found. Record: 1");

        waitToCallback();
    }

    @Test
    void unsubscribeEvents() {

        fluxServer.enqueue(createResponse());

        Consumer<FluxSuccessEvent> listener = event -> {

            Assertions.assertThat(event).isNotNull();
            Assertions.assertThat(event.getFluxQuery()).isEqualToIgnoringWhitespace("");

            countDownLatch.countDown();
        };

        // Subscribe
        fluxClient.subscribeEvents(FluxSuccessEvent.class, listener);

        FluxOptions fluxOptions = FluxOptions.builder()
                .addOption(NowOption.builder().time(Instant.ofEpochSecond(120)).build())
                .build();


        // Unsubscribe
        fluxClient.unsubscribeEvents(listener);
        fluxClient.flux(Flux.from("flux_database"), fluxOptions);

        Assertions.assertThat(countDownLatch.getCount()).isEqualTo(1L);
    }
}