package com.example.medirecord4

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AgregarCitaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var usuarioId: String
    private var fechaSeleccionada: String = ""
    private var horaSeleccionada: String = ""
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_cita)

        // Habilitar botón de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Agregar Cita Médica"

        dbHelper = DatabaseHelper(this)
        
        // Obtener el usuario actual desde el Intent
        usuarioId = intent.getStringExtra("USUARIO_ID") ?: "usr-001"

        val etDoctorNombre = findViewById<EditText>(R.id.etDoctorNombre)
        val etEspecialidad = findViewById<EditText>(R.id.etEspecialidad)
        val etUbicacion = findViewById<EditText>(R.id.etUbicacion)
        val etNotas = findViewById<EditText>(R.id.etNotas)
        val btnSeleccionarFecha = findViewById<Button>(R.id.btnSeleccionarFecha)
        val tvFechaSeleccionada = findViewById<TextView>(R.id.tvFechaSeleccionada)
        val btnSeleccionarHora = findViewById<Button>(R.id.btnSeleccionarHora)
        val tvHoraSeleccionada = findViewById<TextView>(R.id.tvHoraSeleccionada)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)

        // Botón para seleccionar fecha
        btnSeleccionarFecha.setOnClickListener {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    fechaSeleccionada = dateFormat.format(calendar.time)
                    tvFechaSeleccionada.text = "Fecha: $fechaSeleccionada"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Botón para seleccionar hora
        btnSeleccionarHora.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    horaSeleccionada = String.format("%02d:%02d:00", hourOfDay, minute)
                    tvHoraSeleccionada.text = "Hora: ${String.format("%02d:%02d", hourOfDay, minute)}"
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // Formato 24 horas
            )
            timePickerDialog.show()
        }

        // Guardar cita
        btnGuardar.setOnClickListener {
            val doctorNombre = etDoctorNombre.text.toString()
            val especialidad = etEspecialidad.text.toString()
            val ubicacion = etUbicacion.text.toString()
            val notas = etNotas.text.toString()

            // Validaciones
            if (doctorNombre.isEmpty()) {
                Toast.makeText(this, "El nombre del doctor es obligatorio", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (fechaSeleccionada.isEmpty()) {
                Toast.makeText(this, "Selecciona una fecha para la cita", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (horaSeleccionada.isEmpty()) {
                Toast.makeText(this, "Selecciona una hora para la cita", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val fechaHora = "$fechaSeleccionada $horaSeleccionada"
            val citaId = "cita-${UUID.randomUUID()}"
            val db = dbHelper.writableDatabase

            db.execSQL("""
                INSERT INTO citas_medicas 
                (id, usuario_id, doctor_nombre, especialidad, fecha_hora, ubicacion, notas, asistio) 
                VALUES (?, ?, ?, ?, ?, ?, ?, 'pendiente')
            """, arrayOf(citaId, usuarioId, doctorNombre, especialidad, fechaHora, ubicacion, notas))

            Toast.makeText(this, "✓ Cita agregada:\n$doctorNombre\n$fechaHora", Toast.LENGTH_LONG).show()
            finish()
        }

        btnCancelar.setOnClickListener {
            finish()
        }
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

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}

