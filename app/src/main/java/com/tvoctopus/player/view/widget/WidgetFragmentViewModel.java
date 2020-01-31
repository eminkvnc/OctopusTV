package com.tvoctopus.player.view.widget;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.tvoctopus.player.model.DataRepository;
import com.tvoctopus.player.model.ScreenConfig;

public class WidgetFragmentViewModel extends AndroidViewModel {

    private DataRepository dataRepository;


    public WidgetFragmentViewModel(@NonNull Application application) {
        super(application);
        dataRepository = new DataRepository(getApplication());
    }

    public ScreenConfig getConfig(){
        return dataRepository.getScreenConfig();
    }

}
