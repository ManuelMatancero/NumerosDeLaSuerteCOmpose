package com.matancita.loteria.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.matancita.loteria.repository.DailyNumbersData
import com.matancita.loteria.repository.GeneratedNumbersRepository
import com.matancita.loteria.repository.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class NumbersViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GeneratedNumbersRepository(application)

    // Screen 1
    private val _screen1NumbersData = MutableStateFlow<DailyNumbersData?>(null)
    val screen1NumbersData: StateFlow<DailyNumbersData?> = _screen1NumbersData.asStateFlow()

    private val _canGenerateScreen1 = MutableStateFlow(true)
    val canGenerateScreen1: StateFlow<Boolean> = _canGenerateScreen1.asStateFlow()

//    private val _showInterstitial = MutableStateFlow(false)
//    val showInterstitial: StateFlow<Boolean> = _showInterstitial.asStateFlow()

    // Screen 2
    private val _screen2NumbersData = MutableStateFlow<DailyNumbersData?>(null)
    val screen2NumbersData: StateFlow<DailyNumbersData?> = _screen2NumbersData.asStateFlow()

    private val _canGenerateScreen2 = MutableStateFlow(true)
    val canGenerateScreen2: StateFlow<Boolean> = _canGenerateScreen2.asStateFlow()

    // VIP Numbers
    private val _vipNumbers = MutableStateFlow<List<Int>>(emptyList())
    val vipNumbers: StateFlow<List<Int>> = _vipNumbers.asStateFlow()

    fun loadNumbersForScreen(screenId: String, userProfile: UserProfile?) {
        viewModelScope.launch {
            val data = repository.getDailyNumbersData(screenId).firstOrNull()
            val canGenerate = checkIfCanGenerate(data?.timestamp)

            if (screenId == "screen1") {
                _screen1NumbersData.value = if (canGenerate) null else data // Clear if can generate new
                _canGenerateScreen1.value = canGenerate
                if (canGenerate) {
                    _vipNumbers.value = emptyList()
                    repository.clearDailyNumbersData("vip")
                } else {
                    // Restore VIP numbers from today if available
                    val vipData = repository.getDailyNumbersData("vip").firstOrNull()
                    val vipCanGenerate = checkIfCanGenerate(vipData?.timestamp)
                    _vipNumbers.value = if (vipCanGenerate) emptyList() else vipData?.numbers ?: emptyList()
                }
            } else if (screenId == "screen2") {
                _screen2NumbersData.value = if (canGenerate) null else data
                _canGenerateScreen2.value = canGenerate
            }
            // If can't generate, but data is null (e.g., first time ever), numbers will be empty
            // and button will be enabled.
            if (!canGenerate && data != null) {
                // Data is from a previous day but still within 24h, or it's today's data
                // and we should show it.
                if (screenId == "screen1") _screen1NumbersData.value = data
                if (screenId == "screen2") _screen2NumbersData.value = data
            } else if (canGenerate) {
                // It's a new day or 24h passed, clear previous data for display
                if (screenId == "screen1") _screen1NumbersData.value = null
                if (screenId == "screen2") _screen2NumbersData.value = null
            }
        }
    }


    private fun checkIfCanGenerate(lastGenerationTimestamp: Long?): Boolean {
        if (lastGenerationTimestamp == null) return true // Never generated
        val now = Calendar.getInstance()
        val lastGenCal = Calendar.getInstance().apply { timeInMillis = lastGenerationTimestamp }

        // Check if it's a different day OR more than 24 hours have passed
        val isDifferentDay = now.get(Calendar.DAY_OF_YEAR) != lastGenCal.get(Calendar.DAY_OF_YEAR) ||
                now.get(Calendar.YEAR) != lastGenCal.get(Calendar.YEAR)

        // More sophisticated: Check if 24 hours have strictly passed
        // val twentyFourHoursInMillis = 24 * 60 * 60 * 1000
        // return (System.currentTimeMillis() - lastGenerationTimestamp) >= twentyFourHoursInMillis

        return isDifferentDay // Simpler: just check if it's a new calendar day
    }

    fun generateNumbersForScreen(screenId: String, count: Int, userProfile: UserProfile?) {
        if (userProfile == null) return // Should not happen if setup is complete

        val todayString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        // Incorporate screenId into the seed to ensure different numbers for screen1 and screen2
        val seedString = "${userProfile.name}-${userProfile.dob}-$todayString-$screenId"
        val seed = seedString.hashCode().toLong()
        val random = Random(seed)
        val numbers = mutableSetOf<Int>()
        while (numbers.size < count) {
            numbers.add(random.nextInt(1, 101)) // Numbers from 1 to 100
        }
        val generatedNumbers = numbers.toList().sorted()
        val currentTime = System.currentTimeMillis()

        viewModelScope.launch {
            repository.saveDailyNumbersData(screenId, generatedNumbers, currentTime)
            if (screenId == "screen1") {
                _screen1NumbersData.value = DailyNumbersData(generatedNumbers, currentTime)
                _canGenerateScreen1.value = false
            } else if (screenId == "screen2") {
                _screen2NumbersData.value = DailyNumbersData(generatedNumbers, currentTime)
                _canGenerateScreen2.value = false
            }
        }
    }

    fun generateVipNumber(userProfile: UserProfile?): Int {
        if (userProfile == null) return -1
        val seed = "${userProfile.name}-${userProfile.dob}-vip-${System.currentTimeMillis()}".hashCode().toLong()
        return Random(seed).nextInt(1, 101)
    }

    fun commitVipNumber(number: Int) {
        val updated = _vipNumbers.value + number
        _vipNumbers.value = updated
        viewModelScope.launch {
            repository.saveDailyNumbersData("vip", updated, System.currentTimeMillis())
        }
    }

    fun clearVipNumbers() {
        _vipNumbers.value = emptyList()
        viewModelScope.launch {
            repository.clearDailyNumbersData("vip")
        }
    }
}