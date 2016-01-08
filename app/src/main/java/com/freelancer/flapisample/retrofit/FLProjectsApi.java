package com.freelancer.flapisample.retrofit;

import android.util.Log;

import com.freelancer.flapisample.model.retrofit.RetrofitResponse;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;
import retrofit.http.QueryMap;

/**
 * Created by neil on 9/21/15.
 *
 * An interface for talking with the projects API
 */
public interface FLProjectsApi{

    public static final String API_VERSION = "0.1";

    /**
     * Basic endpoint for getting recommended projects.
     *
     * For more advanced usage of the endpoint, use {@link #getRecommendedProjects(int, int, Map, Callback)}
     *
     * @param offset offset for paging of requests. Use 0 to get the most recent recommended projects
     * @param limit the maximum number of return objects to be returned
     * @param cb a callback where the results will be delivered
     */
    @Headers("User-Agent: android-api-sample")
    @GET("/projects/" + API_VERSION + "/projects/recommended/")
    void getRecommendedProjects(@Query("offset") int offset, @Query("limit") int limit, Callback<RetrofitResponse> cb);

    /**
     * A more advanced endpoint for getting recommended projects with support for additional http
     * parameters.
     *
     * @param offset offset for paging of requests. Use 0 to get the most recent recommended projects
     * @param limit the maximum number of return objects to be returned
     * @param options a map of other parameters to be included in the http request
     * @param cb a callback where the results will be delivered
     */
    @Headers("User-Agent: android-api-sample")
    @GET("/projects/" + API_VERSION + "/projects/recommended/")
    void getRecommendedProjects(@Query("offset") int offset, @Query("limit") int limit, @QueryMap Map<String, String> options, Callback<RetrofitResponse> cb);
}
