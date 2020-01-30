package com.tvoctopus.player.view.fullscreenactivity;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.tvoctopus.player.model.DataRepository;
import com.tvoctopus.player.model.Playlist;
import com.tvoctopus.player.model.ScreenConfig;

public class FullScreenActivityViewModel extends AndroidViewModel {

    private DataRepository dataRepository;

    public FullScreenActivityViewModel(@NonNull Application application) {
        super(application);
        dataRepository = new DataRepository(application);
    }

    public MutableLiveData<Boolean> getNetworkConnected(){
        return dataRepository.getNetworkConnected();
    }

    public MutableLiveData<Boolean> getScreenRegistered(){
        return dataRepository.getScreenRegistered();
    }

    public MutableLiveData<Playlist> getPlaylist(){
        return dataRepository.getPlaylist();
    }

    public MutableLiveData<String> getScreenId(){
        return dataRepository.getScreenId();
    }

    public String getScreenIdValue(){
        return dataRepository.getScreenIdValue();
    }

    public int getScreenOrientationValue(){
        return dataRepository.getScreenOrientationValue();
    }

    public boolean getScreenRegisteredValue(){
        return dataRepository.getScreenRegisteredValue();
    }

    public ScreenConfig getConfig(){
        return dataRepository.getScreenConfig();
    }
}
