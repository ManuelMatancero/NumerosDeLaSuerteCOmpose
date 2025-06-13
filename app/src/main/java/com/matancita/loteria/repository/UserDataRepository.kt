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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserProfile(
    val name: String,
    val dob: Long,
    val zodiacSign: String? = null
)

class UserDataRepository(private val context: Context) {
    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_DOB = longPreferencesKey("user_dob")
        val USER_ZODIAC_SIGN = stringPreferencesKey("user_zodiac_sign")
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
}