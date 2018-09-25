package com.shortesttour.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class PlaceRepository {
    private PlaceDao placeDao;
    private LiveData<List<PlaceEntity>> placeList;

    PlaceRepository(Application application){
        AppDatabase db = AppDatabase.getDatabase(application);
        placeDao = db.placeDao();
        placeList = placeDao.getAll();
    }

    LiveData<List<PlaceEntity>> getPlaceList(){
        return placeList;
    }

    public void insert(PlaceEntity place){
        new insertAsyncTask(placeDao).execute(place);
    }

    public void deleteAll(){
        new deleteAsyncTask(placeDao).execute();
    }

    private static class insertAsyncTask extends AsyncTask<PlaceEntity,Void,Void>{
        private PlaceDao placeDao;

        insertAsyncTask(PlaceDao placeDao){
            this.placeDao = placeDao;
        }

        @Override
        protected Void doInBackground(PlaceEntity... placeEntities) {
            placeDao.insertAll(placeEntities);
            return null;
        }
    }

    private static class deleteAsyncTask extends AsyncTask<Void,Void,Void>{
        private PlaceDao placeDao;

        deleteAsyncTask(PlaceDao placeDao){
            this.placeDao = placeDao;
        }

        @Override
        protected Void doInBackground(Void... params) {
            placeDao.deleteAll();
            return null;
        }
    }
}
