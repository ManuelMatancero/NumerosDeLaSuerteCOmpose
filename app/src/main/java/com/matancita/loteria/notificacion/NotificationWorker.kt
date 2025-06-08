package com.matancita.loteria.notificacion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.matancita.loteria.MainActivity
import com.matancita.loteria.R

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "LUCKY_NUMBERS_REMINDER_CHANNEL"
        const val NOTIFICATION_ID = 101
    }

    override suspend fun doWork(): Result {
        // La lógica ahora es más simple: solo muestra la notificación.
        showNotification()

        // Indica que el trabajo se completó exitosamente.
        return Result.success()
    }

    private fun showNotification() {
        // 1. Crear el canal de notificación (solo para Android 8.0+)
        createNotificationChannel()

        // 2. Crear un intent para abrir la app al tocar la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // 3. Construir la notificación
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.suertelogo) // ¡IMPORTANTE: Reemplaza con tu propio ícono!
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Acción al tocar
            .setAutoCancel(true) // La notificación se cierra al tocarla

        // 4. Mostrar la notificación
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Si no tenemos permiso (Android 13+), no hacemos nada.
                // La solicitud de permisos debe manejarse en la UI.
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Registrar el canal con el sistema
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}