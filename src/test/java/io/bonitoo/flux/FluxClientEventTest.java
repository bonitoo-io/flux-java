package io.bonitoo.flux;

import java.time.Instant;
import java.util.function.Consumer;

import io.bonitoo.flux.events.FluxErrorEvent;
import io.bonitoo.flux.events.FluxSuccessEvent;
import io.bonitoo.flux.events.UnhandledErrorEvent;
import io.bonitoo.flux.mapper.FluxResultMapperException;
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

            String expected = "option now = () => 1970-01-01T00:02:00.000000000Z from(db:\"flux_database\")";

            Assertions.assertThat(event).isNotNull();
            Assertions.assertThat(event.getFluxQuery()).isEqualToIgnoringWhitespace(expected);

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

            String expected = "option now = () => 1970-01-01T00:02:00.000000000Z from(db:\"flux_database\")";

            Assertions.assertThat(event).isNotNull();
            Assertions.assertThat(event.getFluxQuery()).isEqualToIgnoringWhitespace(expected);
            Assertions.assertThat(event.getException()).hasMessage("rpc error: code = Unavailable desc = all SubConns are in Transie");

            countDownLatch.countDown();
        });

        FluxOptions fluxOptions = FluxOptions.builder()
                .addOption(NowOption.builder().time(Instant.ofEpochSecond(120)).build())
                .build();

        fluxClient.flux(Flux.from("flux_database"), fluxOptions);

        waitToCallback();
    }

    @Test
    void fluxUnhandledErrorEvent() {

        fluxServer.enqueue(createResponse("un-parsable"));

        fluxClient.subscribeEvents(UnhandledErrorEvent.class, event -> {

            Assertions.assertThat(event).isNotNull();
            Assertions.assertThat(event.getThrowable())
                    .isInstanceOf(FluxException.class)
                    .hasCauseInstanceOf(FluxResultMapperException.class)
                    .hasMessageContaining("Unable to parse CSV response. Table definition was not found. Row:0");

            countDownLatch.countDown();
        });

        FluxOptions fluxOptions = FluxOptions.builder()
                .addOption(NowOption.builder().time(Instant.ofEpochSecond(120)).build())
                .build();

        fluxClient.flux(Flux.from("flux_database"), fluxOptions);

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