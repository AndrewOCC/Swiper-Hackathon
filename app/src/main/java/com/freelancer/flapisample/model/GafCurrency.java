package com.freelancer.flapisample.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by neil on 9/22/15.
 *
 * Model class for a project's currency
 */
public class GafCurrency {

    private long id;

    private String code;

    private String name;

    private String country;

    private String sign;

    @SerializedName("exchange_rate")
    private float exchangeRate;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public float getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(float exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
