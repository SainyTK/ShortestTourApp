package com.shortesttour.ui.main;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.shortesttour.R;
import com.shortesttour.models.Place;
import com.shortesttour.utils.FindPathUtils;

import java.util.ArrayList;
import java.util.List;

public class MainPresenter implements MainContract.Presenter, FindPathUtils.TaskListener {

    private MainContract.View mView;

    private Place currentPlace;

    private List<Place> mSearchData;
    private List<Place> mSuggestData;

    private List<Place> preventRepeatPlaceList;

    private FindPathUtils mFindPathUtils;

    public MainPresenter(MainContract.View view){
        mView = view;

        mSearchData = new ArrayList<>();
        mSuggestData = new ArrayList<>();
        preventRepeatPlaceList = new ArrayList<>();

        mFindPathUtils = new FindPathUtils(mView.getActivity());
        mFindPathUtils.setOnTaskFinishListener(this);
    }

    public void setupCurrentPlace(LatLng latLng){
        currentPlace = new Place(0,"You","Your Location",latLng.latitude,latLng.longitude);
        mFindPathUtils.addPlace(currentPlace);
    }

    public void addPlace(Place place){
//        List<Place> searchPlaceList = searchFragment.getPlaceList();
//        for(int i=0;i<30;i++){
//            Place p = searchPlaceList.get(i);
//            bottomSheetPlaceList.add(p);
//            mFindPathUtils.addPlace(p);
//        }
        if(!checkHasPlace(preventRepeatPlaceList,place)){
            preventRepeatPlaceList.add(place);
            mFindPathUtils.addPlace(place);
        }
    }

    synchronized public boolean checkHasPlace(List<Place> placeList,Place newPlace){
        for(Place place:placeList){
            if(place.getPlaceTitle().contentEquals(newPlace.getPlaceTitle()))
                return true;
        }
        return false;
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

    public List<Place> getPlaceList(){
        return mFindPathUtils.getPlaceList();
    }

    public int getNumPlace(){
        return mFindPathUtils.getPlaceList().size();
    }

    @Override
    public void OnStartTask(String placeTitle) {

    }

    @Override
    public void onUpdateValue(int value) {

    }

    @Override
    public void onGetPath(int[] path) {
        mView.onFinishCalculatePath(path);
    }

    @Override
    public void onDrawPath(List<PolylineOptions> polylineOptions) {
        mView.onFinishDrawPath(polylineOptions);
    }

    public void removePlace(int position){
        mFindPathUtils.collapseGraph(position+1);
    }


    public int getSumDistance(){
        return mFindPathUtils.getNearestSumDistance();
    }

    public int getSumDuration(){
        return mFindPathUtils.getNearestSumDuration();
    }

    public int[] getDistances(){
        return mFindPathUtils.getNearestDistance();
    }

    public int[] getDurations(){
        return mFindPathUtils.getNearestDuration();
    }

    public List<Place> getOrderedPlaceList(){
        return mFindPathUtils.getOrderedPlaceList();
    }

    public List<Place> excludeCurrentPlace(Boolean ordered){
        if(getNumPlace()>1){
            if(ordered){
                return getOrderedPlaceList().subList(1,getNumPlace());
            }else{
                return getPlaceList().subList(1,getNumPlace());
            }
        }
        return new ArrayList<>();
    }
}
