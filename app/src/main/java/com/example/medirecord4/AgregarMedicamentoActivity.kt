package com.example.medirecord4

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class AgregarMedicamentoActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var usuarioId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_medicamento)

        // Habilitar botón de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Agregar Medicamento"

        dbHelper = DatabaseHelper(this)
        
        // Obtener el usuario actual desde el Intent
        usuarioId = intent.getStringExtra("USUARIO_ID") ?: "usr-001"

        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etDosis = findViewById<EditText>(R.id.etDosis)
        val etTipo = findViewById<EditText>(R.id.etTipo)
        val etInstrucciones = findViewById<EditText>(R.id.etInstrucciones)
        val etStock = findViewById<EditText>(R.id.etStock)
        val spinnerTipoTratamiento = findViewById<Spinner>(R.id.spinnerTipoTratamiento)
        val etDuracionDias = findViewById<EditText>(R.id.etDuracionDias)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)

        // Configurar spinner de tipo de tratamiento
        val tiposTratamiento = arrayOf("Temporal", "Permanente (Diabetes, Presión, VIH, etc.)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposTratamiento)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoTratamiento.adapter = adapter

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val dosis = etDosis.text.toString()
            val tipo = etTipo.text.toString()
            val instrucciones = etInstrucciones.text.toString()
            val stock = etStock.text.toString()
            val tipoTratamiento = if (spinnerTipoTratamiento.selectedItemPosition == 0) "temporal" else "permanente"
            val duracionDias = if (tipoTratamiento == "temporal") {
                etDuracionDias.text.toString().toIntOrNull() ?: 7
            } else {
                0
            }

            if (nombre.isEmpty() || dosis.isEmpty()) {
                Toast.makeText(this, "Nombre y dosis son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (tipoTratamiento == "temporal" && duracionDias <= 0) {
                Toast.makeText(this, "Ingresa la duración del tratamiento en días", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val medicamentoId = "med-${UUID.randomUUID()}"
            val db = dbHelper.writableDatabase
            
            db.execSQL("""
                INSERT INTO medicamentos 
                (id, usuario_id, nombre, dosis, tipo, via_administracion, instrucciones, stock_actual, stock_total, activo, tipo_tratamiento, duracion_dias) 
                VALUES (?, ?, ?, ?, ?, 'oral', ?, ?, ?, 1, ?, ?)
            """, arrayOf(medicamentoId, usuarioId, nombre, dosis, tipo, instrucciones, stock, stock, tipoTratamiento, duracionDias))

            val mensajeTipo = if (tipoTratamiento == "permanente") " (Tratamiento permanente)" else " (Tratamiento de $duracionDias días)"
            Toast.makeText(this, "Medicamento agregado exitosamente$mensajeTipo", Toast.LENGTH_LONG).show()
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

