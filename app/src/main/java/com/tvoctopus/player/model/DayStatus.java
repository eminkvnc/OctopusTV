package com.tvoctopus.player.model;


import android.app.AlarmManager;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.TimeZone;

public class DayStatus implements Parcelable {

    public static final String STATUS_ON = "on";
    public static final String STATUS_OFF = "off";
    public static final String STATUS_SCHEDULED = "scheduled";

    private Calendar on;
    private Calendar off;
    private String status;


    public DayStatus(Calendar on, Calendar off, String status) {
        this.on = on;
        this.off = off;
        this.status = status;
    }


    public Calendar getOn() {
        return on;
    }

    public void setOn(Calendar on) {
        this.on = on;
    }

    public Calendar getOff() {
        return off;
    }

    public void setOff(Calendar off) {
        this.off = off;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void fitTimes(){
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTimeInMillis(System.currentTimeMillis());
        currentDate.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));

        Calendar currentDateOn = Calendar.getInstance();
        currentDateOn.setTimeInMillis(System.currentTimeMillis());
        currentDateOn.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        Calendar onDate = getOn();
        currentDateOn.set(Calendar.DAY_OF_WEEK, onDate.get(Calendar.DAY_OF_WEEK));
        currentDateOn.set(Calendar.HOUR_OF_DAY, onDate.get(Calendar.HOUR_OF_DAY));
        currentDateOn.set(Calendar.MINUTE, onDate.get(Calendar.MINUTE));
        currentDateOn.set(Calendar.SECOND, onDate.get(Calendar.SECOND));
        if(currentDate.after(currentDateOn)){
            currentDateOn.setTimeInMillis(currentDateOn.getTimeInMillis() + AlarmManager.INTERVAL_DAY*7);
        }
        setOn(currentDateOn);

        Calendar currentDateOff = Calendar.getInstance();
        currentDateOff.setTimeInMillis(System.currentTimeMillis());
        currentDateOff.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        Calendar offDate = getOff();
        currentDateOff.set(Calendar.DAY_OF_WEEK, offDate.get(Calendar.DAY_OF_WEEK));
        currentDateOff.set(Calendar.HOUR_OF_DAY, offDate.get(Calendar.HOUR_OF_DAY));
        currentDateOff.set(Calendar.MINUTE, offDate.get(Calendar.MINUTE));
        currentDateOff.set(Calendar.SECOND, offDate.get(Calendar.SECOND));
        if(currentDate.after(currentDateOff)){
            currentDateOff.setTimeInMillis(currentDateOff.getTimeInMillis() + AlarmManager.INTERVAL_DAY*7);
        }
        setOff(currentDateOff);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.on);
        dest.writeSerializable(this.off);
        dest.writeString(this.status);
    }

    protected DayStatus(Parcel in) {
        this.on = (Calendar) in.readSerializable();
        this.off = (Calendar) in.readSerializable();
        this.status = in.readString();
    }

    public static final Creator<DayStatus> CREATOR = new Creator<DayStatus>() {
        @Override
        public DayStatus createFromParcel(Parcel source) {
            return new DayStatus(source);
        }

        @Override
        public DayStatus[] newArray(int size) {
            return new DayStatus[size];
        }
    };
}
