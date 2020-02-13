package com.tvoctopus.player.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tvoctopus.player.view.fullscreenactivity.FullscreenActivity;

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

public class WeatherService extends Service {

    private static final String TAG = "WeatherService";

    private final int SCHEDULE_PERIOD = 1000*120;
    private final int SCHEDULE_DELAY = 300;
    public static final String ACTION_WEATHER_QUERY = "WeatherQuery";
    public static final String PARAM_WEATHER_RESULT = "WeatherResult";
    public static final String WEATHER_CITY_KEY = "WEATHER_CITY_KEY";
    private static final String urlP1 = "https://api.openweathermap.org/data/2.5/weather?q=";
    private static final String urlP2 = "&units=metric&appid=aaba7194c4a518878cbc6c226db04586";

    private URL url;
    private String result;
    private Timer timer;
    private WeatherTask weatherTask;
    private boolean isWaiting;
    private BroadcastReceiver waitReceiver;

    public WeatherService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        waitReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    // do whatever you need to do here
                    isWaiting = true;
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    // and do whatever you need to do here
                    isWaiting = false;
                }
                if(intent.getAction().equals(FullscreenActivity.ACTION_WAITING)){
                    isWaiting = intent.getBooleanExtra(FullscreenActivity.PARAM_WAITING,true);
                }
            }
        };
        IntentFilter intentFilter2 = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter2.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter2.addAction(FullscreenActivity.ACTION_WAITING);
        registerReceiver(waitReceiver,intentFilter2);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            if(timer != null){
                timer.cancel();
                timer = null;
            }
            try {
                timer = new Timer();
                String city = "istanbul";
                if(intent.getStringExtra(WEATHER_CITY_KEY) != null){
                    city = intent.getStringExtra(WEATHER_CITY_KEY);
                }
                url = new URL(urlP1+city+urlP2);
                weatherTask = new WeatherTask();
                if(timer != null){
                    timer.schedule(weatherTask, SCHEDULE_DELAY, SCHEDULE_PERIOD);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(waitReceiver);
        if(weatherTask != null){
            weatherTask.cancel();
            weatherTask = null;
        }
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class WeatherTask extends TimerTask {

        @Override
        public void run() {
            //Query here!
            if(!isWaiting){
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
                    intent.setAction(ACTION_WEATHER_QUERY);
                    intent.putExtra(PARAM_WEATHER_RESULT, result);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "run:Weather query works...");
            }
        }
    }
}
