package com.example.medirecord4

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Helper para gestionar notificaciones locales programadas
 * Para recordatorios diarios de medicamentos
 */
class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID_DAILY = "daily_reminders"
        private const val CHANNEL_ID_CONFIRMATION = "medication_confirmation"
    }

    init {
        createNotificationChannels()
    }

    /**
     * Programa un recordatorio diario para un medicamento
     */
    fun scheduleDailyMedicationReminder(
        medicationId: String,
        medicationName: String,
        dosis: String,
        hora: String // Formato "HH:mm"
    ) {
        val inputData = workDataOf(
            "medication_id" to medicationId,
            "medication_name" to medicationName,
            "dosis" to dosis,
            "hora" to hora
        )

        // Calcular el delay hasta la próxima hora programada
        val delay = calculateDelayToNextScheduledTime(hora)

        val workRequest = PeriodicWorkRequestBuilder<MedicationReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("medication_$medicationId")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "medication_reminder_$medicationId",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }

    /**
     * Cancela un recordatorio programado
     */
    fun cancelMedicationReminder(medicationId: String) {
        WorkManager.getInstance(context)
            .cancelUniqueWork("medication_reminder_$medicationId")
    }

    /**
     * Muestra una notificación de recordatorio inmediata
     */
    fun showMedicationReminder(
        medicationName: String,
        dosis: String,
        hora: String
    ) {
        val intent = Intent(context, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_DAILY)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💊 Hora de tu medicamento")
            .setContentText("$medicationName - $dosis")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Es hora de tomar: $medicationName\nDosis: $dosis\nHora: $hora"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    /**
     * Muestra una notificación de confirmación de toma
     */
    fun showConfirmationNotification(medicationName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_CONFIRMATION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("✓ Medicamento Tomado")
            .setContentText("$medicationName marcado como tomado")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    /**
     * Calcula el delay en milisegundos hasta la próxima hora programada
     */
    private fun calculateDelayToNextScheduledTime(hora: String): Long {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()
        
        try {
            val scheduledTime = sdf.parse(hora)
            val scheduledCalendar = Calendar.getInstance().apply {
                time = scheduledTime ?: return 0L
                set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            }

            // Si la hora ya pasó hoy, programar para mañana
            if (scheduledCalendar.before(calendar)) {
                scheduledCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            return scheduledCalendar.timeInMillis - calendar.timeInMillis
        } catch (e: Exception) {
            return 0L
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            // Canal para recordatorios diarios
            val dailyChannel = NotificationChannel(
                CHANNEL_ID_DAILY,
                "Recordatorios Diarios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios diarios para tomar medicamentos"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            // Canal para confirmaciones
            val confirmationChannel = NotificationChannel(
                CHANNEL_ID_CONFIRMATION,
                "Confirmaciones",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Confirmaciones de toma de medicamentos"
            }

            notificationManager.createNotificationChannel(dailyChannel)
            notificationManager.createNotificationChannel(confirmationChannel)
        }
    }
}

/**
 * Worker para ejecutar recordatorios periódicos
 */
class MedicationReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val medicationName = inputData.getString("medication_name") ?: return Result.failure()
        val dosis = inputData.getString("dosis") ?: return Result.failure()
        val hora = inputData.getString("hora") ?: return Result.failure()

        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.showMedicationReminder(medicationName, dosis, hora)

        return Result.success()
    }
}

