package com.example.loginapp.domain.usecase

import com.example.loginapp.common.Result
import com.example.loginapp.data.local.entity.UserEntity
import com.example.loginapp.domain.repository.UserRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke(user: UserEntity): Result<Unit> {
        return userRepository.insertUser(user)
    }
}
