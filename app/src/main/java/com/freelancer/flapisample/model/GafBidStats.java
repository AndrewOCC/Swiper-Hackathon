package com.freelancer.flapisample.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by neil on 9/22/15.
 *
 * Model class for a project's bid stats
 */
public class GafBidStats {
    @SerializedName("bid_count")
    int bidCount;

    @SerializedName("bid_avg")
    int bidAverage;

    public int getBidCount() {
        return bidCount;
    }

    public void setBidCount(int bidCount) {
        this.bidCount = bidCount;
    }

    public int getBidAverage() {
        return bidAverage;
    }

    public void setBidAverage(int bidAverage) {
        this.bidAverage = bidAverage;
    }
}
