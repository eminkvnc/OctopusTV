package com.tvoctopus.player.API;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tvoctopus.player.R;
import com.tvoctopus.player.model.MediaData;
import com.tvoctopus.player.model.Playlist;

import java.io.File;
import java.util.HashMap;

import static android.content.Context.DOWNLOAD_SERVICE;

public class Downloader{

    public static final String TAG = "Downloader";

    public static final String MEDIA_DOWNLOAD_URL = "http://panel.tvoctopus.net/uploads/media/";
    public static final String DOWNLOAD_DIR = "OctopusDownloads";
    public static final String DOWNLOAD_DIR_TEMP = "temp_OctopusDownloads";

    public static final String ACTION_DOWNLOAD_FILE_COMPLETE = "ACTION_DOWNLOAD_FILE_COMPLETE";
    public static final String PARAM_DOWNLOAD_COMPLETE_STATUS = "PARAM_DOWNLOAD_COMPLETE_STATUS";
    public static final String PARAM_DOWNLOAD_COMPLETE_NAME = "PARAM_DOWNLOAD_COMPLETE_NAME";
    public static final String PARAM_DOWNLOAD_COMPLETE_PLAYLIST = "PARAM_DOWNLOAD_COMPLETE_PLAYLIST";
    public static final String PARAM_FILE_NAME = "PARAM_FILE_NAME";

    private DownloadManager downloadManager;
//    private Context context;
    private long lastDownload=-1L;
    private BroadcastReceiver onCompleteBroadcastReceiver;
    private HashMap<Long, String> downloadMap;
    private Playlist playlist;

    private static Downloader instance;
    private Context context;

    public static Downloader getInstance(Context context){
        if (instance == null){
            instance = new Downloader(context);
        }
        return instance;
    }

    private Downloader(Context con) {

        this.context = con;
        downloadMap = new HashMap<>();
        //TODO: Execute not queued downloads.
        onCompleteBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context ctx, Intent intent) {
                DownloadManager.Query query = new DownloadManager.Query();
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                query.setFilterByStatus(DownloadManager.STATUS_PAUSED |
                        DownloadManager.STATUS_PENDING |
                        DownloadManager.STATUS_RUNNING);
                Cursor cursor = downloadManager.query(query);
                if (cursor != null && cursor.getCount() > 0) {
                    Intent i = new Intent(ACTION_DOWNLOAD_FILE_COMPLETE);
                    i.putExtra(PARAM_DOWNLOAD_COMPLETE_STATUS,false);
                    i.putExtra(PARAM_DOWNLOAD_COMPLETE_NAME, downloadMap.get(downloadId));
                    LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                    Log.d(TAG, "Broadcast from Downloader with action: "+ACTION_DOWNLOAD_FILE_COMPLETE);
                    cursor.close();
                } else {
                    Intent i = new Intent(ACTION_DOWNLOAD_FILE_COMPLETE);
                    i.putExtra(PARAM_DOWNLOAD_COMPLETE_STATUS,true);
                    i.putExtra(PARAM_DOWNLOAD_COMPLETE_NAME, downloadMap.get(downloadId));
                    i.putExtra(PARAM_DOWNLOAD_COMPLETE_PLAYLIST, (Parcelable) playlist);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                    Log.d(TAG, "Broadcast from Downloader with action: "+ACTION_DOWNLOAD_FILE_COMPLETE);
                }
                downloadMap.remove(downloadId);
            }
        };
        Log.d(TAG, "Downloader: constructor");
        context.registerReceiver(onCompleteBroadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        downloadManager = (DownloadManager)context.getSystemService(DOWNLOAD_SERVICE);
    }


    public void stop(){
        try{
            context.unregisterReceiver(onCompleteBroadcastReceiver);
        } catch(IllegalArgumentException e) {

            e.printStackTrace();
        }
        for(long l : downloadMap.keySet()){
            downloadManager.remove(l);
            File file = new File(DOWNLOAD_DIR+"/"+downloadMap.get(l));
            if(file.exists()){
                boolean b = file.delete();
            }
        }
        //TODO: Queue not completed downloads.(Use SharedPreferences)
        downloadMap.clear();
        instance = null;
    }


    public void startDownload(String fileName) {

        if(fileName != null){
            File file = new File(DOWNLOAD_DIR+"/"+fileName);
            if(!file.exists()){
                Uri uri=Uri.parse(MEDIA_DOWNLOAD_URL+fileName);
                lastDownload = downloadManager.enqueue(new DownloadManager.Request(uri)
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(false)
                        .setTitle(fileName)
                        .setDescription(context.getResources().getString(R.string.downloader_notification_media_downloading))
                        .setDestinationInExternalFilesDir(context, DOWNLOAD_DIR_TEMP, fileName)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE));
                Log.d(getClass().getName(), "startDownload: "+DOWNLOAD_DIR+"/"+fileName);
                downloadMap.put(lastDownload, fileName);
            }
        }
    }

    public void startDownloads(Playlist playlist) {

        this.playlist = playlist;
        for(MediaData mediaData : playlist){
            String fileName = mediaData.getName();
            if(fileName != null){

                Uri uri=Uri.parse(MEDIA_DOWNLOAD_URL+fileName);
                lastDownload = downloadManager.enqueue(new DownloadManager.Request(uri)
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(false)
                        .setTitle(fileName)
                        .setDescription(context.getResources().getString(R.string.downloader_notification_media_downloading))
                        .setDestinationInExternalFilesDir(context, DOWNLOAD_DIR_TEMP, fileName)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE));
                Log.d(getClass().getName(), "startDownload: "+DOWNLOAD_DIR+"/"+fileName);
                downloadMap.put(lastDownload, fileName);

            }
        }

    }


}
