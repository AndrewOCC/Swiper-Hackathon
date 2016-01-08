package com.freelancer.flapisample.retrofit;

/**
 * Created by neil on 9/21/15.
 *
 * An interface that describes what details are needed to be saved
 * and retrieved for the authentication workflow.
 */
public interface AuthStorage {

    public void saveAuthToken(String authToken);

    public String getAuthToken();

    public void saveUsername(String username);

    public String getUsername();

    public void savePassword(String password);

    public String getPassword();

    public void saveUserId(String userId);

    public String getUserId();

    public String getAuthId();

    public String getAuthSecret();

    public String getAuthHeader();
}
