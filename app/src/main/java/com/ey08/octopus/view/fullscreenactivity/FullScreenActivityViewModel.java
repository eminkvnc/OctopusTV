package com.ey08.octopus.view.fullscreenactivity;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;

import com.ey08.octopus.model.DataRepository;
import com.ey08.octopus.model.StatusFlags;
import com.ey08.octopus.model.StatusFlagsLiveData;

public class FullScreenActivityViewModel extends AndroidViewModel {

    private DataRepository dataRepository;

    public FullScreenActivityViewModel(@NonNull Application application) {
        super(application);
        dataRepository = new DataRepository(application);
    }

//    public MutableLiveData<Boolean> getScreenRegistered(){
//        return dataRepository.getScreenRegistered();
//    }
//
//    public MutableLiveData<Boolean> getNetworkConnected(){
//        return dataRepository.getNetworkConnected();
//    }

    public MediatorLiveData<StatusFlags> getStatusFlags(){

        StatusFlagsLiveData statusFlagsLiveData =
                new StatusFlagsLiveData(dataRepository.getNetworkConnected(), dataRepository.getScreenRegistered());
        statusFlagsLiveData.setValue(new StatusFlags());
        return statusFlagsLiveData;
    }

}
