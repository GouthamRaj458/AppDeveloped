package com.implantaire.weatherforecast.viewmodel

import androidx.lifecycle.ViewModel
import com.implantaire.weatherforecast.repository.WeatherRepository
import com.implantaire.weatherforecast.server.ApiClient
import com.implantaire.weatherforecast.server.ApiServices

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel()  {
    constructor():this(WeatherRepository(ApiClient().getClient().create(ApiServices::class.java)))

    fun loadCurrentWeather(lat:Double,lon:Double,unit:String)=
        repository.getCurrentWeather(lat,lon,unit)

    fun loadForecastWeather(lat:Double,lon:Double,unit:String)=
        repository.getForecastWeather(lat,lon,unit)
}

