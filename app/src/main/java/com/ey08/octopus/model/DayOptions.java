package com.ey08.octopus.model;


import android.os.Parcel;
import android.os.Parcelable;

public class DayOptions implements Parcelable {

    public static final String STATUS_ON = "on";
    public static final String STATUS_OFF = "off";
    public static final String STATUS_SCHEDULED = "scheduled";

    private String on;
    private String off;
    private String status;


    public DayOptions(String on, String off, String status) {
        this.on = on;
        this.off = off;
        this.status = status;
    }


    public String getOn() {
        return on;
    }

    public void setOn(String on) {
        this.on = on;
    }

    public String getOff() {
        return off;
    }

    public void setOff(String off) {
        this.off = off;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.on);
        dest.writeString(this.off);
        dest.writeString(this.status);
    }

    protected DayOptions(Parcel in) {
        this.on = in.readString();
        this.off = in.readString();
        this.status = in.readString();
    }

    public static final Parcelable.Creator<DayOptions> CREATOR = new Parcelable.Creator<DayOptions>() {
        @Override
        public DayOptions createFromParcel(Parcel source) {
            return new DayOptions(source);
        }

        @Override
        public DayOptions[] newArray(int size) {
            return new DayOptions[size];
        }
    };
}
