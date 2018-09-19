package com.shortesttour.utils.SQLite;

import android.provider.BaseColumns;

public final class PlacesTable {

    private PlacesTable(){}

    public static class PlacesEntry implements BaseColumns{
        public static final String TABLE_NAME = "places";
        public static final String COLUMN_NAME_PLACE_TITLE = "placeTitle";
        public static final String COLUMN_NAME_USER_NAME = "userName";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longtitude";
    }

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + PlacesEntry.TABLE_NAME + " (" +
                    PlacesEntry._ID + " INTEGER PRIMARY KEY," +
                    PlacesEntry.COLUMN_NAME_PLACE_TITLE + " TEXT," +
                    PlacesEntry.COLUMN_NAME_USER_NAME + " TEXT," +
                    PlacesEntry.COLUMN_NAME_LATITUDE + " FLOAT," +
                    PlacesEntry.COLUMN_NAME_LONGITUDE + " FLOAT)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PlacesEntry.TABLE_NAME;
}
