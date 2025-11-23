package com.example.loginapp.domain.usecase

import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.User
import com.example.loginapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: Int): Flow<Result<User?>> {
        return userRepository.getUserById(userId)
    }
}
