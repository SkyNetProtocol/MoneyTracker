package com.example.loginapp.data.repository

import com.example.loginapp.common.Result
import com.example.loginapp.data.local.dao.UserDao
import com.example.loginapp.data.local.entity.UserEntity
import com.example.loginapp.di.IoDispatcher
import com.example.loginapp.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    override fun getUserByUsername(username: String): Flow<Result<UserEntity?>> = flow {
        emit(Result.Loading)
        userDao.getUserByUsername(username)
            .map { Result.Success(it) }
            .collect { emit(it) }
    }.catch { e ->
        emit(Result.Error(e))
    }.flowOn(ioDispatcher)

    override suspend fun insertUser(user: UserEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            userDao.insertUser(user)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
