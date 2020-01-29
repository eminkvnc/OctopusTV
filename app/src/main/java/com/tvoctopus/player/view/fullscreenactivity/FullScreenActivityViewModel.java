package com.tvoctopus.player.view.fullscreenactivity;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.tvoctopus.player.model.DataRepository;
import com.tvoctopus.player.model.Playlist;
import com.tvoctopus.player.model.ScreenConfig;
import com.tvoctopus.player.model.StatusFlags;
import com.tvoctopus.player.model.StatusFlagsLiveData;

public class FullScreenActivityViewModel extends AndroidViewModel {

    private DataRepository dataRepository;

    public FullScreenActivityViewModel(@NonNull Application application) {
        super(application);
        dataRepository = new DataRepository(application);
    }

    public MediatorLiveData<StatusFlags> getStatusFlags(){

        StatusFlagsLiveData statusFlagsLiveData =
                new StatusFlagsLiveData(dataRepository.getNetworkConnected(), dataRepository.getScreenRegistered());
        statusFlagsLiveData.setValue(new StatusFlags());
        return statusFlagsLiveData;
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

    public ScreenConfig getConfig(){
        return dataRepository.getScreenConfig();
    }
}
