package io.bonitoo.flux;

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
    void queryCallback() {

        fluxServer.enqueue(createResponse());

        fluxClient.flux(Flux.from("flux_database"), result -> {
            assertSuccessResult(result);

            countDownLatch.countDown();
        });

        waitToCallback();
    }

    private void assertSuccessResult(@Nonnull final FluxResult result) {

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getTables()).hasSize(1);
        Assertions.assertThat(result.getTables().get(0).getRecords()).hasSize(4);
    }
}