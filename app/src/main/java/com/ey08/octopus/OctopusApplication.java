package com.ey08.octopus;

import android.app.Application;

public class OctopusApplication extends Application {

    private static OctopusApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static OctopusApplication getInstance() {
        return instance;
    }
}
