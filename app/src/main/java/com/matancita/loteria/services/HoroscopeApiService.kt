package com.matancita.loteria.services

import com.matancita.loteria.objects.HoroscopeApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HoroscopeApiService {
    // La URL base ya está en el ApiClient. Esta es la parte final.
    @GET("api/v1/get-horoscope/daily")
    suspend fun getDailyHoroscope(
        @Query("sign") sign: String,
        @Query("day") day: String = "TODAY" // Usamos @Query y un valor por defecto
    ): HoroscopeApiResponse // La respuesta ahora es nuestro nuevo modelo
}