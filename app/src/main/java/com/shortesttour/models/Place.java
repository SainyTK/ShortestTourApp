package com.shortesttour.models;

import com.google.android.gms.maps.model.LatLng;

public class Place {
    private String placeTitle;
    private LatLng placeLatLng;
    private int distance;
    private int duration;

    public Place(){

    }

    public Place(String placeTitle, LatLng placeLatLng) {
        this.placeTitle = placeTitle;
        this.placeLatLng = placeLatLng;
    }

    public String getPlaceTitle() {
        return placeTitle;
    }

    public void setPlaceTitle(String placeTitle) {
        this.placeTitle = placeTitle;
    }

    public LatLng getPlaceLatLng() {
        return placeLatLng;
    }

    public void setPlaceLatLng(LatLng placeLatLng) {
        this.placeLatLng = placeLatLng;
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
}
