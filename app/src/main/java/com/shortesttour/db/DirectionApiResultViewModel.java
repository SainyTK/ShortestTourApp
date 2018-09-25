package com.shortesttour.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class DirectionApiResultViewModel extends AndroidViewModel{
    private DirectionApiResultRepository repository;
    private LiveData<List<DirectionApiResult>> results;

    public DirectionApiResultViewModel(@NonNull Application application) {
        super(application);
        repository = new DirectionApiResultRepository(application);
        results = repository.getResults();
    }

    public LiveData<List<DirectionApiResult>> getResults(){
        return  results;
    }

    public void insert(DirectionApiResult result){
        repository.insert(result);
    }

    public void deleteAll(){
        repository.deleteAll();
    }

    public void delete(int sourceId,int destinationId){
        repository.delete(sourceId,destinationId);
    }

    public DirectionApiResult getResult(int sourceId, int destinationId){
        return repository.getApiResult(sourceId,destinationId);
    }
}
