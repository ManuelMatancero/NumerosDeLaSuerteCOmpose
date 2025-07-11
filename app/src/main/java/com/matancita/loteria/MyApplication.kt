package com.matancita.loteria

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.matancita.loteria.notificacion.NotificationScheduler

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa el SDK de Mobile Ads
        MobileAds.initialize(this)
        // Programar la notificación diaria cuando la app se inicia por primera vez.
        NotificationScheduler.scheduleDailyReminder(this)
    }
}