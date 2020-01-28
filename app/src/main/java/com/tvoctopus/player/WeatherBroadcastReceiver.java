package com.tvoctopus.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tvoctopus.player.services.WeatherService;

import org.json.JSONException;
import org.json.JSONObject;

public class WeatherBroadcastReceiver extends BroadcastReceiver {

    WeatherListener weatherListener;

    public WeatherBroadcastReceiver(WeatherListener weatherListener) {
        this.weatherListener = weatherListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(WeatherService.ACTION_WEATHER_QUERY)){
            try {
                String result = intent.getStringExtra("WeatherResult");
                JSONObject jo = null;
                if (result != null) {
                    jo = new JSONObject(result);
                }
                weatherListener.weatherUpdated(jo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
