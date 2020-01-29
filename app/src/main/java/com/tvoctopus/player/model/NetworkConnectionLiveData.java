package com.tvoctopus.player.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

public class NetworkConnectionLiveData extends MutableLiveData<Boolean> {

    private ConnectivityManager cm;

    public NetworkConnectionLiveData(Context context) {
        cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback(){

        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            postValue(true);
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            postValue(false);
        }
    };

    @NonNull
    @Override
    public Boolean getValue() {
        if(super.getValue() == null){
            return false;
        } else {
            return super.getValue();
        }
    }

    @Override
    protected void onActive() {
        super.onActive();
        if (cm != null) {
            cm.registerNetworkCallback(new NetworkRequest.Builder().build(), networkCallback);
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        cm.unregisterNetworkCallback(networkCallback);
    }
}
