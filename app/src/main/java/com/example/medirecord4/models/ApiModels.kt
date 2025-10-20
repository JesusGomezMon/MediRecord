package com.example.medirecord4.models

import com.google.gson.annotations.SerializedName

/**
 * Modelos de datos para las respuestas de la API
 */

// Respuesta de búsqueda de medicamentos
data class MedicamentoResponse(
    @SerializedName("idGroup")
    val idGroup: IdGroup?
)

data class IdGroup(
    @SerializedName("name")
    val nombre: String?,
    @SerializedName("rxnormId")
    val rxnormIds: List<String>?
)

// Respuesta de interacciones medicamentosas
data class InteraccionResponse(
    @SerializedName("fullInteractionTypeGroup")
    val interacciones: List<InteractionGroup>?
)

data class InteractionGroup(
    @SerializedName("fullInteractionType")
    val tipo: List<InteractionType>?
)

data class InteractionType(
    @SerializedName("comment")
    val comentario: String?,
    @SerializedName("minConcept")
    val medicamentos: List<Medicamento>?,
    @SerializedName("interactionPair")
    val pares: List<InteractionPair>?
)

data class Medicamento(
    @SerializedName("rxcui")
    val rxcui: String?,
    @SerializedName("name")
    val nombre: String?
)

data class InteractionPair(
    @SerializedName("severity")
    val severidad: String?,
    @SerializedName("description")
    val descripcion: String?
)

// Modelo simple para mostrar resultados
data class MedicamentoInfo(
    val nombre: String,
    val rxcui: String,
    val descripcion: String = "Información del medicamento",
    val imagenUrl: String? = null
)

data class InteraccionInfo(
    val medicamento1: String,
    val medicamento2: String,
    val severidad: String,
    val descripcion: String
)

