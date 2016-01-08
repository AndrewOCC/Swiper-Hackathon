package com.freelancer.flapisample.model.retrofit;

import com.freelancer.flapisample.model.AuthToken;

/**
 * Created by neil on 9/21/15.
 *
 * The response of an auth request that contains the status of the request as well as the
 * auth token and user id.
 */
public class AuthResponse {
    String status;
    AuthToken result;

    public String getAuthTokenAsString() {
        return result != null ? result.getToken() : null;
    }

    public String getUserIdAsString() {
        return result != null ? result.getUser() : null;
    }

    @Override
    public String toString() {
        return "Status: " + status + " Auth Token: " + getAuthTokenAsString();
    }
}
