package com.freelancer.flapisample.retrofit;

import com.freelancer.flapisample.model.retrofit.AuthResponse;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import retrofit.RestAdapter;


/**
 * Created by neil on 9/21/15.
 *
 * An interceptor that attempts to refresh an expired auth token when the response returns a 401
 * and restarts the failed request with a new auth token.
 */
public class AuthInterceptor implements Interceptor {

    private AuthStorage storage;

    public AuthInterceptor(AuthStorage storage) {
        this.storage = storage;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        // try the request
        Response response = chain.proceed(request);

        return response;
    }

}
