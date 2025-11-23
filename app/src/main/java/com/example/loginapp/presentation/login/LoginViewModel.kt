package com.example.loginapp.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.User
import com.example.loginapp.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(username: String, passwordHash: String) {
        viewModelScope.launch {
            loginUseCase(username, passwordHash).collect { result ->
                when (result) {
                    is Result.Loading -> _loginState.value = LoginState.Loading
                    is Result.Success -> {
                        if (result.data != null) {
                            _loginState.value = LoginState.Success(result.data)
                        } else {
                            _loginState.value = LoginState.Error("Invalid credentials")
                        }
                    }
                    is Result.Error -> _loginState.value = LoginState.Error(result.exception.message ?: "Unknown error")
                }
            }
        }
    }
}


