package com.ey08.octopus.API;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Reporter {


    public Reporter() {

    }

    public static String TAG = "Reporter";

    private String queryUrl = "http://panel.tvoctopus.net/";

    public void reportCommandStatus(CommandData commandData, String status){

        try {

            String urlString = queryUrl+"api/command/"+commandData.getId();
            urlString += "?"+"status"+"="+status;
            URL commandQueryUrl = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) commandQueryUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);


            StringBuilder result = new StringBuilder();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK){
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String data = bufferedReader.readLine();

                while (data != null){

                    result.append(data);
                    data = bufferedReader.readLine();
                }
                Log.d(TAG, "reportCommandStatus: "+result.toString());
            }else{
                Log.d(TAG, "reportCommandStatus: connection error!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
