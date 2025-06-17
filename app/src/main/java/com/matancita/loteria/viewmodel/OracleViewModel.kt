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

class OracleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GeneratedNumbersRepository(application)
    private val screenId = "oracle_of_time"

    // Estado para los números del oráculo
    private val _oracleNumbers = MutableStateFlow<List<Int>?>(null)
    val oracleNumbers: StateFlow<List<Int>?> = _oracleNumbers.asStateFlow()

    // Estado del juego
    private val _gameState = MutableStateFlow(OracleGameState.READY_TO_PLAY)
    val gameState: StateFlow<OracleGameState> = _gameState.asStateFlow()

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
        } else {
            _gameState.value = OracleGameState.REVEALED
            _oracleNumbers.value = data?.numbers
        }
    }

    // El usuario detiene el oráculo
    fun stopOracle() = viewModelScope.launch {
        if (_gameState.value != OracleGameState.READY_TO_PLAY) return@launch

        _gameState.value = OracleGameState.REVEALING

        // Genera los números basados en el día actual para consistencia
        val seed = Calendar.getInstance().get(Calendar.DAY_OF_YEAR).toLong()
        val random = Random(seed)
        val numbers = generateUniqueNumbers(3, 1..100, random)

        _oracleNumbers.value = numbers
        repository.saveDailyNumbersData(screenId, numbers, System.currentTimeMillis())

        // Cambia a REVEALED después de un momento para la animación
        kotlinx.coroutines.delay(1500) // Simula la animación de frenado de las manecillas
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
