package com.shortesttour.utils.SQLite;

import android.content.Context;
import android.os.AsyncTask;

import com.shortesttour.models.PlaceData;

public class SQLiteUtils {
    private SQLiteAdapter sqLiteAdapter;
    private SQLiteListener mListener;

    public SQLiteUtils(Context context){
        sqLiteAdapter = new SQLiteAdapter(context);
    }

    public void addSQLiteListener(SQLiteListener listener){
        mListener = listener;
    }

    public void insert(PlaceData place){
        InsertTask task = new InsertTask();
        task.execute(place);
    }

    public void fetch(int placeId){
        FetchTask task = new FetchTask();
        task.execute(placeId);
    }

    public void delete(int placeId){
        DeleteTask task = new DeleteTask();
        task.execute(placeId);
    }

    public int getNumRow(){
        return sqLiteAdapter.getNumRow();
    }

    public interface SQLiteListener{
        void onFinishInsert(long rowId);
        void onFetchSuccess(PlaceData place);
        void onDeleted(int deleteResult);
    }

    private class InsertTask extends AsyncTask<PlaceData,Integer,Long>{
        @Override
        protected Long doInBackground(PlaceData... places) {
            PlaceData place = places[0];
            return sqLiteAdapter.insert(place);
        }

        @Override
        protected void onPostExecute(Long rowId) {
            if(mListener!=null)
                mListener.onFinishInsert(rowId);
        }
    }

    private class FetchTask extends AsyncTask<Integer,Integer,PlaceData>{
        @Override
        protected PlaceData doInBackground(Integer... integers) {
            int i = integers[0];
            PlaceData place = sqLiteAdapter.getPlaceFromDatabase(i);
            return place;
        }

        @Override
        protected void onPostExecute(PlaceData placeData) {
            super.onPostExecute(placeData);
            if(mListener!=null)
                mListener.onFetchSuccess(placeData);
        }
    }

    private class DeleteTask extends AsyncTask<Integer,Integer,Integer>{
        @Override
        protected Integer doInBackground(Integer... integers) {
            int i = integers[0];
            int rowId = sqLiteAdapter.deleteRow(i);
            return rowId;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(mListener!=null)
                mListener.onDeleted(integer);
        }
    }

    public void closeDatabase(){
        sqLiteAdapter.closeDatabase();
    }
}
