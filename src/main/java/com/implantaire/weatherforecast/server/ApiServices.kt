package com.implantaire.weatherforecast.server

import com.implantaire.weatherforecast.models.CityResponseApi
import com.implantaire.weatherforecast.models.CurrentResponseApi
import com.implantaire.weatherforecast.models.ForeCastResponseApi
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {
    @GET("/data/2.5/weather")
    fun getCurrentWeather(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("units") unit:String,
        @Query("appid") apiKey:String,
        ): Call<CurrentResponseApi>

    @GET("/data/2.5/forecast")
    fun getForecastWeather(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("units") unit:String,
        @Query("appid") apikey:String,
    ): Call<ForeCastResponseApi>

    @GET("/geo/1.0/direct")
    fun getCitiesList(
        @Query("q")q:String,
        @Query("limit") limiter: Int,
        @Query("appid") apiKey: String
    ):Call<CityResponseApi>
}