package com.ey08.octopus.API;

import org.json.JSONObject;

import java.util.HashMap;


public class CommandData {

    private static final String TAG = "CommandData";

    private String id;
    private String command;
    private JSONObject params;

    private Playlist playlist;
    private HashMap<Integer, DayOptions> daySchedule;
    private HashMap<String, Object> metaData;


    public CommandData(String id, String command, JSONObject params) {
        this.id = id;
        this.command = command;
        this.params = params;
        daySchedule = null;
        playlist = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public JSONObject getParams() {
        return params;
    }

    public void setParams(JSONObject params) {
        this.params = params;
    }

    public HashMap<Integer, DayOptions> getDaySchedule() {
        return daySchedule;
    }

    public void setDaySchedule(HashMap<Integer, DayOptions> daySchedule) {
        this.daySchedule = daySchedule;
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public HashMap<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(HashMap<String, Object> metaData) {
        this.metaData = metaData;
    }

}
