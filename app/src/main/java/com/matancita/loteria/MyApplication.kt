package com.matancita.loteria

import android.app.Application
import com.google.android.gms.ads.MobileAds

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa el SDK de Mobile Ads
        MobileAds.initialize(this) {} // El {} es un listener opcional de finalización
    }
}