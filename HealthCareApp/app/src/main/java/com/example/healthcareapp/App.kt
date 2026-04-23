package com.example.healthcareapp

import android.app.Application
import co.airbridge.sdk.android.Airbridge
import com.example.healthcareapp.BuildConfig
import com.example.healthcareapp.network.RetrofitClient

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Airbridge.init(this, "ttdev", BuildConfig.AIRBRIDGE_APP_TOKEN)
        RetrofitClient.tokenProvider = {
            getSharedPreferences("auth_prefs", MODE_PRIVATE)
                .getString("access_token", null)
        }
    }
}
