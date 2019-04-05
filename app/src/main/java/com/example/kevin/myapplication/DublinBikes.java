package com.example.kevin.myapplication;

public class DublinBikes {
    String name;
    Double Longitude;
    Double Latitude;

    public DublinBikes(){

    }

    public DublinBikes(String name, Double Longitude, Double Latitude) {
        this.name = name;
        this.Longitude = Longitude;
        this.Latitude = Latitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLongitude(Double Longitude) {
        this.Longitude = Longitude;
    }

    public void setLatitude(Double Latitude) {
        this.Latitude = Latitude;
    }

    public String getName() {
        return name;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public Double getLatitude() {
        return Latitude;
    }
}

