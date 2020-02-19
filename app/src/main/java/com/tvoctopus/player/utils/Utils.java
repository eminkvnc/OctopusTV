package com.tvoctopus.player.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.tvoctopus.player.R;
import com.tvoctopus.player.model.Playlist;
import com.tvoctopus.player.services.Downloader;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static com.tvoctopus.player.model.DataRepository.SHARED_PREF_CONFIG;
import static com.tvoctopus.player.model.DataRepository.SHARED_PREF_OCTOPUS_DATA;
import static com.tvoctopus.player.model.DataRepository.SHARED_PREF_PLAYLIST;
import static com.tvoctopus.player.services.Downloader.DOWNLOAD_DIR;
import static com.tvoctopus.player.services.Downloader.DOWNLOAD_FILE_PREFIX;

public class Utils {


    public static void replaceMediaFiles(File downloadDir, Playlist playlist){
        String[] mediaFiles = downloadDir.list();
        if (mediaFiles != null) {
            for (String mediaFile : mediaFiles) {
                File file = new File(downloadDir, mediaFile);
                if(file.exists() && !mediaFile.startsWith(DOWNLOAD_FILE_PREFIX)){
                    file.delete();
                }
            }
            mediaFiles = downloadDir.list();
            if(mediaFiles != null){
                for (String mediaFile : mediaFiles) {
                    File file = new File(downloadDir, mediaFile);
                    String nameWithoutPrefix = mediaFile.substring(Downloader.DOWNLOAD_FILE_PREFIX.length());
                    if (playlist.getMediaNames().contains(nameWithoutPrefix)) {
                        file.renameTo(new File(downloadDir, nameWithoutPrefix));
                    }
                }
            }
            removeTempFiles(downloadDir);
        }
    }

    private static void removeTempFiles(File downloadDir){
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

    public static byte[] takeScreenShot(Activity activity){

        View view = activity.getWindow().getDecorView();
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        bitmap.recycle();
        return bytes;
    }


    public static void showGif(Context context, ImageView imageView){
        Glide.with(context)
                .load(R.drawable.octopus_white)
                .placeholder(R.drawable.octopus_white)
                .centerCrop()
                .into(new DrawableImageViewTarget(imageView));
        imageView.setVisibility(View.VISIBLE);
    }

    public static void dismissGif(ImageView imageView){
        imageView.setVisibility(View.GONE);
    }

    public static void clearApplicationData(Context context){
        context.getSharedPreferences(SHARED_PREF_OCTOPUS_DATA, Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences(SHARED_PREF_PLAYLIST, Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences("ReportQueue", Context.MODE_PRIVATE).edit().clear().apply();
        File file = context.getExternalFilesDir(DOWNLOAD_DIR);
        if (file != null) {
            deleteDirectory(file);
        }
    }

    private static void deleteDirectory(File path) {
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

}
