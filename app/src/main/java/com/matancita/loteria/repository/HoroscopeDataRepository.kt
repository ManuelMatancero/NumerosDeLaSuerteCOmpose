package com.matancita.loteria.repository
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

data class HoroscopeData(
    val timestamp: Long,
    val luckyNumbers: List<Int>,
    val dailyHoroscope: String,
    val tappedStars: Set<Int>,
    val numbersRevealed: Boolean
)

// Usamos el mismo userDataStore, pero con claves diferentes.
class HoroscopeDataRepository(private val context: Context) {

    private object PreferencesKeys {
        val HOROSCOPE_TIMESTAMP = longPreferencesKey("horoscope_timestamp")
        val HOROSCOPE_NUMBERS = stringPreferencesKey("horoscope_numbers")
        val HOROSCOPE_TEXT = stringPreferencesKey("horoscope_text")
        val HOROSCOPE_TAPPED_STARS = stringPreferencesKey("horoscope_tapped_stars")
        val HOROSCOPE_REVEALED = booleanPreferencesKey("horoscope_revealed")
    }

    val horoscopeDataFlow: Flow<HoroscopeData?> = context.userDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val timestamp = preferences[PreferencesKeys.HOROSCOPE_TIMESTAMP]
            val numbersStr = preferences[PreferencesKeys.HOROSCOPE_NUMBERS]
            val text = preferences[PreferencesKeys.HOROSCOPE_TEXT]

            if (timestamp == null || numbersStr == null || text == null) {
                null
            } else {
                val numbers = numbersStr.split(",").mapNotNull { it.toIntOrNull() }
                val tappedStarsStr = preferences[PreferencesKeys.HOROSCOPE_TAPPED_STARS] ?: ""
                val tappedStars = tappedStarsStr.split(",").mapNotNull { it.toIntOrNull() }.toSet()
                val revealed = preferences[PreferencesKeys.HOROSCOPE_REVEALED] ?: false

                HoroscopeData(timestamp, numbers, text, tappedStars, revealed)
            }
        }

    suspend fun saveHoroscopeData(data: HoroscopeData) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.HOROSCOPE_TIMESTAMP] = data.timestamp
            preferences[PreferencesKeys.HOROSCOPE_NUMBERS] = data.luckyNumbers.joinToString(",")
            preferences[PreferencesKeys.HOROSCOPE_TEXT] = data.dailyHoroscope
            preferences[PreferencesKeys.HOROSCOPE_TAPPED_STARS] = data.tappedStars.joinToString(",")
            preferences[PreferencesKeys.HOROSCOPE_REVEALED] = data.numbersRevealed
        }
    }

    suspend fun clearHoroscopeData() {
        context.userDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.HOROSCOPE_TIMESTAMP)
            preferences.remove(PreferencesKeys.HOROSCOPE_NUMBERS)
            preferences.remove(PreferencesKeys.HOROSCOPE_TEXT)
            preferences.remove(PreferencesKeys.HOROSCOPE_TAPPED_STARS)
            preferences.remove(PreferencesKeys.HOROSCOPE_REVEALED)
        }
    }
}