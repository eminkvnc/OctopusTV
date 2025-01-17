package com.tvoctopus.player.services;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.tvoctopus.player.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class Reporter {


    private static Reporter instance;
    private Context context;

    public static Reporter getInstance(Context context){
        if(instance == null){
            instance = new Reporter(context);
        }
        return instance;
    }

    private Reporter(Context context) {
        this.context = context;
    }

    public static String TAG = "Reporter";

    public static final String COMMAND_STATUS_SUCCEEDED = "succeeded";
    public static final String COMMAND_STATUS_INPROGRESS = "in-progress";
    private String queryUrl = "http://panel.tvoctopus.net/";

    public void reportCommandStatus(String commandId, String status){

        new Thread(() -> {
            try {
                String urlString = queryUrl+"api/command/"+commandId;
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
        }).start();
    }
    public void reportDeviceStatus(String screenId){

        new Thread(() -> {
            String ipAddress;
            String macAddress;
            String wifiSSID;
            String playerVersion;
            String cecStatus;
            // add cpu and ram stats, disk space, android version, start time, network type(WIFI, MOBILE, ETH)

            try {
                ipAddress = getIPAddress(true);
                macAddress = getMacAddress();
                playerVersion = BuildConfig.VERSION_NAME;
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService (Context.WIFI_SERVICE);
                WifiInfo info;
                if (wifiManager != null) {
                    info = wifiManager.getConnectionInfo ();
                    wifiSSID  = info.getSSID().trim();
                }else {
                    wifiSSID = "";
                }
                File file1 = new File("/sys/class/cec/cmd");
                if (file1.exists()){
                    cecStatus = "true";
                }else{
                    cecStatus = "false";
                }

                JSONObject joStatus = new JSONObject();
                joStatus.put("version",playerVersion);
                joStatus.put("tag",screenId);
                joStatus.put("ip",ipAddress);
                joStatus.put("mac",macAddress);
                joStatus.put("cec",cecStatus);
                joStatus.put("net","wlan0");
                joStatus.put("wifi_name",wifiSSID);
                joStatus.put("tv_status","on");

                JSONObject joData = new JSONObject();
                joData.put("status",joStatus);

                JSONObject jo = new JSONObject();
                jo.put("event","report");
                jo.put("data",joData);
                Log.d(TAG, "reportDeviceStatus: "+jo.toString());

                String urlString = queryUrl+"api/screen/"+screenId;
                URL commandQueryUrl = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) commandQueryUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.addRequestProperty("Accept","application/json");
                connection.addRequestProperty("Content-Type","application/json");
                connection.setDoOutput(true);
                connection.setInstanceFollowRedirects(false);
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
                writer.write(jo.toString());
                writer.close();

                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK){
                    InputStream inputStream = connection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String data = bufferedReader.readLine();
                    StringBuilder result = new StringBuilder();
                    while (data != null){
                        result.append(data);
                        data = bufferedReader.readLine();
                    }
                    Log.d(TAG, "reportDeviceStatus: "+result.toString());
                }

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }

    private static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }


}
