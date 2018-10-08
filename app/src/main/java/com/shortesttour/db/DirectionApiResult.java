package com.shortesttour.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(primaryKeys = {"srcLat","srcLng","desLat","desLng"})

public class DirectionApiResult {

    @ColumnInfo
    private double srcLat;

    @ColumnInfo
    private double srcLng;

    @ColumnInfo
    private double desLat;

    @ColumnInfo
    private double desLng;

    @ColumnInfo
    private String requestUrl;

    @ColumnInfo
    private String apiResult;

    public double getSrcLat() {
        return srcLat;
    }

    public void setSrcLat(double srcLat) {
        this.srcLat = srcLat;
    }

    public double getSrcLng() {
        return srcLng;
    }

    public void setSrcLng(double srcLng) {
        this.srcLng = srcLng;
    }

    public double getDesLat() {
        return desLat;
    }

    public void setDesLat(double desLat) {
        this.desLat = desLat;
    }

    public double getDesLng() {
        return desLng;
    }

    public void setDesLng(double desLng) {
        this.desLng = desLng;
    }

    public String getApiResult() {
        return apiResult;
    }

    public void setApiResult(String apiResult) {
        this.apiResult = apiResult;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    @Override
    public String toString() {
        return "Src = " + srcLat + "," + srcLng + " Des = " + desLat + "," + desLng + " req = " + requestUrl + " res = " + apiResult;
    }
}
