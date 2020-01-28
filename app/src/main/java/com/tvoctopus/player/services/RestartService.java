package com.tvoctopus.player.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.tvoctopus.player.view.fullscreenactivity.FullscreenActivity;

public class RestartService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), FullscreenActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            stopSelf();
        },8000);

    }
}
