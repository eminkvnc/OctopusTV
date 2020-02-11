package com.tvoctopus.player.view.fullscreenactivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import com.tvoctopus.player.R;
import com.tvoctopus.player.model.CommandData;
import com.tvoctopus.player.model.DayStatus;
import com.tvoctopus.player.model.JSonParser;
import com.tvoctopus.player.services.Downloader;
import com.tvoctopus.player.services.QuerySchedulerService;
import com.tvoctopus.player.services.Reporter;
import com.tvoctopus.player.services.RestartService;
import com.tvoctopus.player.services.WeatherService;
import com.tvoctopus.player.utils.ShellExecutor;
import com.tvoctopus.player.view.GifDialog;
import com.tvoctopus.player.view.player.PlayerFragment;
import com.tvoctopus.player.view.widget.WidgetFragment;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.UUID;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import static com.tvoctopus.player.model.APIKeys.KEY_COMMANDS_SCREENSHOT;
import static com.tvoctopus.player.model.APIKeys.KEY_COMMANDS_TURN_OFF_TV;
import static com.tvoctopus.player.model.APIKeys.KEY_COMMANDS_TURN_ON_TV;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_ORIENTATION;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_OVERSCAN_BOTTOM;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_OVERSCAN_LEFT;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_OVERSCAN_RIGHT;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_OVERSCAN_TOP;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_WIFI_PASSWORD;
import static com.tvoctopus.player.model.APIKeys.KEY_PARAMS_WIFI_SSID;
import static com.tvoctopus.player.model.APIKeys.KEY_VALUES_ROTATION_0;
import static com.tvoctopus.player.model.APIKeys.KEY_VALUES_ROTATION_180;
import static com.tvoctopus.player.model.APIKeys.KEY_VALUES_ROTATION_270;
import static com.tvoctopus.player.model.APIKeys.KEY_VALUES_ROTATION_90;
import static com.tvoctopus.player.model.DataRepository.SHARED_PREF_CONFIG;
import static com.tvoctopus.player.model.DataRepository.SHARED_PREF_OCTOPUS_DATA;
import static com.tvoctopus.player.model.DataRepository.SHARED_PREF_PLAYLIST;
import static com.tvoctopus.player.model.DataRepository.SHARED_PREF_SCREEN_ID_KEY;
import static com.tvoctopus.player.services.Downloader.DOWNLOAD_DIR;
import static com.tvoctopus.player.services.QuerySchedulerService.ACTION_COMMAND_REPORT;
import static com.tvoctopus.player.services.QuerySchedulerService.ACTION_COMMAND_RESET;
import static com.tvoctopus.player.services.QuerySchedulerService.ACTION_COMMAND_SYNC;
import static com.tvoctopus.player.services.QuerySchedulerService.ACTION_SCREEN_REGISTERED;
import static com.tvoctopus.player.services.QuerySchedulerService.PARAM_SCREEN_REGISTERED;
import static com.tvoctopus.player.services.WeatherService.WEATHER_CITY_KEY;
import static com.tvoctopus.player.view.widget.WidgetFragment.POSITION_TOP;

public class FullscreenActivity extends AppCompatActivity {

    public static final String TAG = "FullscreenActivity";

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3001;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 100;

    public static final String ACTION_WAITING = "ACTION_WAITING";
    public static final String PARAM_WAITING = "PARAM_WAITING";
    public static final String ACTION_SCREEN_WAKEUP = "ACTION_SCREEN_WAKEUP";
    public static final String PARAM_SCREEN_WAKEUP = "PARAM_SCREEN_WAKEUP";

    private final Handler mHideHandler = new Handler();
    private RotateLayout mainFrame;
    private FrameLayout playerFrame;
    private FrameLayout widgetFrame;
    private FrameLayout captionFrame;
    private PlayerFragment playerFragment;
    private WidgetFragment widgetFragment;


    private TextView captionTextView;
    private TextView messageTextView;
    private ImageView qrImageView;
    private GifDialog gifDialog;
    private Activity activity;

    private BroadcastReceiver screenRegisteredReceiver;
    private BroadcastReceiver syncCommandReceiver;
    private BroadcastReceiver reportCommandReceiver;
    private BroadcastReceiver resetCommandReceiver;
    private BroadcastReceiver rebootCommandReceiver;
    private BroadcastReceiver turnOnTvCommandReceiver;
    private BroadcastReceiver turnOffTvCommandReceiver;
    private BroadcastReceiver screenShotCommandReceiver;
    private BroadcastReceiver downloadCompleteReceiver;
    private BroadcastReceiver startStopReceiver;

    private Intent querySchedulerService = null;
    private Intent weatherService = null;

    private ArrayList<CommandData> commands;
    private Bitmap qrBitmap;

    AlarmManager alarmManager;
    HashMap<Integer, PendingIntent[]> pendingIntentMap;

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
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        pendingIntentMap = new HashMap<>();
        activity = this;

        gifDialog = new GifDialog(FullscreenActivity.this);
        playerFrame = findViewById(R.id.player_frame);
        widgetFrame = findViewById(R.id.widgets_frame);
        captionFrame = findViewById(R.id.caption_frame);

        qrImageView = findViewById(R.id.qr_code_imageView);
        messageTextView = findViewById(R.id.textView);
        messageTextView.setTextColor(Color.WHITE);

        mainFrame = findViewById(R.id.main_frame);
        mainFrame.setOnTouchListener(mDelayHideTouchListener);
        mainFrame.setOnClickListener(view -> toggle());

        captionTextView = findViewById(R.id.caption_tv);
        captionTextView.setSelected(true);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        viewModel = new ViewModelProvider(this).get(FullScreenActivityViewModel.class);

        initFragments();
        initServices(viewModel.getScreenIdValue());

        viewModel.getNetworkConnected().observe(this, networkConnected -> {
            boolean screenRegistered = viewModel.getScreenRegisteredValue();
            if(networkConnected && !screenRegistered){
                generateAndShowQrCode();
                startServices();
            }
            if(!networkConnected && !screenRegistered){
                showNetworkConnectionMessage();
                stopServices();
            }
            //Screen registered cases
            if(networkConnected && screenRegistered){
                dismissMessages();
                startServices();
            }
            if(!networkConnected && screenRegistered){
                dismissMessages();
                stopServices();
            }
        });

        viewModel.getScreenRegistered().observe(this, screenRegistered -> {
            if ((!screenRegistered)) {
                showNetworkConnectionMessage();
            } else {
                dismissMessages();
            }
        });

        viewModel.getScreenId().observe(this, s -> querySchedulerService.putExtra(SHARED_PREF_SCREEN_ID_KEY, s));

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

//        viewModel.getConfig().getDayStatusMap().observe(this, hashMap -> updateAlarms(hashMap));

        viewModel.getConfig().getWidgetBarPosition().observe(this, integer -> setWidgetBarPosition(integer));

        viewModel.getCaptionData().observe(this, s -> captionTextView.setText(s));

        startStopReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean screenAwake = intent.getBooleanExtra(PARAM_SCREEN_WAKEUP,false);
                if (screenAwake) {
                    mainFrame.setVisibility(View.VISIBLE);
                } else {
                    mainFrame.setVisibility(View.GONE);
                }
            }
        };

        screenRegisteredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isRegistered = intent.getBooleanExtra(PARAM_SCREEN_REGISTERED,false);
                viewModel.getScreenRegistered().postValue(isRegistered);
            }
        };

        syncCommandReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                CommandData commandData = intent.getParcelableExtra(QuerySchedulerService.PARAM_COMMAND_DATA);
                if (commandData != null){
                    applyConfigurations(commandData);
                    if (!commandData.getPlaylist().isEmpty()) {
                        Downloader.getInstance(getApplicationContext()).startDownloads(commandData);
                        showGif();
                    }
                }
            }
        };

        reportCommandReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Reporter.getInstance(getApplicationContext()).reportDeviceStatus(viewModel.getScreenIdValue());
            }
        };

        resetCommandReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                CommandData commandData = intent.getParcelableExtra(QuerySchedulerService.PARAM_COMMAND_DATA);
                if (commandData != null){
                    Reporter.getInstance(getApplicationContext()).reportCommandStatus(commandData.getId(),"succeeded");
                    clearApplicationData();
                    finishAffinity();
                }
            }
        };

        downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean allDownloadsComplete = intent.getBooleanExtra(Downloader.PARAM_DOWNLOAD_COMPLETE_STATUS,false);
                String commandId = intent.getStringExtra(Downloader.PARAM_DOWNLOAD_COMMAND_ID);
                if(allDownloadsComplete){
                    Reporter.getInstance(getApplicationContext()).reportCommandStatus(commandId, Reporter.COMMAND_STATUS_SUCCEEDED);
                    dismissGif();
                } else {
                    Reporter.getInstance(getApplicationContext()).reportCommandStatus(commandId, Reporter.COMMAND_STATUS_INPROGRESS);
                }
            }
        };

        registerReceiver(startStopReceiver, new IntentFilter(ACTION_SCREEN_WAKEUP));
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        lbm.registerReceiver(screenRegisteredReceiver, new IntentFilter(ACTION_SCREEN_REGISTERED));
        lbm.registerReceiver(downloadCompleteReceiver, new IntentFilter(Downloader.ACTION_DOWNLOAD_FILE_COMPLETE));
        lbm.registerReceiver(syncCommandReceiver, new IntentFilter(ACTION_COMMAND_SYNC));
        lbm.registerReceiver(reportCommandReceiver, new IntentFilter(ACTION_COMMAND_REPORT));
        lbm.registerReceiver(resetCommandReceiver, new IntentFilter(ACTION_COMMAND_RESET));

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
        checkPermission(getApplicationContext(), this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerFragment.stopPlayer();
        stopService(querySchedulerService);
        stopService(weatherService);
        unregisterReceiver(startStopReceiver);
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        lbm.unregisterReceiver(screenRegisteredReceiver);
        lbm.unregisterReceiver(downloadCompleteReceiver);
        lbm.unregisterReceiver(syncCommandReceiver);
        lbm.unregisterReceiver(reportCommandReceiver);
        lbm.unregisterReceiver(resetCommandReceiver);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = new Intent(getApplicationContext(), RestartService.class);
//        startService(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent(ACTION_WAITING);
        intent.putExtra(PARAM_WAITING,true);
        sendBroadcast(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
//        Intent intent = new Intent(ACTION_WAITING);
//        intent.putExtra(PARAM_WAITING,false);
//        sendBroadcast(intent);

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
    }

    private void initServices(String screenId){
        if(querySchedulerService == null){
            //start scheduler service
            querySchedulerService = new Intent(FullscreenActivity.this, QuerySchedulerService.class);
            querySchedulerService.putExtra(SHARED_PREF_SCREEN_ID_KEY, screenId);
        }
        if(weatherService == null){
            String city = "istanbul";
            weatherService = new Intent(FullscreenActivity.this, WeatherService.class);
            weatherService.putExtra(WEATHER_CITY_KEY, city);
        }
    }

    private void initRecievers(){



    }

    private void updateAlarms(HashMap<Integer, DayStatus> hashMap){
        //TODO: Check alarms is working.
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTimeInMillis(System.currentTimeMillis());
        currentDate.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        Intent intentOn = new Intent(ACTION_SCREEN_WAKEUP);
        intentOn.putExtra(PARAM_SCREEN_WAKEUP,true);
        Intent intentOff = new Intent(ACTION_SCREEN_WAKEUP);
        intentOff.putExtra(PARAM_SCREEN_WAKEUP,false);
        PendingIntent pendingIntentOn = PendingIntent.getBroadcast(getApplicationContext(), 3230, intentOn, 0);
        PendingIntent pendingIntentOff = PendingIntent.getBroadcast(getApplicationContext(), 3231, intentOff, 0);
        if(!hashMap.isEmpty()) {
            for (int i = 1; i < 8; i++) {
                PendingIntent[] pendingIntent = pendingIntentMap.get(i);
                DayStatus dayStatus = hashMap.get(i);
                if (pendingIntent != null) {
                    if (pendingIntent[0] != null) {
                        alarmManager.cancel(pendingIntent[0]);
                    }
                    if (pendingIntent[1] != null) {
                        alarmManager.cancel(pendingIntent[1]);
                    }
                }
                pendingIntent = new PendingIntent[2];
                if (dayStatus != null) {
                    if (dayStatus.getStatus().equals(DayStatus.STATUS_ON)) {
                        int currentDay = currentDate.get(Calendar.DAY_OF_WEEK);
                        if (i == currentDay) {
                            sendBroadcast(intentOn);
                        }
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
                        calendar.set(Calendar.DAY_OF_WEEK, i - 1);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntentOn);
                        pendingIntent[0] = pendingIntentOn;
                        pendingIntentMap.put(i, pendingIntent);
                    }
                    if (dayStatus.getStatus().equals(DayStatus.STATUS_OFF)) {
                        int currentDay = currentDate.get(Calendar.DAY_OF_WEEK);
                        //TODO: Days not matching.
                        if (i == currentDay) {
                            sendBroadcast(intentOff);
                        }
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
                        calendar.set(Calendar.DAY_OF_WEEK, i - 1);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntentOff);
                        pendingIntent[1] = pendingIntentOff;
                        pendingIntentMap.put(i, pendingIntent);
                    }
                    if (dayStatus.getStatus().equals(DayStatus.STATUS_SCHEDULED)) {
                        dayStatus.fitTimes();
                        if (dayStatus.getOn().before(currentDate) && dayStatus.getOff().after(currentDate)) {
                            sendBroadcast(intentOn);
                        }else{
                            sendBroadcast(intentOff);
                        }
                        pendingIntent[0] = pendingIntentOn;
                        pendingIntent[1] = pendingIntentOff;
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, dayStatus.getOn().getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent[0]);
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, dayStatus.getOff().getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent[1]);
                        Log.d(TAG, "updateAlarms: alarmSet On: "+dayStatus.getOn().toString());
                        Log.d(TAG, "updateAlarms: alarmSet Off: "+dayStatus.getOff().toString());
                        pendingIntentMap.put(i, pendingIntent);
                    }
                }
            }
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
        getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences(SHARED_PREF_PLAYLIST, Context.MODE_PRIVATE).edit().clear().apply();
        File file = getExternalFilesDir(DOWNLOAD_DIR);
        if (file != null) {
            deleteDirectory(file);
        }
//        File file2 = getExternalFilesDir(DOWNLOAD_DIR_TEMP);
//        if (file2 != null) {
//            deleteDirectory(file2);
//        }
    }

    public void setWidgetBarPosition(int position){

        switch (position){
            case POSITION_TOP:
//                widgetFragment.setWidgetBarOrientation(LinearLayout.HORIZONTAL,widthPercentage,heightPercentage);
                ConstraintLayout.LayoutParams paramsTop = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                paramsTop.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                paramsTop.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                paramsTop.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                widgetFrame.setLayoutParams(paramsTop);
                widgetFrame.requestLayout();
                break;
            case WidgetFragment.POSITION_BOTTOM:

//                widgetFragment.setWidgetBarOrientation(LinearLayout.HORIZONTAL,widthPercentage,heightPercentage);
                ConstraintLayout.LayoutParams paramsBottom = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                paramsBottom.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                paramsBottom.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                paramsBottom.bottomToTop = R.id.caption_frame;
                widgetFrame.setLayoutParams(paramsBottom);
                widgetFrame.requestLayout();
                break;
            case WidgetFragment.POSITION_LEFT:
//                widgetFragment.setWidgetBarOrientation(LinearLayout.VERTICAL,widthPercentage,heightPercentage);
                ConstraintLayout.LayoutParams paramsLeft = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT);
                paramsLeft.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
                paramsLeft.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                paramsLeft.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                widgetFrame.setLayoutParams(paramsLeft);
                widgetFrame.requestLayout();
                break;
            case WidgetFragment.POSITION_RIGHT:

//                widgetFragment.setWidgetBarOrientation(LinearLayout.VERTICAL,widthPercentage,heightPercentage);
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
//                                if(!playerFragment.isLaunched()){
//                                    playerFragment.launchPlayer();
//                                }
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
                                if(playerFragment.isLaunched()){
                                    // if cec cmd not found we can stop playing media from player fragment
                                }
                            }
                            break;

                        case KEY_COMMANDS_SCREENSHOT:
                            //TODO: Implement sending screenshot.

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
            messageTextView.setText(firstMessage);
            qrImageView.setImageBitmap(qrBitmap);
            messageTextView.setVisibility(View.VISIBLE);
            qrImageView.setVisibility(View.VISIBLE);
        });
    }

    public void showNetworkConnectionMessage(){
        runOnUiThread(() -> {
            messageTextView.setText(getResources().getString(R.string.fullscreen_activity_connect_network_textview));
            qrImageView.setImageResource(R.drawable.ic_octopus_logo);
            messageTextView.setVisibility(View.VISIBLE);
            qrImageView.setVisibility(View.VISIBLE);
        });
    }

    public void dismissMessages(){
        messageTextView.setVisibility(View.GONE);
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

        //TODO: Implement caption fragment. Implement recyclerView endless loop.
        // Parse caption data from API.
        String captionText = (String) commandData.getMetaData().get(KEY_PARAMS_WIFI_PASSWORD);
        viewModel.getCaptionData().postValue(captionText);

        String orientationString = (String)commandData.getMetaData().get(KEY_PARAMS_ORIENTATION);
        if (orientationString != null){
            int orientation = Integer.parseInt(orientationString);
            viewModel.getConfig().getScreenOrientation().postValue(orientation);
        }

        viewModel.getConfig().getDayStatusMap().postValue(commandData.getDayStatus());

        //TODO: Handle widget bar position, percentage, width and height when API results
        // available for widgets.
        // (temporarily used wifi-SSID and overscan metadata fields. Percentage not handled yet.)
        HashMap<String, Object> hashMap = commandData.getMetaData();
        String cityKey = (String) hashMap.get(KEY_PARAMS_WIFI_SSID);
        viewModel.getConfig().getWeatherCity().postValue(cityKey);

        int widgetBarPosition = -1;
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
}

