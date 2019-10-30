package com.ey08.octopus.API;

import org.json.JSONObject;

public interface QueryListener {
    void onNewQuery(JSONObject result);
}
