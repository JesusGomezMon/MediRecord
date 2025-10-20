package com.example.medirecord4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class DashboardActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private val usuarioActual = "usr-001" // Usuario de ejemplo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Habilitar botón de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Dashboard"

        // Inicializar base de datos
        dbHelper = DatabaseHelper(this)

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
    }

    private fun mostrarMedicamentosParaTomar() {
        val container = findViewById<LinearLayout>(R.id.containerMedicamentos)
        container.removeAllViews()

        val recordatorios = dbHelper.obtenerRecordatoriosHoy()

        if (recordatorios.isEmpty()) {
            val tvVacio = TextView(this)
            tvVacio.text = "No hay medicamentos programados para hoy"
            tvVacio.setPadding(16, 16, 16, 16)
            container.addView(tvVacio)
        } else {
            recordatorios.forEach { rec ->
                // Crear un LinearLayout horizontal para cada medicamento
                val itemLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(16, 8, 16, 8)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // Texto del medicamento
                val tvMedicamento = TextView(this).apply {
                    text = "${rec["medicamento"]} - ${rec["dosis"]} a las ${rec["hora"]}"
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                // Botón para marcar como tomado
                val btnTomado = Button(this).apply {
                    text = "Tomado"
                    setOnClickListener {
                        marcarComoTomado(rec["id"] ?: "", rec["medicamento"] ?: "")
                    }
                }

                itemLayout.addView(tvMedicamento)
                itemLayout.addView(btnTomado)
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
