package com.shortesttour.utils.Room;

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
    LiveData<List<DirectionApiResult>> getAll();

    @Query("SELECT * FROM directionapiresult WHERE sourceId = :sourceId AND destinationId = :destinationId")
    LiveData<DirectionApiResult> getApiResult(int sourceId,int destinationId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DirectionApiResult result);

    @Query("DELETE FROM DirectionApiResult")
    void deleteAll();

    @Query("DELETE FROM DirectionApiResult WHERE sourceId = :sourceId AND destinationId = :destinationId")
    void delete(int sourceId,int destinationId);

    @Delete
    void delete(DirectionApiResult... directionApiResults);
}
