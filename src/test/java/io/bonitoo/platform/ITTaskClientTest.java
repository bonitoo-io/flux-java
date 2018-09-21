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

import java.util.List;
import java.util.logging.Logger;

import io.bonitoo.platform.dto.Task;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 15:54)
 */
@RunWith(JUnitPlatform.class)
class ITTaskClientTest extends AbstractITClientTest {

    private static final Logger LOG = Logger.getLogger(ITTaskClientTest.class.getName());

    private TaskClient taskClient;

    @BeforeEach
    void setUp() {

        super.setUp();

        taskClient = platformService.createTaskClient();
    }

    @Test
    void createTask() {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskEvery(taskName, "from(bucket:\"telegraf\") |> sum()", "1h", "01", "01");

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
        Assertions.assertThat(task.getFlux()).isEqualToIgnoringWhitespace("from(bucket:\"telegraf\") |> sum()");
    }

    @Test
    void findTaskByID() {

        String taskName = generateName("it task");

        Task task = taskClient.createTaskCron(taskName, "from(bucket:\"telegraf\") |> sum()", "0 2 * * *", "01", "01");

        Task taskByID = taskClient.findTaskByID(task.getId());
        LOG.info("TaskByID: " + task);

        Assertions.assertThat(taskByID).isNotNull();
        Assertions.assertThat(taskByID.getId()).isEqualTo(task.getId());
        Assertions.assertThat(taskByID.getName()).isEqualTo(task.getName());
        Assertions.assertThat(taskByID.getOwner()).isNotNull();
        Assertions.assertThat(taskByID.getOwner().getId()).isEqualTo(task.getOwner().getId());
        Assertions.assertThat(taskByID.getOwner().getName()).isEqualTo(task.getOwner().getName());
        Assertions.assertThat(taskByID.getOrganizationId()).isEqualTo(task.getOrganizationId());
        Assertions.assertThat(taskByID.getEvery()).isNull();
        Assertions.assertThat(taskByID.getCron()).isEqualTo(task.getCron());
        Assertions.assertThat(taskByID.getFlux()).isEqualTo(task.getFlux());

        // TODO enable after fix https://github.com/influxdata/platform/issues/799
        // Assertions.assertThat(taskByID.getStatus()).isEqualTo(Task.TaskStatus.ENABLED);
    }

    @Test
    void findTasks() {

        int size = taskClient.findTasks().size();

        taskClient.createTaskCron(generateName("it task"), "from(bucket:\"telegraf\") |> sum()", "0 2 * * *", "01", "01");

        List<Task> tasks = taskClient.findTasks();
        Assertions.assertThat(tasks).hasSize(size + 1);
    }

    @Test
    void findTasksByUserID() {

        String user = generateName("0");
        taskClient.createTaskCron(generateName("it task"), "from(bucket:\"telegraf\") |> sum()", "0 2 * * *", user, "01");

        List<Task> tasks = taskClient.findTasksByUserID(user);
        Assertions.assertThat(tasks).hasSize(1);
    }

    @Test
    void findTasksByOrganizationID() {
        String org = generateName("0");
        taskClient.createTaskCron(generateName("it task"), "from(bucket:\"telegraf\") |> sum()", "0 2 * * *", "01", org);

        List<Task> tasks = taskClient.findTasksByOrganizationID(org);
        Assertions.assertThat(tasks).hasSize(1);
    }

    @Test
    void findTasksAfterSpecifiedID() {

        Task task1 = taskClient.createTaskCron(generateName("it task"), "from(bucket:\"telegraf\") |> sum()", "0 2 * * *", "01", "01");
        Task task2 = taskClient.createTaskCron(generateName("it task"), "from(bucket:\"telegraf\") |> sum()", "0 2 * * *", "01", "01");

        List<Task> tasks = taskClient.findTasks(task1.getId(), null, null);

        Assertions.assertThat(tasks).hasSize(1);
        Assertions.assertThat(tasks.get(0).getId()).isEqualTo(task2.getId());
    }

    @Test
    void deleteTask() {

        Task createdTask = taskClient.createTaskCron(generateName("it task"), "from(bucket:\"telegraf\") |> sum()", "0 2 * * *", "01", "01");
        Assertions.assertThat(createdTask).isNotNull();

        Task foundTask = taskClient.findTaskByID(createdTask.getId());
        Assertions.assertThat(foundTask).isNotNull();

        // delete task
        taskClient.deleteTask(createdTask);

        foundTask = taskClient.findTaskByID(createdTask.getId());
        Assertions.assertThat(foundTask).isNull();
    }

    @Test
    void updateTask() {

        Task cronTask = taskClient.createTaskCron(generateName("it task"), "from(bucket:\"telegraf\") |> sum()", "0 2 * * *", "01", "01");
        cronTask.setEvery("2m");
        cronTask.setCron(null);
        cronTask.setFlux("from(bucket:\"telegraf\") |> last()");
        cronTask.setStatus(Task.TaskStatus.DISABLED);

        Task updatedTask = taskClient.updateTask(cronTask);

        Assertions.assertThat(updatedTask).isNotNull();
        Assertions.assertThat(updatedTask.getId()).isEqualTo(cronTask.getId());
        Assertions.assertThat(updatedTask.getOwner()).isNotNull();
        Assertions.assertThat(updatedTask.getOwner().getName()).isEqualTo(cronTask.getOwner().getName());
        Assertions.assertThat(updatedTask.getEvery()).isEqualTo("2m0s");
        Assertions.assertThat(updatedTask.getCron()).isNull();
        Assertions.assertThat(updatedTask.getFlux()).isEqualTo("from(bucket:\"telegraf\") |> last()");
        Assertions.assertThat(updatedTask.getStatus()).isEqualTo(Task.TaskStatus.DISABLED);

        // TODO enable after fix TODOs in https://github.com/influxdata/platform/blob/master/task/platform_adapter.go#L89
        // Assertions.assertThat(updatedTask.getOwner().getId()).isEqualTo(cronTask.getOwner().getId());
        // Assertions.assertThat(updatedTask.getOrganizationId()).isEqualTo(cronTask.getOrganizationId());
        // Assertions.assertThat(updatedTask.getName()).isEqualTo(cronTask.getName());
    }
}
