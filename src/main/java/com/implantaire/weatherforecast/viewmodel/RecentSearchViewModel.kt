package com.implantaire.weatherforecast.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel

class RecentSearchViewModel(private val context: Context) : ViewModel() {
    private var recentSearches: MutableList<String> = mutableListOf()
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("recent_searches", Context.MODE_PRIVATE)

    init {
        recentSearches =
            (sharedPreferences.getString("recent_searches", "")?.split(",")?.toSet()?.toSet() ?: mutableListOf()).toMutableList()
    }

    fun getRecentSearches(): String {
        return recentSearches.joinToString(",")
    }

    fun saveRecentSearch(cityText: String) {
        if (!recentSearches.contains(cityText)) {
            recentSearches.add(cityText)
            sharedPreferences.edit().putString("recent_searches", recentSearches.joinToString(",")).apply()
        }
    }
}
