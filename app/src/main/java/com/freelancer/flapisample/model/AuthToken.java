package com.freelancer.flapisample.model;

/**
 * Created by neil on 9/21/15.
 *
 * The auth token of an auth request response
 */
public class AuthToken {
    String token;
    String user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
