package com.tvoctopus.player.API;

import org.json.JSONException;
import org.json.JSONObject;

public class MediaData {

    public static final String MEDIA_TYPE_VIDEO = "video";
    public static final String MEDIA_TYPE_JPG = "image";

    private String name;
    private String type;
    private String md5;
    private String time;
    private long startTime = -1;
    private long stopTime = -1;

    public MediaData(String name, String type, String md5, String time) {
        this.name = name;
        this.type = type;
        this.md5 = md5;
        this.time = time;
    }


    public MediaData(JSONObject jo) {
        try {
            this.name = jo.getString("name");
            this.type = jo.getString("type");
            this.md5 = jo.getString("md5");
            this.time = jo.getString("time");
            this.time = jo.getString("time");
            this.startTime = jo.getLong("startTime");
            this.stopTime = jo.getLong("stopTime");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public JSONObject toJson(){

        JSONObject jo = new JSONObject();
        try {
            jo.put("name",name);
            jo.put("type",type);
            jo.put("md5",md5);
            jo.put("time",time);
            jo.put("startTime",startTime);
            jo.put("stopTime",stopTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }

}
