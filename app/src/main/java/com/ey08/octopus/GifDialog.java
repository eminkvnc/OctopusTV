package com.ey08.octopus;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

public class GifDialog extends Dialog {
    private ImageView imageView;
    private TextView textView;
    private Context context;

    public GifDialog(@NonNull Context context) {
        super(context);
        this.context = context;

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gif_dialog);
        textView = findViewById(R.id.gif_text_view);
        imageView = findViewById(R.id.gif_image_view);
        textView.setText(context.getResources().getString(R.string.gif_dialog_content_loading));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(580, 435);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        setCancelable(true);
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });


        Glide.with(context)
                .load(R.drawable.octopus)
                .placeholder(R.drawable.octopus)
                .centerCrop()
                .into(new DrawableImageViewTarget(imageView));
    }

    @Override
    public void show() {
        super.show();

        //AnimationDrawable frameAnimation = (AnimationDrawable)imageView.getBackground();
        //frameAnimation.start();

    }

}
