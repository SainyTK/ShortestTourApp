package com.shortesttour.utils.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.shortesttour.models.PlaceData;

public class SQLiteAdapter {
    private SQLiteHelper sqLiteHelper;

    public SQLiteAdapter(Context context){
        sqLiteHelper = new SQLiteHelper(context);
    }

    public long insert(PlaceData place){

        // Gets the data repository in write mode
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PlacesTable.PlacesEntry.COLUMN_NAME_PLACE_TITLE, place.getPlaceTitle());
        values.put(PlacesTable.PlacesEntry.COLUMN_NAME_USER_NAME, place.getUserName());
        values.put(PlacesTable.PlacesEntry.COLUMN_NAME_LATITUDE, place.getLatitude());
        values.put(PlacesTable.PlacesEntry.COLUMN_NAME_LONGITUDE, place.getLongitude());

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(PlacesTable.PlacesEntry.TABLE_NAME, null, values);
        return newRowId;
    }

    public PlaceData getPlaceFromDatabase(int placeId){
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                PlacesTable.PlacesEntry.COLUMN_NAME_PLACE_TITLE,
                PlacesTable.PlacesEntry.COLUMN_NAME_USER_NAME,
                PlacesTable.PlacesEntry.COLUMN_NAME_LATITUDE,
                PlacesTable.PlacesEntry.COLUMN_NAME_LONGITUDE
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = PlacesTable.PlacesEntry._ID + " = ?";
        String[] selectionArgs = { placeId+"" };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = PlacesTable.PlacesEntry._ID + " DESC";

        Cursor cursor = db.query(
                PlacesTable.PlacesEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        PlaceData place = new PlaceData();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(PlacesTable.PlacesEntry._ID));
            String placeTitle = cursor.getString(cursor.getColumnIndexOrThrow(PlacesTable.PlacesEntry.COLUMN_NAME_PLACE_TITLE));
            String userName = cursor.getString(cursor.getColumnIndexOrThrow(PlacesTable.PlacesEntry.COLUMN_NAME_USER_NAME));
            double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(PlacesTable.PlacesEntry.COLUMN_NAME_LATITUDE));
            double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(PlacesTable.PlacesEntry.COLUMN_NAME_LONGITUDE));

            place.setPlaceId(itemId+"");
            place.setPlaceTitle(placeTitle);
            place.setUserName(userName);
            place.setLatitude(latitude);
            place.setLongitude(longitude);
        }
        cursor.close();
        return place;
    }

    public int deleteRow(int row){
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        // Define 'where' part of query.
        String selection = PlacesTable.PlacesEntry._ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { row+"" };
        // Issue SQL statement.
        int deletedRows = db.delete(PlacesTable.PlacesEntry.TABLE_NAME, selection, selectionArgs);
        return deletedRows;
    }

    public int getNumRow(){
        String countQuery = "SELECT  * FROM " + PlacesTable.PlacesEntry.TABLE_NAME;
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int numRow = cursor.getCount();
        cursor.close();

        // return count
        return numRow;
    }

    public void closeDatabase(){
        sqLiteHelper.close();
    }
}
