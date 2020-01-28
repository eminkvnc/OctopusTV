package com.tvoctopus.player.API;

import org.json.JSONObject;

public interface QueryListener {
    void onNewQuery(JSONObject result);
}
