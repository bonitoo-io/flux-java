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

import io.bonitoo.core.InfluxException;
import io.bonitoo.platform.dto.Task;
import io.bonitoo.platform.dto.User;
import io.bonitoo.platform.impl.AbstractPlatformClientTest;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 12:31)
 */
@RunWith(JUnitPlatform.class)
class TaskClientTest extends AbstractPlatformClientTest {

    private TaskClient taskClient;

    @BeforeEach
    protected void setUp() {
        super.setUp();

        taskClient = platformClient.createTaskClient();
    }

    @Test
    void mappingTaskNullFlux() {

        String data = "{\n"
                + "    \"organizationId\": \"01\",\n"
                + "    \"owner\": {\n"
                + "        \"name\": \"Frank Radler\",\n"
                + "        \"id\": \"02\"\n"
                + "    },\n"
                + "    \"name\": \"test task\",\n"
                + "    \"flux\": null,\n"
                + "    \"id\": \"0c\",\n"
                + "    \"every\": \"1m0s\",\n"
                + "    \"status\": \"enabled\"\n"
                + "}";

        platformServer.enqueue(createResponse(data));

        Task task = taskClient.findTaskByID("0c");
        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getFlux()).isEqualTo(null);
    }

    @Test
    void createTaskCronRequest() {

        platformServer.enqueue(createResponse("{}"));

        Task task = taskClient.createTaskCron("task name", "from(bucket: \"telegraf\") |> last()", "0 2 * * *", "10", "15");
        Assertions.assertThat(task).isNotNull();

        JSONObject body = getRequestBodyAsJSON(platformServer);

        Assertions.assertThat(body.get("name")).isEqualTo("task name");
        Assertions.assertThat(body.get("organizationId")).isEqualTo("15");
        Assertions.assertThat(body.get("owner").toString()).isEqualTo("{\"id\":\"10\"}");
        Assertions.assertThat(body.get("status")).isEqualTo("enabled");
        Assertions.assertThat(body.get("flux").toString()).isEqualToIgnoringWhitespace("option task = {name: \"task name\", cron: \"0 2 * * *\"} from(bucket: \"telegraf\") |> last()");
    }

    @Test
    void createTaskEveryRequest() {

        platformServer.enqueue(createResponse("{}"));

        taskClient.createTaskEvery("task name", "from(bucket: \"telegraf\") |> last()", "10m", "10", "15");

        JSONObject body = getRequestBodyAsJSON(platformServer);

        Assertions.assertThat(body.get("name")).isEqualTo("task name");
        Assertions.assertThat(body.get("organizationId")).isEqualTo("15");
        Assertions.assertThat(body.get("owner").toString()).isEqualTo("{\"id\":\"10\"}");
        Assertions.assertThat(body.get("status")).isEqualTo("enabled");
        Assertions.assertThat(body.get("flux").toString()).isEqualToIgnoringWhitespace("option task = {name: \"task name\", every: 10m} from(bucket: \"telegraf\") |> last()");
    }

    @Test
    void createTaskResponse() {

        String data = "{\n"
                + "    \"organizationId\": \"01\",\n"
                + "    \"owner\": {\n"
                + "        \"name\": \"Frank Radler\",\n"
                + "        \"id\": \"02\"\n"
                + "    },\n"
                + "    \"name\": \"test task\",\n"
                + "    \"flux\": \"option task = {name: \\\"test task\\\",every: 1m} from(bucket:\\\"test\\\") |> range(start:-1h)\",\n"
                + "    \"id\": \"0c\",\n"
                + "    \"every\": \"1m0s\",\n"
                + "    \"status\": \"enabled\"\n"
                + "}";

        platformServer.enqueue(createResponse(data));

        Task task = taskClient.createTaskEvery("test task", "from(bucket: \"test\") |> range(start:-1h)", "1m", "02", "01");

        assertTask(task);
    }

    @Test
    void findTaskByIDExist() {

        String data = "{\n"
                + "    \"organizationId\": \"01\",\n"
                + "    \"owner\": {\n"
                + "        \"name\": \"Frank Radler\",\n"
                + "        \"id\": \"02\"\n"
                + "    },\n"
                + "    \"name\": \"test task\",\n"
                + "    \"flux\": \"option task = {name: \\\"test task\\\",every: 1m} from(bucket:\\\"test\\\") |> range(start:-1h)\",\n"
                + "    \"id\": \"0c\",\n"
                + "    \"every\": \"1m0s\",\n"
                + "    \"status\": \"enabled\"\n"
                + "}";

        platformServer.enqueue(createResponse(data));

        Task task = taskClient.findTaskByID("0c");
        assertTask(task);
    }

    @Test
    void findTaskByIDNotExist() {

        String data = "null\n";

        platformServer.enqueue(createResponse(data));

        Task task = taskClient.findTaskByID("00");
        Assertions.assertThat(task).isNull();
    }

    @Test
    void deleteTask() throws InterruptedException {

        platformServer.enqueue(new MockResponse().setResponseCode(202));

        Task task = new Task();
        task.setId("00");

        taskClient.deleteTask(task);

        RecordedRequest request = platformServer.takeRequest();
        Assertions.assertThat(request.getMethod()).isEqualTo("DELETE");
        Assertions.assertThat(request.getPath()).endsWith("/00");
    }

    @Test
    void deleteTaskNotExist() {

        platformServer.enqueue(createErrorResponse("task not claimed"));

        Assertions.assertThatThrownBy(() -> taskClient.deleteTask("00"))
                .isInstanceOf(InfluxException.class)
                .hasMessage("task not claimed");
    }

    @Test
    void updateTask() {

        String data = "{\n"
                + "    \"organizationId\": \"01\",\n"
                + "    \"owner\": {\n"
                + "        \"name\": \"Frank Radler\",\n"
                + "        \"id\": \"02\"\n"
                + "    },\n"
                + "    \"name\": \"test task\",\n"
                + "    \"flux\": \"option task = {name: \\\"test task\\\",every: 1m} from(bucket:\\\"test\\\") |> range(start:-1h)\",\n"
                + "    \"id\": \"0c\",\n"
                + "    \"every\": \"1m0s\",\n"
                + "    \"status\": \"disabled\"\n"
                + "}";

        User owner = new User();
        owner.setId("02");
        owner.setName("Frank Radler");

        Task task = new Task();
        task.setId("0c");
        task.setStatus(Task.TaskStatus.DISABLED);
        task.setEvery("1m");
        task.setName("test task");
        task.setOrganizationId("01");
        task.setOwner(owner);
        task.setFlux("from(bucket:\\\"test\\\") |> range(start:-1h)");

        platformServer.enqueue(createResponse(data));

        taskClient.updateTask(task);

        JSONObject requestBody = getRequestBodyAsJSON(platformServer);
        Assertions.assertThat(requestBody.getString("flux"))
                .isEqualToIgnoringWhitespace("option task = {name: \"test task\", every: 1m} from(bucket:\\\"test\\\") |> range(start:-1h)");
        Assertions.assertThat(requestBody.getString("status")).isEqualTo("disabled");
    }

    @Test
    void errorResponse() {

        platformServer.enqueue(createErrorResponse("task name already in use by current user or target organization"));

        Assertions.assertThatThrownBy(() -> taskClient.createTaskEvery("test task", "from(bucket: \"test\") |> range(start:-1h)", "1m", "02", "01"))
                .isInstanceOf(InfluxException.class)
                .hasMessage("task name already in use by current user or target organization");
    }

    private void assertTask(Task task) {
        Assertions.assertThat(task).isNotNull();
        Assertions.assertThat(task.getId()).isEqualTo("0c");
        Assertions.assertThat(task.getName()).isEqualTo("test task");
        Assertions.assertThat(task.getOwner()).isNotNull();
        Assertions.assertThat(task.getOwner().getId()).isEqualTo("02");
        Assertions.assertThat(task.getOwner().getName()).isEqualTo("Frank Radler");
        Assertions.assertThat(task.getOrganizationId()).isEqualTo("01");
        Assertions.assertThat(task.getStatus()).isEqualTo(Task.TaskStatus.ENABLED);
        Assertions.assertThat(task.getFlux()).isEqualTo("from(bucket:\"test\") |> range(start:-1h)");
        Assertions.assertThat(task.getEvery()).isEqualTo("1m0s");
        Assertions.assertThat(task.getCron()).isNull();
    }
}