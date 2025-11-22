package com.example.loginapp.domain.usecase

import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.User
import com.example.loginapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val userRepository: UserRepository) {
    operator fun invoke(username: String, passwordHash: String): Flow<Result<User?>> {
        return userRepository.getUserByUsername(username).map { result ->
            when (result) {
                is Result.Success -> {
                    val user = result.data
                    if (user != null && user.passwordHash == passwordHash) {
                        Result.Success(user)
                    } else {
                        Result.Success(null) // User not found or password mismatch
                    }
                }
                is Result.Error -> Result.Error(result.exception)
                is Result.Loading -> Result.Loading
            }
        }
    }
}
