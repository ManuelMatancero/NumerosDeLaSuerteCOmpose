package com.matancita.loteria.services

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private const val TAG_API = "ApiClient"

object ApiClient {

    private const val BASE_URL = "https://horoscope-app-api.vercel.app/"

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG_API, message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    // Configura Moshi para que funcione con Kotlin (lenient para campos extra)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Crea la instancia de Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
        .build()

    // Crea una implementación de la interfaz del servicio
    val apiService: HoroscopeApiService by lazy {
        retrofit.create(HoroscopeApiService::class.java)
    }
}