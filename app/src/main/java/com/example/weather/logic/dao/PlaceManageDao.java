package com.example.weather.logic.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.weather.logic.model.PlaceManage;

import java.util.List;

@Dao
public interface PlaceManageDao {

    @Insert
    long insertPlaceManage(PlaceManage placeManage);

    @Update
    void updatePlaceManage(PlaceManage placeManage);

    @Query("select * from PlaceManage")
    List<PlaceManage> loadAllPlaceManages();

    @Query("select * from PlaceManage where lng = :lng and lat = :lat")
    PlaceManage querySpecifyPlaceManage(String lng, String lat);

    @Query("delete from PlaceManage where lng = :lng and lat = :lat")
    int deletePlaceManageByLngLat(String lng, String lat);
}
