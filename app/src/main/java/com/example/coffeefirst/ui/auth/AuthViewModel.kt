package com.example.coffeefirst.ui.auth

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeefirst.data.auth.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val sharedPref: SharedPreferences
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(loadSavedAuthState())
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repo.login(email, password)
            _authState.value = if (result.isSuccess) {
                sharedPref.edit().putBoolean("is_logged_in", true).apply()
                AuthState.Success
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = repo.register(email, password)
                _authState.value = if (result.isSuccess) {
                    sharedPref.edit().putBoolean("is_logged_in", true).apply()
                    AuthState.Success
                } else {
                    AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.message}")
            }
        }
    }

    private fun loadSavedAuthState(): AuthState {
        return if (sharedPref.getBoolean("is_logged_in", false)) {
            AuthState.Success
        } else {
            AuthState.Idle
        }
    }

    fun deleteAccount(): LiveData<Result<Unit>> {
        val result = MutableLiveData<Result<Unit>>()
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().currentUser?.delete()?.await()
                sharedPref.edit().putBoolean("is_logged_in", false).apply()
                clearUserData()
                result.postValue(Result.success(Unit))
            } catch (e: Exception) {
                result.postValue(Result.failure(e))
            }
        }
        return result
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        sharedPref.edit().putBoolean("is_logged_in", false).apply()
        clearUserData()
        _authState.value = AuthState.Idle
    }

    private fun clearUserData() {
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)
        sharedPref.edit().clear().apply()
        sharedPref.edit().putBoolean("is_logged_in", isLoggedIn).apply()
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
