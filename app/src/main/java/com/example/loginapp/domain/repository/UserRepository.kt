package com.example.loginapp.domain.repository

import com.example.loginapp.common.Result
import com.example.loginapp.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserByUsername(username: String): Flow<Result<UserEntity?>>
    suspend fun insertUser(user: UserEntity): Result<Unit>
}
