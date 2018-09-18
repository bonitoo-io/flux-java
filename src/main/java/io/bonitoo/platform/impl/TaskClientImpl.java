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
package io.bonitoo.platform.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.AbstractRestClient;
import io.bonitoo.Preconditions;
import io.bonitoo.flux.Flux;
import io.bonitoo.flux.FluxChain;
import io.bonitoo.flux.options.query.AbstractOption;
import io.bonitoo.flux.options.query.TaskOption;
import io.bonitoo.platform.TaskClient;
import io.bonitoo.platform.dto.Task;
import io.bonitoo.platform.dto.User;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import retrofit2.Call;

/**
 * @author Jakub Bednar (bednar@github) (11/09/2018 07:59)
 */
final class TaskClientImpl extends AbstractRestClient implements TaskClient {

    private final PlatformService platformService;
    private final JsonAdapter<Task> adapter;

    TaskClientImpl(@Nonnull final PlatformService platformService, @Nonnull final Moshi moshi) {
        this.platformService = platformService;
        this.adapter = moshi.adapter(Task.class);
    }

    @Nullable
    @Override
    public Task findTaskByID(@Nonnull final String taskID) {

        Preconditions.checkNonEmptyString(taskID, "taskID");

        Call<Task> task = platformService.findTaskByID(taskID);

        return execute(task);
    }

    @Nonnull
    @Override
    public List<Task> findTasks() {
        return findTasks(null, null, null);
    }

    @Nonnull
    @Override
    public List<Task> findTasksByUserID(@Nullable final String userID) {

        return findTasks(null, userID, null);
    }

    @Nonnull
    @Override
    public List<Task> findTasksByOrganizationID(@Nullable final String organizationID) {
        return findTasks(null, null, organizationID);
    }

    @Nonnull
    @Override
    public List<Task> findTasks(@Nullable final String afterID,
                                @Nullable final String userID,
                                @Nullable final String organizationID) {

        Call<List<Task>> task = platformService.findTasks(afterID, userID, organizationID);

        return execute(task);
    }

    @Nonnull
    @Override
    public Task createTaskCron(@Nonnull final String name,
                               @Nonnull final String flux,
                               @Nonnull final String cron,
                               @Nonnull final String userID,
                               @Nonnull final String organizationID) {

        Preconditions.checkNonEmptyString(name, "name of the task");
        Preconditions.checkNonEmptyString(flux, "Flux script to run");
        Preconditions.checkNonEmptyString(cron, "cron expression");
        Preconditions.checkNonEmptyString(userID, "User ID");
        Preconditions.checkNonEmptyString(organizationID, "Organization ID");

        return createTaskCron(name, new Flux() {
            @Override
            protected void appendActual(@Nonnull final FluxChain fluxChain) {
                fluxChain.append(flux);
            }
        }, cron, userID, organizationID);
    }

    @Nonnull
    @Override
    public Task createTaskCron(@Nonnull final String name,
                               @Nonnull final Flux flux,
                               @Nonnull final String cron,
                               @Nonnull final String userID,
                               @Nonnull final String organizationID) {

        Preconditions.checkNonEmptyString(name, "name of the task");
        Objects.requireNonNull(flux, "Flux script to run");
        Preconditions.checkNonEmptyString(cron, "cron expression");
        Preconditions.checkNonEmptyString(userID, "User ID");
        Preconditions.checkNonEmptyString(organizationID, "Organization ID");

        return createTask(name, flux, null, cron, userID, organizationID);
    }

    @Nonnull
    @Override
    public Task createTaskEvery(@Nonnull final String name,
                                @Nonnull final String flux,
                                @Nonnull final String every,
                                @Nonnull final String userID,
                                @Nonnull final String organizationID) {

        Preconditions.checkNonEmptyString(name, "name of the task");
        Preconditions.checkNonEmptyString(flux, "Flux script to run");
        Preconditions.checkNonEmptyString(every, "every expression");
        Preconditions.checkNonEmptyString(userID, "User ID");
        Preconditions.checkNonEmptyString(organizationID, "Organization ID");

        return createTaskEvery(name, new Flux() {
            @Override
            protected void appendActual(@Nonnull final FluxChain fluxChain) {
                fluxChain.append(flux);
            }
        }, every, userID, organizationID);
    }

    @Nonnull
    @Override
    public Task createTaskEvery(@Nonnull final String name,
                                @Nonnull final Flux flux,
                                @Nonnull final String every,
                                @Nonnull final String userID,
                                @Nonnull final String organizationID) {

        Preconditions.checkNonEmptyString(name, "name of the task");
        Objects.requireNonNull(flux, "Flux script to run");
        Preconditions.checkNonEmptyString(every, "every expression");
        Preconditions.checkNonEmptyString(userID, "User ID");
        Preconditions.checkNonEmptyString(organizationID, "Organization ID");

        return createTask(name, flux, every, null, userID, organizationID);
    }

    @Nonnull
    @Override
    public Task updateTask(@Nonnull final Task task) {

        Objects.requireNonNull(task, "Task is required");

        Flux flux = new Flux() {
            @Override
            protected void appendActual(@Nonnull final FluxChain fluxChain) {
                fluxChain.append(task.getFlux());
            }
        };

        task.setFlux(getFluxQuery(flux, task));

        Call<Task> taskCall = platformService.updateTask(task.getId(), createBody(adapter.toJson(task)));

        return execute(taskCall);
    }

    @Override
    public void deleteTask(@Nonnull final Task task) {

        Objects.requireNonNull(task, "Task is required");

        deleteTask(task.getId());
    }

    @Override
    public void deleteTask(@Nonnull final String taskID) {

        Preconditions.checkNonEmptyString(taskID, "taskID");

        Call<Void> call = platformService.deleteTask(taskID);
        execute(call);
    }

    @Nonnull
    private Task createTask(@Nonnull final String name,
                            @Nonnull final Flux flux,
                            @Nullable final String every,
                            @Nullable final String cron,
                            @Nonnull final String userID,
                            @Nonnull final String organizationID) {

        Preconditions.checkNonEmptyString(name, "name of the task");
        Objects.requireNonNull(flux, "Flux script to run");
        Preconditions.checkNonEmptyString(userID, "User ID");
        Preconditions.checkNonEmptyString(organizationID, "Organization ID");

        User owner = new User();
        owner.setId(userID);

        Task task = new Task();
        task.setName(name);
        task.setOrganizationId(organizationID);
        task.setOwner(owner);
        task.setStatus(Task.TaskStatus.ENABLED);
        task.setEvery(every);
        task.setCron(cron);
        task.setFlux(getFluxQuery(flux, task));

        Call<Task> call = platformService.createTask(createBody(adapter.toJson(task)));

        return execute(call);
    }

    @Nonnull
    private String getFluxQuery(@Nonnull final Flux flux, final Task task) {
        TaskOption.Builder taskOptionBuilder = TaskOption.builder(task.getName());
        if (task.getCron() != null) {
            taskOptionBuilder.cron(task.getCron());
        } else if (task.getEvery() != null) {
            taskOptionBuilder.every(task.getEvery());
        }

        List<AbstractOption> options = new ArrayList<>();
        options.add(taskOptionBuilder.build());

        return flux.print(new FluxChain().addOptions(options));
    }
}