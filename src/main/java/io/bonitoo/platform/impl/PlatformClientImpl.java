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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.AbstractRestClient;
import io.bonitoo.InfluxException;
import io.bonitoo.Preconditions;
import io.bonitoo.flux.Flux;
import io.bonitoo.flux.FluxChain;
import io.bonitoo.flux.options.query.AbstractOption;
import io.bonitoo.flux.options.query.TaskOption;
import io.bonitoo.platform.PlatformClient;
import io.bonitoo.platform.dto.Task;
import io.bonitoo.platform.options.PlatformOptions;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 11:17)
 */
public final class PlatformClientImpl extends AbstractRestClient implements PlatformClient {

    private final PlatformService platformService;

    private final HttpLoggingInterceptor loggingInterceptor;

    public PlatformClientImpl(@Nonnull final PlatformOptions options) {
        Objects.requireNonNull(options, "PlatformOptions are required");

        this.loggingInterceptor = new HttpLoggingInterceptor();
        this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);

        OkHttpClient okHttpClient = options.getOkHttpClient()
                .addInterceptor(loggingInterceptor)
                .build();


        Moshi moshi = new Moshi.Builder().add(new TaskStatusAdapter()).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(options.getUrl())
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
                .build();

        this.platformService = retrofit.create(PlatformService.class);
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
        return null;
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


        TaskOption.Builder taskOptionBuilder = TaskOption.builder(name);
        if (cron != null) {
            taskOptionBuilder.cron(cron);
        } else if (every != null) {
            taskOptionBuilder.every(every);
        }

        List<AbstractOption> options = new ArrayList<>();
        options.add(taskOptionBuilder.build());

        FluxChain fluxChain = new FluxChain().addOptions(options);

        JSONObject user = new JSONObject()
                .put("id", userID);

        JSONObject body = new JSONObject()
                .put("name", name)
                .put("organizationId", organizationID)
                .put("owner", user)
                .put("status", Task.TaskStatus.ENABLED.name().toLowerCase())
                .put("flux", flux.print(fluxChain));

        Call<Task> task = platformService.createTask(createBody(body.toString()));

        return execute(task);
    }

    @Nonnull
    @Override
    public HttpLoggingInterceptor.Level getLogLevel() {
        return loggingInterceptor.getLevel();
    }

    @Nonnull
    @Override
    public PlatformClient setLogLevel(@Nonnull final HttpLoggingInterceptor.Level logLevel) {

        Objects.requireNonNull(logLevel, "Level is required");

        this.loggingInterceptor.setLevel(logLevel);

        return this;
    }

    private <T> T execute(@Nonnull final Call<T> call) throws InfluxException {

        Objects.requireNonNull(call, "call is required");

        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                String error = InfluxException.getErrorMessage(response);

                throw new InfluxException(error);
            }
        } catch (IOException e) {
            throw InfluxException.fromCause(e);
        }
    }

    /**
     * TODO remove after fix https://github.com/influxdata/platform/issues/799.
     */
    private final class TaskStatusAdapter {

        @FromJson
        @Nullable
        public Task.TaskStatus fromJson(final JsonReader jsonReader, final JsonAdapter<Task.TaskStatus> delegate)
                throws IOException {

            String statusValue = jsonReader.nextString();
            if (statusValue.isEmpty()) {
                return null;
            } else {
                return delegate.fromJsonValue(statusValue);
            }
        }
    }
}