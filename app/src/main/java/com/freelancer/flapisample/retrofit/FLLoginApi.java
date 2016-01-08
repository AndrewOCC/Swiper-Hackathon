package com.freelancer.flapisample.retrofit;

import com.freelancer.flapisample.model.retrofit.AuthResponse;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Headers;
import retrofit.http.POST;

/**
 * Created by neil on 9/21/15.
 */
public interface FLLoginApi {

    @FormUrlEncoded
    @Headers("User-Agent: android-api-sample")
    @POST("/login/?type=app")
    public AuthResponse login(@Field("user") String username,
                       @Field("password") String password);

    @FormUrlEncoded
    @Headers("User-Agent: android-api-sample")
    @POST("/login/oauth/")
    public AuthResponse loginOauth(@Field("app_id") String appId,
                                 @Field("oauth_type") String oauthType,
                                 @Field("credentials") String token);
}
