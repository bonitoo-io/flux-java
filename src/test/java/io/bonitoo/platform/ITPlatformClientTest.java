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

import java.util.logging.Level;
import java.util.logging.Logger;

import io.bonitoo.platform.dto.Task;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 15:54)
 */
class ITPlatformClientTest {

    private static final Logger LOG = Logger.getLogger(ITPlatformClientTest.class.getName());

    private PlatformClient platformClient;

    @BeforeEach
    void setUp() {

        String platformIP = System.getenv().getOrDefault("PLATFORM_IP", "127.0.0.1");
        String platformPort = System.getenv().getOrDefault("PLATFORM_IP_API", "9999");

        String platformURL = "http://" + platformIP + ":" + platformPort;
        LOG.log(Level.FINEST, "Platform URL: {0}", platformURL);

        platformClient = PlatformClientFactory.connect(platformURL);
    }

    @Test
    void createTask() {

        String taskName = "it task" + System.currentTimeMillis();
        
        Task task = platformClient.createTaskEvery(taskName, "from(bucket:\"telegraf\") |> sum()", "1h", "01", "01");

        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getId()).isNotBlank();
        Assertions.assertThat(task.getName()).isEqualTo(taskName);
        Assertions.assertThat(task.getOwner()).isNotNull();
        Assertions.assertThat(task.getOwner().getId()).isEqualTo("01");
        Assertions.assertThat(task.getOwner().getName()).isEqualTo("");
        Assertions.assertThat(task.getOrganizationId()).isEqualTo("01");
        Assertions.assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ENABLED);
        Assertions.assertThat(task.getEvery()).isEqualTo("1h0m0s");
        Assertions.assertThat(task.getCron()).isNull();
        Assertions.assertThat(task.getFlux()).isEqualToIgnoringWhitespace("option task = {name: \"" + taskName + "\", every: 1h} from(bucket:\"telegraf\") |> sum()");
    }
}
