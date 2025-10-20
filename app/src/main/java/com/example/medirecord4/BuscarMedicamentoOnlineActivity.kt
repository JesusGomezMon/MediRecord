package com.example.medirecord4

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.medirecord4.data.MedicamentosDiccionario

class BuscarMedicamentoOnlineActivity : AppCompatActivity() {

    private lateinit var etBuscar: EditText
    private lateinit var btnBuscar: Button
    private lateinit var tvResultados: TextView
    private lateinit var tvSugerencias: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_medicamento_online)

        // Habilitar botón de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Buscar Medicamento"

        initViews()
        setupListeners()
        mostrarListaMedicamentos()
    }

    private fun initViews() {
        etBuscar = findViewById(R.id.etBuscar)
        btnBuscar = findViewById(R.id.btnBuscar)
        tvResultados = findViewById(R.id.tvResultados)
        tvSugerencias = findViewById(R.id.tvSugerencias)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        btnBuscar.setOnClickListener {
            val nombreMedicamento = etBuscar.text.toString().trim()
            if (nombreMedicamento.isEmpty()) {
                tvResultados.text = "Por favor ingresa el nombre de un medicamento"
                return@setOnClickListener
            }
            buscarMedicamento(nombreMedicamento)
        }
    }

    private fun buscarMedicamento(nombre: String) {
        progressBar.visibility = View.VISIBLE
        tvResultados.text = "Buscando '$nombre'...\n\n"

        // Simular pequeño delay para mejor UX
        tvResultados.postDelayed({
            progressBar.visibility = View.GONE

            val medicamento = MedicamentosDiccionario.buscarMedicamento(nombre)

            if (medicamento != null) {
                mostrarInformacionMedicamento(medicamento)
                mostrarSugerencias("")
            } else {
                mostrarMedicamentoNoEncontrado(nombre)
            }
        }, 500)
    }

    private fun mostrarInformacionMedicamento(med: MedicamentosDiccionario.InfoMedicamento) {
        val resultado = StringBuilder()
        
        resultado.append("═══════════════════════════════\n")
        resultado.append("MEDICAMENTO ENCONTRADO\n")
        resultado.append("═══════════════════════════════\n\n")
        
        resultado.append("Nombre: ${med.nombre}\n")
        if (med.nombreGenerico.isNotEmpty() && med.nombreGenerico != med.nombre) {
            resultado.append("Genérico: ${med.nombreGenerico}\n")
        }
        resultado.append("\n")
        
        resultado.append("TIPO DE VENTA:\n")
        if (med.ventaLibre) {
            resultado.append("VENTA LIBRE\n")
            resultado.append("Este medicamento puede comprarse sin receta médica en farmacias.\n")
        } else {
            resultado.append("REQUIERE RECETA MÉDICA\n")
            resultado.append("Este medicamento solo puede adquirirse con prescripción de un médico.\n")
        }
        resultado.append("\n")
        
        resultado.append("DESCRIPCIÓN:\n")
        resultado.append("${med.descripcion}\n\n")
        
        resultado.append("═══════════════════════════════\n")
        resultado.append("ADVERTENCIA IMPORTANTE\n")
        resultado.append("═══════════════════════════════\n")
        resultado.append("Esta información es solo de referencia.\n")
        resultado.append("Consulte a su médico o farmacéutico\n")
        resultado.append("antes de tomar cualquier medicamento.\n")

        tvResultados.text = resultado.toString()
    }

    private fun mostrarMedicamentoNoEncontrado(nombre: String) {
        val resultado = StringBuilder()
        
        resultado.append("═══════════════════════════════\n")
        resultado.append("MEDICAMENTO NO ENCONTRADO\n")
        resultado.append("═══════════════════════════════\n\n")
        
        resultado.append("No se encontró información para:\n")
        resultado.append("'$nombre'\n\n")
        
        // Buscar sugerencias
        val sugerencias = MedicamentosDiccionario.obtenerSugerencias(nombre)
        
        if (sugerencias.isNotEmpty()) {
            resultado.append("¿Quizás buscabas alguno de estos?\n\n")
            sugerencias.forEach { sugerencia ->
                resultado.append("- $sugerencia\n")
            }
        } else {
            resultado.append("Verifica la ortografía o intenta con otro nombre.\n\n")
            resultado.append("Puedes buscar por nombre comercial o genérico.\n")
        }

        tvResultados.text = resultado.toString()
        mostrarSugerencias(nombre)
    }

    private fun mostrarSugerencias(busqueda: String) {
        if (busqueda.isNotEmpty()) {
            val sugerencias = MedicamentosDiccionario.obtenerSugerencias(busqueda)
            if (sugerencias.isNotEmpty()) {
                val texto = "Sugerencias: ${sugerencias.joinToString(", ")}"
                tvSugerencias.text = texto
                tvSugerencias.visibility = View.VISIBLE
            } else {
                tvSugerencias.visibility = View.GONE
            }
        } else {
            tvSugerencias.visibility = View.GONE
        }
    }

    private fun mostrarListaMedicamentos() {
        val medicamentos = MedicamentosDiccionario.obtenerTodosMedicamentos()
        val resultado = StringBuilder()
        
        resultado.append("═══════════════════════════════\n")
        resultado.append("BASE DE DATOS DE MEDICAMENTOS\n")
        resultado.append("═══════════════════════════════\n\n")
        resultado.append("══Aun no logro conectar a la web para sacar esta info═══\n\n")
        resultado.append("Ingresa el nombre de un medicamento\n")
        resultado.append("para consultar su información.\n\n")
        resultado.append("Medicamentos disponibles (${medicamentos.size}):\n\n")
        
        medicamentos.forEachIndexed { index, nombre ->
            resultado.append("${index + 1}. ${nombre.capitalize()}\n")
            if ((index + 1) % 5 == 0) resultado.append("\n")
        }

        tvResultados.text = resultado.toString()
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
}
