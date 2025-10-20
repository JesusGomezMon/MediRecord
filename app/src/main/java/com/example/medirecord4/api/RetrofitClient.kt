package com.example.medirecord4.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit singleton para conexiones HTTP
 */
object RetrofitClient {
    
    // URL base de RxNorm API (API pública de NIH)
    private const val BASE_URL = "https://rxnav.nlm.nih.gov/REST/"
    
    // Configuración de logging para debug
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // Cliente HTTP con configuraciones
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
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

