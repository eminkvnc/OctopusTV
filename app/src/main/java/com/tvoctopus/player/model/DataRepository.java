package com.tvoctopus.player.model;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DataRepository {

    public static final String SHARED_PREF_OCTOPUS_DATA = "OctopusData";
    public static final String SHARED_PREF_CONFIG = "Config";
    public static final String SHARED_PREF_PLAYLIST = "Playlist";
    public static final String SHARED_PREF_PLAYLIST_KEY = "PlaylistKey";
    public static final String SHARED_PREF_CURRENT_MEDIA_KEY = "CurrentMedia";
    public static final String SHARED_PREF_SCREEN_REGISTERED_KEY = "ScreenRegistered";
    public static final String SHARED_PREF_SCREEN_ID_KEY = "screenID";
    public static final String SHARED_PREF_WIDGET_WEATHER_CITY_KEY = "weatherCity";
    public static final String SHARED_PREF_WIDGET_BAR_POSITION_KEY = "widgetBarPosition";
    public static final String SHARED_PREF_WIDGET_WEATHER_ENABLED_KEY = "weatherEnabled";
    public static final String SHARED_PREF_WIDGET_RSS_ENABLED_KEY = "rssEnabled";
    public static final String SHARED_PREF_WIDGET_BAR_ENABLED_KEY = "widgetBarEnabled";
    public static final String SHARED_PREF_SCREEN_ORIENTATION_KEY = "ScreenOrientation";
    public static final String SHARED_PREF_DAY_STATUS_KEY = "DayStatus";
    public static final String SHARED_PREF_CAPTION_DATA_KEY = "CaptionData";

    private Application application;
    private FirebaseHelper firebaseHelper;

    public DataRepository(Application application) {
        this.application = application;
        firebaseHelper = FirebaseHelper.getInstance(application.getApplicationContext());
    }

    public MutableLiveData<Boolean> getScreenRegistered(){
        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_OCTOPUS_DATA, Context.MODE_PRIVATE);
        return new SharedPreferenceLiveData<Boolean>(sp, SHARED_PREF_SCREEN_REGISTERED_KEY, false) {
            @Override
            Boolean getValueFromPreferences(String key, Boolean defValue) {
                return sp.getBoolean(key, defValue);
            }

            @Override
            public void setAndPostValue(Boolean value) {
                sp.edit().putBoolean(SHARED_PREF_SCREEN_REGISTERED_KEY, value).apply();
            }
        };
    }

    public MutableLiveData<String> getScreenId(){
        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_OCTOPUS_DATA, Context.MODE_PRIVATE);
        return new SharedPreferenceLiveData<String>(sp, SHARED_PREF_SCREEN_ID_KEY, null) {
            @Override
            String getValueFromPreferences(String key, String defValue) {
                return sp.getString(key, defValue);
            }

            @Override
            public void setAndPostValue(String value) {
                sp.edit().putString(SHARED_PREF_SCREEN_ID_KEY, value).apply();
            }
        };
    }

    public MutableLiveData<String> getCaptionData(){
        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_OCTOPUS_DATA, Context.MODE_PRIVATE);
        return new SharedPreferenceLiveData<String>(sp, SHARED_PREF_CAPTION_DATA_KEY, null) {
            @Override
            String getValueFromPreferences(String key, String defValue) {
                return sp.getString(key, defValue);
            }

            @Override
            public void setAndPostValue(String value) {
                sp.edit().putString(SHARED_PREF_CAPTION_DATA_KEY, value).apply();
            }
        };
    }

    public MutableLiveData<Playlist> getPlaylist(){
        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_PLAYLIST, Context.MODE_PRIVATE);
        return new SharedPreferenceLiveData<Playlist>(sp, SHARED_PREF_PLAYLIST_KEY, null) {
            @Override
            Playlist getValueFromPreferences(String key, Playlist defValue) {
                return getLastPlaylist();
            }

            @Override
            public void setAndPostValue(Playlist value) {
                SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_PLAYLIST, Context.MODE_PRIVATE);
                Set<String> mediaSet = new HashSet<>();
                for (int i = 0; i < value.size(); i ++){
                    MediaData media = value.get(i);
                    String mediaString = media.getName()+"%%%"+media.getType()+"%%%"+media.getMd5()+"%%%"+media.getTime()+"%%%"+i;
                    mediaSet.add(mediaString);
                }
                sp.edit().putStringSet(SHARED_PREF_PLAYLIST_KEY, mediaSet).apply();
            }
        };
    }

    private MutableLiveData<Integer> getScreenOrientation(){
        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return new SharedPreferenceLiveData<Integer>(sp, SHARED_PREF_SCREEN_ORIENTATION_KEY, -1) {

            @Override
            Integer getValueFromPreferences(String key, Integer defValue) {
                return sp.getInt(key, defValue);
            }

            @Override
            public void setAndPostValue(Integer value) {
                sp.edit().putInt(SHARED_PREF_SCREEN_ORIENTATION_KEY, value).apply();
            }
        };

    }

    private MutableLiveData<String> getWidgetWeatherCity(){
        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return new SharedPreferenceLiveData<String>(sp, SHARED_PREF_WIDGET_WEATHER_CITY_KEY, null) {
            @Override
            String getValueFromPreferences(String key, String defValue) {
                return sp.getString(key, defValue);
            }

            @Override
            public void setAndPostValue(String value) {
                sp.edit().putString(SHARED_PREF_WIDGET_WEATHER_CITY_KEY, value).apply();
            }
        };
    }

    private MutableLiveData<Integer> getWidgetBarPosition(){
        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return new SharedPreferenceLiveData<Integer>(sp, SHARED_PREF_WIDGET_BAR_POSITION_KEY, -1) {
            @Override
            Integer getValueFromPreferences(String key, Integer defValue) {
                return sp.getInt(key, defValue);
            }

            @Override
            public void setAndPostValue(Integer value) {
                sp.edit().putInt(SHARED_PREF_WIDGET_BAR_POSITION_KEY, value).apply();
            }
        };
    }

    private MutableLiveData<Boolean> getWidgetBarEnabled(){
        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return new SharedPreferenceLiveData<Boolean>(sp, SHARED_PREF_WIDGET_BAR_ENABLED_KEY, false) {
            @Override
            Boolean getValueFromPreferences(String key, Boolean defValue) {
                return sp.getBoolean(key, defValue);
            }

            @Override
            public void setAndPostValue(Boolean value) {
                sp.edit().putBoolean(SHARED_PREF_WIDGET_BAR_ENABLED_KEY, value).apply();
            }
        };
    }

    private MutableLiveData<Boolean> getWidgetWeatherEnabled(){
        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return new SharedPreferenceLiveData<Boolean>(sp, SHARED_PREF_WIDGET_WEATHER_ENABLED_KEY, false) {
            @Override
            Boolean getValueFromPreferences(String key, Boolean defValue) {
                return sp.getBoolean(key, defValue);
            }

            @Override
            public void setAndPostValue(Boolean value) {
                sp.edit().putBoolean(SHARED_PREF_WIDGET_WEATHER_ENABLED_KEY, value).apply();
            }
        };
    }

    private MutableLiveData<Boolean> getWidgetRssEnabled(){
        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return new SharedPreferenceLiveData<Boolean>(sp, SHARED_PREF_WIDGET_RSS_ENABLED_KEY, false) {
            @Override
            Boolean getValueFromPreferences(String key, Boolean defValue) {
                return sp.getBoolean(key, defValue);
            }

            @Override
            public void setAndPostValue(Boolean value) {
                sp.edit().putBoolean(SHARED_PREF_WIDGET_RSS_ENABLED_KEY, value).apply();
            }
        };
    }

    private MutableLiveData<HashMap<Integer, DayStatus>> getDayStatus(){
        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return new SharedPreferenceLiveData<HashMap<Integer, DayStatus>>(sp, SHARED_PREF_DAY_STATUS_KEY, null) {

            @Override
            HashMap<Integer, DayStatus> getValueFromPreferences(String key, HashMap<Integer, DayStatus> defValue) {

                HashMap<Integer, DayStatus> hashMap = new HashMap<>();
                Set<String> stringSet = sp.getStringSet(key, new HashSet<>());

                for(String s : stringSet){
                    if(s != null){
                        Calendar calendarOn = null;
                        Calendar calendarOff = null;
                        String[] split = s.split("%%%");
                        if(split[3].equals(DayStatus.STATUS_SCHEDULED)){
                            String[] splitOn = split[1].split(":");
                            calendarOn = Calendar.getInstance();
                            calendarOn.set(Calendar.DAY_OF_WEEK, Integer.parseInt(split[0])-1);
                            calendarOn.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitOn[0]));
                            calendarOn.set(Calendar.MINUTE, Integer.parseInt(splitOn[1]));
                            calendarOn.set(Calendar.SECOND, Integer.parseInt(splitOn[2]));

                            String[] splitOff = split[2].split(":");
                            calendarOff = Calendar.getInstance();
                            calendarOff.set(Calendar.DAY_OF_WEEK, Integer.parseInt(split[0])-1);
                            calendarOff.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitOff[0]));
                            calendarOff.set(Calendar.MINUTE, Integer.parseInt(splitOff[1]));
                            calendarOff.set(Calendar.SECOND, Integer.parseInt(splitOff[2]));
                        }

                        DayStatus dayStatus = new DayStatus(calendarOn,calendarOff,split[3]);
                        hashMap.put(Integer.parseInt(split[0]), dayStatus);
                    }

                    }

                return hashMap;
            }

            @Override
            public void setAndPostValue(HashMap<Integer, DayStatus> value) {
                Set<String> stringSet = new HashSet<>();
                for(int j = 1; j < 8 ; j++){
                    DayStatus dayStatus = value.get(j);
                    String s = null;
                    if (dayStatus != null) {
                        String on = null;
                        String off = null;
                        if(dayStatus.getStatus().equals(DayStatus.STATUS_SCHEDULED)){
                            on = dayStatus.getOn().get(Calendar.HOUR_OF_DAY)+":"+dayStatus.getOn().get(Calendar.MINUTE)+":"+dayStatus.getOn().get(Calendar.SECOND);
                            off = dayStatus.getOff().get(Calendar.HOUR_OF_DAY)+":"+dayStatus.getOff().get(Calendar.MINUTE)+":"+dayStatus.getOff().get(Calendar.SECOND);
                        }

                        s = j+"%%%"+on+"%%%"+off+"%%%"+dayStatus.getStatus();
                    }
                    stringSet.add(s);
                }
                sp.edit().putStringSet(SHARED_PREF_DAY_STATUS_KEY, stringSet).apply();
            }
        };
    }

    public MutableLiveData<Boolean> getNetworkConnected(){
        return new NetworkConnectionLiveData(application.getApplicationContext());
    }

    public String getScreenIdValue(){
        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_OCTOPUS_DATA, Context.MODE_PRIVATE);
        return sp.getString(SHARED_PREF_SCREEN_ID_KEY, null);
    }

    public int getScreenOrientationValue(){
        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return sp.getInt(SHARED_PREF_SCREEN_ORIENTATION_KEY, -1);
    }

    public boolean getScreenRegisteredValue(){
        SharedPreferences sp = application.getSharedPreferences(SHARED_PREF_OCTOPUS_DATA, Context.MODE_PRIVATE);
        return sp.getBoolean(SHARED_PREF_SCREEN_REGISTERED_KEY, false);
    }

    public ScreenConfig getScreenConfig(){
        ScreenConfig screenConfig = new ScreenConfig();
        screenConfig.setWidgetBarEnabled(getWidgetBarEnabled());
        screenConfig.setScreenOrientation(getScreenOrientation());
        screenConfig.setWeatherCity(getWidgetWeatherCity());
        screenConfig.setWidgetBarPosition(getWidgetBarPosition());
        screenConfig.setWeatherEnabled(getWidgetWeatherEnabled());
        screenConfig.setRssEnabled(getWidgetRssEnabled());
        screenConfig.setDayStatusMap(getDayStatus());
        return screenConfig;
    }

    public Playlist getLastPlaylist(){
        SharedPreferences sp = application.getApplicationContext()
                .getSharedPreferences(SHARED_PREF_PLAYLIST, Context.MODE_PRIVATE);
        Playlist playlist = new Playlist();
        try {
            Set<String> playlistSet = sp.getStringSet(SHARED_PREF_PLAYLIST_KEY, null);
            if (playlistSet != null) {
                MediaData[] mediaDataArray = new MediaData[playlistSet.size()];
                for(String mediaString : playlistSet){
                    String[] mediaData = mediaString.split("%%%");
                    MediaData media = new MediaData(
                            mediaData[0],
                            mediaData[1],
                            mediaData[2],
                            mediaData[3]);
                    int index = Integer.valueOf(mediaData[4]);
                    mediaDataArray[index] = media;
                }
                Collections.addAll(playlist, mediaDataArray);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return playlist;
    }

    public void reportMediaData(MediaData mediaData, boolean networkConnected){
        if(networkConnected){
            executeQueueReport();
            firebaseHelper.addMediaData(mediaData);
        } else {
            queueReport(mediaData);
        }
    }

    private void executeQueueReport(){

        SharedPreferences sharedPreferences = application.getApplicationContext()
                .getSharedPreferences("ReportQueue", Context.MODE_PRIVATE);
        try {
            JSONArray ja;
            if (sharedPreferences.contains("queue")) {
                String queueString = sharedPreferences.getString("queue", "");
                ja = new JSONArray(queueString);
                for(int i = 0; i < ja.length(); i++){
                    firebaseHelper.addMediaData(new MediaData(ja.getJSONObject(i)));
                }
                sharedPreferences.edit().remove("queue").apply();
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void queueReport(MediaData mediaData){
        SharedPreferences sharedPreferences = application.getApplicationContext()
                .getSharedPreferences("ReportQueue", Context.MODE_PRIVATE);

        try {
            JSONObject jo = mediaData.toJson();
            JSONArray ja;
            if (sharedPreferences.contains("queue")) {
                String queueString = sharedPreferences.getString("queue", "");
                ja = new JSONArray(queueString);
            } else {
                ja = new JSONArray();
            }
            ja.put(jo);
            sharedPreferences.edit().putString("queue", ja.toString()).apply();
        } catch (JSONException e){
            e.printStackTrace();
        }
    }
}
