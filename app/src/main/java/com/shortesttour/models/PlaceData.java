package com.shortesttour.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class PlaceData{

    private String placeId;
    private String userName;
    private String placeTitle;
    private double latitude;
    private double longitude;
    private List<String> jsonPaths;

    public PlaceData(){

    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPlaceTitle() {
        return placeTitle;
    }

    public void setPlaceTitle(String placeTitle) {
        this.placeTitle = placeTitle;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<String> getJsonPaths() {
        return jsonPaths;
    }

    public void setJsonPaths(List<String> jsonPaths) {
        this.jsonPaths = jsonPaths;
    }

    public LatLng getLatLng(){
        return new LatLng(latitude,longitude);
    }
}
