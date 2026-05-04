package com.example.healthcareapp

import android.app.Application
import com.example.healthcareapp.network.RetrofitClient

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        RetrofitClient.tokenProvider = {
            getSharedPreferences("auth_prefs", MODE_PRIVATE)
                .getString("access_token", null)
        }
    }
}
