package com.example.deliveryshipperapp

import android.app.Application
import com.example.deliveryshipperapp.utils.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ShipperApp: Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}