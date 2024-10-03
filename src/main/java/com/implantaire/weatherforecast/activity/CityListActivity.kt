package com.implantaire.weatherforecast.activity


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.implantaire.weatherforecast.R
import com.implantaire.weatherforecast.adapter.CityAdapter
import com.implantaire.weatherforecast.adapter.RecentSearchAdapter
import com.implantaire.weatherforecast.databinding.ActivityCityListBinding
import com.implantaire.weatherforecast.models.CityResponseApi
import com.implantaire.weatherforecast.viewmodel.CityViewModel
import com.implantaire.weatherforecast.viewmodel.RecentSearchViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CityListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCityListBinding
    private val cityAdapter by lazy { CityAdapter() }
    private val cityViewModel: CityViewModel by viewModels()
    private lateinit var recentSearchViewModel: RecentSearchViewModel
    private lateinit var recentSearchAdapter: RecentSearchAdapter

    companion object {
        const val CITY_REQUEST_CODE = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        binding = ActivityCityListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
        val sharedPreferences = getSharedPreferences("theme", Context.MODE_PRIVATE)
        val nightMode = sharedPreferences.getBoolean("night", false)
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        binding.bottomNavigation.visibility = View.VISIBLE
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                    true
                }
                R.id.settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> {
                    bottomNavigationView.menu.findItem(R.id.home).iconTintList = null
                    bottomNavigationView.menu.findItem(R.id.settings).iconTintList = null
                    false
                }
            }
        }
        binding.apply {
            city.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    cityProgressBar.visibility = View.VISIBLE
                    cityViewModel.loadCity(s.toString(), 10)
                        .enqueue(object : Callback<CityResponseApi> {
                            override fun onResponse(
                                call: Call<CityResponseApi>,
                                response: Response<CityResponseApi>
                            ) {
                                if (response.isSuccessful) {
                                    val data = response.body()
                                    data?.let {
                                        cityProgressBar.visibility = View.GONE
                                        cityAdapter.differ.submitList(it)
                                        cityView.apply {
                                            layoutManager = LinearLayoutManager(
                                                this@CityListActivity, LinearLayoutManager.HORIZONTAL, false)
                                            adapter = cityAdapter
                                        }
                                        recentSearchViewModel.saveRecentSearch(s.toString())
                                    }
                                }
                            }
                            override fun onFailure(call: Call<CityResponseApi>, t: Throwable) {}
                        })
                }
            })
            recentSearchViewModel = RecentSearchViewModel(applicationContext)
            recentSearchAdapter =
                RecentSearchAdapter(object : RecentSearchAdapter.OnItemClickListener {
                    override fun onItemClick(searchValue: String) {
                        cityProgressBar.visibility = View.VISIBLE
                        cityViewModel.loadCity(searchValue, 10)
                            .enqueue(object : Callback<CityResponseApi> {
                                override fun onResponse(
                                    call: Call<CityResponseApi>,
                                    response: Response<CityResponseApi>
                                ) {
                                    if (response.isSuccessful) {
                                        val data = response.body()
                                        data?.let {
                                            cityProgressBar.visibility = View.GONE
                                            cityAdapter.differ.submitList(it)
                                            cityView.apply {
                                                layoutManager = LinearLayoutManager(
                                                    this@CityListActivity,
                                                    LinearLayoutManager.HORIZONTAL,
                                                    false
                                                )
                                                adapter = cityAdapter
                                            }
                                        }
                                    }
                                }
                                override fun onFailure(call: Call<CityResponseApi>, t: Throwable) {}
                            })
                    }
                })
        }
        binding.recentSearchView.apply {
            layoutManager = LinearLayoutManager(this@CityListActivity, LinearLayoutManager.VERTICAL, false)
            adapter = recentSearchAdapter
        }
        val recentSearches = recentSearchViewModel.getRecentSearches()
        val recentSearchList = recentSearches.split(",").toSet().toSet().toSet().reversed().take(5)
        recentSearchAdapter.submitList(recentSearchList.toList())
    }
}