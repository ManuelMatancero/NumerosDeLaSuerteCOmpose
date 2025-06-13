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
            } else {
                generateAndSaveNewHoroscope(userProfile)
            }
        }
    }

    // --- NUEVA FUNCIÓN PARA INICIAR LA TRADUCCIÓN ---
    fun requestTranslation() {
        val originalText = _uiState.value.data?.dailyHoroscope ?: return

        // No traducir si ya está traducido o si el texto está vacío
        if (_uiState.value.translatedHoroscope != null || originalText.isBlank()) {
            // Si el usuario quiere volver al original, reseteamos la traducción
            _uiState.update { it.copy(translatedHoroscope = null) }
            return
        }

        // Obtener el idioma del dispositivo (ej: "es" para español)
        val targetLanguage = Locale.getDefault().language

        // No tiene sentido traducir de inglés a inglés
        if (targetLanguage == TranslateLanguage.ENGLISH) {
            _uiState.update { it.copy(translationError = "Device is already in English.") }
            return
        }

        viewModelScope.launch {
            translateText(originalText, targetLanguage)
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
        val horoscopeText = try {
            Log.d("HoroscopeViewModel", "Llamando a la API para $sign")
            val response = ApiClient.apiService.getDailyHoroscope(sign = sign)
            Log.d("HoroscopeViewModel", "API devolvió: $response")

            if (response.success && response.data?.horoscope_data != null) {
                // Caso exitoso: todo está bien
                response.data.horoscope_data!!
            } else {
                // Caso de error: usamos el mensaje de la API si existe, si no, uno genérico.
                val errorMessage = response.message ?: "La predicción para hoy no está disponible."
                Log.w("HoroscopeViewModel", "API no devolvió un horóscopo válido para $sign. Mensaje: $errorMessage")
                errorMessage // Devolvemos el mensaje de error (de la API o el nuestro)
            }
        } catch (e: Exception) {
            Log.e("HoroscopeViewModel", "Fallo al llamar o parsear la API para $sign", e)
            "No se pudo conectar con el servicio. Revisa tu conexión a internet."
        }
        // --- FIN DE CAMBIOS ---

        val newData = HoroscopeData(
            timestamp = System.currentTimeMillis(),
            luckyNumbers = numbers,
            dailyHoroscope = horoscopeText, // <-- Usa el texto obtenido de la API
            tappedStars = emptySet(),
            numbersRevealed = false
        )
        repository.saveHoroscopeData(newData)
        _uiState.update { it.copy(data = newData, isLoading = false) }
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