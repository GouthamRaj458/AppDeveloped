package com.implantaire.weatherforecast.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.matteobattilana.weather.BuildConfig
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.implantaire.weatherforecast.R
import com.implantaire.weatherforecast.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var themeSwitch: SwitchCompat
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.settings
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.settings -> {
                    true
                }
                else -> {
                    false
                }
            }
        }
            themeSwitch = findViewById(R.id.themeSwitch)
            val sharedPreferences = getSharedPreferences("theme", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val nightMode = sharedPreferences.getBoolean("night", false)
            if (nightMode) {
                themeSwitch.isChecked = true
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            themeSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }, 200)
                    editor.putBoolean("night", true)
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }, 200)
                    editor.putBoolean("night", false)
                }
                editor.apply()

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            val appVersion = findViewById<TextView>(R.id.app_version)
            appVersion.text = buildString {
                append("Version: ")
                append(BuildConfig.VERSION_NAME)
            }
    }
}

