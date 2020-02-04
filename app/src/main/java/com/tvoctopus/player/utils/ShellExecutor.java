package com.tvoctopus.player.utils;

import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellExecutor extends Thread {


    public static final String TAG = "ShellExecutor";
    private String shellCommand;
    private boolean asSuperUser = false;

    public ShellExecutor(String shellCommand){
        this.shellCommand = shellCommand;
    }

    @Override
    public void run() {
        super.run();
        String response;
        if(asSuperUser){
            try{
                Process su = Runtime.getRuntime().exec("su");
                DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

                outputStream.writeBytes(shellCommand+"\n");
                outputStream.flush();

                outputStream.writeBytes("exit\n");
                outputStream.flush();
                su.waitFor();
                StringBuffer output = new StringBuffer();

                BufferedReader reader = new BufferedReader(new InputStreamReader(su.getInputStream()));
                String line = "";
                while ((line = reader.readLine())!= null) {
                    output.append(line + "n");
                }

                response = output.toString();
                Log.d(TAG, "run: "+response);
            }catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                Process p = Runtime.getRuntime().exec(shellCommand);
                p.waitFor();
                StringBuffer output = new StringBuffer();

                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = "";
                while ((line = reader.readLine())!= null) {
                    output.append(line + "n");
                }

                response = output.toString();
                Log.d(TAG, "run: "+response);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        asSuperUser = false;
    }

    public ShellExecutor asSuperUser(){
        this.asSuperUser = true;
        return this;
    }

}
