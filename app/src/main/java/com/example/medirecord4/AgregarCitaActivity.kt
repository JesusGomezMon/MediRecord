package com.example.medirecord4

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class AgregarCitaActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var usuarioId: String

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
        val etFechaHora = findViewById<EditText>(R.id.etFechaHora)
        val etUbicacion = findViewById<EditText>(R.id.etUbicacion)
        val etNotas = findViewById<EditText>(R.id.etNotas)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)

        btnGuardar.setOnClickListener {
            val doctorNombre = etDoctorNombre.text.toString()
            val especialidad = etEspecialidad.text.toString()
            val fechaHora = etFechaHora.text.toString()
            val ubicacion = etUbicacion.text.toString()
            val notas = etNotas.text.toString()

            if (doctorNombre.isEmpty() || fechaHora.isEmpty()) {
                Toast.makeText(this, "Nombre del doctor y fecha/hora son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val citaId = "cita-${UUID.randomUUID()}"
            val db = dbHelper.writableDatabase

            db.execSQL("""
                INSERT INTO citas_medicas 
                (id, usuario_id, doctor_nombre, especialidad, fecha_hora, ubicacion, notas, asistio) 
                VALUES (?, ?, ?, ?, ?, ?, ?, 'pendiente')
            """, arrayOf(citaId, usuarioId, doctorNombre, especialidad, fechaHora, ubicacion, notas))

            Toast.makeText(this, "Cita médica agregada exitosamente", Toast.LENGTH_SHORT).show()
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

