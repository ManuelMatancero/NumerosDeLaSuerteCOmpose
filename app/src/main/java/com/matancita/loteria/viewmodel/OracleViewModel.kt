package com.matancita.loteria.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.matancita.loteria.repository.DailyNumbersData
import com.matancita.loteria.repository.GeneratedNumbersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

// Representa el estado actual del juego del Oráculo
enum class OracleGameState {
    READY_TO_PLAY, // El usuario puede jugar
    REVEALING,     // Animación de revelado en curso
    REVEALED       // Números revelados, no se puede volver a jugar hoy
}

data class SyncResult(
    val rating: SyncRating,
    val accuracyPercent: Int, // 0-100
    val targetAngle: Float,
    val stoppedAngle: Float
)

enum class SyncRating {
    PERFECT, GREAT, GOOD, MISSED
}

class OracleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GeneratedNumbersRepository(application)
    private val screenId = "oracle_of_time"

    // Estado para los números del oráculo
    private val _oracleNumbers = MutableStateFlow<List<Int>?>(null)
    val oracleNumbers: StateFlow<List<Int>?> = _oracleNumbers.asStateFlow()

    // Estado del juego
    private val _gameState = MutableStateFlow(OracleGameState.READY_TO_PLAY)
    val gameState: StateFlow<OracleGameState> = _gameState.asStateFlow()

    // Ángulo objetivo del sweet spot (0-360)
    private val _targetAngle = MutableStateFlow(0f)
    val targetAngle: StateFlow<Float> = _targetAngle.asStateFlow()

    // Resultado de sincronía
    private val _syncResult = MutableStateFlow<SyncResult?>(null)
    val syncResult: StateFlow<SyncResult?> = _syncResult.asStateFlow()

    init {
        loadOracleState()
    }

    // Carga el estado del juego al iniciar
    private fun loadOracleState() = viewModelScope.launch {
        val data = repository.getDailyNumbersData(screenId).firstOrNull()
        val canPlay = checkIfCanPlay(data?.timestamp)

        if (canPlay) {
            _gameState.value = OracleGameState.READY_TO_PLAY
            _oracleNumbers.value = null
            _syncResult.value = null
            // Genera un ángulo objetivo aleatorio para el sweet spot
            _targetAngle.value = Random.nextFloat() * 360f
        } else {
            _gameState.value = OracleGameState.REVEALED
            _oracleNumbers.value = data?.numbers
            _syncResult.value = null
        }
    }

    // El usuario detiene el oráculo
    fun stopOracle(stoppedAngle: Float = 0f) = viewModelScope.launch {
        if (_gameState.value != OracleGameState.READY_TO_PLAY) return@launch

        _gameState.value = OracleGameState.REVEALING

        // Calcula la precisión de sincronía
        val target = _targetAngle.value
        val rawDiff = kotlin.math.abs((stoppedAngle - target + 540) % 360 - 180)
        val accuracy = ((180 - rawDiff) / 180 * 100).toInt().coerceIn(0, 100)
        val rating = when {
            rawDiff < 15 -> SyncRating.PERFECT
            rawDiff < 40 -> SyncRating.GREAT
            rawDiff < 80 -> SyncRating.GOOD
            else -> SyncRating.MISSED
        }
        _syncResult.value = SyncResult(rating, accuracy, target, stoppedAngle)

        // Genera los números basados en el día actual para consistencia
        val seed = Calendar.getInstance().get(Calendar.DAY_OF_YEAR).toLong()
        val random = Random(seed)
        val numbers = generateUniqueNumbers(3, 1..100, random)

        _oracleNumbers.value = numbers
        repository.saveDailyNumbersData(screenId, numbers, System.currentTimeMillis())

        // Cambia a REVEALED después de un momento para la animación
        kotlinx.coroutines.delay(2000)
        _gameState.value = OracleGameState.REVEALED
    }

    // Comprueba si ya se ha jugado hoy
    private fun checkIfCanPlay(lastPlayedTimestamp: Long?): Boolean {
        if (lastPlayedTimestamp == null) return true
        val now = Calendar.getInstance()
        val lastPlayCal = Calendar.getInstance().apply { timeInMillis = lastPlayedTimestamp }
        return now.get(Calendar.DAY_OF_YEAR) != lastPlayCal.get(Calendar.DAY_OF_YEAR) ||
                now.get(Calendar.YEAR) != lastPlayCal.get(Calendar.YEAR)
    }

    // Genera números únicos en un rango
    private fun generateUniqueNumbers(count: Int, range: IntRange, random: Random): List<Int> {
        return List(range.last - range.first + 1) { it + range.first }.shuffled(random).take(count).sorted()
    }
}
