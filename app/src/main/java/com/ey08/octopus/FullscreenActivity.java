package com.ey08.octopus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ey08.octopus.API.APIKeys;
import com.ey08.octopus.API.CommandData;
import com.ey08.octopus.API.DownloadCompleteListener;
import com.ey08.octopus.API.Downloader;
import com.ey08.octopus.API.JSonParser;
import com.ey08.octopus.API.MediaData;
import com.ey08.octopus.API.Playlist;
import com.ey08.octopus.API.QueryListener;
import com.ey08.octopus.API.QuerySchedulerService;
import com.ey08.octopus.API.Reporter;
import com.github.rongi.rotate_layout.layout.RotateLayout;
import com.google.zxing.WriterException;

import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static com.ey08.octopus.API.APIKeys.KEY_COMMANDS_REPORT;
import static com.ey08.octopus.API.APIKeys.KEY_COMMANDS_RESET;
import static com.ey08.octopus.API.APIKeys.KEY_COMMANDS_SYNC;
import static com.ey08.octopus.API.APIKeys.KEY_COMMANDS_TURN_OFF_TV;
import static com.ey08.octopus.API.APIKeys.KEY_COMMANDS_TURN_ON_TV;
import static com.ey08.octopus.API.APIKeys.KEY_PARAMS_OVERSCAN_BOTTOM;
import static com.ey08.octopus.API.APIKeys.KEY_PARAMS_OVERSCAN_LEFT;
import static com.ey08.octopus.API.APIKeys.KEY_PARAMS_OVERSCAN_RIGHT;
import static com.ey08.octopus.API.APIKeys.KEY_PARAMS_OVERSCAN_TOP;
import static com.ey08.octopus.API.APIKeys.KEY_VALUES_ROTATION_0;
import static com.ey08.octopus.API.APIKeys.KEY_VALUES_ROTATION_180;
import static com.ey08.octopus.API.APIKeys.KEY_VALUES_ROTATION_270;
import static com.ey08.octopus.API.APIKeys.KEY_VALUES_ROTATION_90;
import static com.ey08.octopus.API.QuerySchedulerService.ACTION_ON_NEW_QUERY;
import static com.ey08.octopus.WeatherService.ACTION_WEATHER_QUERY;

public class FullscreenActivity extends AppCompatActivity implements DownloadCompleteListener, QueryListener, NetworkStateListener, ScreenListener, WeatherListener {

    public static final String TAG = "FullscreenActivity";

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3001;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 100;

    public static final String SHARED_PREF_OCTOPUS_DATA = "OctopusData";
    public static final String SHARED_PREF_PLAYLIST = "Playlist";

    private final Handler mHideHandler = new Handler();
    private RotateLayout mainFrame;
    private FrameLayout playerFrame;
    private FrameLayout widgetFrame;
    private ConstraintLayout constraintLayout;
    private PlayerFragment playerFragment;
    private WidgetFragment widgetFragment;

    private TextView textView;
    private ImageView qrImageView;
    private GifDialog gifDialog;
    private Activity activity;

    private NetworkStateBroadcastReceiver networkStateReceiver;
    private ScreenReciever screenReciever;
    private QueryBroadcastReceiver queryBroadcastReceiver;
    private WeatherBroadcastReceiver weatherBroadcastReceiver;

    private Intent querySchedulerService = null;
    private Intent weatherService = null;

    private ArrayList<CommandData> commands;
    private Playlist playlist;
    private Downloader downloader;
    private Reporter reporter;
    private boolean isDownloading = false;

    private String screenID;
    private Bitmap qrBitmap;

    private boolean isScreenLogOn = false;
    private boolean isScreenRegistered = true;
    private boolean isQueryServiceRunning = false;
    private boolean isWeatherServiceRunning = false;


    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mainFrame.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = () -> hide();

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        networkStateReceiver = new NetworkStateBroadcastReceiver(this);
        registerReceiver(networkStateReceiver, intentFilter);

        IntentFilter intentFilter2 = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter2.addAction(Intent.ACTION_SCREEN_OFF);
        screenReciever = new ScreenReciever(this);
        registerReceiver(screenReciever, intentFilter2);


        IntentFilter intentFilter3 = new IntentFilter(ACTION_ON_NEW_QUERY);
        queryBroadcastReceiver = new QueryBroadcastReceiver(this);
        registerReceiver(queryBroadcastReceiver,intentFilter3);

        IntentFilter intentFilter4 = new IntentFilter(ACTION_WEATHER_QUERY);
        weatherBroadcastReceiver = new WeatherBroadcastReceiver(this);
        registerReceiver(weatherBroadcastReceiver,intentFilter4);

        mVisible = true;
        commands = new ArrayList<>();
        activity = this;

        gifDialog = new GifDialog(FullscreenActivity.this);
        //gifDialog.show();

        playerFrame = findViewById(R.id.player_frame);
        widgetFrame = findViewById(R.id.widgets_frame);
        constraintLayout = findViewById(R.id.activity_fullscreen_constraint_layout);
        qrImageView = findViewById(R.id.qr_code_imageView);
        textView = findViewById(R.id.textView);
        textView.setTextColor(Color.WHITE);

        mainFrame = findViewById(R.id.main_frame);
        mainFrame.setOnTouchListener(mDelayHideTouchListener);
        mainFrame.setOnClickListener(view -> toggle());

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        delayedHide(100);

        SharedPreferences sp = getSharedPreferences(SHARED_PREF_OCTOPUS_DATA,MODE_PRIVATE);
        int screenOrientation = sp.getInt("ScreenOrientation",-1);
        if(screenOrientation != -1){
            rotateScreen(screenOrientation);
        }

        downloader = new Downloader(getApplicationContext(), this);
        reporter = new Reporter();
        checkPermission(getApplicationContext(), this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerFragment.stopPlayer();
        stopService(querySchedulerService);
        stopService(weatherService);
        unregisterReceiver(networkStateReceiver);
        unregisterReceiver(screenReciever);
        unregisterReceiver(queryBroadcastReceiver);
        unregisterReceiver(weatherBroadcastReceiver);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = new Intent(getApplicationContext(),RestartService.class);
        //startService(intent);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mainFrame.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void setFragment(View view, Fragment fragment) {
        //getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(view.getId(), fragment);
        fragmentTransaction.commit();
    }

    private void checkPermission(Context context, Activity activity) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            initFragments();
            initQueryScheduler();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initFragments();
                initQueryScheduler();
            } else {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.fullscreen_activity_storage_permission_toast),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initFragments(){
        playerFragment = new PlayerFragment();
        playerFragment.setContext(activity);
        widgetFragment = new WidgetFragment();
        setFragment(playerFrame, playerFragment);
        setFragment(widgetFrame, widgetFragment);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setWidgetBarPosition(WidgetFragment.POSITION_RIGHT,15,15);
            }
        },300);
    }

    private void initQueryScheduler(){
        //ADD ALL API QUERY URL HERE!!
        try {
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_OCTOPUS_DATA,MODE_PRIVATE);
            screenID = sharedPreferences.getString("screenID",null);
            if(screenID == null){
                SharedPreferences.Editor editor = sharedPreferences.edit();
                screenID = generateRandomID();
                editor.putString("screenID",screenID);
                editor.apply();
            }

            // Append screenID to url
            URL url = new URL("http://panel.tvoctopus.net/api/screen/"+screenID);

            //start scheduler service
            querySchedulerService = new Intent(FullscreenActivity.this, QuerySchedulerService.class);
            querySchedulerService.putExtra("URL",url.toString());
            startService(querySchedulerService);
            isQueryServiceRunning = true;
            //start weather service
            //TODO: Edit url with API location data.
            String weatherurlString = "https://api.openweathermap.org/data/2.5/weather?q=istanbul&units=metric&appid=aaba7194c4a518878cbc6c226db04586";
            weatherService = new Intent(FullscreenActivity.this, WeatherService.class);
            weatherService.putExtra("weatherURL",weatherurlString);
            startService(weatherService);
            isQueryServiceRunning = true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    // reverse clockwise
    private void rotateScreen(int degree){
        runOnUiThread(() -> {
            switch(degree){
                case KEY_VALUES_ROTATION_0:
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mainFrame.setAngle(0);
                    break;
                case KEY_VALUES_ROTATION_90:
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mainFrame.setAngle(270);
                    break;
                case KEY_VALUES_ROTATION_180:
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    mainFrame.setAngle(180);
                    break;
                case KEY_VALUES_ROTATION_270:
                    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    mainFrame.setAngle(90);
                    break;
            }
            mainFrame.requestLayout();
        });
    }

    private String generateRandomID(){
        return UUID.randomUUID().toString().substring(0,7);
    }

    private void deleteDirectory(File path) {
        if(path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        boolean wasSuccessful = file.delete();
                        if (wasSuccessful) {
                            Log.i("Deleted ", "successfully");
                        }
                    }
                }
            }
        }
    }

    private void clearApplicationData(){
        playerFragment.stopPlayer();
        getSharedPreferences(SHARED_PREF_OCTOPUS_DATA, Context.MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences(SHARED_PREF_PLAYLIST, Context.MODE_PRIVATE).edit().clear().apply();
        File file = getExternalFilesDir("OctopusDownloads");
        if (file != null) {
            deleteDirectory(file);
        }
    }

    public void setWidgetBarPosition(int position, int widthPercentage, int heightPercentage){

        switch (position){
            case WidgetFragment.POSITION_TOP:
                widgetFragment.setWidgetBarOrientation(LinearLayout.HORIZONTAL,widthPercentage,heightPercentage);
                ConstraintLayout.LayoutParams paramsTop = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                paramsTop.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                paramsTop.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                paramsTop.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                widgetFrame.setLayoutParams(paramsTop);
                widgetFrame.requestLayout();
                break;
            case WidgetFragment.POSITION_BOTTOM:

                widgetFragment.setWidgetBarOrientation(LinearLayout.HORIZONTAL,widthPercentage,heightPercentage);
                ConstraintLayout.LayoutParams paramsBottom = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                paramsBottom.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                paramsBottom.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                paramsBottom.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                widgetFrame.setLayoutParams(paramsBottom);
                widgetFrame.requestLayout();
                break;
            case WidgetFragment.POSITION_LEFT:
                widgetFragment.setWidgetBarOrientation(LinearLayout.VERTICAL,widthPercentage,heightPercentage);
                ConstraintLayout.LayoutParams paramsLeft = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
                paramsLeft.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
                paramsLeft.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                paramsLeft.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                widgetFrame.setLayoutParams(paramsLeft);
                widgetFrame.requestLayout();
                break;
            case WidgetFragment.POSITION_RIGHT:

                widgetFragment.setWidgetBarOrientation(LinearLayout.VERTICAL,widthPercentage,heightPercentage);
                ConstraintLayout.LayoutParams paramsRight = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
                paramsRight.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                paramsRight.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
                paramsRight.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                widgetFrame.setLayoutParams(paramsRight);
                widgetFrame.requestLayout();
                break;
        }
    }

    //This method only triggers when network connection is available.
    @Override
    public void onNewQuery(JSONObject result) {
        runOnUiThread(() -> {
            if(isScreenLogOn){
                textView.setText(result.toString());
            }
        });
        Log.d("QuerySchedulerService", "onNewQuery: "+result);
        //PARSE COMMANDS HERE
        commands = new JSonParser().parseCommands(result);
        //process commands
        if(commands != null){
            isScreenRegistered = true;
            if(commands.isEmpty()){
                File file = getExternalFilesDir("OctopusDownloads");
                if(file != null && file.list() != null){
                    if(!playerFragment.isPlaying()){
                        playerFragment.launchPlayer();
                    }
                }
            }else{
                for(CommandData command : commands){
                    switch (command.getCommand()){
                        case KEY_COMMANDS_SYNC:
                            // Check if media already downloaded
                            SharedPreferences sp = getSharedPreferences(SHARED_PREF_PLAYLIST,Context.MODE_PRIVATE);
                            sp.edit().clear().apply();
                            File downloadDir = getExternalFilesDir(Downloader.DOWNLOAD_DIR);
                            int i = 0;
                            for (MediaData media : command.getPlaylist()){
                                File downloadedFile = new File(downloadDir,media.getName());
                                if(!downloadedFile.exists()){
                                    downloader.startDownload(media.getName());
                                    reporter.setDownloadCommand(command);
                                }
                                sp.edit().putString(String.valueOf(i), media.getName()+"%%%"+media.getType()+"%%%"+media.getMd5()+"%%%"+media.getTime()).apply();
                                i++;
                            }
                            // change local playlist object if playlist updated
                            if(playlist == null || !playlist.equals(command.getPlaylist())){
                                playlist = command.getPlaylist();
                            }
                            // apply orientation
                            String orientationString = (String)command.getMetaData().get(APIKeys.KEY_PARAMS_ORIENTATION);
                            if(orientationString != null){
                                int orientation = Integer.parseInt(orientationString);
                                SharedPreferences sp2 = getSharedPreferences(SHARED_PREF_OCTOPUS_DATA,Context.MODE_PRIVATE);
                                int screenOrientation = sp2.getInt("ScreenOrientation",-1);
                                if(screenOrientation != orientation){
                                    sp2.edit().putInt("ScreenOrientation",orientation).apply();
                                }
                            }

                            // if there are no downloads update ui with new playlist
                            if(!isDownloading){
                                playerFragment.playlistUpdated(playlist);
                                SharedPreferences sp3 = getSharedPreferences(SHARED_PREF_OCTOPUS_DATA, Context.MODE_PRIVATE);
                                int screenOrientation = sp3.getInt("ScreenOrientation",-1);
                                rotateScreen(screenOrientation);
                                if(!playerFragment.isPlaying()){
                                    playerFragment.launchPlayer();
                                }
                            }

                            //TODO: Handle widget bar position, width and height when API results available for widgets. (temporarily used wifi-SSID and overscan metadata fields.)
                            //TODO: Cache widget data for offline usage.
                            //TODO: Activate-deactivate weather widget to result of OctopusTV API request.
                            if(command.getMetaData() != null){
                                HashMap<String, Object> hashMap = command.getMetaData();
                                String url = "https://api.openweathermap.org/data/2.5/weather?q="+hashMap.get(APIKeys.KEY_PARAMS_WIFI_SSID);
                                url = url+"&units=metric&appid=aaba7194c4a518878cbc6c226db04586";
                                weatherService.putExtra("weatherURL",url);
                                startService(weatherService);
                                Log.d(TAG, "onNewQuery: top: "+hashMap.get(KEY_PARAMS_OVERSCAN_TOP));
                                Log.d(TAG, "onNewQuery: bottom: "+hashMap.get(KEY_PARAMS_OVERSCAN_BOTTOM));
                                Log.d(TAG, "onNewQuery: left: "+hashMap.get(KEY_PARAMS_OVERSCAN_LEFT));
                                Log.d(TAG, "onNewQuery: right: "+hashMap.get(KEY_PARAMS_OVERSCAN_RIGHT));
                                if(hashMap.get(KEY_PARAMS_OVERSCAN_TOP).equals("1")){
                                    setWidgetBarPosition(WidgetFragment.POSITION_TOP,15,15);
                                }
                                if(hashMap.get(KEY_PARAMS_OVERSCAN_BOTTOM).equals("1")){
                                    setWidgetBarPosition(WidgetFragment.POSITION_BOTTOM,15,15);
                                }
                                if(hashMap.get(KEY_PARAMS_OVERSCAN_LEFT).equals("1")){
                                    setWidgetBarPosition(WidgetFragment.POSITION_LEFT,15,15);
                                }
                                if(hashMap.get(KEY_PARAMS_OVERSCAN_RIGHT).equals("1")){
                                    setWidgetBarPosition(WidgetFragment.POSITION_RIGHT,15,15);
                                }
                            }


                            break;

                        case KEY_COMMANDS_REPORT:
                            //process report command
                            reporter.reportDeviceStatus(getApplicationContext());
                            break;

                        case KEY_COMMANDS_RESET:
                            reporter.reportCommandStatus(command,"succeeded");
                            clearApplicationData();
                            finishAffinity();
                            break;

                        case KEY_COMMANDS_TURN_ON_TV:
                            //TODO: Implement cec commands.
                            File file1 = new File("/sys/class/cec/cmd");
                            if(file1.exists()){
                                //String turnOnShellCommand = "echo 0x40 0x04 > /sys/class/cec/cmd";
                                //ShellExecutor shellExecutorOn = new ShellExecutor(turnOnShellCommand).asSuperUser();
                                //shellExecutorOn.start();
                                String turnOnShellCommand = "input keyevent 26";
                                ShellExecutor shellExecutorOn = new ShellExecutor(turnOnShellCommand);
                                shellExecutorOn.start();
                            } else{
                                if(!playerFragment.isPlaying()){
                                    playerFragment.launchPlayer();
                                }
                            }
                            break;

                        case KEY_COMMANDS_TURN_OFF_TV:
                            //TODO: Implement cec commands.
                            File file2 = new File("/sys/class/cec/cmd");
                            if(file2.exists()) {
                                //String turnOffShellCommand = "echo 0x40 0x36 0x00 0x00 > /sys/class/cec/cmd";
                                //ShellExecutor shellExecutorOn = new ShellExecutor(turnOnShellCommand).asSuperUser();
                                //shellExecutorOn.start();
                                String turnOffShellCommand = "input keyevent 26";
                                ShellExecutor shellExecutorOff = new ShellExecutor(turnOffShellCommand);
                                shellExecutorOff.start();
                            } else{
                                if(playerFragment.isPlaying()){
                                    // if cec cmd not found we can stop playing media from player fragment
                                    //TODO: Stop player from PlayerFragment.
                                }
                            }
                            break;

                        default:
                            // do something
                            break;
                    }
                }
                File file = getExternalFilesDir("OctopusDownloads");
                if(file != null && file.list() != null && !isDownloading){
                    if(!playerFragment.isPlaying()){
                        playerFragment.launchPlayer();
                    }
                }
            }
            textView.setVisibility(View.GONE);
            qrImageView.setVisibility(View.GONE);
            widgetFrame.setVisibility(View.VISIBLE);

        }else {
            // statement for error (ekran bulunamadı...)
            // show qr-code
            isScreenRegistered = false;

            int orientation = getResources().getConfiguration().orientation;
            switch(orientation) {
                case Configuration.ORIENTATION_LANDSCAPE:
                    int height = Resources.getSystem().getDisplayMetrics().heightPixels;
                    qrImageView.getLayoutParams().height = height*2/3;
                    qrImageView.getLayoutParams().width = height*2/3;
                    qrDimention = height*2/3;
                    break;

                case Configuration.ORIENTATION_PORTRAIT:
                    int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                    qrImageView.getLayoutParams().height = width*2/3;
                    qrImageView.getLayoutParams().width = width*2/3;
                    qrDimention = width*2/3;
                    break;
            }
            try {
                QRGEncoder qrgEncoder = new QRGEncoder(screenID, null, QRGContents.Type.TEXT, qrDimention);
                qrBitmap = qrgEncoder.encodeAsBitmap();

            } catch (WriterException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                String firstMessage = getResources().getString(R.string.fullscreen_activity_register_screen_id)+System.getProperty("line.separator")+" ID: " + screenID;
                widgetFrame.setVisibility(View.GONE);
                textView.setText(firstMessage);
                qrImageView.setImageBitmap(qrBitmap);
                textView.setVisibility(View.VISIBLE);
                qrImageView.setVisibility(View.VISIBLE);
            });
        }
    }

    @Override
    public void weatherUpdated(JSONObject result) {
        Log.d(TAG, "weatherUpdated: "+result.toString());
        widgetFragment.updateWeather(new JSonParser().parseWeatherData(result));
    }

    @Override
    public void downloadComplete(boolean isAllDownloadsComplete) {
        if(isAllDownloadsComplete){
            File file = new File(Downloader.DOWNLOAD_DIR);
            if(file.list() == null){
                playerFragment.playlistUpdated(playlist);
                if(!playerFragment.isPlaying()){
                    playerFragment.launchPlayer();
                }
            }
            runOnUiThread(() -> {
                if(gifDialog.isShowing()){
                    gifDialog.dismiss();
                }
            });
            SharedPreferences sp = getSharedPreferences("OctopusData",Context.MODE_PRIVATE);
            int screenOrientation = sp.getInt("ScreenOrientation",-1);
            rotateScreen(screenOrientation);
            isDownloading = false;
            reporter.reportCommandStatus(reporter.getDownloadCommand(),Reporter.COMMAND_STATUS_SUCCEEDED);
        }else{
            //TODO: Send download progress data from Downloader. Waiting for API command structure.
            reporter.reportCommandStatus(reporter.getDownloadCommand(),Reporter.COMMAND_STATUS_INPROGRESS);
        }

    }

    @Override
    public void downloadStart() {
        isDownloading = true;
        runOnUiThread(() -> {
            if(!gifDialog.isShowing()){
                gifDialog.show();
            }
        });

    }
    int qrDimention = 0;
    @Override
    public void networkConnected() {
        if(querySchedulerService != null){
            if(!isQueryServiceRunning){
                startService(querySchedulerService);
                isQueryServiceRunning = true;
            }
        }
        Toast.makeText(activity, getResources().getString(R.string.fullscreen_activity_network_connected), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void networkDisconnected() {
        if(querySchedulerService != null){
            if(isQueryServiceRunning){
                stopService(querySchedulerService);
                isQueryServiceRunning = false;
            }
        }
        if(weatherService != null){
            if(isWeatherServiceRunning){
                stopService(weatherService);
                isWeatherServiceRunning = false;
            }
        }
        if(!isScreenRegistered){
            runOnUiThread(() -> {
                textView.setText(getResources().getString(R.string.fullscreen_activity_connect_network_textview));
                qrImageView.setImageResource(R.drawable.ic_octopus_logo);
                textView.setVisibility(View.VISIBLE);
                qrImageView.setVisibility(View.VISIBLE);

            });
        }
        Toast.makeText(activity, getResources().getString(R.string.fullscreen_activity_network_disconnected), Toast.LENGTH_SHORT).show();
    }

    boolean doubleBackTab = false;

    @Override
    public void onBackPressed() {
        if (doubleBackTab) {
            super.onBackPressed();
            finishAffinity();
            //android.os.Process.killProcess(android.os.Process.myPid());
        } else {
            Toast.makeText(this, getResources().getString(R.string.fullscreen_activity_tap_twice), Toast.LENGTH_SHORT).show();
            doubleBackTab = true;
            Handler handler = new Handler();
            handler.postDelayed(() -> doubleBackTab = false, 500);
        }
    }

    @Override
    public void onScreenLocked() {
        playerFragment.playlistWaited(true);
        Log.d(TAG, "onScreenLocked: ");
    }

    @Override
    public void onScreenAwake() {
        playerFragment.playlistWaited(false);
        Log.d(TAG, "onScreenAwake: ");
    }
}

