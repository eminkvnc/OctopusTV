package com.tvoctopus.player.view.fullscreenactivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.rongi.rotate_layout.layout.RotateLayout;
import com.google.zxing.WriterException;
import com.tvoctopus.player.API.APIKeys;
import com.tvoctopus.player.API.JSonParser;
import com.tvoctopus.player.GifDialog;
import com.tvoctopus.player.R;
import com.tvoctopus.player.ScreenListener;
import com.tvoctopus.player.ScreenReciever;
import com.tvoctopus.player.ShellExecutor;
import com.tvoctopus.player.WeatherBroadcastReceiver;
import com.tvoctopus.player.WeatherListener;
import com.tvoctopus.player.model.CommandData;
import com.tvoctopus.player.model.Playlist;
import com.tvoctopus.player.model.StatusFlags;
import com.tvoctopus.player.services.Downloader;
import com.tvoctopus.player.services.QuerySchedulerService;
import com.tvoctopus.player.services.Reporter;
import com.tvoctopus.player.services.RestartService;
import com.tvoctopus.player.services.WeatherService;
import com.tvoctopus.player.view.player.PlayerFragment;
import com.tvoctopus.player.view.widget.WidgetFragment;

import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static com.tvoctopus.player.API.APIKeys.*;
import static com.tvoctopus.player.model.DataRepository.SHARED_PREF_OCTOPUS_DATA;
import static com.tvoctopus.player.model.DataRepository.SHARED_PREF_PLAYLIST;
import static com.tvoctopus.player.services.Downloader.PARAM_DOWNLOAD_COMPLETE_PLAYLIST;
import static com.tvoctopus.player.services.QuerySchedulerService.ACTION_COMMAND_SYNC;
import static com.tvoctopus.player.services.QuerySchedulerService.ACTION_SCREEN_REGISTERED;
import static com.tvoctopus.player.services.QuerySchedulerService.PARAM_SCREEN_REGISTERED;
import static com.tvoctopus.player.services.WeatherService.ACTION_WEATHER_QUERY;
import static com.tvoctopus.player.services.WeatherService.WEATHER_CITY_KEY;
import static com.tvoctopus.player.view.widget.WidgetFragment.POSITION_TOP;

public class FullscreenActivity extends AppCompatActivity implements ScreenListener, WeatherListener {

    public static final String TAG = "FullscreenActivity";

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3001;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 100;

    private final Handler mHideHandler = new Handler();
    private RotateLayout mainFrame;
    private FrameLayout playerFrame;
    private FrameLayout widgetFrame;
    private PlayerFragment playerFragment;
    private WidgetFragment widgetFragment;

    private TextView textView;
    private ImageView qrImageView;
    private GifDialog gifDialog;
    private Activity activity;

    private ScreenReciever screenReciever;
    private WeatherBroadcastReceiver weatherBroadcastReceiver;

    private BroadcastReceiver screenRegisteredReceiver;
    private BroadcastReceiver syncCommandReceiver;
    private BroadcastReceiver reportCommandReceiver;
    private BroadcastReceiver resetCommandReceiver;
    private BroadcastReceiver rebootCommandReceiver;
    private BroadcastReceiver turnOnTvCommandReceiver;
    private BroadcastReceiver turnOffTvCommandReceiver;
    private BroadcastReceiver screenShotCommandReceiver;
    private BroadcastReceiver networkStateBroadcastReceiver;
    private BroadcastReceiver downloadCompleteReceiver;

    private Intent querySchedulerService = null;
    private Intent weatherService = null;

    private ArrayList<CommandData> commands;
    private Reporter reporter;
    private Bitmap qrBitmap;

    private FullScreenActivityViewModel viewModel;


    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
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

        mVisible = true;
        commands = new ArrayList<>();
        activity = this;

        gifDialog = new GifDialog(FullscreenActivity.this);
        playerFrame = findViewById(R.id.player_frame);
        widgetFrame = findViewById(R.id.widgets_frame);
        qrImageView = findViewById(R.id.qr_code_imageView);
        textView = findViewById(R.id.textView);
        textView.setTextColor(Color.WHITE);

        mainFrame = findViewById(R.id.main_frame);
        mainFrame.setOnTouchListener(mDelayHideTouchListener);
        mainFrame.setOnClickListener(view -> toggle());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        viewModel = new ViewModelProvider(this).get(FullScreenActivityViewModel.class);

        initFragments();
        initServices(viewModel.getScreenIdValue());

        viewModel.getStatusFlags().observe(this, statusFlags -> {
            //Screen not registered cases
            Log.d(TAG, "onChanged: statusFlags: screenRegistered:"+statusFlags.isScreenRegistered()+" networkConnected: "+statusFlags.isNetworkConnected());
            if(statusFlags.isNetworkConnected() && !statusFlags.isScreenRegistered()){
                generateAndShowQrCode();
                startServices();
            }
            if(!statusFlags.isNetworkConnected() && !statusFlags.isScreenRegistered()){
                showNetworkConnectionMessage();
                stopServices();
            }
            //Screen registered cases
            if(statusFlags.isNetworkConnected() && statusFlags.isScreenRegistered()){
                dismissMessages();
                startServices();

                if (playerFragment != null && !playerFragment.isPlaying()) {
                    playerFragment.launchPlayer();
                }

            }
            if(!statusFlags.isNetworkConnected() && statusFlags.isScreenRegistered()){
                dismissMessages();
                stopServices();

                if (playerFragment != null && !playerFragment.isPlaying()) {
                    playerFragment.launchPlayer();
                }
            }
        });

        viewModel.getPlaylist().observe(this, mediaData -> {
            //TODO: Send Broadcast with playlistUpdated flag.
            //  Receive data in PlayerFragment.

        });

        viewModel.getConfig().getScreenOrientation().observe(this, this::rotateScreen);

        viewModel.getConfig().getWidgetBarEnabled().observe(this, aBoolean -> {
            if(aBoolean){
                widgetFrame.setVisibility(View.VISIBLE);
            } else {
                widgetFrame.setVisibility(View.GONE);
            }
        });

        viewModel.getConfig().getWeatherCity().observe(this, s -> {
            weatherService.putExtra(WEATHER_CITY_KEY,s);
            startService(weatherService);
        });

        viewModel.getConfig().getWeatherEnabled().observe(this, aBoolean -> widgetFragment.setEnableWeather(aBoolean));

        viewModel.getConfig().getRssEnabled().observe(this, aBoolean -> widgetFragment.setEnableRss(aBoolean));

        viewModel.getConfig().getWidgetBarPosition().observe(this, integer -> setWidgetBarPosition(integer, 15, 15));

        IntentFilter intentFilter2 = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter2.addAction(Intent.ACTION_SCREEN_OFF);
        screenReciever = new ScreenReciever(this);
        registerReceiver(screenReciever, intentFilter2);

        IntentFilter intentFilter4 = new IntentFilter(ACTION_WEATHER_QUERY);
        weatherBroadcastReceiver = new WeatherBroadcastReceiver(this);
        registerReceiver(weatherBroadcastReceiver,intentFilter4);

        screenRegisteredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isRegistered = intent.getBooleanExtra(PARAM_SCREEN_REGISTERED,false);
                StatusFlags statusFlags = viewModel.getStatusFlags().getValue();
                if (statusFlags != null) {
                    statusFlags.setScreenRegistered(isRegistered);
                    viewModel.getStatusFlags().postValue(statusFlags);
                }
            }
        };

        syncCommandReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                CommandData commandData = intent.getParcelableExtra(QuerySchedulerService.PARAM_COMMAND_DATA);
                if (commandData != null){
                    applyConfigurations(commandData);
                    //TODO: Trigger configurationLiveData.
                    if (!commandData.getPlaylist().isEmpty()) {
                        Downloader.getInstance(getApplicationContext()).startDownloads(commandData.getPlaylist());
                        reporter.setDownloadCommand(commandData);
                        showGif();
                    }
                }
            }
        };

        downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean allDownloadsComplete = intent.getBooleanExtra(Downloader.PARAM_DOWNLOAD_COMPLETE_STATUS,false);
                if(allDownloadsComplete){
                    reporter.reportCommandStatus(reporter.getDownloadCommand(),Reporter.COMMAND_STATUS_SUCCEEDED);
                    Playlist playlist = intent.getParcelableExtra(PARAM_DOWNLOAD_COMPLETE_PLAYLIST);
                    Log.d(TAG, "onReceive: playlistSize: "+playlist.size());
                    viewModel.getPlaylist().postValue(playlist);
                    dismissGif();
                } else {
                    reporter.reportCommandStatus(reporter.getDownloadCommand(),Reporter.COMMAND_STATUS_INPROGRESS);
                }
            }
        };
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
        int screenOrientation = viewModel.getScreenOrientationValue();
        if(screenOrientation != -1){
            viewModel.getConfig().getScreenOrientation().postValue(screenOrientation);
        }
        reporter = new Reporter();
        checkPermission(getApplicationContext(), this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerFragment.stopPlayer();
        stopService(querySchedulerService);
        stopService(weatherService);
        unregisterReceiver(screenReciever);
        unregisterReceiver(weatherBroadcastReceiver);

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        lbm.unregisterReceiver(screenRegisteredReceiver);
        lbm.unregisterReceiver(downloadCompleteReceiver);
        lbm.unregisterReceiver(syncCommandReceiver);
//        lbm.unregisterReceiver(screenRegisteredReceiver);
//        lbm.unregisterReceiver(screenRegisteredReceiver);
//        lbm.unregisterReceiver(screenRegisteredReceiver);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = new Intent(getApplicationContext(), RestartService.class);
//        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        lbm.registerReceiver(screenRegisteredReceiver, new IntentFilter(ACTION_SCREEN_REGISTERED));
        lbm.registerReceiver(downloadCompleteReceiver, new IntentFilter(Downloader.ACTION_DOWNLOAD_FILE_COMPLETE));
        lbm.registerReceiver(syncCommandReceiver, new IntentFilter(ACTION_COMMAND_SYNC));
//        lbm.registerReceiver(screenRegisteredReceiver, new IntentFilter(ACTION_SCREEN_REGISTERED));
//        lbm.registerReceiver(screenRegisteredReceiver, new IntentFilter(ACTION_SCREEN_REGISTERED));
//        lbm.registerReceiver(screenRegisteredReceiver, new IntentFilter(ACTION_SCREEN_REGISTERED));
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if (!(grantResults.length > 0)
                    && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
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
        handler.postDelayed(() -> {
            setWidgetBarPosition(WidgetFragment.POSITION_RIGHT,15,15);
            widgetFragment.setEnableRss(false);
            widgetFragment.setEnableWeather(true);
        },300);
    }

    private void initServices(String screenId){
        if(querySchedulerService == null){
            try {
            // Append screenID to url
            URL url = new URL("http://panel.tvoctopus.net/api/screen/"+screenId);
            //start scheduler service
            querySchedulerService = new Intent(FullscreenActivity.this, QuerySchedulerService.class);
            querySchedulerService.putExtra("URL",url.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        if(weatherService == null){
            String city = "istanbul";
            weatherService = new Intent(FullscreenActivity.this, WeatherService.class);
            weatherService.putExtra(WEATHER_CITY_KEY, city);
        }
    }

    // reverse clockwise
    private void rotateScreen(int degree){
        runOnUiThread(() -> {
            switch(degree){
                case KEY_VALUES_ROTATION_0:
                    mainFrame.setAngle(0);
                    break;
                case KEY_VALUES_ROTATION_90:
                    mainFrame.setAngle(270);
                    break;
                case KEY_VALUES_ROTATION_180:
                    mainFrame.setAngle(180);
                    break;
                case KEY_VALUES_ROTATION_270:
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
            case POSITION_TOP:
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

    public void onNewQuery(JSONObject result) {

        Log.d("QuerySchedulerService", "onNewQuery: "+result);
        commands = new JSonParser().parseCommands(result);
                for(CommandData command : commands){
                    switch (command.getCommand()){

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

                                }
                            }
                            break;

                        case KEY_COMMANDS_SCREENSHOT:
                            //TODO: Implement sending screenshot.

                            break;

                        default:
                            // do something
                            break;
                    }
                }
    }

    public void generateAndShowQrCode(){
        String id;
        if(viewModel.getScreenIdValue() == null){
            id = generateRandomID();
            viewModel.getScreenId().postValue(id);
        } else {
            id = viewModel.getScreenIdValue();
        }
        // Init services with generated screenId.
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
            QRGEncoder qrgEncoder = new QRGEncoder(id, null, QRGContents.Type.TEXT, qrDimention);
            qrBitmap = qrgEncoder.encodeAsBitmap();
            Log.d(TAG, "onNewQuery: screen_id = "+id);

        } catch (WriterException e) {
            e.printStackTrace();
        }
        runOnUiThread(() -> {
            String firstMessage = getResources().getString(R.string.fullscreen_activity_register_screen_id)+System.getProperty("line.separator")+" ID: " + id;
            widgetFrame.setVisibility(View.GONE);
            textView.setText(firstMessage);
            qrImageView.setImageBitmap(qrBitmap);
            textView.setVisibility(View.VISIBLE);
            qrImageView.setVisibility(View.VISIBLE);
        });
    }

    public void showNetworkConnectionMessage(){
        runOnUiThread(() -> {
            textView.setText(getResources().getString(R.string.fullscreen_activity_connect_network_textview));
            qrImageView.setImageResource(R.drawable.ic_octopus_logo);
            textView.setVisibility(View.VISIBLE);
            qrImageView.setVisibility(View.VISIBLE);
        });
    }

    public void dismissMessages(){
        textView.setVisibility(View.GONE);
        qrImageView.setVisibility(View.GONE);
    }

    private boolean checkServices(){
        return querySchedulerService != null && weatherService != null;
    }

    private void stopServices(){
        if(checkServices()){
            Log.d(TAG, "stopServices");
            stopService(querySchedulerService);
            stopService(weatherService);
            Downloader.getInstance(getApplicationContext()).stop();
        }

    }

    private void startServices(){
        if(checkServices()){
            Log.d(TAG, "startServices");
            startService(querySchedulerService);
            startService(weatherService);
            Downloader.getInstance(getApplicationContext());
        }
    }

    private void showGif(){
        runOnUiThread(() -> {
            if(!gifDialog.isShowing()){
                gifDialog.show();
            }
        });
    }

    private void dismissGif(){
        runOnUiThread(() -> {
            if(gifDialog.isShowing()){
                gifDialog.dismiss();
            }
        });
    }

    private void applyConfigurations(CommandData commandData){

    //TODO: Aplly Configurations in configurationLiveData. Trigger LiveData for changes.
        String orientationString = (String)commandData.getMetaData().get(APIKeys.KEY_PARAMS_ORIENTATION);
        if (orientationString != null){
            int orientation = Integer.parseInt(orientationString);
            viewModel.getConfig().getScreenOrientation().postValue(orientation);
        }

        //TODO: Handle widget bar position, width and height when API results
        // available for widgets. (temporarily used wifi-SSID and overscan metadata fields.)
        //  Cache widget data for offline usage.
        HashMap<String, Object> hashMap = commandData.getMetaData();
        String cityKey = (String) hashMap.get(APIKeys.KEY_PARAMS_WIFI_SSID);
        viewModel.getConfig().getWeatherCity().postValue(cityKey);

        int widgetBarPosition = -1;
        //TODO: Get widget bar percentages from API and post value.
        if(hashMap.get(KEY_PARAMS_OVERSCAN_TOP).equals("1")){
            widgetBarPosition = WidgetFragment.POSITION_TOP;
        }
        if(hashMap.get(KEY_PARAMS_OVERSCAN_BOTTOM).equals("1")){
            widgetBarPosition = WidgetFragment.POSITION_BOTTOM;
        }
        if(hashMap.get(KEY_PARAMS_OVERSCAN_LEFT).equals("1")){
            widgetBarPosition = WidgetFragment.POSITION_LEFT;
        }
        if(hashMap.get(KEY_PARAMS_OVERSCAN_RIGHT).equals("1")){
            widgetBarPosition = WidgetFragment.POSITION_RIGHT;
        }
        viewModel.getConfig().getWidgetBarPosition().postValue(widgetBarPosition);
        if(widgetBarPosition != -1){
            viewModel.getConfig().getWeatherEnabled().postValue(true);
            viewModel.getConfig().getWidgetBarEnabled().postValue(true);
        }
        else{
            viewModel.getConfig().getWeatherEnabled().postValue(false);
            viewModel.getConfig().getWidgetBarEnabled().postValue(false);
        }
        //TODO: Get RSS data from API and post value.
        viewModel.getConfig().getRssEnabled().postValue(false);
    }


    //TODO: Implement weather receiver and LiveData.
    @Override
    public void weatherUpdated(JSONObject result) {
        Log.d(TAG, "weatherUpdated: "+result.toString());
        widgetFragment.updateWeather(new JSonParser().parseWeatherData(result));
    }

    int qrDimention = 0;
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

