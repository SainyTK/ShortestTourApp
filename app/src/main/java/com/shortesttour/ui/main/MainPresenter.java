package com.shortesttour.ui.main;

import com.google.android.gms.maps.model.LatLng;
import com.shortesttour.models.Place;

import java.util.ArrayList;
import java.util.List;

public class MainPresenter implements MainContract.Presenter{

    private MainContract.View mView;

    private Place currentPlace;

    private List<Place> mPlaceList;
    private List<Place> mSearchData;
    private List<Place> mSuggestData;

    public MainPresenter(MainContract.View view){
        mView = view;

        currentPlace = new Place(0,"You","Your Location",0,0);
        mPlaceList = new ArrayList<>();
        mSearchData = new ArrayList<>();
        mSuggestData = new ArrayList<>();
    }

    public Place getCurrentPlace(){
        return currentPlace;
    }

    public void setCurrentLatLng(LatLng latLng){
        currentPlace.setPlaceLatLng(latLng);
    }

    public void addSuggestData(Place place){
        mSuggestData.add(place);
    }

    public void clearSuggestData(){
        mSuggestData.clear();
    }

    public void setSearchData(List<Place> placeList){
        mSearchData = placeList;
    }

    public void setSuggestData(List<Place> suggestPlace){
        mSuggestData = suggestPlace;
    }

    public List<Place> getSearchData(){
        return mSearchData;
    }

    public List<Place> getSuggestData(){
        return mSuggestData;
    }


}
