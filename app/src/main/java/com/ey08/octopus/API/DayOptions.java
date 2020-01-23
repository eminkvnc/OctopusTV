package com.ey08.octopus.API;


public class DayOptions {

    public static final String STATUS_ON = "on";
    public static final String STATUS_OFF = "off";
    public static final String STATUS_SCHEDULED = "scheduled";

    private String on;
    private String off;
    private String status;


    public DayOptions(String on, String off, String status) {
        this.on = on;
        this.off = off;
        this.status = status;
    }


    public String getOn() {
        return on;
    }

    public void setOn(String on) {
        this.on = on;
    }

    public String getOff() {
        return off;
    }

    public void setOff(String off) {
        this.off = off;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
