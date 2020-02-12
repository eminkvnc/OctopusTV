package com.tvoctopus.player.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.tvoctopus.player.R;

import java.io.ByteArrayOutputStream;

public class Utils {

    static byte[] takeScreenShot(Activity activity){

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
//        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
//        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
//        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(height/8,width/8);
//        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
//        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
//        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
//        layoutParams.topMargin = 4;
//        imageView.setLayoutParams(layoutParams);
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

}
