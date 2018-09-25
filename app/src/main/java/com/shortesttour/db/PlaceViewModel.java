package com.shortesttour.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class PlaceViewModel extends AndroidViewModel {
    private PlaceRepository placeRepository;
    private LiveData<List<PlaceEntity>> placeList;

    public PlaceViewModel(@NonNull Application application) {
        super(application);
        placeRepository = new PlaceRepository(application);
        placeList = placeRepository.getPlaceList();
    }

    public LiveData<List<PlaceEntity>> getPlaceList() {
        return placeList;
    }

    public void insert(PlaceEntity place){
        placeRepository.insert(place);
    }

    public void deleteAll(){
        placeRepository.deleteAll();
    }
}
