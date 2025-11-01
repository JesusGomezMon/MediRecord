package com.example.medirecord4.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit singleton para conexiones HTTP
 * Optimizado sin logging interceptor para mejor rendimiento
 */
object RetrofitClient {
    
    // URL base de RxNorm API (API pública de NIH)
    private const val BASE_URL = "https://rxnav.nlm.nih.gov/REST/"
    
    // Cliente HTTP con configuraciones optimizadas
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Instancia de Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    // API de medicamentos
    val medicamentoAPI: MedicamentoAPI by lazy {
        retrofit.create(MedicamentoAPI::class.java)
    }
    
    /**
     * Cliente alternativo para APIs de imágenes (ej: Unsplash)
     */
    fun getImagenAPI(baseUrl: String): ImagenAPI {
        val imageRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        return imageRetrofit.create(ImagenAPI::class.java)
    }
}

