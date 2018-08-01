package io.bonitoo.flux.impl;

import javax.annotation.Nonnull;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 12:33)
 * @since 3.0.0
 */
public interface FluxService {

    @Streaming
    @POST("/v1/query")
    @Nonnull
    Call<ResponseBody> query(@Query(value = "q", encoded = true) String query,
                             @Query(value = "orgID", encoded = true) String orgID);

    @GET("/ping")
    Call<ResponseBody> ping();
}
