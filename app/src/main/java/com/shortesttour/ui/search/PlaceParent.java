package com.shortesttour.ui.search;

import com.bignerdranch.expandablerecyclerview.model.Parent;
import com.google.android.gms.maps.model.LatLng;
import com.shortesttour.models.Place;

import java.util.ArrayList;
import java.util.List;

public class PlaceParent implements Parent<String> {

    private Place mPlace;
    private List<String> temp;

    public PlaceParent(Place place){
        mPlace = place;

        temp = new ArrayList<>();
        temp.add("");
    }

    @Override
    public List<String> getChildList() {
        return temp;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }

    public String getPlaceTitle(){
        return mPlace.getPlaceTitle();
    }

    public LatLng getPlaceLatLng(){return mPlace.getPlaceLatLng();}

    public Place getPlace(){
        return mPlace;
    }
}
