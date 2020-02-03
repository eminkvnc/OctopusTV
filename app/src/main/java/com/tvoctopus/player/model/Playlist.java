package com.tvoctopus.player.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;

import java.io.File;
import java.util.ArrayList;

public class Playlist extends ArrayList<MediaData> implements Parcelable {

    private int index = 0;

    public ArrayList<String> getMediaNames(){
        ArrayList<String> mediaNames = new ArrayList<>();
        for(MediaData mediaData : this){
            mediaNames.add(mediaData.getName());
        }
        return mediaNames;
    }

    public MediaSource getMediaSource(DataSource.Factory dataSourceFactory, File downloadDir){
        Uri uri = Uri.parse("file://"+downloadDir.getAbsolutePath()+"/"+get(index).getName());
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);

    }

    public MediaData getNext(){
        index++;
        if(index < size()){
            return get(index);
        }
        else{
            resetIndex();
            return get(0);
        }
    }
    public MediaData getCurrent(){
        return get(index);
    }

    public boolean isLast(){
        return (index == size() - 1);
    }

    public void resetIndex(){
        index = 0;
    }

    public int getIndex(){
        return index;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.index);
    }

    public Playlist() {
    }

    public Playlist(Parcel in) {
        this.index = in.readInt();
    }

    public static final Parcelable.Creator<Playlist> CREATOR = new Parcelable.Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel source) {
            return new Playlist(source);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };
}
