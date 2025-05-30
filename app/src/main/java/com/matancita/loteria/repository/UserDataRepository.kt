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

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserProfile(
    val name: String,
    val dob: Long // Store Date of Birth as Long (timestamp)
)

class UserDataRepository(private val context: Context) {
    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_DOB = longPreferencesKey("user_dob")
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
            if (name != null && dob != null) {
                UserProfile(name, dob)
            } else {
                null
            }
        }

    suspend fun saveUserProfile(name: String, dob: Long) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.USER_DOB] = dob
        }
    }

    val isSetupCompleteFlow: Flow<Boolean> = userProfileFlow.map { it != null }
}