package com.matancita.loteria.services

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


object ApiClient {

    private const val BASE_URL = "https://horoscope-app-api.vercel.app/"

    // Configura Moshi para que funcione con Kotlin
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Crea la instancia de Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // Crea una implementación de la interfaz del servicio
    val apiService: HoroscopeApiService by lazy {
        retrofit.create(HoroscopeApiService::class.java)
    }
}