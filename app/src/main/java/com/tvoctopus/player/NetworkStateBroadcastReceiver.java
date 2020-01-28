package com.tvoctopus.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStateBroadcastReceiver extends BroadcastReceiver {

    private NetworkStateListener networkStateListener;
    private boolean isConnected = false;

    public NetworkStateBroadcastReceiver(NetworkStateListener networkStateListener) {
        this.networkStateListener = networkStateListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo == null || !networkInfo.isConnected()){
                networkStateListener.networkDisconnected();
                isConnected = false;
            }
            else {
                if(!isConnected){
                    networkStateListener.networkConnected();
                    isConnected = true;
                }
            }
        }
    }

}
