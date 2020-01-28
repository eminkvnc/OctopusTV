package com.tvoctopus.player.model;

import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;

public abstract class SharedPreferenceLiveData<T> extends MutableLiveData<T> {

    private SharedPreferences sharedPrefs;
    private String key;
    private T defValue;

    SharedPreferenceLiveData(SharedPreferences prefs, String key, T defValue) {
        this.sharedPrefs = prefs;
        this.key = key;
        this.defValue = defValue;
    }

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key1) {
            if (key.equals(key1)) {
                postValue(getValueFromPreferences(key1, defValue));
            }
        }
    };

    abstract T getValueFromPreferences(String key, T defValue);


    @Override
    public void postValue(T value){
        super.postValue(value);
        sharedPrefs.edit().putBoolean(key, (Boolean) value).apply();
    }

    @Override
    public void setValue(T value){
        super.setValue(value);
        sharedPrefs.edit().putBoolean(key, (Boolean) value).apply();
    }

    @Override
    protected void onActive() {
        super.onActive();
        postValue(getValueFromPreferences(key, defValue));
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        super.onInactive();
    }


}