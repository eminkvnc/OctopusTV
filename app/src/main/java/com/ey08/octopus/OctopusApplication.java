package com.ey08.octopus;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class OctopusApplication extends Application {

    private static OctopusApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseApp.initializeApp(this);
    }

    public static OctopusApplication getInstance() {
        return instance;
    }
}
