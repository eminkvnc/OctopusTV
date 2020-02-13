package com.tvoctopus.player.model;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import static com.tvoctopus.player.model.APIKeys.KEY_COMMAND;
import static com.tvoctopus.player.model.APIKeys.KEY_COMMANDS;
import static com.tvoctopus.player.model.APIKeys.KEY_COMMANDS_REPORT;
import static com.tvoctopus.player.model.APIKeys.KEY_COMMANDS_SYNC;
import static com.tvoctopus.player.model.APIKeys.KEY_ERROR;
import static com.tvoctopus.player.model.APIKeys.KEY_MESSAGE;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_IS_MASTER;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_MASTER;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_ORIENTATION;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_OVERSCAN_BOTTOM;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_OVERSCAN_LEFT;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_OVERSCAN_RIGHT;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_OVERSCAN_TOP;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_PLAYLIST;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_PLAYLIST_MEDIA_MD5;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_PLAYLIST_MEDIA_NAME;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_PLAYLIST_MEDIA_TIME;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_PLAYLIST_MEDIA_TYPE;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_SCHEDULE;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_WIFI_COUNTRY;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_WIFI_PASSWORD;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_WIFI_SSID;
import static com.tvoctopus.player.model.APIKeys.KEY_UUID;

public class JSonParser {

    public static final String TAG = "JSonParser";

    public ArrayList<CommandData> commands;

    public JSonParser() {

    }

    public ArrayList<CommandData> parseCommands(JSONObject input){
        commands = new ArrayList<>();
        String uuid;
        String command;
        JSONObject params;
        CommandData c;
        try {
            if (input != null && !input.getBoolean(KEY_ERROR)) {
                if (!input.get(KEY_COMMANDS).toString().equals("null")) {
                    JSONArray jsonArray = input.getJSONArray(KEY_COMMANDS);
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jo = jsonArray.getJSONObject(i);
                            if (!jo.toString().equals("null")) {
                                uuid = jo.get(KEY_UUID).toString();
                                command = jo.get(KEY_COMMAND).toString();
                                params = null;
                                if (!jo.get(KEY_PARAMS).toString().equals("null")) {
                                    params = jo.getJSONObject(KEY_PARAMS);
                                }
                                c = new CommandData(uuid, command, params);
                                parseParams(c, params, command);
                                commands.add(c);
                            }
                        }
                    }
                }
            } else{
                Log.d(TAG, "parseCommands: "+input.get(KEY_MESSAGE));
                commands = null;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return commands;
    }

    private void parseParams(CommandData commandData, JSONObject params, String commandType){

        if(params != null){
            switch (commandType){
                case KEY_COMMANDS_SYNC:
                    Playlist playlist = new Playlist();
                    HashMap<Integer, DayStatus> dayScheduleMap = new HashMap<>();
                    HashMap<String, Object> metaData = new HashMap<>();
                    try {
                        //Playlist mapping
                        JSONArray playlistJSon = params.getJSONArray(KEY_PARAMS_PLAYLIST);
                        for (int i = 0; i < playlistJSon.length(); i ++){
                            JSONObject mediaDataJSon = playlistJSon.getJSONObject(i);
                            MediaData mediaData = new MediaData(
                                    mediaDataJSon.getString(KEY_PARAMS_PLAYLIST_MEDIA_NAME),
                                    mediaDataJSon.getString(KEY_PARAMS_PLAYLIST_MEDIA_TYPE),
                                    mediaDataJSon.getString(KEY_PARAMS_PLAYLIST_MEDIA_MD5),
                                    mediaDataJSon.getString(KEY_PARAMS_PLAYLIST_MEDIA_TIME));
                            playlist.add(mediaData);
                        }

                        //Day schedule mapping
                        if(params.has(KEY_PARAMS_SCHEDULE)){
                        JSONObject dayScheduleJSon = params.getJSONObject(KEY_PARAMS_SCHEDULE);
                            for(int j = 1; j < 8 ; j++){
                                DayStatus dayStatus;

                                Calendar calendarOn = null;
                                Calendar calendarOff = null;
                                if(dayScheduleJSon.getString("day_"+j+"_status").equals(DayStatus.STATUS_SCHEDULED)){
                                    String[] splitOn = dayScheduleJSon.get("day_"+j+"_on").toString().split(":");
                                    String[] splitOff = dayScheduleJSon.get("day_" + j + "_off").toString().split(":");
                                    calendarOn = Calendar.getInstance();
                                    calendarOn.setTimeInMillis(System.currentTimeMillis());
                                    calendarOn.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
                                    calendarOn.set(Calendar.DAY_OF_WEEK, j+1%7);
                                    calendarOn.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitOn[0]));
                                    calendarOn.set(Calendar.MINUTE, Integer.parseInt(splitOn[1]));
                                    calendarOn.set(Calendar.SECOND, Integer.parseInt(splitOn[2]));

                                    calendarOff = Calendar.getInstance();
                                    calendarOff.setTimeInMillis(System.currentTimeMillis());
                                    calendarOff.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
                                    calendarOff.set(Calendar.DAY_OF_WEEK, j+1%7);
                                    calendarOff.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitOff[0]));
                                    calendarOff.set(Calendar.MINUTE, Integer.parseInt(splitOff[1]));
                                    calendarOff.set(Calendar.SECOND, Integer.parseInt(splitOff[2]));
                                }
                                dayStatus = new DayStatus(calendarOn, calendarOff, dayScheduleJSon.getString("day_"+j+"_status"));
                                dayScheduleMap.put(j, dayStatus);
                            }
                        }
                        //TODO: Check params object has keys.
                        //Metadata mapping
                        metaData.put(KEY_PARAMS_MASTER,params.get(KEY_PARAMS_MASTER));
                        metaData.put(KEY_PARAMS_IS_MASTER,params.get(KEY_PARAMS_IS_MASTER));
                        metaData.put(KEY_PARAMS_ORIENTATION,params.get(KEY_PARAMS_ORIENTATION));
                        metaData.put(KEY_PARAMS_OVERSCAN_TOP,params.get(KEY_PARAMS_OVERSCAN_TOP));
                        metaData.put(KEY_PARAMS_OVERSCAN_BOTTOM,params.get(KEY_PARAMS_OVERSCAN_BOTTOM));
                        metaData.put(KEY_PARAMS_OVERSCAN_RIGHT,params.get(KEY_PARAMS_OVERSCAN_RIGHT));
                        metaData.put(KEY_PARAMS_OVERSCAN_LEFT,params.get(KEY_PARAMS_OVERSCAN_LEFT));
                        metaData.put(KEY_PARAMS_WIFI_SSID,params.get(KEY_PARAMS_WIFI_SSID));
                        metaData.put(KEY_PARAMS_WIFI_PASSWORD,params.get(KEY_PARAMS_WIFI_PASSWORD));
                        metaData.put(KEY_PARAMS_WIFI_COUNTRY,params.get(KEY_PARAMS_WIFI_COUNTRY));

                        commandData.setPlaylist(playlist);
                        commandData.setDayStatus(dayScheduleMap);
                        commandData.setMetaData(metaData);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case KEY_COMMANDS_REPORT:
                    //Edit this case for API's Report structure
                    break;
            }

        }else {
            Log.d(TAG, "parseParams: params is null!");
        }

        Log.d(TAG, "parseParams: parse complete");

    }

    public WeatherData parseWeatherData(JSONObject input){

        Gson gson = new Gson();
        return gson.fromJson(input.toString(), WeatherData.class);
    }


}
