package com.matancita.loteria.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.matancita.loteria.repository.HoroscopeData
import com.matancita.loteria.repository.HoroscopeDataRepository
import com.matancita.loteria.repository.UserProfile
import com.matancita.loteria.services.ApiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

private sealed class ApiResult {
    data class Success(val text: String) : ApiResult()
    data class Error(val message: String) : ApiResult()
}

data class HoroscopeUiState(
    val data: HoroscopeData? = null,
    val isLoading: Boolean = true,
    // --- NUEVOS ESTADOS ---
    val isTranslating: Boolean = false,
    val translatedHoroscope: String? = null,
    val translationError: String? = null
)

class HoroscopeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HoroscopeDataRepository(application)

    private val _uiState = MutableStateFlow(HoroscopeUiState())
    val uiState: StateFlow<HoroscopeUiState> = _uiState

    fun loadHoroscopeData(userProfile: UserProfile) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val savedData = repository.horoscopeDataFlow.firstOrNull()

            if (savedData != null && isToday(savedData.timestamp)) {
                _uiState.update { it.copy(data = savedData, isLoading = false) }
                autoTranslateIfNeeded(savedData.dailyHoroscope)
            } else {
                generateAndSaveNewHoroscope(userProfile)
            }
        }
    }

    // --- Traducción automática según idioma del dispositivo ---
    private fun autoTranslateIfNeeded(text: String) {
        val targetLanguage = Locale.getDefault().language
        // No traducir si el dispositivo ya está en inglés
        if (targetLanguage == TranslateLanguage.ENGLISH || targetLanguage == "en") return
        // No traducir si ya existe traducción o el texto está vacío
        if (_uiState.value.translatedHoroscope != null || text.isBlank()) return

        viewModelScope.launch {
            translateText(text, targetLanguage)
        }
    }

    private suspend fun translateText(text: String, targetLanguageCode: String) {
        _uiState.update { it.copy(isTranslating = true, translationError = null) }

        // 1. Configurar las opciones del traductor
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(targetLanguageCode)
            .build()
        val englishSpanishTranslator = Translation.getClient(options)

        // Asegurarnos de que el ViewModel se limpie correctamente
        // y cierre el traductor para evitar fugas de memoria.
        viewModelScope.coroutineContext[Job]?.invokeOnCompletion {
            englishSpanishTranslator.close()
        }

        // 2. Descargar el modelo de lenguaje si es necesario
        englishSpanishTranslator.downloadModelIfNeeded()
            .addOnSuccessListener {
                // 3. El modelo está listo, ahora traducir el texto.
                englishSpanishTranslator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        _uiState.update {
                            it.copy(
                                isTranslating = false,
                                translatedHoroscope = translatedText
                            )
                        }
                    }
                    .addOnFailureListener { exception ->
                        _uiState.update {
                            it.copy(
                                isTranslating = false,
                                translationError = "Translation failed: ${exception.message}"
                            )
                        }
                    }
            }
            .addOnFailureListener { exception ->
                _uiState.update {
                    it.copy(
                        isTranslating = false,
                        translationError = "Model download failed: ${exception.message}"
                    )
                }
            }
    }

    private suspend fun generateAndSaveNewHoroscope(userProfile: UserProfile) {
        // La API espera el signo con la primera letra en mayúscula (ej. "Aries")
        val sign = userProfile.zodiacSign?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        } ?: return

        // Lógica para generar números de la suerte (sin cambios)
        val todayString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val seedString = "${userProfile.dob}-${sign}-$todayString"
        val seed = seedString.hashCode().toLong()
        val random = Random(seed)
        val numbers = List(4) { random.nextInt(1, 101) }.sorted()

        // --- INICIO DE CAMBIOS: Lógica mejorada que usa el mensaje de error de la API ---
        val apiResult = try {
            Log.d("HoroscopeViewModel", "Llamando a la API para $sign")
            val response = ApiClient.apiService.getDailyHoroscope(sign = sign)
            Log.d("HoroscopeViewModel", "API devolvió: $response")

            val horoscopeText = response.data?.horoscope ?: response.data?.horoscope_data
            if (horoscopeText != null) {
                ApiResult.Success(horoscopeText)
            } else {
                val errorMessage = response.message ?: "La predicción para hoy no está disponible."
                Log.w("HoroscopeViewModel", "API no devolvió un horóscopo válido para $sign. Mensaje: $errorMessage")
                ApiResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e("HoroscopeViewModel", "Fallo al llamar o parsear la API para $sign", e)
            ApiResult.Error("No se pudo conectar con el servicio. Revisa tu conexión a internet.")
        }
        // --- FIN DE CAMBIOS ---

        when (apiResult) {
            is ApiResult.Success -> {
                val newData = HoroscopeData(
                    timestamp = System.currentTimeMillis(),
                    luckyNumbers = numbers,
                    dailyHoroscope = apiResult.text,
                    tappedStars = emptySet(),
                    numbersRevealed = false
                )
                repository.saveHoroscopeData(newData)
                _uiState.update { it.copy(data = newData, isLoading = false, translationError = null) }
                autoTranslateIfNeeded(apiResult.text)
            }
            is ApiResult.Error -> {
                // NO guardamos el error en DataStore para que la próxima vez se reintente la API
                val fallbackData = HoroscopeData(
                    timestamp = System.currentTimeMillis(),
                    luckyNumbers = numbers,
                    dailyHoroscope = apiResult.message,
                    tappedStars = emptySet(),
                    numbersRevealed = false
                )
                _uiState.update { it.copy(data = fallbackData, isLoading = false, translationError = null) }
            }
        }
    }

    // ... (El resto del archivo: onStarTapped, isToday no cambian) ...
    fun onStarTapped(index: Int, totalStars: Int) {
        viewModelScope.launch {
            val currentData = _uiState.value.data ?: return@launch
            if (currentData.numbersRevealed) return@launch

            if (index != currentData.tappedStars.size) return@launch

            val newTappedStars = currentData.tappedStars + index
            val revealed = newTappedStars.size == totalStars

            val updatedData = currentData.copy(
                tappedStars = newTappedStars,
                numbersRevealed = revealed
            )

            repository.saveHoroscopeData(updatedData)
            _uiState.update { it.copy(data = updatedData) }
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val savedCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val currentCal = Calendar.getInstance()
        return savedCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR) &&
                savedCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR)
    }
}