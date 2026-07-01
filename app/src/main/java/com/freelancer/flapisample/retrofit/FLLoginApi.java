package com.freelancer.flapisample.retrofit;

import com.freelancer.flapisample.model.retrofit.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface FLLoginApi {

    @FormUrlEncoded
    @Headers("User-Agent: android-api-sample")
    @POST("/login/?type=app")
    Call<AuthResponse> login(@Field("user") String username,
                             @Field("password") String password);

    @FormUrlEncoded
    @Headers("User-Agent: android-api-sample")
    @POST("/login/oauth/")
    Call<AuthResponse> loginOauth(@Field("app_id") String appId,
                                  @Field("oauth_type") String oauthType,
                                  @Field("credentials") String token);
}
