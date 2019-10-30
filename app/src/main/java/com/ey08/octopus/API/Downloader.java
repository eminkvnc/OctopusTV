package com.ey08.octopus.API;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.ey08.octopus.R;

import java.io.File;
import java.util.ArrayList;

import static android.content.Context.DOWNLOAD_SERVICE;

public class Downloader {

    public static final String MEDIA_DOWNLOAD_URL = "http://panel.tvoctopus.net/uploads/media/";
    public static final String DOWNLOAD_DIR = "OctopusDownloads";

    private DownloadManager downloadManager;
    private Context context;
    private long lastDownload=-1L;
    private BroadcastReceiver onCompleteBroadcastReceiver;
    private BroadcastReceiver onNotificationClickBroadcastReceiver;
    private DownloadCompleteListener downloadCompleteListener;
    private ArrayList<Long> downloadIDs;

    private int downloadCount = 0;

    public Downloader(Context context, DownloadCompleteListener downloadCompleteListener) {

        this.context = context;
        downloadIDs = new ArrayList<>();
        this.downloadCompleteListener = downloadCompleteListener;

        onCompleteBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterByStatus(DownloadManager.STATUS_PAUSED |
                        DownloadManager.STATUS_PENDING |
                        DownloadManager.STATUS_RUNNING);
                Cursor cursor = downloadManager.query(query);
                if (cursor != null && cursor.getCount() > 0) {
                    downloadCompleteListener.downloadComplete(false);
                    cursor.close();
                }else {
                    downloadCompleteListener.downloadComplete(true);
                }
            }
        };

        onNotificationClickBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                //DO SOMETHING...
            }
        };

        downloadManager = (DownloadManager)context.getSystemService(DOWNLOAD_SERVICE);
    }

    public void startDownload(String fileName) {
        context.registerReceiver(onCompleteBroadcastReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        context.registerReceiver(onNotificationClickBroadcastReceiver,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
        File file = new File(DOWNLOAD_DIR+"/"+fileName);
        if(!file.exists()){
            Uri uri=Uri.parse(MEDIA_DOWNLOAD_URL+fileName);
            lastDownload = downloadManager.enqueue(new DownloadManager.Request(uri)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                            DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(fileName)
                    .setDescription(context.getResources().getString(R.string.downloader_notification_media_downloading))
                    .setDestinationInExternalFilesDir(context, DOWNLOAD_DIR, fileName)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE));
            Log.d(getClass().getName(), "startDownload: "+DOWNLOAD_DIR+"/"+fileName);
            downloadIDs.add(lastDownload);
            downloadCompleteListener.downloadStart();
        }
    }


}

