package com.tvoctopus.player.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("all")
public class WeatherData {
    @SerializedName("coord")
    private final Coord coord;

    @SerializedName("weather")
    private final List<Weather> weather;

    @SerializedName("base")
    private final String base;

    @SerializedName("main")
    private final Main main;

    @SerializedName("visibility")
    private final int visibility;

    @SerializedName("wind")
    private final Wind wind;

    @SerializedName("clouds")
    private final Clouds clouds;

    @SerializedName("dt")
    private final int dt;

    @SerializedName("sys")
    private final Sys sys;

    @SerializedName("timezone")
    private final int timezone;

    @SerializedName("id")
    private final int id;

    @SerializedName("name")
    private final String name;

    @SerializedName("cod")
    private final int cod;

    public WeatherData(Coord coord, List<Weather> weather, String base, Main main, int visibility,
                       Wind wind, Clouds clouds, int dt, Sys sys, int timezone, int id, String name, int cod) {
        this.coord = coord;
        this.weather = weather;
        this.base = base;
        this.main = main;
        this.visibility = visibility;
        this.wind = wind;
        this.clouds = clouds;
        this.dt = dt;
        this.sys = sys;
        this.timezone = timezone;
        this.id = id;
        this.name = name;
        this.cod = cod;
    }

    public Coord getCoord() {
        return coord;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public String getBase() {
        return base;
    }

    public Main getMain() {
        return main;
    }

    public int getVisibility() {
        return visibility;
    }

    public Wind getWind() {
        return wind;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public int getDt() {
        return dt;
    }

    public Sys getSys() {
        return sys;
    }

    public int getTimezone() {
        return timezone;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCod() {
        return cod;
    }

    public static class Coord {
        @SerializedName("lon")
        private final double lon;

        @SerializedName("lat")
        private final double lat;

        public Coord(double lon, double lat) {
            this.lon = lon;
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public double getLat() {
            return lat;
        }
    }

    public static class Weather {
        @SerializedName("id")
        private final int id;

        @SerializedName("main")
        private final String main;

        @SerializedName("description")
        private final String description;

        @SerializedName("icon")
        private final String icon;

        public Weather(int id, String main, String description, String icon) {
            this.id = id;
            this.main = main;
            this.description = description;
            this.icon = icon;
        }

        public int getId() {
            return id;
        }

        public String getMain() {
            return main;
        }

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }
    }

    public static class Main {
        @SerializedName("temp")
        private final double temp;

        @SerializedName("feels_like")
        private final double feelsLike;

        @SerializedName("temp_min")
        private final int tempMin;

        @SerializedName("temp_max")
        private final double tempMax;

        @SerializedName("pressure")
        private final int pressure;

        @SerializedName("humidity")
        private final int humidity;

        public Main(double temp, double feelsLike, int tempMin, double tempMax, int pressure,
                    int humidity) {
            this.temp = temp;
            this.feelsLike = feelsLike;
            this.tempMin = tempMin;
            this.tempMax = tempMax;
            this.pressure = pressure;
            this.humidity = humidity;
        }

        public double getTemp() {
            return temp;
        }

        public double getFeelsLike() {
            return feelsLike;
        }

        public int getTempMin() {
            return tempMin;
        }

        public double getTempMax() {
            return tempMax;
        }

        public int getPressure() {
            return pressure;
        }

        public int getHumidity() {
            return humidity;
        }
    }

    public static class Wind {
        @SerializedName("speed")
        private final double speed;

        @SerializedName("deg")
        private final int deg;

        public Wind(double speed, int deg) {
            this.speed = speed;
            this.deg = deg;
        }

        public double getSpeed() {
            return speed;
        }

        public int getDeg() {
            return deg;
        }
    }

    public static class Clouds {
        @SerializedName("all")
        private final int all;

        public Clouds(int all) {
            this.all = all;
        }

        public int getAll() {
            return all;
        }
    }

    public static class Sys {
        @SerializedName("type")
        private final int type;

        @SerializedName("id")
        private final int id;

        @SerializedName("country")
        private final String country;

        @SerializedName("sunrise")
        private final int sunrise;

        @SerializedName("sunset")
        private final int sunset;

        public Sys(int type, int id, String country, int sunrise, int sunset) {
            this.type = type;
            this.id = id;
            this.country = country;
            this.sunrise = sunrise;
            this.sunset = sunset;
        }

        public int getType() {
            return type;
        }

        public int getId() {
            return id;
        }

        public String getCountry() {
            return country;
        }

        public int getSunrise() {
            return sunrise;
        }

        public int getSunset() {
            return sunset;
        }
    }
}
