package com.tvoctopus.player.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tvoctopus.player.API.JSonParser;
import com.tvoctopus.player.model.CommandData;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.ssl.HttpsURLConnection;
import static com.tvoctopus.player.API.APIKeys.*;

public class QuerySchedulerService extends Service {

    private static final String TAG = "QuerySchedulerService";

    private final int SCHEDULE_PERIOD = 1000*30;
    private final int SCHEDULE_DELAY = 300;

    public static final String ACTION_ON_NEW_QUERY = "OnNewQuery";
    public static final String ACTION_SCREEN_REGISTERED = "ACTION_SCREEN_REGISTERED";
    public static final String ACTION_COMMAND_SYNC = "ACTION_COMMAND_SYNC";
    public static final String ACTION_COMMAND_REPORT = "ACTION_COMMAND_REPORT";
    public static final String ACTION_COMMAND_REBOOT = "ACTION_COMMAND_REBOOT";
    public static final String ACTION_COMMAND_RESET = "ACTION_COMMAND_RESET";
    public static final String ACTION_COMMAND_TURN_ON_TV = "ACTION_COMMAND_TURN_ON_TV";
    public static final String ACTION_COMMAND_TURN_OFF_TV = "ACTION_COMMAND_TURN_OFF_TV";
    public static final String ACTION_COMMAND_SCREENSHOT = "ACTION_COMMAND_SCREENSHOT";

    public static final String PARAM_SCREEN_REGISTERED = "PARAM_SCREEN_REGISTERED";
    public static final String PARAM_COMMAND_DATA = "PARAM_COMMAND_DATA";

    private URL url;
    private String result;
    private Timer timer;
    private QueryTask queryTask;

    public QuerySchedulerService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            timer = new Timer();
            try {
                url = new URL(intent.getStringExtra("URL"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            queryTask = new QueryTask();
            timer.schedule(queryTask, SCHEDULE_DELAY, SCHEDULE_PERIOD);
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        queryTask.cancel();
        timer.cancel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class QueryTask extends TimerTask {

        @Override
        public void run() {

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
//                Intent intent = new Intent();
//                intent.setAction(ACTION_ON_NEW_QUERY);
//                intent.putExtra("Result",result);
//                sendBroadcast(intent);

                JSONObject jo = null;
                if (result != null) {
                    jo = new JSONObject(result);
                }
                Log.d(TAG, "run: result: "+result);

                ArrayList<CommandData> commands = new JSonParser().parseCommands(jo);
                if(commands == null){
                    Intent screenRegisteredIntent = new Intent(ACTION_SCREEN_REGISTERED);
                    screenRegisteredIntent.putExtra(PARAM_SCREEN_REGISTERED,false);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(screenRegisteredIntent);
                    Log.d(TAG, "Broadcast from QueryScheduler with action: "+ACTION_SCREEN_REGISTERED);
                } else{
                    Intent screenRegisteredIntent = new Intent(ACTION_SCREEN_REGISTERED);
                    screenRegisteredIntent.putExtra(PARAM_SCREEN_REGISTERED,true);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(screenRegisteredIntent);
                    Log.d(TAG, "Broadcast from QueryScheduler with action: "+ACTION_SCREEN_REGISTERED);

                    for(CommandData commandData : commands){
                        switch (commandData.getCommand()){
                            case KEY_COMMANDS_SYNC:
                                Intent syncIntent = new Intent(ACTION_COMMAND_SYNC);
                                syncIntent.putExtra(PARAM_COMMAND_DATA,commandData);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(syncIntent);
                                Log.d(TAG, "Broadcast from QueryScheduler with action: "+ACTION_COMMAND_SYNC);
                                break;

                            case KEY_COMMANDS_REPORT:
                                Intent reportIntent = new Intent(ACTION_COMMAND_REPORT);
                                reportIntent.putExtra(PARAM_COMMAND_DATA,commandData);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(reportIntent);
                                Log.d(TAG, "Broadcast from QueryScheduler with action: "+ACTION_COMMAND_REPORT);
                                break;

                            case KEY_COMMANDS_REBOOT:
                                Intent rebootIntent = new Intent(ACTION_COMMAND_REBOOT);
                                rebootIntent.putExtra(PARAM_COMMAND_DATA,commandData);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(rebootIntent);
                                Log.d(TAG, "Broadcast from QueryScheduler with action: "+ACTION_COMMAND_REBOOT);
                                break;

                            case KEY_COMMANDS_RESET:
                                Intent resetIntent = new Intent(ACTION_COMMAND_RESET);
                                resetIntent.putExtra(PARAM_COMMAND_DATA,commandData);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resetIntent);
                                Log.d(TAG, "Broadcast from QueryScheduler with action: "+ACTION_COMMAND_RESET);
                                break;

                            case KEY_COMMANDS_TURN_OFF_TV:
                                Intent turnOffTvIntent = new Intent(ACTION_COMMAND_TURN_OFF_TV);
                                turnOffTvIntent.putExtra(PARAM_COMMAND_DATA,commandData);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(turnOffTvIntent);
                                Log.d(TAG, "Broadcast from QueryScheduler with action: "+ACTION_COMMAND_TURN_OFF_TV);
                                break;

                            case KEY_COMMANDS_TURN_ON_TV:
                                Intent turnOnTvIntent = new Intent(ACTION_COMMAND_TURN_ON_TV);
                                turnOnTvIntent.putExtra(PARAM_COMMAND_DATA,commandData);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(turnOnTvIntent);
                                Log.d(TAG, "Broadcast from QueryScheduler with action: "+ACTION_COMMAND_TURN_ON_TV);
                                break;

                            case KEY_COMMANDS_SCREENSHOT:
                                Intent screenShotIntent = new Intent(ACTION_COMMAND_SCREENSHOT);
                                screenShotIntent.putExtra(PARAM_COMMAND_DATA,commandData);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(screenShotIntent);
                                Log.d(TAG, "Broadcast from QueryScheduler with action: "+ACTION_COMMAND_SCREENSHOT);
                                break;
                        }
                    }
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }
}