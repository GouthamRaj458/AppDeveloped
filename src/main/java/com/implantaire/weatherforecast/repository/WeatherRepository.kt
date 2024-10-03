package com.implantaire.weatherforecast.repository

import com.implantaire.weatherforecast.server.ApiServices

class WeatherRepository(val api:ApiServices) {
    fun getCurrentWeather(lat:Double,lon:Double,unit:String)=
    api.getCurrentWeather(lat,lon,unit,"0cec0414198442e70d99a6b67b99836f")

    fun getForecastWeather(lat:Double,lon:Double,unit:String)=
        api.getForecastWeather(lat,lon,unit,"0cec0414198442e70d99a6b67b99836f")
}