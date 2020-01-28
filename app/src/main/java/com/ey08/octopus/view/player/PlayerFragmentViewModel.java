package com.ey08.octopus.view.player;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.ey08.octopus.model.DataRepository;

public class PlayerFragmentViewModel extends AndroidViewModel {

    private DataRepository dataRepository;

    public PlayerFragmentViewModel(@NonNull Application application) {
        super(application);
        dataRepository = new DataRepository(application);

    }

    public MutableLiveData<Boolean> getNetworkConnected(){
        return dataRepository.getNetworkConnected();
    }



}
