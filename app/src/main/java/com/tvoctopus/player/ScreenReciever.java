package com.tvoctopus.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenReciever extends BroadcastReceiver {

    private ScreenListener screenListener;

    public ScreenReciever(ScreenListener screenListener) {
        this.screenListener = screenListener;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // do whatever you need to do here
            screenListener.onScreenLocked();
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // and do whatever you need to do here
            screenListener.onScreenAwake();
        }
    }
}
