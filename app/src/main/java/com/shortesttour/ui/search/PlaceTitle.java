package com.shortesttour.ui.search;

import com.bignerdranch.expandablerecyclerview.model.Parent;

import java.util.ArrayList;
import java.util.List;

public class PlaceTitle implements Parent<String> {

    private String mPlaceTitle;
    private List<String> temp;

    public PlaceTitle(String placeTitle){
        mPlaceTitle = placeTitle;
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
        return mPlaceTitle;
    }
}
