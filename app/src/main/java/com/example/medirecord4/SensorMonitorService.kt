package com.example.medirecord4

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.math.sqrt

/**
 * Servicio de monitoreo de sensores para detectar:
 * - Caídas del usuario (adultos mayores)
 * - Actividad física relacionada con la salud
 * - Gestos para confirmar toma de medicamentos
 */
class SensorMonitorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    
    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    
    // Umbrales para detección de caídas
    private val FALL_THRESHOLD = 30f // Aceleración en m/s²
    private val SHAKE_THRESHOLD = 15f // Para confirmar toma de medicamento
    
    // Contador de actividad física (pasos estimados)
    private var stepCount = 0
    private var lastStepTime = 0L
    
    // Estado para detectar gestos
    private var isShaking = false
    private var shakeStartTime = 0L
    
    companion object {
        private const val TAG = "SensorMonitorService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "sensor_monitor_channel"
    }

    override fun onCreate() {
        super.onCreate()
        
        // Inicializar sensor manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        // Obtener sensores disponibles
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        
        // Registrar listeners
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Acelerómetro registrado")
        }
        
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Giroscopio registrado")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        
        val notification = createForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        Log.d(TAG, "Servicio de monitoreo de sensores iniciado")
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(it)
                Sensor.TYPE_GYROSCOPE -> handleGyroscope(it)
            }
        }
    }

    private fun handleAccelerometer(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastUpdate < 100) return // Limitar frecuencia de procesamiento
        
        lastUpdate = currentTime
        
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        // Calcular magnitud de aceleración
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        
        // Detectar caídas (aceleración súbita)
        if (acceleration > FALL_THRESHOLD) {
            detectFall(acceleration)
        }
        
        // Detectar gesto de sacudir (confirmar toma de medicamento)
        val deltaX = kotlin.math.abs(x - lastX)
        val deltaY = kotlin.math.abs(y - lastY)
        val deltaZ = kotlin.math.abs(z - lastZ)
        
        if (deltaX > SHAKE_THRESHOLD || deltaY > SHAKE_THRESHOLD || deltaZ > SHAKE_THRESHOLD) {
            if (!isShaking) {
                isShaking = true
                shakeStartTime = currentTime
            } else if (currentTime - shakeStartTime > 500) { // Sacudir por 500ms
                detectMedicationConfirmation()
                isShaking = false
            }
        } else {
            isShaking = false
        }
        
        // Contador simple de pasos (basado en picos de aceleración)
        if (acceleration > 11f && acceleration < 15f) {
            if (currentTime - lastStepTime > 300) { // Al menos 300ms entre pasos
                stepCount++
                lastStepTime = currentTime
                
                // Actualizar actividad cada 100 pasos
                if (stepCount % 100 == 0) {
                    updateActivityStatus(stepCount)
                }
            }
        }
        
        lastX = x
        lastY = y
        lastZ = z
    }

    private fun handleGyroscope(event: SensorEvent) {
        // Implementar lógica adicional con giroscopio si es necesario
        // Por ejemplo, detectar rotaciones bruscas que indiquen caída
    }

    private fun detectFall(acceleration: Float) {
        Log.w(TAG, "¡POSIBLE CAÍDA DETECTADA! Aceleración: $acceleration m/s²")
        
        // Enviar notificación de emergencia
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚠️ ALERTA DE CAÍDA")
            .setContentText("Se detectó una posible caída. ¿Estás bien?")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        notificationManager.notify(2001, notification)
    }

    private fun detectMedicationConfirmation() {
        Log.i(TAG, "Gesto de confirmación de medicamento detectado")
        
        // Enviar notificación de confirmación
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("✓ Medicamento Confirmado")
            .setContentText("Gesto detectado. Recuerda marcar como tomado en la app.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(2002, notification)
    }

    private fun updateActivityStatus(steps: Int) {
        Log.i(TAG, "Actividad física: $steps pasos registrados")
        
        // Aquí se podría guardar en la base de datos o enviar a un servidor
        // Por ahora solo registramos en log
    }

    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, DashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("MediRecord - Monitoreo Activo")
            .setContentText("Detectando caídas y actividad física")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Monitoreo de Sensores",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones del servicio de monitoreo de sensores"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No se requiere implementación específica
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        Log.d(TAG, "Servicio de monitoreo de sensores detenido")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

