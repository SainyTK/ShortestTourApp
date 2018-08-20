package com.shortesttour.models;

import com.google.android.gms.maps.model.LatLng;

public class Place {
    private String placeTitle;
    private LatLng placeLatLng;

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
}
