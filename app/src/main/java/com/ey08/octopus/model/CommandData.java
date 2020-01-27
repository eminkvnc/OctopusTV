package com.ey08.octopus.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class CommandData implements Parcelable {

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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.command);
        if(params != null){
            dest.writeString(this.params.toString());
        }
        dest.writeParcelable(this.playlist, flags);
        dest.writeSerializable(this.daySchedule);
        dest.writeSerializable(this.metaData);
    }

    protected CommandData(Parcel in) {

        try {
            this.id = in.readString();
            this.command = in.readString();
            if(in.readString() != null){
                this.params = new JSONObject(in.readString());
            }
            this.playlist = in.readParcelable(Playlist.class.getClassLoader());
            this.daySchedule = (HashMap<Integer, DayOptions>) in.readSerializable();
            this.metaData = (HashMap<String, Object>) in.readSerializable();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static final Parcelable.Creator<CommandData> CREATOR = new Parcelable.Creator<CommandData>() {
        @Override
        public CommandData createFromParcel(Parcel source) {
            return new CommandData(source);
        }

        @Override
        public CommandData[] newArray(int size) {
            return new CommandData[size];
        }
    };
}
