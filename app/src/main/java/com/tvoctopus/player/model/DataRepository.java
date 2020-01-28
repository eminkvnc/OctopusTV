package com.tvoctopus.player.model;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;

import java.util.HashSet;
import java.util.Set;

public class DataRepository {

    public static final String SHARED_PREF_OCTOPUS_DATA = "OctopusData";
    public static final String SHARED_PREF_PLAYLIST = "Playlist";
    public static final String SHARED_PREF_PLAYLIST_KEY = "PlaylistKey";
    public static final String SHARED_PREF_SCREEN_REGISTERED_KEY = "ScreenRegistered";

    private Application application;

    public DataRepository(Application application) {
        this.application = application;
    }

    //TODO: implement all SharedPreferences operations.

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
                for (MediaData media : value){
                    String mediaString = media.getName()+"%%%"+media.getType()+"%%%"+media.getMd5()+"%%%"+media.getTime();
                    mediaSet.add(mediaString);
                }
                sp.edit().putStringSet(SHARED_PREF_PLAYLIST_KEY, mediaSet).apply();
            }
        };
    }

    public MutableLiveData<Boolean> getNetworkConnected(){
        return new NetworkConnectionLiveData(application.getApplicationContext());
    }

    private Playlist getLastPlaylist(){
        SharedPreferences sp = application.getApplicationContext()
                .getSharedPreferences(SHARED_PREF_PLAYLIST, Context.MODE_PRIVATE);
        Playlist playlist = new Playlist();
        try {
            Set<String> playlistSet = sp.getStringSet(SHARED_PREF_PLAYLIST_KEY, null);
            if (playlistSet != null) {
                for(String mediaString : playlistSet){
                    String[] mediaData = mediaString.split("%%%");
                    MediaData media = new MediaData(
                            mediaData[0],
                            mediaData[1],
                            mediaData[2],
                            mediaData[3]);
                    playlist.add(media);
                }
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return playlist;
    }
}
