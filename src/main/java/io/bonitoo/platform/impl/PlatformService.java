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

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.bonitoo.platform.dto.Authorization;
import io.bonitoo.platform.dto.Authorizations;
import io.bonitoo.platform.dto.Bucket;
import io.bonitoo.platform.dto.Buckets;
import io.bonitoo.platform.dto.Health;
import io.bonitoo.platform.dto.Organization;
import io.bonitoo.platform.dto.Organizations;
import io.bonitoo.platform.dto.Source;
import io.bonitoo.platform.dto.Sources;
import io.bonitoo.platform.dto.Task;
import io.bonitoo.platform.dto.User;
import io.bonitoo.platform.dto.Users;

import io.reactivex.Completable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @author Jakub Bednar (bednar@github) (05/09/2018 13:30)
 */
interface PlatformService {

    //
    // Health
    //
    @GET("/healthz")
    @Headers("Content-Type: application/json")
    Call<Health> health();

    //
    // User
    //
    @POST("/v1/users")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<User> createUser(@Nonnull @Body final RequestBody user);

    @DELETE("/v1/users/{id}")
    Call<Void> deleteUser(@Nonnull @Path("id") final String userID);

    @PATCH("/v1/users/{id}")
    Call<User> updateUser(@Nonnull @Path("id") final String userID, @Nonnull @Body final RequestBody user);

    @GET("/v1/users/{id}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<User> findUserByID(@Nonnull @Path("id") final String userID);

    @GET("/v1/users/")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Users> findUsers();


    //
    // Organizations
    //
    @POST("/v1/orgs")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Organization> createOrganization(@Nonnull @Body final RequestBody organization);

    @DELETE("/v1/orgs/{id}")
    Call<Void> deleteOrganization(@Nonnull @Path("id") final String organizationID);

    @PATCH("/v1/orgs/{id}")
    Call<Organization> updateOrganization(@Nonnull @Path("id") final String organizationID,
                                          @Nonnull @Body final RequestBody organization);

    @GET("/v1/orgs/{id}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Organization> findOrganizationByID(@Nonnull @Path("id") final String organizationID);

    @GET("/v1/orgs")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Organizations> findOrganizations();

    //
    // Bucket
    //
    @POST("/v1/buckets")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Bucket> createBucket(@Nonnull @Body final RequestBody bucket);

    @DELETE("/v1/buckets/{id}")
    Call<Void> deleteBucket(@Nonnull @Path("id") final String bucketID);

    @PATCH("/v1/buckets/{id}")
    Call<Bucket> updateBucket(@Nonnull @Path("id") final String bucketID, @Nonnull @Body final RequestBody bucket);

    @GET("/v1/buckets/{id}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Bucket> findBucketByID(@Nonnull @Path("id") final String bucketID);

    @GET("/v1/buckets")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Buckets> findBuckets(@Nullable @Query("org") final String organizationName);

    //
    // Task
    //
    @POST("/v1/tasks")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Task> createTask(@Nonnull @Body final RequestBody task);

    @DELETE("/v1/tasks/{id}")
    Call<Void> deleteTask(@Nonnull @Path("id") final String taskID);

    @PATCH("/v1/tasks/{id}")
    Call<Task> updateTask(@Nonnull @Path("id") final String taskID, @Nonnull @Body final RequestBody task);

    @GET("/v1/tasks/")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<List<Task>> findTasks(@Nullable @Query("after") final String after,
                               @Nullable @Query("user") final String user,
                               @Nullable @Query("organization") final String organization);

    @GET("/v1/tasks/{id}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Task> findTaskByID(@Nonnull @Path("id") final String taskID);

    //
    // Authorization
    //
    @POST("/v1/authorizations")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Authorization> createAuthorization(@Nonnull @Body final RequestBody authorization);

    @DELETE("/v1/authorizations/{id}")
    Call<Void> deleteAuthorization(@Nonnull @Path("id") final String authorizationID);

    @PATCH("/v1/authorizations/{id}")
    Call<Authorization> updateAuthorization(@Nonnull @Path("id") final String authorizationID,
                                            @Nonnull @Body final RequestBody authorization);

    @GET("/v1/authorizations/")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Authorizations> findAuthorizations(@Nullable @Query("userID") final String userID,
                                            @Nullable @Query("user") final String user);

    @GET("/v1/authorizations/{id}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Authorization> findAuthorization(@Nonnull @Path("id") final String authorizationID);

    //
    // Source
    //
    @POST("/v2/sources")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Source> createSource(@Nonnull @Body final RequestBody source);

    @DELETE("/v2/sources/{id}")
    Call<Void> deleteSource(@Nonnull @Path("id") final String sourceID);

    @PATCH("/v2/sources/{id}")
    Call<Source> updateSource(@Nonnull @Path("id") final String sourceID,
                              @Nonnull @Body final RequestBody source);

    @GET("/v2/sources/{id}")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Source> findSource(@Nonnull @Path("id") final String sourceID);

    @GET("/v2/sources")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<Sources> findSources();

    @GET("/v2/sources/{id}/buckets")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<List<Bucket>> findSourceBuckets(@Nonnull @Path("id") final String sourceID);

    @GET("/v2/sources/{id}/health")
    @Nonnull
    @Headers("Content-Type: application/json")
    Call<ResponseBody> findSourceHealth(@Nonnull @Path("id") final String sourceID);

    @POST("/v2/write")
    @Nonnull
    Completable writePoints(@Query("org") final String org,
                            @Query("bucket") final String bucket,
                            @Query("precision") final String precision,
                            @Header("Authorization") final String token,
                            @Body final RequestBody points);
}