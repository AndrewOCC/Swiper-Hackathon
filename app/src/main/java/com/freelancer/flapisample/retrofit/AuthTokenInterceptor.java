package com.freelancer.flapisample.retrofit;

import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthTokenInterceptor implements Interceptor {

    private final AuthStorage storage;

    public AuthTokenInterceptor(AuthStorage storage) {
        this.storage = storage;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                .header(FLApiConstants.KEY_AUTH_HEADER, storage.getAuthHeader())
                .build();
        Log.w("myApp", storage.getAuthHeader());
        return chain.proceed(request);
    }
}
