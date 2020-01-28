package com.tvoctopus.player.model;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;

public class DataRepository {

    public static final String SHARED_PREF_OCTOPUS_DATA = "OctopusData";
    public static final String SHARED_PREF_PLAYLIST = "Playlist";

    private Application application;

    public DataRepository(Application application) {
        this.application = application;
    }

    //TODO: implement all SharedPreferences operations.

    public MutableLiveData<Boolean> getScreenRegistered(){

        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_OCTOPUS_DATA, Context.MODE_PRIVATE);
        return new SharedPreferenceLiveData<Boolean>(sp, "ScreenRegistered", false) {
            @Override
            Boolean getValueFromPreferences(String key, Boolean defValue) {
                return sp.getBoolean(key, defValue);
            }

        };
    }

    public MutableLiveData<Boolean> getNetworkConnected(){
        return new NetworkConnectionLiveData(application.getApplicationContext());
    }






}
