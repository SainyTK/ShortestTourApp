package com.shortesttour.utils.Room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PlaceDao {
    @Query("SELECT * FROM placeentity ORDER BY placeId ASC")
    LiveData<List<PlaceEntity>> getAll();

    @Query("SELECT * FROM placeentity WHERE placeId IN (:userIds)")
    List<PlaceEntity> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM placeentity WHERE placeTitle LIKE :placeTitle "
            + "LIMIT 1")
    PlaceEntity findByName(String placeTitle);

    @Insert
    void insertAll(PlaceEntity... placeEntities);

    @Delete
    void delete(PlaceEntity placeEntity);

    @Query("DELETE FROM PlaceEntity")
    void deleteAll();
}
