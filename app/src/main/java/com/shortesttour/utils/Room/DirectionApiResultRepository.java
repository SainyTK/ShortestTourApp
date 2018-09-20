package com.shortesttour.utils.Room;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.RoomDatabase;
import android.os.AsyncTask;

import java.util.List;

import io.reactivex.Single;

public class DirectionApiResultRepository {
    private DirectionApiResultDao dao;
    private LiveData<List<DirectionApiResult>> results;

    public DirectionApiResultRepository(Application application){
        AppDatabase database = AppDatabase.getDatabase(application);
        dao = database.directionApiResultDao();
        results = dao.getAll();
    }

    LiveData<List<DirectionApiResult>> getResults(){
        return  results;
    }

    LiveData<DirectionApiResult> getApiResult(int sourceId, int destinationId){
        return dao.getApiResult(sourceId,destinationId);
    }

    void insert(DirectionApiResult apiResult){
        new insertTask(dao).execute(apiResult);
    }

    void deleteAll(){
        new deleteAllTask(dao).execute();
    }

    void delete(int sourceId,int destinationId){
        Integer[] indices = {sourceId,destinationId};
        new deleteTask(dao).execute(indices);
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

    private static class deleteTask extends AsyncTask<Integer,Void,Void>{
        private DirectionApiResultDao dao;

        deleteTask(DirectionApiResultDao dao){
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            int sourceId = integers[0];
            int destinationId = integers[1];
            dao.delete(sourceId,destinationId);
            return null;
        }
    }


}
