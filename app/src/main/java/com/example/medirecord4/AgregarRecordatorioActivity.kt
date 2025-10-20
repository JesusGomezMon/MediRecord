package com.example.medirecord4

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class AgregarRecordatorioActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var usuarioId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_recordatorio)

        // Habilitar botón de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Agregar Recordatorio"

        dbHelper = DatabaseHelper(this)
        
        // Obtener el usuario actual desde el Intent
        usuarioId = intent.getStringExtra("USUARIO_ID") ?: "usr-001"

        val spinnerMedicamento = findViewById<Spinner>(R.id.spinnerMedicamento)
        val etHora = findViewById<EditText>(R.id.etHora)
        val etDias = findViewById<EditText>(R.id.etDias)
        val etFechaInicio = findViewById<EditText>(R.id.etFechaInicio)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)

        // Cargar medicamentos activos del usuario actual en el spinner
        val medicamentos = dbHelper.obtenerTodosLosMedicamentos(usuarioId)
        val nombresMedicamentos = medicamentos.map { "${it["nombre"]} - ${it["dosis"]}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombresMedicamentos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMedicamento.adapter = adapter

        btnGuardar.setOnClickListener {
            if (medicamentos.isEmpty()) {
                Toast.makeText(this, "Primero debes agregar medicamentos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hora = etHora.text.toString()
            val dias = etDias.text.toString()
            val fechaInicio = etFechaInicio.text.toString()

            if (hora.isEmpty() || fechaInicio.isEmpty()) {
                Toast.makeText(this, "Hora y fecha de inicio son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val medicamentoSeleccionado = medicamentos[spinnerMedicamento.selectedItemPosition]
            val recordatorioId = "rec-${UUID.randomUUID()}"
            val db = dbHelper.writableDatabase

            db.execSQL("""
                INSERT INTO recordatorios 
                (id, medicamento_id, hora, dias_semana, fecha_inicio, fecha_fin, activo) 
                VALUES (?, ?, ?, ?, ?, NULL, 1)
            """, arrayOf(recordatorioId, medicamentoSeleccionado["id"], hora, dias, fechaInicio))

            Toast.makeText(this, "Recordatorio agregado exitosamente", Toast.LENGTH_SHORT).show()
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

