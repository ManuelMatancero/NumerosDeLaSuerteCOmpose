package com.matancita.loteria.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.matancita.loteria.repository.DailyNumbersData
import com.matancita.loteria.repository.GeneratedNumbersRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.random.Random

// Data classes to hold the specific structure for each game
data class SuperLotoMasData(val mainNumbers: List<Int>, val superBall: Int, val superMasBall: Int)
data class GameNumbers(val numbers: List<Int>)

class LotteryGamesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GeneratedNumbersRepository(application)

    // StateFlow for each lottery game
    private val _superLotoMas = MutableStateFlow<SuperLotoMasData?>(null)
    val superLotoMas: StateFlow<SuperLotoMasData?> = _superLotoMas.asStateFlow()

    private val _superKinoTv = MutableStateFlow<GameNumbers?>(null)
    val superKinoTv: StateFlow<GameNumbers?> = _superKinoTv.asStateFlow()

    private val _lotoPool = MutableStateFlow<GameNumbers?>(null)
    val lotoPool: StateFlow<GameNumbers?> = _lotoPool.asStateFlow()

    private val _pegaTres = MutableStateFlow<GameNumbers?>(null)
    val pegaTres: StateFlow<GameNumbers?> = _pegaTres.asStateFlow()

    private val _quinielaPale = MutableStateFlow<GameNumbers?>(null)
    val quinielaPale: StateFlow<GameNumbers?> = _quinielaPale.asStateFlow()

    private val _loteriaReal = MutableStateFlow<GameNumbers?>(null)
    val loteriaReal: StateFlow<GameNumbers?> = _loteriaReal.asStateFlow()

    // State for overall generation status
    private val _canGenerate = MutableStateFlow(true)
    val canGenerate: StateFlow<Boolean> = _canGenerate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAllGames()
    }

    private fun loadAllGames() = viewModelScope.launch {
        val lotoData = repository.getDailyNumbersData("super_loto_mas").firstOrNull()
        val canGen = checkIfCanGenerate(lotoData?.timestamp)
        _canGenerate.value = canGen

        if (!canGen) {
            _superLotoMas.value = parseSuperLotoMas(repository.getDailyNumbersData("super_loto_mas").firstOrNull())
            _superKinoTv.value = parseGameNumbers(repository.getDailyNumbersData("super_kino_tv").firstOrNull())
            _lotoPool.value = parseGameNumbers(repository.getDailyNumbersData("loto_pool").firstOrNull())
            _pegaTres.value = parseGameNumbers(repository.getDailyNumbersData("pega_tres").firstOrNull())
            _quinielaPale.value = parseGameNumbers(repository.getDailyNumbersData("quiniela_pale").firstOrNull())
            _loteriaReal.value = parseGameNumbers(repository.getDailyNumbersData("loteria_real").firstOrNull())
        }
    }

    fun generateAllGames() = viewModelScope.launch {
        if (!_canGenerate.value || _isLoading.value) return@launch

        _isLoading.value = true
        _canGenerate.value = false
        val currentTime = System.currentTimeMillis()

        // Clear previous values to show loading state
        _superLotoMas.value = null
        _superKinoTv.value = null
        _lotoPool.value = null
        _pegaTres.value = null
        _quinielaPale.value = null
        _loteriaReal.value = null

        // Staggered generation with delays
        delay(500)
        val lotoMasMain = generateUniqueNumbers(6, 1..40)
        val lotoMasSuperBall = generateUniqueNumbers(1, 1..12).first()
        val lotoMasSuperMasBall = generateUniqueNumbers(1, 1..15).first()
        val superLotoMasNumbers = lotoMasMain + lotoMasSuperBall + lotoMasSuperMasBall
        _superLotoMas.value = SuperLotoMasData(lotoMasMain, lotoMasSuperBall, lotoMasSuperMasBall)
        repository.saveDailyNumbersData("super_loto_mas", superLotoMasNumbers, currentTime)

        delay(500)
        val kinoNumbers = generateUniqueNumbers(20, 1..80)
        _superKinoTv.value = GameNumbers(kinoNumbers)
        repository.saveDailyNumbersData("super_kino_tv", kinoNumbers, currentTime)

        delay(500)
        val poolNumbers = generateUniqueNumbers(5, 1..31)
        _lotoPool.value = GameNumbers(poolNumbers)
        repository.saveDailyNumbersData("loto_pool", poolNumbers, currentTime)

        delay(500)
        val pegaTresNumbers = generateUniqueNumbers(3, 0..50)
        _pegaTres.value = GameNumbers(pegaTresNumbers)
        repository.saveDailyNumbersData("pega_tres", pegaTresNumbers, currentTime)

        delay(500)
        val quinielaPaleNumbers = generateUniqueNumbers(3, 1..100)
        _quinielaPale.value = GameNumbers(quinielaPaleNumbers)
        repository.saveDailyNumbersData("quiniela_pale", quinielaPaleNumbers, currentTime)

        delay(500)
        val loteriaRealNumbers = generateUniqueNumbers(3, 1..100)
        _loteriaReal.value = GameNumbers(loteriaRealNumbers)
        repository.saveDailyNumbersData("loteria_real", loteriaRealNumbers, currentTime)

        _isLoading.value = false
    }

    private fun checkIfCanGenerate(lastGenerationTimestamp: Long?): Boolean {
        if (lastGenerationTimestamp == null) return true
        val now = Calendar.getInstance()
        val lastGenCal = Calendar.getInstance().apply { timeInMillis = lastGenerationTimestamp }
        return now.get(Calendar.DAY_OF_YEAR) != lastGenCal.get(Calendar.DAY_OF_YEAR) ||
                now.get(Calendar.YEAR) != lastGenCal.get(Calendar.YEAR)
    }

    private fun generateUniqueNumbers(count: Int, range: IntRange): List<Int> {
        return List(range.last - range.first + 1) { it + range.first }.shuffled(Random).take(count).sorted()
    }

    private fun parseSuperLotoMas(data: DailyNumbersData?): SuperLotoMasData? {
        return data?.numbers?.let {
            if (it.size == 8) SuperLotoMasData(
                mainNumbers = it.take(6),
                superBall = it[6],
                superMasBall = it[7]
            ) else null
        }
    }

    private fun parseGameNumbers(data: DailyNumbersData?): GameNumbers? {
        return data?.numbers?.let { GameNumbers(it) }
    }
}
