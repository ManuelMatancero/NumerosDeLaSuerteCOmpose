package com.matancita.loteria.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Extensión para DataStore (puedes tener múltiples archivos de preferencias)
val Context.generatedNumbersDataStore: DataStore<Preferences> by preferencesDataStore(name = "generated_numbers")

data class DailyNumbersData(
    val numbers: List<Int>,
    val timestamp: Long
)

class GeneratedNumbersRepository(private val context: Context) {

    private fun getNumbersKey(screenId: String) = stringPreferencesKey("${screenId}_numbers")
    private fun getTimestampKey(screenId: String) = longPreferencesKey("${screenId}_timestamp")

    fun getDailyNumbersData(screenId: String): Flow<DailyNumbersData?> {
        return context.generatedNumbersDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val numbersString = preferences[getNumbersKey(screenId)]
                val timestamp = preferences[getTimestampKey(screenId)]
                if (numbersString != null && timestamp != null) {
                    val numbers = numbersString.split(",").mapNotNull { it.toIntOrNull() }
                    DailyNumbersData(numbers, timestamp)
                } else {
                    null
                }
            }
    }

    suspend fun saveDailyNumbersData(screenId: String, numbers: List<Int>, timestamp: Long) {
        context.generatedNumbersDataStore.edit { preferences ->
            preferences[getNumbersKey(screenId)] = numbers.joinToString(",")
            preferences[getTimestampKey(screenId)] = timestamp
        }
    }

    suspend fun clearDailyNumbersData(screenId: String) {
        context.generatedNumbersDataStore.edit { preferences ->
            preferences.remove(getNumbersKey(screenId))
            preferences.remove(getTimestampKey(screenId))
        }
    }
}
