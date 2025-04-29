package com.example.weather.logic.dao;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.weather.WeatherApplication;
import com.example.weather.logic.model.Place;
import com.google.gson.Gson;

public class PlaceDao {

    public static void savePlace(Place place) {
        SharedPreferences.Editor editor = sharedPreferences().edit();
        editor.putString("place", new Gson().toJson(place));
        editor.apply();
    }

    public static Place getSavedPlace() {
        String placeJson = sharedPreferences().getString("place", "");
        return new Gson().fromJson(placeJson, Place.class);
    }

    public static boolean isPlaceSaved() {
        return sharedPreferences().contains("place");
    }

    private static SharedPreferences sharedPreferences() {
        return WeatherApplication.context.getSharedPreferences("weather", Context.MODE_PRIVATE);
    }
}

// 兼容：30 - 34都可以，29不确定
// api30 和 api32 不一样    红米k60pro是33-34左右    oppo版本是33左右    华为是33
// 不同的安卓版本的 permission 不一样，位置共享

// dao层调用DBHelper  DBHelper调用底层数据库
// dao层被 model 用，减少重复率，复用率比较高
// SQLite的数据结构