package com.freelancer.flapisample.retrofit;

/**
 * Created by neil on 9/21/15.
 *
 * A base interface that holds shared constants used by the api.
 *
 */
public interface FLApiConstants {

    /**
     * Key used for the authentication header
     */
    static final String KEY_AUTH_HEADER = "Freelancer-Developer-Auth-V1";

    /**
     * Base url for the freelancer api
     */
    static final String BASE_URL = "https://www.freelancer.com/api";
}
