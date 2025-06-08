package com.matancita.loteria.notificacion

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    const val DAILY_REMINDER_WORK_TAG = "daily_lucky_number_reminder"

    fun scheduleDailyReminder(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Calcular el tiempo hasta las 8 AM
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)

            // Si ya pasaron las 8 AM hoy, programar para mañana
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

        // Crear la solicitud de trabajo periódico
        val dailyWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            24, TimeUnit.HOURS // Repetir cada 24 horas
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(DAILY_REMINDER_WORK_TAG)
            .build()

        // Poner en cola el trabajo, reemplazando cualquier trabajo anterior
        workManager.enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK_TAG,
            ExistingPeriodicWorkPolicy.REPLACE, // Reemplaza la tarea si ya existe
            dailyWorkRequest
        )
    }
}