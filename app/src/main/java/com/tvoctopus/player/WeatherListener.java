package com.tvoctopus.player;

import org.json.JSONObject;

public interface WeatherListener {
    void weatherUpdated(JSONObject result);
}
