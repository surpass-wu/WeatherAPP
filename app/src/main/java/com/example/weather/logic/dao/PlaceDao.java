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
