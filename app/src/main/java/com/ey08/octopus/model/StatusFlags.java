package com.ey08.octopus.model;

import android.os.Parcel;
import android.os.Parcelable;

public class StatusFlags implements Parcelable {

    private boolean isNetworkConnected;
    private boolean isScreenRegistered;

    public StatusFlags() {
    }

    public StatusFlags(boolean isNetworkConnected, boolean isScreenRegistered) {
        this.isNetworkConnected = isNetworkConnected;
        this.isScreenRegistered = isScreenRegistered;
    }

    public boolean isNetworkConnected() {
        return isNetworkConnected;
    }

    public void setNetworkConnected(boolean networkConnected) {
        isNetworkConnected = networkConnected;
    }

    public boolean isScreenRegistered() {
        return isScreenRegistered;
    }

    public void setScreenRegistered(boolean screenRegistered) {
        isScreenRegistered = screenRegistered;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isNetworkConnected ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isScreenRegistered ? (byte) 1 : (byte) 0);
    }

    protected StatusFlags(Parcel in) {
        this.isNetworkConnected = in.readByte() != 0;
        this.isScreenRegistered = in.readByte() != 0;
    }

    public static final Parcelable.Creator<StatusFlags> CREATOR = new Parcelable.Creator<StatusFlags>() {
        @Override
        public StatusFlags createFromParcel(Parcel source) {
            return new StatusFlags(source);
        }

        @Override
        public StatusFlags[] newArray(int size) {
            return new StatusFlags[size];
        }
    };
}
