package com.example.stock

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StockApplication : Application(), Configuration.Provider {
    
    override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(this, workManagerConfiguration)
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
} 