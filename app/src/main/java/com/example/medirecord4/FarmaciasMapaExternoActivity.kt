package com.example.medirecord4

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * Actividad alternativa que abre Google Maps externo
 * No requiere API Key
 */
class FarmaciasMapaExternoActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farmacias_mapa_externo)

        // Habilitar botón de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Farmacias Cercanas"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val btnBuscarFarmacias = findViewById<Button>(R.id.btnBuscarFarmacias)
        val btnBuscarUbicacion = findViewById<Button>(R.id.btnBuscarUbicacion)

        // Buscar farmacias cercanas
        btnBuscarFarmacias.setOnClickListener {
            if (checkLocationPermission()) {
                abrirGoogleMapsFarmacias()
            } else {
                requestLocationPermission()
            }
        }

        // Buscar por ubicación actual
        btnBuscarUbicacion.setOnClickListener {
            if (checkLocationPermission()) {
                obtenerUbicacionYBuscar()
            } else {
                requestLocationPermission()
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun abrirGoogleMapsFarmacias() {
        // Abre Google Maps buscando farmacias cercanas
        val uri = Uri.parse("geo:0,0?q=farmacia")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Si no tiene Google Maps instalado, abrir en navegador
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/farmacia")
            )
            startActivity(browserIntent)
        }
    }

    private fun obtenerUbicacionYBuscar() {
        if (!checkLocationPermission()) {
            return
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Buscar farmacias cerca de la ubicación actual
                        val uri = Uri.parse("geo:${location.latitude},${location.longitude}?q=farmacia")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.setPackage("com.google.android.apps.maps")

                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        } else {
                            // Si no tiene Google Maps, abrir en navegador
                            val browserIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/maps/search/farmacia/@${location.latitude},${location.longitude},15z")
                            )
                            startActivity(browserIntent)
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "No se pudo obtener tu ubicación. Intenta activar el GPS.",
                            Toast.LENGTH_LONG
                        ).show()
                        // Buscar farmacias sin ubicación específica
                        abrirGoogleMapsFarmacias()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Error al obtener ubicación: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    abrirGoogleMapsFarmacias()
                }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error de permisos de ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido. Presiona el botón nuevamente.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Permiso denegado. La búsqueda será general.",
                    Toast.LENGTH_LONG
                ).show()
            }
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
}

