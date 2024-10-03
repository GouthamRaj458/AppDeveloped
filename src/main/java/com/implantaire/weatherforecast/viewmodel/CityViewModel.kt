package com.implantaire.weatherforecast.viewmodel

import androidx.lifecycle.ViewModel
import com.implantaire.weatherforecast.repository.CityRepository
import com.implantaire.weatherforecast.server.ApiClient
import com.implantaire.weatherforecast.server.ApiServices

class CityViewModel( val respository:CityRepository):ViewModel() {
    constructor():this (CityRepository(ApiClient().getClient().create(ApiServices::class.java)))

    fun loadCity(q:String,limit:Int)=
        respository.getCities(q,limit)
}