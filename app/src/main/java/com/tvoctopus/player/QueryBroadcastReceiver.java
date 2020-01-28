package com.tvoctopus.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tvoctopus.player.API.QueryListener;
import com.tvoctopus.player.API.QuerySchedulerService;

import org.json.JSONException;
import org.json.JSONObject;

public class QueryBroadcastReceiver extends BroadcastReceiver {

    QueryListener queryListener;

    public QueryBroadcastReceiver(QueryListener queryListener) {
        this.queryListener = queryListener;
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
    }
}
