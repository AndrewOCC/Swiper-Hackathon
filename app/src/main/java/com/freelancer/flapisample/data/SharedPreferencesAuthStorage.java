package com.freelancer.flapisample.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.freelancer.flapisample.retrofit.AuthStorage;

/**
 * Created by neil on 9/21/15.
 *
 * A simple {@link SharedPreferences}-backed storage for caching auth details.
 */
public class SharedPreferencesAuthStorage implements AuthStorage{
    private static final String PREF_NAME = "auth_details";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_ID = "user_id";

    private static SharedPreferencesAuthStorage instance;
    public static SharedPreferencesAuthStorage instance(Application context) {
        if (instance == null) {
            instance = new SharedPreferencesAuthStorage(context);
        }
        return instance;
    }

    private Context context;

    private SharedPreferencesAuthStorage(Context context) {
        this.context = context;
    }

    public String getAuthToken() {
        return getString(KEY_AUTH_TOKEN);
    }

    public void saveAuthToken(String authToken) {
        saveString(KEY_AUTH_TOKEN, authToken);
    }

    public void saveUsername(String username) {
        saveString(KEY_USERNAME, username);
    }

    public String getUsername() {
        return getString(KEY_USERNAME);
    }

    public void savePassword(String password) {
        saveString(KEY_PASSWORD, password);
    }

    public String getPassword() {
        return getString(KEY_PASSWORD);
    }

    @Override
    public void saveUserId(String userId) {
        saveString(KEY_USER_ID, userId);
    }

    @Override
    public String getUserId() {
        return getString(KEY_USER_ID);
    }

    /**
     * The user's id is a valid auth id, so we're going to use that
     *
     * @return the saved user id
     */
    @Override
    public String getAuthId() {
        return getUserId();
    }

    /**
     * The auth token is a valid auth secret value, so we're going to use that
     *
     * @return the saved auth token
     */
    @Override
    public String getAuthSecret() {
        return getAuthToken();
    }

    /**
     * Usually when authenticating with an oauth service, the credentials
     * in the authorization header are in this format: auth_id; auth_secret
     *
     * Valid values for the auth id and auth secret are the user id and the user auth key, respectively
     *
     * @return the auth credentials in the format that the api needs
     */
    @Override
    public String getAuthHeader() {
        return getAuthId() + "; " + getAuthSecret();
    }

    private String getString (String key) {
        return getSharedPreferences().getString(key, null);
    }

    private void saveString (String key, String value) {
        getEditor().putString(key, value).commit();
    }

    private SharedPreferences getSharedPreferences () {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getEditor() {
        return getSharedPreferences().edit();
    }
}
