package com.tvoctopus.player.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

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
        Calendar currentDateOn = Calendar.getInstance();
        currentDateOn.setTimeInMillis(System.currentTimeMillis());
        Calendar onDate = getOn();
        currentDateOn.set(Calendar.DAY_OF_WEEK, onDate.get(Calendar.DAY_OF_WEEK));
        currentDateOn.set(Calendar.HOUR_OF_DAY, onDate.get(Calendar.HOUR_OF_DAY));
        currentDateOn.set(Calendar.MINUTE, onDate.get(Calendar.MINUTE));
        currentDateOn.set(Calendar.SECOND, onDate.get(Calendar.SECOND));
        setOn(currentDateOn);

        Calendar currentDateOff = Calendar.getInstance();
        currentDateOff.setTimeInMillis(System.currentTimeMillis());
        Calendar offDate = getOff();
        currentDateOff.set(Calendar.DAY_OF_WEEK, offDate.get(Calendar.DAY_OF_WEEK));
        currentDateOff.set(Calendar.HOUR_OF_DAY, offDate.get(Calendar.HOUR_OF_DAY));
        currentDateOff.set(Calendar.MINUTE, offDate.get(Calendar.MINUTE));
        currentDateOff.set(Calendar.SECOND, offDate.get(Calendar.SECOND));
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
