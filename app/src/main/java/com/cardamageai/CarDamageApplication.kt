package com.cardamageai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CarDamageApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}