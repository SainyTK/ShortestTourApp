package com.shortesttour.ui.main;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.shortesttour.R;
import com.shortesttour.models.Place;
import com.shortesttour.utils.FindPathUtils;
import com.shortesttour.utils.graph.GraphNode;

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
        currentPlace = new Place("Your Location",latLng.latitude,latLng.longitude);
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
            mView.displayAddLocation(place.getPlaceTitle());
            preventRepeatPlaceList.add(place);
            mFindPathUtils.addPlace(place);
        }else{
            mView.showToast(mView.getActivity().getString(R.string.cannot_add_exist));
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
    public void OnStartTask() {
        mView.onStartTask();
    }

    @Override
    public void onUpdateValue(int value) {
        mView.onUpdateValue(value);
    }

    @Override
    public void onComplete() {
        mView.onFinishCalculatePath();
    }

    @Override
    public void onDrawPath(List<PolylineOptions> polylineOptions) {
        mView.onFinishDrawPath(polylineOptions);
    }

    public void removePlace(int position){
        if(mFindPathUtils.isTaskRunning()){
            mView.showToast(mView.getActivity().getString(R.string.text_cannot_remove));
        }else{
            preventRepeatPlaceList.remove(position);
            mFindPathUtils.collapseGraph(position+1);
        }
//        Log.d("test", "removePlace: " + mFindPathUtils.isTaskRunning());
    }

    public void calculatePath(){
        mFindPathUtils.calculatePath();
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

    public void cancelTask(){
        mFindPathUtils.cancelTask();
    }

    @Override
    public void onCancel() {
        preventRepeatPlaceList.clear();
        preventRepeatPlaceList.addAll(mFindPathUtils.getPlaceList());

        mView.onCancel();
    }

    public int[] getPath(){
        return mFindPathUtils.getPath();
    }

    public GraphNode[][] getGraph(){
        return mFindPathUtils.getGraph();
    }
}
