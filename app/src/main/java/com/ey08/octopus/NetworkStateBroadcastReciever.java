package com.ey08.octopus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStateBroadcastReciever extends BroadcastReceiver {

    NetworkStateListener networkStateListener;

    public NetworkStateBroadcastReciever(NetworkStateListener networkStateListener) {
        this.networkStateListener = networkStateListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null || !networkInfo.isConnected()){
            networkStateListener.networkDisconnected();
        }
        else {
            networkStateListener.networkConnected();
        }
    }

}
