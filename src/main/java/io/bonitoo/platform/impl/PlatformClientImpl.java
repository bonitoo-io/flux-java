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
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.AbstractRestClient;
import io.bonitoo.platform.AuthorizationClient;
import io.bonitoo.platform.BucketClient;
import io.bonitoo.platform.OrganizationClient;
import io.bonitoo.platform.PlatformClient;
import io.bonitoo.platform.TaskClient;
import io.bonitoo.platform.UserClient;
import io.bonitoo.platform.dto.Health;
import io.bonitoo.platform.dto.Task;
import io.bonitoo.platform.options.PlatformOptions;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 11:17)
 */
public final class PlatformClientImpl extends AbstractRestClient implements PlatformClient {

    private final PlatformService platformService;

    private final UserClientImpl userClient;
    private final OrganizationClientImpl organizationClient;
    private final BucketClientImpl bucketClient;
    private final TaskClientImpl taskClient;
    private final AuthorizationClientImpl authorizationClient;

    private final HttpLoggingInterceptor loggingInterceptor;

    public PlatformClientImpl(@Nonnull final PlatformOptions options) {
        Objects.requireNonNull(options, "PlatformOptions are required");

        this.loggingInterceptor = new HttpLoggingInterceptor();
        this.loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);

        OkHttpClient okHttpClient = options.getOkHttpClient()
                .addInterceptor(loggingInterceptor)
                .build();


        Moshi moshi = new Moshi.Builder().add(new TaskAdapter()).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(options.getUrl())
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
                .build();

        platformService = retrofit.create(PlatformService.class);

        this.userClient = new UserClientImpl(platformService);
        this.organizationClient = new OrganizationClientImpl(platformService);
        this.bucketClient = new BucketClientImpl(platformService);
        this.taskClient = new TaskClientImpl(platformService);
        this.authorizationClient = new AuthorizationClientImpl(platformService);
    }

    @Nonnull
    @Override
    public UserClient getUserClient() {
        return userClient;
    }

    @Nonnull
    @Override
    public OrganizationClient getOrganizationClient() {
        return organizationClient;
    }

    @Nonnull
    @Override
    public BucketClient getBucketClient() {
        return bucketClient;
    }

    @Nonnull
    @Override
    public TaskClient getTaskClient() {
        return taskClient;
    }

    @Nonnull
    @Override
    public AuthorizationClient getAuthorizationClient() {
        return authorizationClient;
    }

    @Nonnull
    @Override
    public Health health() {

        //TODO check correct impl over - https://github.com/influxdata/platform/blob/master/http/health.go

        Call<Health> health = platformService.health();

        return execute(health);
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

    private final class TaskAdapter {

        /**
         * TODO remove after fix https://github.com/influxdata/platform/issues/799.
         */
        @FromJson
        @Nullable
        public Task.TaskStatus toTaskStatus(final JsonReader jsonReader, final JsonAdapter<Task.TaskStatus> delegate)
                throws IOException {

            String statusValue = jsonReader.nextString();
            if (statusValue.isEmpty()) {
                return null;
            } else {
                return delegate.fromJsonValue(statusValue);
            }
        }

        @FromJson
        @Nullable
        public String toFlux(final JsonReader jsonReader, final JsonAdapter<String> delegate)
                throws IOException {

            if ("$.flux".equals(jsonReader.getPath())) {

                String flux = delegate.fromJson(jsonReader);
                if (flux == null) {
                    return null;
                }

                return flux.substring(flux.indexOf("}") + 1).trim();
            }

            return delegate.fromJson(jsonReader);
        }
    }
}