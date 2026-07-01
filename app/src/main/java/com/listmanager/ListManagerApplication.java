package com.listmanager;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class ListManagerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
