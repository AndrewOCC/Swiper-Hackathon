package com.freelancer.flapisample.retrofit;

import com.freelancer.flapisample.model.retrofit.RetrofitResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface FLProjectsApi {

    String API_VERSION = "0.1";

    @Headers("User-Agent: android-api-sample")
    @GET("/projects/" + API_VERSION + "/projects/recommended/")
    Call<RetrofitResponse> getRecommendedProjects(@Query("offset") int offset,
                                                  @Query("limit") int limit);

    @Headers("User-Agent: android-api-sample")
    @GET("/projects/" + API_VERSION + "/projects/recommended/")
    Call<RetrofitResponse> getRecommendedProjects(@Query("offset") int offset,
                                                  @Query("limit") int limit,
                                                  @QueryMap Map<String, String> options);
}
