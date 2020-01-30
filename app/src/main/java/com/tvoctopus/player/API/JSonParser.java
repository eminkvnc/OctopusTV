package com.tvoctopus.player.API;

import android.util.Log;

import com.tvoctopus.player.model.CommandData;
import com.tvoctopus.player.model.DayOptions;
import com.tvoctopus.player.model.MediaData;
import com.tvoctopus.player.model.Playlist;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static com.tvoctopus.player.API.APIKeys.*;

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
            if (!input.getBoolean(KEY_ERROR)) {
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
                    HashMap<Integer, DayOptions> dayScheduleMap = new HashMap<>();
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
                                DayOptions dayOptions = new DayOptions(
                                        dayScheduleJSon.get("day_"+j+"_on").toString(),
                                        dayScheduleJSon.get("day_"+j+"_off").toString(),
                                        dayScheduleJSon.getString("day_"+j+"_status"));
                                dayScheduleMap.put(j,dayOptions);
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
                        commandData.setDaySchedule(dayScheduleMap);
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
    //TODO: Generate weatherData object and parse data to object.
    public HashMap<String, String> parseWeatherData(JSONObject input){

        HashMap<String, String> weatherData = new HashMap<>();

        try {
            String icon = input.getJSONArray("weather").getJSONObject(0).getString("icon");
            String temperature = input.getJSONObject("main").getString("temp");
            String location = input.getString("name");
            weatherData.put("icon",icon);
            weatherData.put("temperature",temperature);
            weatherData.put("location",location);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weatherData;
    }


}
