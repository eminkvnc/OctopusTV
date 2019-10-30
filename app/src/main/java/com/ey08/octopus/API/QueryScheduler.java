package com.ey08.octopus.API;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class QueryScheduler {

    private static final String TAG = "QueryScheduler";

    private final int SCHEDULE_PERIOD = 1000*30;
    private final int SCHEDULE_DELAY = 300;

    private URL url;
    private String result;
    private QueryListener queryListener;
    private Timer timer;
    private QueryTask queryTask;
    private boolean isStarted = false;

    public QueryScheduler(URL url, QueryListener queryListener) {
        this.url = url;
        this.queryListener = queryListener;
    }

    public void startSchduler(){
        isStarted = true;
        timer = new Timer();
        queryTask = new QueryTask();
        timer.schedule(queryTask, SCHEDULE_DELAY, SCHEDULE_PERIOD);
    }

    public void stopScheduler(){
        isStarted = false;
        timer.cancel();
    }

    public boolean isStarted() {
        return isStarted;
    }

    private class QueryTask extends TimerTask {

        @Override
        public void run() {
            //!! CHECK NETWORK STATE HERE !!
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
                    queryListener.onNewQuery(new JSONObject(result));
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "run:Query scheduler works...");
        }
    }

}
