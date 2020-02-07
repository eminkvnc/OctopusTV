package com.tvoctopus.player.model;

import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;

public class ScreenConfig{

    private MutableLiveData<Integer> screenOrientation;
    private MutableLiveData<String> weatherCity;
    private MutableLiveData<Integer> widgetBarPosition;
    private MutableLiveData<Boolean> weatherEnabled;
    private MutableLiveData<Boolean> rssEnabled;
    private MutableLiveData<Boolean> widgetBarEnabled;
    private MutableLiveData<HashMap<Integer, DayStatus>> dayStatusMap;

    public MutableLiveData<String> getWeatherCity() {
        return weatherCity;
    }

    public void setWeatherCity(MutableLiveData<String> weatherCity) {
        this.weatherCity = weatherCity;
    }

    public MutableLiveData<Integer> getWidgetBarPosition() {
        return widgetBarPosition;
    }

    public void setWidgetBarPosition(MutableLiveData<Integer> widgetBarPosition) {
        this.widgetBarPosition = widgetBarPosition;
    }

    public MutableLiveData<Boolean> getWeatherEnabled() {
        return weatherEnabled;
    }

    public void setWeatherEnabled(MutableLiveData<Boolean> weatherEnabled) {
        this.weatherEnabled = weatherEnabled;
    }

    public MutableLiveData<Boolean> getRssEnabled() {
        return rssEnabled;
    }

    public void setRssEnabled(MutableLiveData<Boolean> rssEnabled) {
        this.rssEnabled = rssEnabled;
    }

    public MutableLiveData<Integer> getScreenOrientation() {
        return screenOrientation;
    }

    public void setScreenOrientation(MutableLiveData<Integer> screenOrientation) {
        this.screenOrientation = screenOrientation;
    }

    public MutableLiveData<Boolean> getWidgetBarEnabled() {
        return widgetBarEnabled;
    }

    public void setWidgetBarEnabled(MutableLiveData<Boolean> widgetBarEnabled) {
        this.widgetBarEnabled = widgetBarEnabled;
    }

    public MutableLiveData<HashMap<Integer, DayStatus>> getDayStatusMap() {
        return dayStatusMap;
    }

    public void setDayStatusMap(MutableLiveData<HashMap<Integer, DayStatus>> dayStatusMap) {
        this.dayStatusMap = dayStatusMap;
    }
}
