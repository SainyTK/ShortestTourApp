package com.shortesttour.models;

import com.bignerdranch.expandablerecyclerview.model.Parent;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Place implements Parent<String> {

    private String placeTitle;
    private double latitude;
    private double longitude;
    private int distance;
    private int duration;
    private int order;

    public Place(){
        this.distance = 0;
        this.duration = 0;
        this.order = 0;
    }

    public Place(String placeTitle, double latitude, double longitude) {
        this.placeTitle = placeTitle;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = 0;
        this.duration = 0;
        this.order = 0;
    }

    public String getPlaceTitle() {
        return placeTitle;
    }

    public void setPlaceTitle(String placeTitle) {
        this.placeTitle = placeTitle;
    }

    public LatLng getPlaceLatLng() {
        return new LatLng(latitude,longitude);
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public void setPlaceLatLng(LatLng placeLatLng) {
        latitude = placeLatLng.latitude;
        longitude = placeLatLng.longitude;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getDistanceText(){
        if(distance<1000)
            return distance + " meters";
        else
            return (distance/1000) + " km";
    }

    public String getDurationText(){
        int hours = duration/60;
        int minutes = duration%60;
        String durationText = "";
        if(hours>0)
            durationText = hours + " hr ";
        durationText = durationText + minutes + " min";
        return durationText;
    }

    @Override
    public List<String> getChildList() {
        List<String> temp = new ArrayList<>();
        temp.add("");
        return temp;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
