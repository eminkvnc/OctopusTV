package com.ey08.octopus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellExecuter extends Thread {


    private String shellCommand;

    public ShellExecuter(String shellCommand){


        this.shellCommand = shellCommand;
    }

    @Override
    public void run() {
        super.run();
        String response = "";

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
        }catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

}
