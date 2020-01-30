package com.tvoctopus.player.view.player;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.tvoctopus.player.model.DataRepository;
import com.tvoctopus.player.model.Playlist;

public class PlayerFragmentViewModel extends AndroidViewModel {

    private DataRepository dataRepository;

    public PlayerFragmentViewModel(@NonNull Application application) {
        super(application);
        dataRepository = new DataRepository(application);

    }

    public MutableLiveData<Boolean> getNetworkConnected(){
        return dataRepository.getNetworkConnected();
    }

    public MutableLiveData<Playlist> getPlaylist(){
        return dataRepository.getPlaylist();
    }

    public Playlist getLastPlaylist(){
        return dataRepository.getLastPlaylist();
    }



}
