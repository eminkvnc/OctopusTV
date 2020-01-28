package com.ey08.octopus.API;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ey08.octopus.R;

import java.io.File;
import java.util.HashMap;

public class Downloader extends Service {

    public static final String MEDIA_DOWNLOAD_URL = "http://panel.tvoctopus.net/uploads/media/";
    public static final String DOWNLOAD_DIR = "OctopusDownloads";

    public static final String ACTION_DOWNLOAD_FILE_COMPLETE = "ACTION_DOWNLOAD_FILE_COMPLETE";
    public static final String PARAM_DOWNLOAD_COMPLETE = "PARAM_DOWNLOAD_COMPLETE";
    public static final String PARAM_FILE_NAME = "PARAM_FILE_NAME";

    private DownloadManager downloadManager;
//    private Context context;
    private long lastDownload=-1L;
    private BroadcastReceiver onCompleteBroadcastReceiver;
    private HashMap<Long, String> downloadMap;

    public Downloader() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        downloadMap = new HashMap<>();

        onCompleteBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                DownloadManager.Query query = new DownloadManager.Query();
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                downloadMap.remove(downloadId);
                query.setFilterByStatus(DownloadManager.STATUS_PAUSED |
                        DownloadManager.STATUS_PENDING |
                        DownloadManager.STATUS_RUNNING);
                Cursor cursor = downloadManager.query(query);
                if (cursor != null && cursor.getCount() > 0) {
                    Intent i = new Intent(ACTION_DOWNLOAD_FILE_COMPLETE);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
                    i.putExtra(PARAM_DOWNLOAD_COMPLETE,false);
                    sendBroadcast(i);
                    cursor.close();
                } else {
                    Intent i = new Intent(ACTION_DOWNLOAD_FILE_COMPLETE);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
                    i.putExtra(PARAM_DOWNLOAD_COMPLETE,true);
                    sendBroadcast(i);
                    stopSelf();
                }
            }
        };

        registerReceiver(onCompleteBroadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        downloadManager = (DownloadManager)getApplicationContext().getSystemService(DOWNLOAD_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onCompleteBroadcastReceiver);
        for(long l : downloadMap.keySet()){
            downloadManager.remove(l);
            File file = new File(DOWNLOAD_DIR+"/"+downloadMap.get(l));
            if(file.exists()){
                boolean b = file.delete();
            }
        }
        downloadMap.clear();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String fileName = intent.getStringExtra(PARAM_FILE_NAME);
        if(fileName != null){
            File file = new File(DOWNLOAD_DIR+"/"+fileName);
            if(!file.exists()){
                Uri uri=Uri.parse(MEDIA_DOWNLOAD_URL+fileName);
                lastDownload = downloadManager.enqueue(new DownloadManager.Request(uri)
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(false)
                        .setTitle(fileName)
                        .setDescription(getApplicationContext().getResources().getString(R.string.downloader_notification_media_downloading))
                        .setDestinationInExternalFilesDir(getApplicationContext(), DOWNLOAD_DIR, fileName)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE));
                Log.d(getClass().getName(), "startDownload: "+DOWNLOAD_DIR+"/"+fileName);
                downloadMap.put(lastDownload, fileName);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

//    public void startDownload(String fileName) {
//
//
//        File file = new File(DOWNLOAD_DIR+"/"+fileName);
//        if(!file.exists()){
//            Uri uri=Uri.parse(MEDIA_DOWNLOAD_URL+fileName);
//            lastDownload = downloadManager.enqueue(new DownloadManager.Request(uri)
//                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
//                            DownloadManager.Request.NETWORK_MOBILE)
//                    .setAllowedOverRoaming(false)
//                    .setTitle(fileName)
//                    .setDescription(context.getResources().getString(R.string.downloader_notification_media_downloading))
//                    .setDestinationInExternalFilesDir(context, DOWNLOAD_DIR, fileName)
//                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE));
//            Log.d(getClass().getName(), "startDownload: "+DOWNLOAD_DIR+"/"+fileName);
//            downloadIDs.add(lastDownload);
//        }
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

