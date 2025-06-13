package com.matancita.loteria.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.matancita.loteria.repository.UserDataRepository
import com.matancita.loteria.repository.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserDataViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserDataRepository(application)

    val userProfile: StateFlow<UserProfile?> = repository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isSetupComplete: StateFlow<Boolean?> = repository.isSetupCompleteFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveUserProfile(name: String, dob: Long) {
        viewModelScope.launch {
            repository.saveUserProfile(name, dob)
        }
    }

    /**
     * Guarda solo el signo zodiacal, manteniendo el nombre y la fecha de nacimiento existentes.
     */
    fun saveZodiacSign(sign: String) {
        viewModelScope.launch {
            // Obtenemos el perfil actual para no perder sus datos.
            val currentProfile = userProfile.value
            if (currentProfile != null) {
                // Llamamos a la función de guardado unificada con los datos antiguos y el nuevo signo.
                repository.saveUserProfile(currentProfile.name, currentProfile.dob, sign)
            }
        }
    }
}