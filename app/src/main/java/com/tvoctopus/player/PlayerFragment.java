package com.tvoctopus.player;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tvoctopus.player.API.MediaData;
import com.tvoctopus.player.API.Playlist;
import com.tvoctopus.player.API.PlaylistListener;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class PlayerFragment extends Fragment implements PlaylistListener, NetworkStateListener{

    public static final String TAG = "PlayerFragment";

    private SimpleExoPlayer player;

    private PlayerView playerView;
    private ImageView imageView;

    private DataSource.Factory dataSourceFactory;
    private File downloadDir;

    private Playlist playlist;
    private Playlist updatedPlaylist;
    private boolean loopInterrupt = false;
    private boolean isPlaylistUpdated = false;
    private boolean isNetworkConnected = false;

    private boolean isPlaying = false;
    private Activity context;
    private MediaData currentMedia;
    private MediaData previousMedia;

    private Handler playlistLooperHandler = null;
    private Runnable playlistLooperRunnable = null;

    public PlayerFragment() {

    }

    @Override
    public void onPause() {
        super.onPause();
        if(isPlaying){
            playlistWaited(true);
        }
        //context.finishAffinity();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isPlaying){
            playlistWaited(false);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        downloadDir = context.getExternalFilesDir("OctopusDownloads");
        updatedPlaylist = getLastPlaylist();
        playlist = getLastPlaylist();
        return v;
    }

    void launchPlayer(){
        if((!updatedPlaylist.isEmpty() || !playlist.isEmpty()) && downloadDir.list() != null){
            isPlaying = true;
            loopPlaylist(0L);
        }
    }

    void stopPlayer(){
        player.release();
        loopInterrupt = true;
    }

    private void loopPlaylist(Long delay){

        if(isPlaylistUpdated){
            this.playlist = updatedPlaylist;
            // Remove media from storage if deleted on API
            String[] mediaFiles = downloadDir.list();
            if (mediaFiles != null) {
                for (String mediaFile : mediaFiles) {
                    if (!playlist.getMediaNames().contains(mediaFile)) {
                        File existingMediaFile = new File(downloadDir, mediaFile);
                        existingMediaFile.delete();
                    }
                }
            }
            isPlaylistUpdated = false;
            currentMedia = playlist.get(0);
            previousMedia = playlist.get(playlist.size()-1);
        }else{
            if(currentMedia != null){
                previousMedia = currentMedia;
            }
            currentMedia = playlist.getNext();
        }
        playlistStartTime = System.currentTimeMillis();


        playlistLooperRunnable = () -> {
            currentMedia.setStartTime(System.currentTimeMillis());
            if(previousMedia != null){
                previousMedia.setStopTime(System.currentTimeMillis());
                if(previousMedia.getStartTime() != -1 && previousMedia.getStopTime() != -1){
                    FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
                    if(isNetworkConnected){
                        firebaseHelper.addMediaData(previousMedia);
                    } else{
                        queueReport(previousMedia);
                    }
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

        // TODO: Report media data to server.
    }

    @Override
    public void playlistUpdated(Playlist playlist){
        // Update SharedPreferences when playlist updated
        SharedPreferences sp = context.getSharedPreferences("Playlist", Context.MODE_PRIVATE);
        sp.edit().clear().apply();
        int mediaIndex = 0;
        for (MediaData media : playlist){
            String playlistConcat;
            playlistConcat = media.getName() + "%%%" + media.getType() + "%%%" + media.getMd5() + "%%%" + media.getTime();
            sp.edit().putString(String.valueOf(mediaIndex), playlistConcat).apply();
            mediaIndex++;
        }
        updatedPlaylist = playlist;
        this.playlist = getLastPlaylist();

        isPlaylistUpdated = true;
    }

    boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    @Nullable
    @Override
    public Activity getContext() {
        return context;
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    private Playlist getLastPlaylist(){
        SharedPreferences sp = context.getSharedPreferences("Playlist", Context.MODE_PRIVATE);
        Playlist playlist = new Playlist();
        try {
        HashMap<String, String> playlistMap = (HashMap<String, String>) sp.getAll();
        for(int i = 0 ; i < playlistMap.size() ; i++){
            String[] mediaData = playlistMap.get(String.valueOf(i)).split("%%%");
            MediaData media = new MediaData(
                    mediaData[0],
                    mediaData[1],
                    mediaData[2],
                    mediaData[3]);
            playlist.add(media);
        }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return playlist;
    }

    private void queueReport(MediaData mediaData){
        SharedPreferences sharedPreferences = context.getSharedPreferences("ReportQueue", Context.MODE_PRIVATE);

        try {
            JSONObject jo = mediaData.toJson();
            JSONArray ja;
            if (sharedPreferences.contains("queue")) {
                String queueString = sharedPreferences.getString("queue", "");
                ja = new JSONArray(queueString);
            } else {
                ja = new JSONArray();
            }
            ja.put(jo);
            sharedPreferences.edit().putString("queue", ja.toString()).apply();
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void executeQueueReport(){

        SharedPreferences sharedPreferences = context.getSharedPreferences("ReportQueue", Context.MODE_PRIVATE);
        try {
            JSONArray ja;
            if (sharedPreferences.contains("queue")) {
                String queueString = sharedPreferences.getString("queue", "");
                ja = new JSONArray(queueString);
                FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
                for(int i = 0; i < ja.length(); i++){
                    firebaseHelper.addMediaData(new MediaData(ja.getJSONObject(i)));
                }
                sharedPreferences.edit().remove("queue").apply();
            }

        } catch (JSONException e){
            e.printStackTrace();
        }

    }


    private Long playlistStopTime;
    private Long playlistStartTime;
    private Long playlistRemainingTime;

    @Override
    public void playlistWaited(boolean isWaiting) {
        if(isWaiting){
            if(previousMedia.getType().equals(MediaData.MEDIA_TYPE_VIDEO)){
                player.setPlayWhenReady(false);
            }
            playlistLooperHandler.removeCallbacks(playlistLooperRunnable);
            playlistStopTime = System.currentTimeMillis();
            playlistRemainingTime = Long.valueOf(previousMedia.getTime()) - (playlistStopTime - playlistStartTime);
        }else{
            if(previousMedia.getType().equals(MediaData.MEDIA_TYPE_VIDEO)){
                player.setPlayWhenReady(true);
            }
            playlistLooperHandler.postDelayed(playlistLooperRunnable, playlistRemainingTime);

        }
    }

    @Override
    public void networkConnected() {
        isNetworkConnected = true;
        executeQueueReport();
    }

    @Override
    public void networkDisconnected() {
        isNetworkConnected = false;
    }
}
