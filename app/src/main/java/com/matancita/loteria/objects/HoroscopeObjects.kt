package com.matancita.loteria.objects

import com.squareup.moshi.Json

// Clase para el objeto anidado "data"
data class HoroscopeDetails(
    @field:Json(name = "horoscope") val horoscope: String?,
    @field:Json(name = "horoscope_data") val horoscope_data: String?, // keep for backward compat
    @field:Json(name = "date") val date: String?
)

data class HoroscopeApiResponse(
    @field:Json(name = "data") val data: HoroscopeDetails?,
    @field:Json(name = "success") val success: Boolean = true,
    @field:Json(name = "message") val message: String? = null
)