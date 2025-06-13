package com.matancita.loteria.objects

import com.squareup.moshi.Json

// Clase para el objeto anidado "data"
data class HoroscopeDetails(
    @field:Json val horoscope_data: String?,
    @field:Json(name = "date") val date: String?
)

data class HoroscopeApiResponse(
    @field:Json(name = "data") val data: HoroscopeDetails?,
    @field:Json(name = "success") val success: Boolean,
    @field:Json(name = "message") val message: String? // <-- AÑADIR ESTA LÍNEA
)