package com.ey08.octopus;

import org.json.JSONObject;

public interface WeatherListener {
    void weatherUpdated(JSONObject result);
}
