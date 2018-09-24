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

import io.bonitoo.platform.dto.Health;
import io.bonitoo.platform.impl.AbstractPlatformClientTest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (18/09/2018 13:49)
 */
@RunWith(JUnitPlatform.class)
class SourceClientTest extends AbstractPlatformClientTest {

    private SourceClient sourceClient;

    @BeforeEach
    protected void setUp() {
        super.setUp();

        sourceClient = platformClient.createSourceClient();
    }

    @Test
    void healthSuccess() {

        platformServer.enqueue(createResponse(""));

        Health health = sourceClient.health("01");

        Assertions.assertThat(health).isNotNull();
        Assertions.assertThat(health.isHealthy()).isTrue();
    }

    @Test
    void healthFailure() {

        platformServer.enqueue(createErrorResponse("unreachable source"));

        Health health = sourceClient.health("01");

        Assertions.assertThat(health).isNotNull();
        Assertions.assertThat(health.isHealthy()).isFalse();
        Assertions.assertThat(health.getMessage()).isEqualTo("unreachable source");
    }
}