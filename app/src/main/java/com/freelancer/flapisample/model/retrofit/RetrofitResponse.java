package com.freelancer.flapisample.model.retrofit;

/**
 * Created by neil on 9/22/15.
 *
 * A class that models how a usual retrofit response from the api looks like.
 */
public class RetrofitResponse {
    private String status;
    private GenericResult result;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public GenericResult getResult() {
        return result;
    }

    public void setResult(GenericResult result) {
        this.result = result;
    }
}
