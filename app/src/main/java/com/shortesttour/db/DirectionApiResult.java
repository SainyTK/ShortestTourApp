package com.shortesttour.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(primaryKeys = {"sourceId","destinationId"})

public class DirectionApiResult {

    @ColumnInfo
    private int sourceId;

    @ColumnInfo
    private int destinationId;

    @ColumnInfo
    private String requestUrl;

    @ColumnInfo
    private String apiResult;

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(int destinationId) {
        this.destinationId = destinationId;
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
        return "SrcId = " + sourceId +" DesId = " + destinationId + " req = " + requestUrl + " res = " + apiResult;
    }
}
