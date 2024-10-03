package com.implantaire.weatherforecast.repository

import com.implantaire.weatherforecast.server.ApiServices

class CityRepository(private val api:ApiServices) {
    fun getCities(q:String , limit:Int)= api.getCitiesList(q,limit,"0cec0414198442e70d99a6b67b99836f")
}