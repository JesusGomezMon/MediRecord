package com.example.medirecord4.api

import com.example.medirecord4.models.MedicamentoResponse
import com.example.medirecord4.models.InteraccionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface de API para consultar información de medicamentos
 * Usaremos RxNorm API y OpenFDA API (APIs públicas de medicamentos)
 */
interface MedicamentoAPI {
    
    // RxNorm API - Buscar medicamentos por nombre
    @GET("rxcui.json")
    suspend fun buscarMedicamento(
        @Query("name") nombre: String
    ): Response<MedicamentoResponse>
    
    // RxNorm API - Obtener interacciones
    @GET("interaction/list.json")
    suspend fun obtenerInteracciones(
        @Query("rxcuis") rxcuis: String
    ): Response<InteraccionResponse>
}

/**
 * Interface para API de imágenes (ejemplo con API pública)
 */
interface ImagenAPI {
    
    // Ejemplo: Unsplash API para imágenes de medicamentos
    @GET("photos/random")
    suspend fun obtenerImagenMedicamento(
        @Query("query") query: String = "medicine pills",
        @Query("client_id") clientId: String = "DEMO_KEY"
    ): Response<Any>
}

