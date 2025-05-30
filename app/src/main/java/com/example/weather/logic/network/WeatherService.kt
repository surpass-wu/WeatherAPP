package com.example.weather.logic.network

import com.example.weather.WeatherApplication
import com.example.weather.logic.model.DailyResponse
import com.example.weather.logic.model.HourlyResponse
import com.example.weather.logic.model.RealtimeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

// 定义与天气 API 交互的 Retrofit 接口，包含实时天气、小时级天气和每日天气的请求方法。
interface WeatherService {

    @GET("v2.6/${WeatherApplication.TOKEN}/{lng},{lat}/realtime")
    fun getRealtimeWeather(@Path("lng") lng: String, @Path("lat") lat: String): Call<RealtimeResponse>
    @GET("v2.6/${WeatherApplication.TOKEN}/{lng},{lat}/hourly?hourlysteps=24")
    fun getHourlyWeather(@Path("lng") lng: String, @Path("lat") lat: String): Call<HourlyResponse>
    @GET("v2.6/${WeatherApplication.TOKEN}/{lng},{lat}/daily?dailysteps=7")
    fun getDailyWeather(@Path("lng") lng: String, @Path("lat") lat: String): Call<DailyResponse>
}