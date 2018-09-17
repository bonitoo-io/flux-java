package io.bonitoo.platform;

import io.bonitoo.platform.dto.Health;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (17/09/2018 08:39)
 */
@RunWith(JUnitPlatform.class)
class ITPlatformClientTest extends AbstractITClientTest {

    @Test
    void health() {

        Health health = platformService.health();

        Assertions.assertThat(health.isHealthy()).isTrue();
        Assertions.assertThat(health.getMessage()).isEqualTo("howdy y'all");
    }
}