package com.ey08.octopus;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class WidgetFragment extends Fragment {
    //TODO: Add weather widget_weather layout to this fragment. Listen weather UI changes with WeatherListener.
    //TODO: Activate-deactivate weather widget to result of OctopusTV API request.


    public static final int POSITION_LEFT = 0;
    public static final int POSITION_RIGHT = 1;
    public static final int POSITION_TOP = 2;
    public static final int POSITION_BOTTOM = 3;

    private LinearLayout widgetLinearLayout;
    private LinearLayout weatherLinearLayout;
    private CardView weatherCardView;
    private TextView weatherTempratureTextView;
    private ImageView weatherIconImageView;
    private WebView rssWebView;

    public WidgetFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //TODO: Configure widget position with API widget and device orientation data.
        View v = inflater.inflate(R.layout.fragment_widget, container, false);
        widgetLinearLayout = v.findViewById(R.id.widget_linear_layout);

        View weatherLayout = inflater.inflate(R.layout.widget_wheather,container,false);
        weatherLinearLayout = weatherLayout.findViewById(R.id.weather_linear_layout);
        weatherCardView = weatherLayout.findViewById(R.id.weather_card_view);
        weatherTempratureTextView = weatherLayout.findViewById(R.id.weather_temprature_text_view);
        weatherIconImageView = weatherLayout.findViewById(R.id.weather_icon_image_view);

        rssWebView = new WebView(getContext());

        rssWebView.getSettings().getUseWideViewPort();
        rssWebView.getSettings().setJavaScriptEnabled(true);
        rssWebView.loadUrl("http://report.tvoctopus.net/haberturk/");

        widgetLinearLayout.addView(weatherLayout);
        widgetLinearLayout.addView(rssWebView);
        return v;
    }

    public void setWidgetBarOrientation(int orientation, int widthPercentage, int heightPercentage){
        widgetLinearLayout.setOrientation(orientation);
        weatherLinearLayout.setOrientation(orientation);
        LinearLayout.LayoutParams weatherLayoutParams;
        LinearLayout.LayoutParams rssLayoutParams;
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        if(orientation == LinearLayout.VERTICAL){
            weatherLayoutParams = new LinearLayout.LayoutParams(size.x*widthPercentage/100,size.y*heightPercentage/100,4);
            rssLayoutParams = new LinearLayout.LayoutParams(size.x*widthPercentage/100, ViewGroup.LayoutParams.WRAP_CONTENT,1);

        } else{
            weatherLayoutParams = new LinearLayout.LayoutParams(size.x*widthPercentage/100,size.y*heightPercentage/100,4);
            rssLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,size.y*heightPercentage/100,1);
        }
        weatherIconImageView.setLayoutParams(weatherLayoutParams);
        rssWebView.setLayoutParams(rssLayoutParams);

    }

}
