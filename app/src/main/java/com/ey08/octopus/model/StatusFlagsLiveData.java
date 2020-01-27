package com.ey08.octopus.model;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

public class StatusFlagsLiveData extends MediatorLiveData<StatusFlags> {

    private MutableLiveData<Boolean> networkConnected;
    private MutableLiveData<Boolean> screenRegistered;

    public StatusFlagsLiveData(MutableLiveData<Boolean> networkConnected, MutableLiveData<Boolean> screenRegistered) {
        this.networkConnected = networkConnected;
        this.screenRegistered = screenRegistered;

        addSource(screenRegistered, aBoolean -> {
            StatusFlags statusFlags = getValue();
            if (statusFlags != null) {
                statusFlags.setScreenRegistered(aBoolean);
                setValue(statusFlags);
            }
        });

        addSource(networkConnected, aBoolean -> {
            StatusFlags statusFlags = getValue();
            if (statusFlags != null) {
                statusFlags.setNetworkConnected(aBoolean);
                setValue(statusFlags);
            }
        });

    }

    @Override
    public void postValue(StatusFlags value) {
        super.postValue(value);
        networkConnected.postValue(value.isNetworkConnected());
        screenRegistered.postValue(value.isScreenRegistered());
    }
}



