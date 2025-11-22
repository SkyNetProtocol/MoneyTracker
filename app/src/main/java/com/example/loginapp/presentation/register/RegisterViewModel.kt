package com.example.loginapp.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginapp.common.Result
import com.example.loginapp.data.local.entity.UserEntity
import com.example.loginapp.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun register(username: String, passwordHash: String) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            val user = UserEntity(username = username, passwordHash = passwordHash)
            val result = registerUseCase(user)
            when (result) {
                is Result.Success -> _registerState.value = RegisterState.Success
                is Result.Error -> _registerState.value = RegisterState.Error(result.exception.message ?: "Registration failed")
                is Result.Loading -> _registerState.value = RegisterState.Loading
            }
        }
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}
