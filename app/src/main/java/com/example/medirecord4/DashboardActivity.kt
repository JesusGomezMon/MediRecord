package com.example.medirecord4

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DashboardActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var notificationHelper: NotificationHelper
    private val usuarioActual = "usr-001" // Usuario de ejemplo
    private var isSensorServiceRunning = false
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Habilitar botón de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Dashboard"

        // Inicializar base de datos y helpers
        dbHelper = DatabaseHelper(this)
        notificationHelper = NotificationHelper(this)

        // Solicitar permisos necesarios
        requestNecessaryPermissions()

        // Configurar botones
        setupButtons()

        // Mostrar información de la base de datos
        mostrarInformacionDB()

        // Mostrar medicamentos para marcar como tomados
        mostrarMedicamentosParaTomar()
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnAgregarMedicamento).setOnClickListener {
            val intent = Intent(this, AgregarMedicamentoActivity::class.java)
            intent.putExtra("USUARIO_ID", usuarioActual)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnAgregarRecordatorio).setOnClickListener {
            val intent = Intent(this, AgregarRecordatorioActivity::class.java)
            intent.putExtra("USUARIO_ID", usuarioActual)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnAgregarCita).setOnClickListener {
            val intent = Intent(this, AgregarCitaActivity::class.java)
            intent.putExtra("USUARIO_ID", usuarioActual)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnVerHistorial).setOnClickListener {
            val intent = Intent(this, HistorialActivity::class.java)
            intent.putExtra("USUARIO_ID", usuarioActual)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnBuscarOnline).setOnClickListener {
            val intent = Intent(this, BuscarMedicamentoOnlineActivity::class.java)
            startActivity(intent)
        }
        
        // Nuevo botón: Ver farmacias cercanas
        findViewById<Button>(R.id.btnVerFarmacias).setOnClickListener {
            // Usar versión externa (no requiere API Key)
            val intent = Intent(this, FarmaciasMapaExternoActivity::class.java)
            startActivity(intent)
        }
        
        // Nuevo botón: Activar/Desactivar servicio de sensores
        findViewById<Button>(R.id.btnActivarSensores).setOnClickListener {
            toggleSensorService()
        }
    }
    
    /**
     * Solicita todos los permisos necesarios para las funcionalidades
     */
    private fun requestNecessaryPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        // Permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        // Permisos de notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Permisos de reconocimiento de actividad
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * Activa o desactiva el servicio de monitoreo de sensores
     */
    private fun toggleSensorService() {
        val button = findViewById<Button>(R.id.btnActivarSensores)
        
        if (isSensorServiceRunning) {
            // Detener servicio
            val intent = Intent(this, SensorMonitorService::class.java)
            stopService(intent)
            isSensorServiceRunning = false
            button.text = "🛡️ Activar Protección"
            button.backgroundTintList = ContextCompat.getColorStateList(this, 
                android.R.color.holo_red_dark)
            Toast.makeText(this, "Protección desactivada", Toast.LENGTH_SHORT).show()
        } else {
            // Iniciar servicio
            val intent = Intent(this, SensorMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            isSensorServiceRunning = true
            button.text = "✓ Protección Activa"
            button.backgroundTintList = ContextCompat.getColorStateList(this, 
                android.R.color.holo_green_dark)
            Toast.makeText(this, 
                "Protección activada:\n• Detección de caídas\n• Monitor de actividad\n• Confirmación por gestos", 
                Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = mutableListOf<String>()
            
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i])
                }
            }
            
            if (deniedPermissions.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Algunos permisos fueron denegados. Algunas funciones pueden no estar disponibles.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarMedicamentosParaTomar() {
        val container = findViewById<LinearLayout>(R.id.containerMedicamentos)
        container.removeAllViews()

        val recordatorios = dbHelper.obtenerRecordatoriosHoy()

        if (recordatorios.isEmpty()) {
            val tvVacio = TextView(this).apply {
                text = "✓ No hay medicamentos programados para hoy"
                textSize = 20f
                setTextColor(0xFF666666.toInt())
                setPadding(20, 20, 20, 20)
                gravity = Gravity.CENTER
            }
            container.addView(tvVacio)
        } else {
            recordatorios.forEach { rec ->
                // Crear un LinearLayout VERTICAL para cada medicamento
                // MEJORADO PARA ADULTOS MAYORES: Todo el texto visible y botones grandes
                val itemLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL // Cambiado a VERTICAL
                    setPadding(20, 16, 20, 16)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setBackgroundColor(0xFFFFF9C4.toInt()) // Fondo amarillo claro
                }

                // Texto del nombre del medicamento - MÁS GRANDE Y LEGIBLE
                val tvNombre = TextView(this).apply {
                    text = "💊 ${rec["medicamento"]}"
                    textSize = 22f // Nombre del medicamento grande
                    setTextColor(0xFF000000.toInt()) // Negro para alto contraste
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                
                // Texto de la dosis - VISIBLE Y CLARO
                val tvDosis = TextView(this).apply {
                    text = "Dosis: ${rec["dosis"]}"
                    textSize = 20f
                    setTextColor(0xFF000000.toInt())
                    setPadding(0, 8, 0, 0)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                
                // Texto del horario - GRANDE Y DESTACADO
                val tvHora = TextView(this).apply {
                    text = "⏰ Hora: ${rec["hora"]}"
                    textSize = 20f
                    setTextColor(0xFF1976D2.toInt()) // Azul para destacar la hora
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setPadding(0, 8, 0, 16)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // Botón para marcar como tomado - ANCHO COMPLETO Y GRANDE
                val btnTomado = Button(this).apply {
                    text = "✓ MARCAR COMO TOMADO"
                    textSize = 18f
                    setTextColor(0xFFFFFFFF.toInt())
                    backgroundTintList = ContextCompat.getColorStateList(
                        this@DashboardActivity, 
                        android.R.color.holo_green_dark
                    )
                    // Botón grande con ancho completo
                    minimumHeight = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 65f, resources.displayMetrics
                    ).toInt()
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setOnClickListener {
                        marcarComoTomado(rec["id"] ?: "", rec["medicamento"] ?: "")
                    }
                }

                // Agregar todos los elementos en orden
                itemLayout.addView(tvNombre)
                itemLayout.addView(tvDosis)
                itemLayout.addView(tvHora)
                itemLayout.addView(btnTomado)
                
                // Añadir margen entre items
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = 16 // Mayor margen entre medicamentos
                itemLayout.layoutParams = params
                
                container.addView(itemLayout)
            }
        }
    }

    private fun marcarComoTomado(recordatorioId: String, nombreMedicamento: String) {
        val historialId = "hist-${UUID.randomUUID()}"
        val fechaHoraActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        val db = dbHelper.writableDatabase
        
        // Obtener el medicamento_id del recordatorio
        val cursorMed = db.rawQuery("SELECT medicamento_id FROM recordatorios WHERE id = ?", arrayOf(recordatorioId))
        var medicamentoId: String? = null
        if (cursorMed.moveToFirst()) {
            medicamentoId = cursorMed.getString(0)
        }
        cursorMed.close()
        
        // Insertar en historial de tomas
        db.execSQL("""
            INSERT INTO historial_tomas 
            (id, recordatorio_id, medicamento_id, usuario_id, fecha_hora_programada, fecha_hora_toma, estado) 
            SELECT ?, r.id, r.medicamento_id, ?, ?, ?, 'tomado'
            FROM recordatorios r 
            WHERE r.id = ?
        """, arrayOf(historialId, usuarioActual, fechaHoraActual, fechaHoraActual, recordatorioId))
        
        // Verificar si el medicamento temporal completó su tratamiento
        if (medicamentoId != null) {
            val progreso = dbHelper.obtenerProgresoMedicamento(medicamentoId, usuarioActual)
            val esPermanente = progreso["es_permanente"] as? Boolean ?: false
            val porcentaje = (progreso["progreso"] as? String)?.toDoubleOrNull() ?: 0.0
            
            if (!esPermanente && porcentaje >= 100.0) {
                // Desactivar medicamento y recordatorios automáticamente
                dbHelper.verificarYDesactivarTratamientoCompleto(medicamentoId, usuarioActual)
                Toast.makeText(this, 
                    "$nombreMedicamento marcado como tomado\nTratamiento completado al 100% - Medicamento dado de baja", 
                    Toast.LENGTH_LONG).show()
            } else {
                if (esPermanente) {
                    Toast.makeText(this, "$nombreMedicamento marcado como tomado (Tratamiento permanente)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "$nombreMedicamento marcado como tomado - Progreso: ${String.format("%.0f", porcentaje)}%", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "$nombreMedicamento marcado como tomado", Toast.LENGTH_SHORT).show()
        }
        
        // Mostrar notificación de confirmación
        notificationHelper.showConfirmationNotification(nombreMedicamento)
        
        // Actualizar la vista
        mostrarInformacionDB()
        mostrarMedicamentosParaTomar()
    }

    private fun mostrarInformacionDB() {
        val tvInfo1 = findViewById<TextView>(R.id.tvInfo1)
        val tvInfo2 = findViewById<TextView>(R.id.tvInfo2)
        val tvInfo3 = findViewById<TextView>(R.id.tvInfo3)

        // Obtener medicamentos del usuario con progreso
        val medicamentos = dbHelper.obtenerTodosLosMedicamentos(usuarioActual)
        val textoMedicamentos = buildString {
            append("Medicamentos Registrados (${medicamentos.size}):\n\n")
            if (medicamentos.isEmpty()) {
                append("No hay medicamentos registrados")
            } else {
                medicamentos.forEach { med ->
                    val medId = med["id"] ?: ""
                    val progreso = dbHelper.obtenerProgresoMedicamento(medId, usuarioActual)
                    val esPermanente = progreso["es_permanente"] as? Boolean ?: false
                    val porcentajeProgreso = progreso["progreso"] as? String ?: "0.0"
                    
                    append("- ${med["nombre"]} - ${med["dosis"]}")
                    if (esPermanente) {
                        append(" [PERMANENTE]")
                    } else {
                        append(" [Progreso: $porcentajeProgreso%]")
                    }
                    append("\n")
                }
            }
        }
        tvInfo1.text = textoMedicamentos

        // Obtener recordatorios de hoy
        val recordatorios = dbHelper.obtenerRecordatoriosHoy()
        val textoRecordatorios = buildString {
            append("Recordatorios Activos (${recordatorios.size}):\n\n")
            if (recordatorios.isEmpty()) {
                append("No hay recordatorios activos")
            } else {
                recordatorios.take(5).forEach { rec ->
                    append("• ${rec["medicamento"]} a las ${rec["hora"]}\n")
                }
            }
        }
        tvInfo2.text = textoRecordatorios

        // Obtener estadísticas de cumplimiento
        val estadisticas = dbHelper.obtenerEstadisticasCumplimiento(usuarioActual)
        val textoHistorial = buildString {
            append("Estadísticas de Cumplimiento:\n\n")
            if (estadisticas.isEmpty()) {
                append("No hay datos de cumplimiento aún")
            } else {
                estadisticas.forEach { est ->
                    append("• ${est["medicamento"]}: ${est["porcentaje"]}%\n")
                    append("  (${est["realizadas"]}/${est["total"]} tomas)\n\n")
                }
            }
        }
        tvInfo3.text = textoHistorial
    }

    override fun onResume() {
        super.onResume()
        // Actualizar la información cada vez que se vuelve a esta pantalla
        mostrarInformacionDB()
        mostrarMedicamentosParaTomar()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}
