package com.example.medirecord4

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HistorialActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var usuarioId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        // Habilitar botón de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Historial Completo"

        dbHelper = DatabaseHelper(this)
        
        // Obtener el usuario actual desde el Intent
        usuarioId = intent.getStringExtra("USUARIO_ID") ?: "usr-001"

        mostrarHistorial()
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

    private fun mostrarHistorial() {
        val tvHistorialTomas = findViewById<TextView>(R.id.tvHistorialTomas)
        val tvCitasMedicas = findViewById<TextView>(R.id.tvCitasMedicas)
        val tvEstadisticas = findViewById<TextView>(R.id.tvEstadisticas)
        val tvMedicamentosStock = findViewById<TextView>(R.id.tvMedicamentosStock)

        // Historial de tomas
        val db = dbHelper.readableDatabase
        val cursorTomas = db.rawQuery("""
            SELECT 
                m.nombre,
                ht.estado,
                ht.fecha_hora_programada,
                ht.fecha_hora_toma
            FROM historial_tomas ht
            JOIN medicamentos m ON ht.medicamento_id = m.id
            WHERE ht.usuario_id = ?
            ORDER BY ht.fecha_hora_programada DESC
            LIMIT 10
        """, arrayOf(usuarioId))

        val textoTomas = buildString {
            append("HISTORIAL DE TOMAS (ultimas 10):\n\n")
            while (cursorTomas.moveToNext()) {
                val nombre = cursorTomas.getString(0)
                val estado = cursorTomas.getString(1)
                val programada = cursorTomas.getString(2)
                val toma = cursorTomas.getString(3) ?: "No tomado"
                
                append("- $nombre\n")
                append("   Estado: $estado\n")
                append("   Programado: $programada\n")
                if (toma != "No tomado") {
                    append("   Tomado: $toma\n")
                }
                append("\n")
            }
        }
        cursorTomas.close()
        tvHistorialTomas.text = textoTomas

        // Citas médicas
        val cursorCitas = db.rawQuery("""
            SELECT 
                doctor_nombre,
                especialidad,
                fecha_hora,
                ubicacion,
                asistio
            FROM citas_medicas
            WHERE usuario_id = ?
            ORDER BY fecha_hora DESC
            LIMIT 5
        """, arrayOf(usuarioId))

        val textoCitas = buildString {
            append("CITAS MEDICAS:\n\n")
            while (cursorCitas.moveToNext()) {
                val doctor = cursorCitas.getString(0)
                val especialidad = cursorCitas.getString(1)
                val fecha = cursorCitas.getString(2)
                val ubicacion = cursorCitas.getString(3)
                val asistio = cursorCitas.getString(4)
                
                append("- $doctor - $especialidad\n")
                append("   Fecha: $fecha\n")
                append("   Lugar: $ubicacion\n")
                append("   Estado: $asistio\n\n")
            }
        }
        cursorCitas.close()
        tvCitasMedicas.text = textoCitas

        // Estadísticas generales
        val estadisticas = dbHelper.obtenerEstadisticasCumplimiento(usuarioId)
        val textoEstadisticas = buildString {
            append("ESTADISTICAS DE CUMPLIMIENTO:\n\n")
            estadisticas.forEach { est ->
                append("${est["medicamento"]}\n")
                append("   Cumplimiento: ${est["porcentaje"]}%\n")
                append("   Tomas realizadas: ${est["realizadas"]}/${est["total"]}\n\n")
            }
        }
        tvEstadisticas.text = textoEstadisticas

        // Información adicional
        val textoAdicional = buildString {
            append("RECORDATORIO:\n\n")
            append("Mantén un registro constante de tus medicamentos.\n\n")
            append("Consulta con tu médico regularmente sobre:\n")
            append("- Efectividad del tratamiento\n")
            append("- Posibles ajustes de dosis\n")
            append("- Nuevas opciones terapéuticas\n\n")
            append("No suspendas medicamentos sin consultar a tu médico.")
        }
        tvMedicamentosStock.text = textoAdicional
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}

