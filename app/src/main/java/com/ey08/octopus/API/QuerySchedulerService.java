package com.ey08.octopus.API;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class QuerySchedulerService extends Service {

    private static final String TAG = "QuerySchedulerService";

    private final int SCHEDULE_PERIOD = 1000*30;
    private final int SCHEDULE_DELAY = 300;

    public static final String ACTION_ON_NEW_QUERY = "OnNewQuery";

    private URL url;
    private String result;
    private Timer timer;
    private QueryTask queryTask;
    private boolean isStarted = false;

    public QuerySchedulerService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                isStarted = true;
                timer = new Timer();
                try {
                    url = new URL(intent.getStringExtra("URL"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                queryTask = new QueryTask();
                timer.schedule(queryTask, SCHEDULE_DELAY, SCHEDULE_PERIOD);
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStarted = false;
        queryTask.cancel();
        timer.cancel();
    }

    public boolean isStarted() {
        return isStarted;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class QueryTask extends TimerTask {

        @Override
        public void run() {
            //Query here!
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream;
                StringBuilder stringBuilder = new StringBuilder();
                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK){
                    inputStream = connection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    String data = bufferedReader.readLine();
                    while (data != null){
                        stringBuilder.append(data);
                        data = bufferedReader.readLine();
                    }
                    connection.disconnect();
                    result = stringBuilder.toString();
                }
                Intent intent = new Intent();
                intent.setAction(ACTION_ON_NEW_QUERY);
                intent.putExtra("Result",result);
                sendBroadcast(intent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "run:Query scheduler works...");
        }
    }
}
