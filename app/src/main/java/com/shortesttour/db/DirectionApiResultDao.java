package com.shortesttour.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface DirectionApiResultDao {

    @Query("SELECT * FROM directionapiresult")
    List<DirectionApiResult> getAll();

    @Query("SELECT * FROM directionapiresult WHERE srcLat == :srcLat " +
            " AND srcLng == :srcLng " +
            " AND desLat == :desLat " +
            " AND desLng == :desLng ")
    DirectionApiResult getApiResult(double srcLat,double srcLng,double desLat,double desLng);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DirectionApiResult result);

    @Query("DELETE FROM DirectionApiResult")
    void deleteAll();

    @Query("DELETE FROM DirectionApiResult WHERE srcLat == :srcLat " +
            " AND srcLng == :srcLng " +
            " AND desLat == :desLat " +
            " AND desLng == :desLng ")
    void delete(double srcLat,double srcLng,double desLat,double desLng);

    @Delete
    void delete(DirectionApiResult... directionApiResults);
}
