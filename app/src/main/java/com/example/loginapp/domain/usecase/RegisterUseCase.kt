package com.example.loginapp.domain.usecase

import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.User
import com.example.loginapp.domain.repository.UserRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke(user: User): Result<Unit> {
        return userRepository.insertUser(user)
    }
}
