package com.shortesttour.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class DirectionApiResultRepository {
    private DirectionApiResultDao dao;
//    private List<DirectionApiResult> results;

    public DirectionApiResultRepository(Application application){
        AppDatabase database = AppDatabase.getDatabase(application);
        dao = database.directionApiResultDao();
        //results = dao.getAll();
    }

//    public List<DirectionApiResult> getResults(){
//        return  results;
//    }

    public DirectionApiResult getApiResult(double srcLat,double srcLng,double desLat,double desLng){
        return dao.getApiResult(srcLat,srcLng,desLat,desLng);
    }

    public void insert(DirectionApiResult apiResult){
        new insertTask(dao).execute(apiResult);
    }

    public void deleteAll(){
        new deleteAllTask(dao).execute();
    }

    public void delete(double srcLat,double srcLng,double desLat,double desLng){
        Double[] latLng = {srcLat,srcLng,desLat,desLng};
        new deleteTask(dao).execute(latLng);
    }

    private static class insertTask extends AsyncTask<DirectionApiResult,Void,Void>{
        private DirectionApiResultDao dao;

        insertTask(DirectionApiResultDao dao){
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(DirectionApiResult... directionApiResults) {
            dao.insert(directionApiResults[0]);
            return null;
        }
    }

    private static class deleteAllTask extends AsyncTask<Void,Void,Void>{
        private DirectionApiResultDao dao;

        deleteAllTask(DirectionApiResultDao dao){
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dao.deleteAll();
            return null;
        }
    }

    private static class deleteTask extends AsyncTask<Double,Void,Void>{
        private DirectionApiResultDao dao;

        deleteTask(DirectionApiResultDao dao){
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Double... latLngs) {
            double srcLat = latLngs[0];
            double srcLng = latLngs[1];
            double desLat = latLngs[2];
            double desLng = latLngs[3];
            dao.delete(srcLat,srcLng,desLat,desLng);
            return null;
        }
    }


}
