package com.matancita.loteria.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Calendar

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserProfile(
    val name: String,
    val dob: Long,
    val zodiacSign: String? = null
)

data class StreakData(val current: Int, val longest: Int)

class UserDataRepository(private val context: Context) {
    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_DOB = longPreferencesKey("user_dob")
        val USER_ZODIAC_SIGN = stringPreferencesKey("user_zodiac_sign")
        val STREAK_COUNT = intPreferencesKey("streak_count")
        val STREAK_LAST_DATE = longPreferencesKey("streak_last_date")
        val LONGEST_STREAK = intPreferencesKey("longest_streak")
    }

    val userProfileFlow: Flow<UserProfile?> = context.userDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val name = preferences[PreferencesKeys.USER_NAME]
            val dob = preferences[PreferencesKeys.USER_DOB]

            // --- LÓGICA CORREGIDA ---
            if (name != null && dob != null) {
                // Si tenemos los datos base, creamos el perfil.
                // El signo es opcional y puede ser null.
                val zodiacSign = preferences[PreferencesKeys.USER_ZODIAC_SIGN]
                UserProfile(name, dob, zodiacSign)
            } else {
                // Solo si faltan los datos base, el perfil es null.
                null
            }
        }

    /**
     * Guarda el perfil completo. Esta es ahora la ÚNICA función que escribe los datos del usuario.
     */
    suspend fun saveUserProfile(name: String, dob: Long, zodiacSign: String? = null) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.USER_DOB] = dob
            // Si el signo no es nulo, lo guardamos. Si es nulo, no lo tocamos.
            zodiacSign?.let {
                preferences[PreferencesKeys.USER_ZODIAC_SIGN] = it
            }
        }
    }

    val isSetupCompleteFlow: Flow<Boolean> = userProfileFlow.map { it != null }

    val streakFlow: Flow<StreakData> = context.userDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            StreakData(
                current = preferences[PreferencesKeys.STREAK_COUNT] ?: 0,
                longest = preferences[PreferencesKeys.LONGEST_STREAK] ?: 0
            )
        }

    suspend fun recordStreakCheck() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayMillis = now.timeInMillis

        context.userDataStore.edit { prefs ->
            val lastDate = prefs[PreferencesKeys.STREAK_LAST_DATE] ?: 0L
            val current = prefs[PreferencesKeys.STREAK_COUNT] ?: 0
            val longest = prefs[PreferencesKeys.LONGEST_STREAK] ?: 0

            val lastCal = Calendar.getInstance().apply {
                timeInMillis = lastDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val yesterday = Calendar.getInstance().apply {
                timeInMillis = todayMillis
                add(Calendar.DAY_OF_YEAR, -1)
            }

            val newStreak = when {
                lastDate == 0L -> 1
                lastCal.timeInMillis == todayMillis -> current
                lastCal.timeInMillis == yesterday.timeInMillis -> current + 1
                else -> 1
            }

            prefs[PreferencesKeys.STREAK_COUNT] = newStreak
            prefs[PreferencesKeys.STREAK_LAST_DATE] = todayMillis
            if (newStreak > longest) {
                prefs[PreferencesKeys.LONGEST_STREAK] = newStreak
            }
        }
    }
}