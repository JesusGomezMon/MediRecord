package com.example.medirecord4

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Servicio de Firebase Cloud Messaging para:
 * - Recordatorios de medicamentos remotos
 * - Alertas de stock bajo
 * - Notificaciones de emergencia
 */
class FCMService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID_MEDICATION = "medication_reminders"
        private const val CHANNEL_ID_ALERTS = "stock_alerts"
        private const val CHANNEL_ID_EMERGENCY = "emergency_alerts"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM: $token")
        
        // Aquí deberías enviar el token a tu servidor
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "Mensaje recibido de: ${message.from}")
        
        // Procesar la notificación según el tipo
        message.data.let { data ->
            val type = data["type"] ?: "general"
            val title = data["title"] ?: "MediRecord"
            val body = data["body"] ?: ""
            val medicationId = data["medication_id"]
            val priority = data["priority"] ?: "normal"
            
            when (type) {
                "medication_reminder" -> {
                    showMedicationReminder(title, body, medicationId)
                }
                "low_stock_alert" -> {
                    showLowStockAlert(title, body, medicationId)
                }
                "emergency" -> {
                    showEmergencyAlert(title, body)
                }
                else -> {
                    showGeneralNotification(title, body)
                }
            }
        }
        
        // También procesar notificación si viene en el payload de notificación
        message.notification?.let {
            showGeneralNotification(
                it.title ?: "MediRecord",
                it.body ?: ""
            )
        }
    }

    private fun showMedicationReminder(title: String, body: String, medicationId: String?) {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            medicationId?.let { putExtra("medication_id", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_MEDICATION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💊 $title")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun showLowStockAlert(title: String, body: String, medicationId: String?) {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            medicationId?.let { putExtra("medication_id", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚠️ $title")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun showEmergencyAlert(title: String, body: String) {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_EMERGENCY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚨 $title")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun showGeneralNotification(title: String, body: String) {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_MEDICATION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // Canal para recordatorios de medicamentos
            val medicationChannel = NotificationChannel(
                CHANNEL_ID_MEDICATION,
                "Recordatorios de Medicamentos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para recordar tomar medicamentos"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }
            
            // Canal para alertas de stock bajo
            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "Alertas de Stock",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas cuando el stock de medicamentos está bajo"
            }
            
            // Canal para emergencias
            val emergencyChannel = NotificationChannel(
                CHANNEL_ID_EMERGENCY,
                "Alertas de Emergencia",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de emergencia que requieren atención inmediata"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            }
            
            notificationManager.createNotificationChannel(medicationChannel)
            notificationManager.createNotificationChannel(alertsChannel)
            notificationManager.createNotificationChannel(emergencyChannel)
        }
    }

    private fun sendTokenToServer(token: String) {
        // Implementar envío del token a tu servidor backend
        // Por ejemplo, usando Retrofit:
        /*
        lifecycleScope.launch {
            try {
                apiService.updateFCMToken(userId, token)
                Log.d(TAG, "Token enviado al servidor")
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar token: ${e.message}")
            }
        }
        */
        Log.d(TAG, "Token listo para enviar al servidor: $token")
    }
}

