package com.example.kevin.myapplication;

public class LocationLongLat {
    double longitude;
    double latitude;
    String bikeLocation;

    public LocationLongLat(){

    }

    public LocationLongLat(double longitude, double latitude, String bikeLocation){
        this.latitude = latitude;
        this.longitude = longitude;
        this.bikeLocation = bikeLocation;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getBikeLocation() {
        return bikeLocation;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setBikeLocation(String bikeLocation) {
        this.bikeLocation = bikeLocation;
    }
}
