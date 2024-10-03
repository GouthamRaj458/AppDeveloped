package com.implantaire.weatherforecast.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.matteobattilana.weather.PrecipType
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.implantaire.weatherforecast.R
import com.implantaire.weatherforecast.adapter.ForecastAdapter
import com.implantaire.weatherforecast.databinding.ActivityMainBinding
import com.implantaire.weatherforecast.models.CurrentResponseApi
import com.implantaire.weatherforecast.models.ForeCastResponseApi
import com.implantaire.weatherforecast.viewmodel.WeatherViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.text.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val forecastAdapter by lazy { ForecastAdapter() }
    private val weatherViewModel: WeatherViewModel by viewModels()
    private var isCitySearched = false // Flag to track if a city has been searched
    private var locationCallback: LocationCallback? = null // Declare as class-level variable
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                fetchLocationAndLoadWeather()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
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
        bottomNavigationView.selectedItemId = R.id.home
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    true
                }
                R.id.settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> {
                    false
                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fetchLocationAndLoadWeather()
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdateDelayMillis(5000)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val newLatitude = location.latitude
                    val newLongitude = location.longitude
                    if (currentLatitude != newLatitude || currentLongitude != newLongitude) {
                        currentLatitude = newLatitude
                        currentLongitude = newLongitude
                        loadWeatherData(newLatitude, newLongitude)
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback as LocationCallback, Looper.getMainLooper())
        val getResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    if (data != null) {
                        val lat = data.getDoubleExtra("lat", 0.0)
                        val lon = data.getDoubleExtra("lon", 0.0)
                        val city = data.getStringExtra("city") ?: ""
                        currentLatitude = lat
                        currentLongitude = lon
                        loadWeatherData(lat, lon, city)
                        locationCallback.let {
                            fusedLocationClient.removeLocationUpdates(it)
                        }
                        isCitySearched = true
                    }
                }
            }
        binding.addCity.setOnClickListener {
            val intent = Intent(this@MainActivity, CityListActivity::class.java)
            getResult.launch(intent)
        }
        binding.apply {

            val radius = 10f
            val decorView = window.decorView
            val rootView = (decorView.findViewById<View>(android.R.id.content) as ViewGroup?)
            val windowBackground = decorView.background
            rootView?.let {
                blurView.setupWith(it)
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(radius)
                blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                blurView.clipToOutline = true


                bottomNavigationView.bringToFront()
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (!isCitySearched) {
            fetchLocationAndLoadWeather()
        }
    }
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CityListActivity.CITY_REQUEST_CODE && resultCode == RESULT_OK) {
            currentLatitude = data!!.getDoubleExtra("lat", 0.0)
            currentLongitude = data.getDoubleExtra("lon", 0.0)
            val searchedCityName = data.getStringExtra("city") ?: ""
            loadWeatherData(currentLatitude, currentLongitude, searchedCityName)
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
            isCitySearched = true
        }
    }
    @SuppressLint("MissingPermission")
    private fun fetchLocationAndLoadWeather() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    loadWeatherData(currentLatitude, currentLongitude)
                } else {
                    Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun loadWeatherData(latitude: Double, longitude: Double, cityName: String = "") {
        binding.apply {
            cityText.text = cityName.ifEmpty { "Loading..." }
            progressBar.visibility = View.VISIBLE
            weatherViewModel.loadCurrentWeather(latitude, longitude, "metric")
                .enqueue(object : Callback<CurrentResponseApi> {
                    override fun onResponse(
                        call: Call<CurrentResponseApi>,
                        response: Response<CurrentResponseApi>
                    ) {
                        if (response.isSuccessful) {
                            val data = response.body()
                            progressBar.visibility = View.GONE
                            detailedLayout.visibility = View.VISIBLE
                            data?.let { it ->
                                if (cityName.isEmpty()) {
                                    cityText.text = it.name ?: "Unknown Location"
                                }
                                status.text = it.weather?.get(0)?.main ?: "-"
                                windText.text = buildString {
                                    append(it.wind?.speed?.let { Math.round(it).toString() })
                                    append("Km/h")
                                }
                                humidityText.text = buildString {
                                    append(it.main?.humidity?.toString() ?: "-")
                                    append("%")
                                }
                                currentTemp.text =
                                    buildString {
                                        append(it.main?.temp?.let { Math.round(it).toString() })
                                        append("°")
                                }
                                maxTemp.text =
                                    buildString {
                                        append(it.main?.tempMax?.let { Math.round(it).toString() })
                                        append("°")
                                }
                                minTemp.text =
                                    buildString {
                                        append(it.main?.tempMin?.let { Math.round(it).toString() })
                                        append("°")
                                }
                                val drawable = if (isNightNow()) R.drawable.night_bg
                                else {
                                    setDynamicWallpaper(it.weather?.get(0)?.icon ?: "-")
                                }
                                backgroundImage.setImageResource(drawable)
                                setRainSnowEffect(it.weather?.get(0)?.icon ?: "-")
                            }
                        }
                    }
                    override fun onFailure(call: Call<CurrentResponseApi>, t: Throwable) {
                        Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT)
                            .show()
                    }
                })
            forecastView.apply {
                layoutManager = LinearLayoutManager(
                    this@MainActivity, LinearLayoutManager.HORIZONTAL,false
                )
                adapter = forecastAdapter
            }
            weatherViewModel.loadForecastWeather(latitude, longitude, "metric")
                .enqueue(object : Callback<ForeCastResponseApi> {
                    override fun onResponse(
                        call: Call<ForeCastResponseApi>,
                        response: Response<ForeCastResponseApi>
                    ) {
                        if (response.isSuccessful) {
                            val data = response.body()
                            blurView.visibility = View.VISIBLE

                            data?.let {
                                forecastAdapter.differ.submitList(it.list)
                            }
                        }
                    }
                    override fun onFailure(call: Call<ForeCastResponseApi>, t: Throwable) {
                        t.message?.let {
                            Toast.makeText(
                                this@MainActivity,"Unable to load the data", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }
    }
    private fun isNightNow(): Boolean {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY) >= 18
    }
    private fun setDynamicWallpaper(icon: String): Int {
        return when (icon.dropLast(1)) {
            "01" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.snow_bg
            }
            "02", "03", "04" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.cloudy_bg
            }
            "09", "10", "11" -> {
                initWeatherView(PrecipType.RAIN)
                R.drawable.rainy_bg
            }
            "13" -> {
                initWeatherView(PrecipType.SNOW)
                R.drawable.snow_bg
            }
            "50" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.haze_bg
            }
            else -> 0
        }
    }
    private fun setRainSnowEffect(icon: String) {
        when (icon.dropLast(1)) {
            "01" -> initWeatherView(PrecipType.CLEAR)
            "02", "03", "04" -> initWeatherView(PrecipType.CLEAR)
            "09", "10", "11" -> initWeatherView(PrecipType.RAIN)
            "13" -> initWeatherView(PrecipType.SNOW)
            "50" -> initWeatherView(PrecipType.CLEAR)
            else -> {}
        }
    }
    private fun initWeatherView(type: PrecipType) {
        binding.weatherView.apply {
            setWeatherData(type)
            angle = -20
            emissionRate = 100.0f
        }
    }
}