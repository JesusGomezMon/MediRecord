package com.example.medirecord4

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AgregarRecordatorioActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var usuarioId: String
    private var fechaSeleccionada: String = ""
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_recordatorio)

        // Habilitar botón de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Agregar Recordatorio"

        dbHelper = DatabaseHelper(this)
        
        // Obtener el usuario actual desde el Intent
        usuarioId = intent.getStringExtra("USUARIO_ID") ?: "usr-001"

        // Inicializar fecha con la fecha actual
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        fechaSeleccionada = dateFormat.format(calendar.time)

        // Referencias a los controles
        val spinnerMedicamento = findViewById<Spinner>(R.id.spinnerMedicamento)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val cbLunes = findViewById<CheckBox>(R.id.cbLunes)
        val cbMartes = findViewById<CheckBox>(R.id.cbMartes)
        val cbMiercoles = findViewById<CheckBox>(R.id.cbMiercoles)
        val cbJueves = findViewById<CheckBox>(R.id.cbJueves)
        val cbViernes = findViewById<CheckBox>(R.id.cbViernes)
        val cbSabado = findViewById<CheckBox>(R.id.cbSabado)
        val cbDomingo = findViewById<CheckBox>(R.id.cbDomingo)
        val btnTodosLosDias = findViewById<Button>(R.id.btnTodosLosDias)
        val btnSeleccionarFecha = findViewById<Button>(R.id.btnSeleccionarFecha)
        val tvFechaSeleccionada = findViewById<TextView>(R.id.tvFechaSeleccionada)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)

        // Configurar TimePicker para formato 24 horas
        timePicker.setIs24HourView(true)

        // Mostrar fecha actual por defecto
        tvFechaSeleccionada.text = "Fecha: $fechaSeleccionada"

        // Cargar medicamentos activos del usuario actual en el spinner
        val medicamentos = dbHelper.obtenerTodosLosMedicamentos(usuarioId)
        val nombresMedicamentos = medicamentos.map { "${it["nombre"]} - ${it["dosis"]}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombresMedicamentos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMedicamento.adapter = adapter

        // Botón para seleccionar todos los días
        btnTodosLosDias.setOnClickListener {
            val todosSeleccionados = cbLunes.isChecked && cbMartes.isChecked && 
                                     cbMiercoles.isChecked && cbJueves.isChecked &&
                                     cbViernes.isChecked && cbSabado.isChecked && cbDomingo.isChecked
            
            val nuevoEstado = !todosSeleccionados
            cbLunes.isChecked = nuevoEstado
            cbMartes.isChecked = nuevoEstado
            cbMiercoles.isChecked = nuevoEstado
            cbJueves.isChecked = nuevoEstado
            cbViernes.isChecked = nuevoEstado
            cbSabado.isChecked = nuevoEstado
            cbDomingo.isChecked = nuevoEstado
            
            Toast.makeText(
                this, 
                if (nuevoEstado) "Todos los días seleccionados" else "Todos los días deseleccionados",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Botón para seleccionar fecha con DatePicker
        btnSeleccionarFecha.setOnClickListener {
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

        // Guardar recordatorio
        btnGuardar.setOnClickListener {
            if (medicamentos.isEmpty()) {
                Toast.makeText(this, "Primero debes agregar medicamentos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtener hora del TimePicker
            val hora = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String.format("%02d:%02d:00", timePicker.hour, timePicker.minute)
            } else {
                @Suppress("DEPRECATION")
                String.format("%02d:%02d:00", timePicker.currentHour, timePicker.currentMinute)
            }

            // Obtener días seleccionados
            val diasSeleccionados = mutableListOf<String>()
            if (cbLunes.isChecked) diasSeleccionados.add("LUNES")
            if (cbMartes.isChecked) diasSeleccionados.add("MARTES")
            if (cbMiercoles.isChecked) diasSeleccionados.add("MIERCOLES")
            if (cbJueves.isChecked) diasSeleccionados.add("JUEVES")
            if (cbViernes.isChecked) diasSeleccionados.add("VIERNES")
            if (cbSabado.isChecked) diasSeleccionados.add("SABADO")
            if (cbDomingo.isChecked) diasSeleccionados.add("DOMINGO")

            val dias = diasSeleccionados.joinToString(",")

            // Validaciones
            if (diasSeleccionados.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos un día de la semana", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (fechaSeleccionada.isEmpty()) {
                Toast.makeText(this, "Selecciona una fecha de inicio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar en la base de datos
            val medicamentoSeleccionado = medicamentos[spinnerMedicamento.selectedItemPosition]
            val recordatorioId = "rec-${UUID.randomUUID()}"
            val db = dbHelper.writableDatabase

            db.execSQL("""
                INSERT INTO recordatorios 
                (id, medicamento_id, hora, dias_semana, fecha_inicio, fecha_fin, activo) 
                VALUES (?, ?, ?, ?, ?, NULL, 1)
            """, arrayOf(recordatorioId, medicamentoSeleccionado["id"], hora, dias, fechaSeleccionada))

            Toast.makeText(this, "✓ Recordatorio agregado:\n$hora - $dias", Toast.LENGTH_LONG).show()
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

