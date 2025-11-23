package com.example.loginapp.domain.repository

import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserByUsername(username: String): Flow<Result<User?>>
    fun getUserById(userId: Int): Flow<Result<User?>>
    suspend fun insertUser(user: User): Result<Unit>
}
