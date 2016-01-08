package com.freelancer.flapisample.retrofit;

import android.util.Log;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import retrofit.RequestInterceptor;

/**
 * Created by neil on 9/21/15.
 *
 * A request interceptor that inserts the needed auth headers into any
 * {@link com.squareup.okhttp.Request} that needs authentication.
 *
 */
public class AuthTokenInterceptor implements RequestInterceptor {

    private AuthStorage storage;

    public AuthTokenInterceptor(AuthStorage storage) {
        this.storage = storage;
    }

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader(FLApiConstants.KEY_AUTH_HEADER, storage.getAuthHeader());
        Log.w("myApp", storage.getAuthHeader());
    }
}


