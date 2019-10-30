package com.ey08.octopus.API;

public class MediaData {

    public static final String MEDIA_TYPE_VIDEO = "video";
    public static final String MEDIA_TYPE_JPG = "image";

    private String name;
    private String type;
    private String md5;
    private String time;

    public MediaData(String name, String type, String md5, String time) {
        this.name = name;
        this.type = type;
        this.md5 = md5;
        this.time = time;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
