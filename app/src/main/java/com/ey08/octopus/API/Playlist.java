package com.ey08.octopus.API;

import android.net.Uri;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;

import java.io.File;
import java.util.ArrayList;

public class Playlist extends ArrayList<MediaData> {

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

    public boolean isLast(){
        return (index == size() - 1);
    }

    public void resetIndex(){
        index = 0;
    }

    public int getIndex(){
        return index;
    }

}
