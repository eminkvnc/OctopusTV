package com.tvoctopus.player.view.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tvoctopus.player.API.JSonParser;
import com.tvoctopus.player.R;
import com.tvoctopus.player.model.WeatherData;
import com.tvoctopus.player.services.WeatherService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class WidgetFragment extends Fragment {


    private HashMap<String, Integer> iconsMap = new HashMap<>();

    public static final int POSITION_LEFT = 0;
    public static final int POSITION_RIGHT = 1;
    public static final int POSITION_TOP = 2;
    public static final int POSITION_BOTTOM = 3;
    public static final int POSITION_NONE = 4;

    private LinearLayout widgetLinearLayout;
    private LinearLayout weatherLinearLayout;
    private CardView weatherCardView;
    private TextView weatherTemperatureTextView;
    private TextView weatherLocationTextView;
    private ImageView weatherIconImageView;
    private WebView rssWebView;

    private WidgetFragmentViewModel viewModel;

    private BroadcastReceiver weatherReceiver;

    public WidgetFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iconsMap.put("01d", R.drawable.ic_sunny);
        iconsMap.put("01n", R.drawable.ic_clear_night);
        iconsMap.put("02d", R.drawable.ic_cloudy);
        iconsMap.put("02n", R.drawable.ic_cloudy_night);
        iconsMap.put("03d", R.drawable.ic_overcast);
        iconsMap.put("03n", R.drawable.ic_overcast);
        iconsMap.put("04d", R.drawable.ic_overcast);
        iconsMap.put("04n", R.drawable.ic_overcast);
        iconsMap.put("09d", R.drawable.ic_showers);
        iconsMap.put("09n", R.drawable.ic_showers);
        iconsMap.put("10d", R.drawable.ic_rain);
        iconsMap.put("10n", R.drawable.ic_rain);
        iconsMap.put("11d", R.drawable.ic_storm);
        iconsMap.put("11n", R.drawable.ic_storm);
        iconsMap.put("13d", R.drawable.ic_snow);
        iconsMap.put("13n", R.drawable.ic_snow);
        iconsMap.put("50d", R.drawable.ic_fog);
        iconsMap.put("50n", R.drawable.ic_fog);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(WidgetFragmentViewModel.class);

        viewModel.getConfig().getWeatherEnabled().observe(getViewLifecycleOwner(), weatherEnabled -> {
            if(weatherEnabled){
                weatherCardView.setVisibility(View.VISIBLE);
            }else {
                weatherCardView.setVisibility(View.GONE);
            }
        });

        viewModel.getConfig().getRssEnabled().observe(getViewLifecycleOwner(), rssEnabled -> {
            if(rssEnabled){
                rssWebView.setVisibility(View.VISIBLE);
            }else {
                rssWebView.setVisibility(View.GONE);
            }
        });

        viewModel.getConfig().getWidgetBarPosition().observe(getViewLifecycleOwner(), integer -> {
            switch (integer){
                case POSITION_TOP:
                case WidgetFragment.POSITION_BOTTOM:
                    setWidgetBarOrientation(LinearLayout.HORIZONTAL,15,15);
                    break;
                case WidgetFragment.POSITION_LEFT:
                case WidgetFragment.POSITION_RIGHT:
                    setWidgetBarOrientation(LinearLayout.VERTICAL,15,15);
                    break;
            }
        });


        //TODO: Implement weather LiveData and post updated value.(Cache weather data)
        weatherReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String result = intent.getStringExtra(WeatherService.PARAM_WEATHER_RESULT);
                updateWeather(result);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(weatherReceiver, new IntentFilter(WeatherService.ACTION_WEATHER_QUERY));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(weatherReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_widget, container, false);
        widgetLinearLayout = v.findViewById(R.id.widget_linear_layout);

        View weatherLayout = inflater.inflate(R.layout.widget_wheather,container,false);
        weatherLinearLayout = weatherLayout.findViewById(R.id.weather_linear_layout);
        weatherCardView = weatherLayout.findViewById(R.id.weather_card_view);
        weatherTemperatureTextView = weatherLayout.findViewById(R.id.weather_temprature_text_view);
        weatherLocationTextView = weatherLayout.findViewById(R.id.weather_location_text_view);
        weatherIconImageView = weatherLayout.findViewById(R.id.weather_icon_image_view);

        rssWebView = new WebView(getContext());
        rssWebView.getSettings().getUseWideViewPort();
        rssWebView.getSettings().setJavaScriptEnabled(true);
        rssWebView.loadUrl("http://report.tvoctopus.net/haberturk/");
        widgetLinearLayout.addView(weatherLayout);
        widgetLinearLayout.addView(rssWebView);
        return v;
    }



    //TODO: Set transparency from API.
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
            rssLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,1);

        } else{
            weatherLayoutParams = new LinearLayout.LayoutParams(size.x*widthPercentage/100,size.y*heightPercentage/100,4);
            rssLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT,1);
        }
        weatherIconImageView.setLayoutParams(weatherLayoutParams);
        rssWebView.setLayoutParams(rssLayoutParams);
    }

    public void updateWeather(String data){
        try {
            if (data != null) {
                JSONObject jo = new JSONObject(data);
                WeatherData weatherData = new JSonParser().parseWeatherData(jo);
                int temperature = Double.valueOf(weatherData.getMain().getTemp()).intValue();
                weatherTemperatureTextView.setText(String.valueOf(temperature));
                weatherLocationTextView.setText(weatherData.getName());
                weatherIconImageView.setImageResource(iconsMap.get(weatherData.getWeather().get(0).getIcon()));
            }
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}

