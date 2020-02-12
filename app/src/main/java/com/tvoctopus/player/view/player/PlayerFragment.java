package com.tvoctopus.player.view.player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.tvoctopus.player.R;
import com.tvoctopus.player.model.MediaData;
import com.tvoctopus.player.model.Playlist;
import com.tvoctopus.player.services.Downloader;
import com.tvoctopus.player.view.fullscreenactivity.FullscreenActivity;

import java.io.File;

import static com.tvoctopus.player.services.Downloader.PARAM_DOWNLOAD_COMPLETE_PLAYLIST;
import static com.tvoctopus.player.view.fullscreenactivity.FullscreenActivity.ACTION_SCREEN_WAKEUP;
import static com.tvoctopus.player.view.fullscreenactivity.FullscreenActivity.PARAM_SCREEN_WAKEUP;

public class PlayerFragment extends Fragment {

    public static final String TAG = "PlayerFragment";

    private SimpleExoPlayer player;

    private PlayerView playerView;
    private ImageView imageView;

    private DataSource.Factory dataSourceFactory;
    private File downloadDir;

    private Playlist playlist;
    private boolean loopInterrupt = false;
    private boolean isPlaylistUpdated = false;
    private boolean isNetworkConnected = false;

    private boolean isLaunched = false;
    public boolean screenAwake = true;
    private Activity context;
    private MediaData currentMedia;
    private MediaData previousMedia;

    private Handler playlistLooperHandler = null;
    private Runnable playlistLooperRunnable = null;

    private PlayerFragmentViewModel playerFragmentViewModel;

    private BroadcastReceiver waitReceiver;
    private BroadcastReceiver updateReceiver;
    private BroadcastReceiver startStopReceiver;

    public PlayerFragment() {

    }

    @Override
    public void onPause() {
        super.onPause();
        //context.finishAffinity();
        playlistWaited(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.unregisterReceiver(waitReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(updateReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(startStopReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        playlistWaited(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        playerFragmentViewModel = new ViewModelProvider(this).get(PlayerFragmentViewModel.class);

        playlist = playerFragmentViewModel.getLastPlaylist();

        playerFragmentViewModel.getNetworkConnected().observe(getViewLifecycleOwner(), aBoolean -> {
            isNetworkConnected = aBoolean;
        });

        playerFragmentViewModel.getPlaylist().observe(getViewLifecycleOwner(), p -> {
            boolean screenRegistered = playerFragmentViewModel.getScreenRegisteredValue();
            if(!isLaunched && p != null && !p.isEmpty() && screenRegistered){
                launchPlayer();
            }
        });

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean allDownloadsComplete = intent.getBooleanExtra(Downloader.PARAM_DOWNLOAD_COMPLETE_STATUS,false);
                if(allDownloadsComplete){
                    Playlist p = intent.getParcelableExtra(PARAM_DOWNLOAD_COMPLETE_PLAYLIST);
                    isPlaylistUpdated = true;
                    playlist = p;
                    playerFragmentViewModel.getPlaylist().postValue(p);
                }
            }
        };

        waitReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    // do whatever you need to do here
                    playlistWaited(true);
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    // and do whatever you need to do here
                    playlistWaited(false);
                }
                if(intent.getAction().equals(FullscreenActivity.ACTION_WAITING)){
                    playlistWaited(intent.getBooleanExtra(FullscreenActivity.PARAM_WAITING,true));
                }
            }
        };

        startStopReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                screenAwake = intent.getBooleanExtra(PARAM_SCREEN_WAKEUP,false);

                if (screenAwake) {
                    playlistWaited(false);
                } else {
                    playlistWaited(true);
                }
            }
        };

        IntentFilter intentFilter2 = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter2.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter2.addAction(FullscreenActivity.ACTION_WAITING);
        context.registerReceiver(waitReceiver, intentFilter2);
        LocalBroadcastManager.getInstance(context).registerReceiver(updateReceiver, new IntentFilter(Downloader.ACTION_DOWNLOAD_FILE_COMPLETE));
        LocalBroadcastManager.getInstance(context).registerReceiver(startStopReceiver, new IntentFilter(ACTION_SCREEN_WAKEUP));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =inflater.inflate(R.layout.fragment_player, container, false);
        playerView = v.findViewById(R.id.player_view);
        imageView = v.findViewById(R.id.player_image_view);

        imageView.setImageResource(android.R.color.black);
        imageView.setVisibility(View.GONE);

        playerView.setUseController(false);
        playerView.setVisibility(View.GONE);

        player = ExoPlayerFactory.newSimpleInstance(getContext());
        dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "octopus"));
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        playerView.setPlayer(player);
        downloadDir = context.getExternalFilesDir(Downloader.DOWNLOAD_DIR);

        return v;
    }

    public void launchPlayer(){
        if(playlist != null && !playlist.isEmpty() && downloadDir != null && downloadDir.list() != null){
            isLaunched = true;
            loopPlaylist(0L);
        }
    }

    public void stopPlayer(){
        player.release();
        loopInterrupt = true;
    }

    private void loopPlaylist(Long delay){

        if(isPlaylistUpdated){
            replaceMediaFiles();
            isPlaylistUpdated = false;
            playlist.resetIndex();
            previousMedia = playlist.get(0);
            currentMedia = playlist.getNext();
            if(playlistLooperHandler != null){
                playlistLooperHandler.removeCallbacksAndMessages(null);
            }
            loopPlaylist(Long.valueOf(currentMedia.getTime()));
        }else{
            if(currentMedia != null){
                previousMedia = currentMedia;
            }
            currentMedia = playlist.getNext();

        playlistStartTime = System.currentTimeMillis();

        playlistLooperRunnable = () -> {
            currentMedia.setStartTime(System.currentTimeMillis());
            if(previousMedia != null){
                previousMedia.setStopTime(System.currentTimeMillis());
                if(previousMedia.getStartTime() != -1 && previousMedia.getStopTime() != -1){
                    playerFragmentViewModel.reportMediaData(previousMedia, isNetworkConnected);
                }
            }
            if(currentMedia.getType().equals(MediaData.MEDIA_TYPE_VIDEO)){
                context.runOnUiThread(() -> {
                    //player.next();
                    playerView.setVisibility(View.VISIBLE);
                    imageView.setImageResource(android.R.color.black);
                    imageView.setVisibility(View.GONE);
                    player.prepare(playlist.getMediaSource(dataSourceFactory, downloadDir));
                    player.setPlayWhenReady(true);
                });
            }
            if(currentMedia.getType().equals(MediaData.MEDIA_TYPE_JPG)){
                context.runOnUiThread(() -> {
                    playerView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    player.setPlayWhenReady(false);
                    Picasso.get()
                            .load("file://"+downloadDir+"/"+currentMedia.getName())
                            .placeholder(android.R.color.black)
                            .into(imageView);
                });
            }
            if(!loopInterrupt){
                loopPlaylist(Long.valueOf(currentMedia.getTime()));
            }
        };
        context.runOnUiThread(() -> {
            playlistLooperHandler = new Handler();
            playlistLooperHandler.postDelayed(playlistLooperRunnable,delay);
        });
        }
    }

    // Remove media from storage if deleted on API
    private void replaceMediaFiles(){
        String[] mediaFiles = downloadDir.list();
        if (mediaFiles != null) {
            for (String mediaFile : mediaFiles) {
                File file = new File(downloadDir, mediaFile);
                String nameWithoutPrefix = mediaFile.substring(Downloader.DOWNLOAD_FILE_PREFIX.length());
                if (playlist.getMediaNames().contains(nameWithoutPrefix)) {
                    file.renameTo(new File(downloadDir, nameWithoutPrefix));
                }
                else {
                    file.delete();
                }
            }
        }
    }

    private void removeTempFiles(){
        String[] mediaFiles = downloadDir.list();
        if (mediaFiles != null) {
            for (String mediaFile : mediaFiles) {
                if (mediaFile.startsWith(Downloader.DOWNLOAD_FILE_PREFIX)) {
                    File file = new File(downloadDir, mediaFile);
                    file.delete();
                }
            }
        }
    }

    public boolean isLaunched() {
        return isLaunched;
    }

    @Nullable
    @Override
    public Activity getContext() {
        return context;
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    private Long playlistStopTime;
    private Long playlistStartTime;
    private Long playlistRemainingTime;

    //TODO: Report mediaData to server when media paused and played again.
    public void playlistWaited(boolean isWaiting) {
        if(isLaunched){
            if(isWaiting){
                if(previousMedia.getType().equals(MediaData.MEDIA_TYPE_VIDEO)){
                    player.setPlayWhenReady(false);
                }
                playlistLooperHandler.removeCallbacks(playlistLooperRunnable);
                playlistStopTime = System.currentTimeMillis();
                playlistRemainingTime = Long.valueOf(previousMedia.getTime()) - (playlistStopTime - playlistStartTime);
            }else{
                if(screenAwake){
                    if(previousMedia.getType().equals(MediaData.MEDIA_TYPE_VIDEO)){
                        player.setPlayWhenReady(true);
                    }
                    if(playlistLooperHandler != null && playlistLooperRunnable != null && playlistRemainingTime != null){
                        playlistLooperHandler.postDelayed(playlistLooperRunnable, playlistRemainingTime);
                    }
                }
            }
        }
    }

}
