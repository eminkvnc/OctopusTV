package com.ey08.octopus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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
import com.ey08.octopus.API.QueryScheduler;
import com.ey08.octopus.API.Reporter;
import com.github.rongi.rotate_layout.layout.RotateLayout;

import org.json.JSONObject;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import static com.ey08.octopus.API.APIKeys.*;

public class FullscreenActivity extends AppCompatActivity implements DownloadCompleteListener, QueryListener, NetworkStateListener {

    public static final String TAG = "FullscreenActivity";

    private static final int PERMISSIONS_REQUEST_WRITE_EXTARNAL_STORAGE = 3001;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 100;

    private static final String SHARED_PREF_OCTOPUS_DATA = "OctopusData";
    private static final String SHARED_PREF_PLAYLIST = "Playlist";

    private final Handler mHideHandler = new Handler();
    private RotateLayout mainFrame;
    private FrameLayout playerFrame;
    private FrameLayout widgetFrame;
    private PlayerFragment playerFragment;
    private WidgetFragment widgetFragment;

    private TextView textView;
    private GifDialog gifDialog;
    private Activity activity;

    private NetworkStateBroadcastReciever reciever;

    private QueryScheduler scheduler;
    private ArrayList<CommandData> commands;
    private Playlist playlist;
    private Downloader downloader;
    private Reporter reporter;
    private boolean isDownloading = false;

    private String screenID;

    private boolean isScreenLogOn = false;

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
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

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
        reciever = new NetworkStateBroadcastReciever(this);
        registerReceiver(reciever,intentFilter);

        mVisible = true;
        commands = new ArrayList<>();
        activity = this;

        gifDialog = new GifDialog(FullscreenActivity.this);
        //gifDialog.show();

        playerFrame = findViewById(R.id.player_frame);
        widgetFrame = findViewById(R.id.widgets_frame);
        textView = findViewById(R.id.textView);
        textView.setTextColor(Color.WHITE);

        mainFrame = findViewById(R.id.main_frame);
        mainFrame.setOnTouchListener(mDelayHideTouchListener);
        mainFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        initFragments();

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
        Intent intent = new Intent(getApplicationContext(),RestartService.class);
        startService(intent);
        unregisterReceiver(reciever);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTARNAL_STORAGE);
        } else {
            initQueryScheduler();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTARNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initQueryScheduler();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.fullscreen_activity_storage_permission_toast),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initFragments(){
        playerFragment = new PlayerFragment();
        playerFragment.setContext(activity);
        widgetFragment = new WidgetFragment();
        setFragment(playerFrame, playerFragment);
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
            scheduler = new QueryScheduler(url, this);
            scheduler.startSchduler();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    // reverse clockwise
    private void rotateScreen(int degree){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                    default:
                        mainFrame.setAngle(0);
                        break;
                }
                mainFrame.requestLayout();
            }
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
        getSharedPreferences(SHARED_PREF_OCTOPUS_DATA, Context.MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences(SHARED_PREF_PLAYLIST, Context.MODE_PRIVATE).edit().clear().apply();
        File file = getExternalFilesDir("OctopusDownloads");
        if (file != null) {
            deleteDirectory(file);
        }
    }

    @Override
    public void onNewQuery(JSONObject result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isScreenLogOn){
                    textView.setText(result.toString());
                }
            }
        });
        Log.d("QueryScheduler", "onNewQuery: "+result);
        //PARSE COMMANDS HERE
        JSonParser parser = new JSonParser(result);
        commands = parser.parseCommands();

        //process commands
        if(commands != null){
            if(commands.isEmpty()){
                File file = getExternalFilesDir("OctopusDownloads");
                if(file.list() != null){
                    if(!playerFragment.isPlaying()){
                        playerFragment.launchPleyer();
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
                                    playerFragment.launchPleyer();
                                }
                            }
                            break;

                        case KEY_COMMANDS_REPORT:
                            //process report command

                            // !! REPORT DEVICE STATUS TO SERVER !!
                            break;

                        case KEY_COMMANDS_RESET:
                            /*clearApplicationData();
                            reporter.reportCommandStatus(command,"succeeded");
                            finishAffinity();*/

                            break;

                        case KEY_COMMANDS_TURN_ON_TV:
                            String turnOnShellCommand = "echo 0x40 0x04 > /sys/class/cec/cmd";
                            ShellExecuter shellExecuterOn = new ShellExecuter(turnOnShellCommand);
                            shellExecuterOn.start();

                            break;

                        case KEY_COMMANDS_TURN_OFF_TV:
                            String turnOffShellCommand = "echo 0x40 0x36 0x00 0x00 > /sys/class/cec/cmd";
                            ShellExecuter shellExecuterOff = new ShellExecuter(turnOffShellCommand);
                            shellExecuterOff.start();
                            break;

                        default:
                            // do something
                            break;
                    }
                    // !! REPORT COMMAND EXECUTION STATUS TO SERVER !!
                }
                File file = getExternalFilesDir("OctopusDownloads");
                if(file.list() != null && !isDownloading){
                    if(!playerFragment.isPlaying()){
                        playerFragment.launchPleyer();
                    }
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setVisibility(View.GONE);
                }
            });
        }else {
            // statement for error (ekran bulunamadı...)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String firstMessage = getResources().getString(R.string.fullscreen_activity_register_screen_id)+System.getProperty("line.separator")+" ID: " + screenID;
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(firstMessage);
                }
            });
        }
    }



    @Override
    public void downloadComplete(boolean isAllDownloadsComplete) {
        if(isAllDownloadsComplete){
            File file = new File(Downloader.DOWNLOAD_DIR);
            if(file.list() == null){
                playerFragment.playlistUpdated(playlist);
                if(!playerFragment.isPlaying()){
                    playerFragment.launchPleyer();
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(gifDialog.isShowing()){
                        gifDialog.dismiss();
                    }
                }
            });
            SharedPreferences sp = getSharedPreferences("OctopusData",Context.MODE_PRIVATE);
            int screenOrientation = sp.getInt("ScreenOrientation",-1);
            rotateScreen(screenOrientation);
            isDownloading = false;
        }else{
            // !! REPORT DOWNLOAD STATUS TO SERVER !!
        }

    }

    @Override
    public void downloadStart() {
        isDownloading = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!gifDialog.isShowing()){
                    gifDialog.show();
                }
            }
        });
    }

    @Override
    public void networkConnected() {
        if(scheduler != null){
            if(!scheduler.isStarted()){
                scheduler.startSchduler();
            }
        }
        Toast.makeText(activity, getResources().getString(R.string.fullscreen_activity_network_connected), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void networkDisconnected() {
        if(scheduler != null){
            if(scheduler.isStarted()){
                scheduler.stopScheduler();
            }
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
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackTab = false;
                }
            }, 500);
        }
    }
}

