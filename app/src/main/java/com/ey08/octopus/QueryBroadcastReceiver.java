package com.ey08.octopus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ey08.octopus.API.QueryListener;
import com.ey08.octopus.API.QuerySchedulerService;

import org.json.JSONException;
import org.json.JSONObject;

public class QueryBroadcastReceiver extends BroadcastReceiver {

    QueryListener queryListener;
    WeatherListener weatherListener;

    public QueryBroadcastReceiver(QueryListener queryListener, WeatherListener weatherListener) {
        this.queryListener = queryListener;
        this.weatherListener = weatherListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(QuerySchedulerService.ACTION_ON_NEW_QUERY)){
            try {
                String result = intent.getStringExtra("Result");
                JSONObject jo = null;
                if (result != null) {
                    jo = new JSONObject(result);
                }
                queryListener.onNewQuery(jo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

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
