package com.example.loginapp.data.repository

import com.example.loginapp.common.Result
import com.example.loginapp.data.local.dao.MoneyTransactionDao
import com.example.loginapp.data.local.dao.UserDao
import com.example.loginapp.data.local.entity.MoneyTransactionEntity
import com.example.loginapp.di.IoDispatcher
import com.example.loginapp.domain.model.MoneyTransaction
import com.example.loginapp.domain.repository.MoneyRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoneyRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val moneyTransactionDao: MoneyTransactionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MoneyRepository {

    override fun getTransactionsForUser(userId: Int, category: String, limit: Int): Flow<Result<List<MoneyTransaction>>> =
        moneyTransactionDao.getTransactionsForUser(userId, category, limit)
            .map { entities ->
                Result.Success(entities.map { entity ->
                    MoneyTransaction(
                        id = entity.id,
                        userId = entity.userId,
                        title = entity.title,
                        amount = entity.amount,
                        type = entity.type,
                        category = entity.category,
                        timestamp = entity.timestamp
                    )
                }) as Result<List<MoneyTransaction>>
            }
            .catch { e -> emit(Result.Error(e)) }
            .flowOn(ioDispatcher)

    override suspend fun insertTransaction(transaction: MoneyTransactionEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            moneyTransactionDao.insertTransaction(transaction)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateTransaction(transaction: MoneyTransactionEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            moneyTransactionDao.updateTransaction(transaction)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteTransaction(transaction: MoneyTransactionEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            moneyTransactionDao.deleteTransaction(transaction)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getHighestIncome(userId: Int, category: String): Flow<Result<Double>> =
        moneyTransactionDao.getHighestIncome(userId, category)
            .map { Result.Success(it ?: 0.0) as Result<Double> }
            .catch { e -> emit(Result.Error(e)) }
            .flowOn(ioDispatcher)

    override fun getHighestExpense(userId: Int, category: String): Flow<Result<Double>> =
        moneyTransactionDao.getHighestExpense(userId, category)
            .map { Result.Success(it ?: 0.0) as Result<Double> }
            .catch { e -> emit(Result.Error(e)) }
            .flowOn(ioDispatcher)

    override fun getTotalExpense(userId: Int, category: String): Flow<Result<Double>> =
        moneyTransactionDao.getTotalExpense(userId, category)
            .map { Result.Success(it ?: 0.0) as Result<Double> }
            .catch { e -> emit(Result.Error(e)) }
            .flowOn(ioDispatcher)

    override fun getTotalIncome(userId: Int, category: String): Flow<Result<Double>> =
        moneyTransactionDao.getTotalIncome(userId, category)
            .map { Result.Success(it ?: 0.0) as Result<Double> }
            .catch { e -> emit(Result.Error(e)) }
            .flowOn(ioDispatcher)

    override fun getDistinctDates(userId: Int, category: String): Flow<Result<List<String>>> =
        moneyTransactionDao.getDistinctDates(userId, category)
            .map { Result.Success(it) as Result<List<String>> }
            .catch { e -> emit(Result.Error(e)) }
            .flowOn(ioDispatcher)

    override fun getIncomeForDate(userId: Int, category: String, date: String): Flow<Result<Double>> =
        moneyTransactionDao.getIncomeForDate(userId, category, date)
            .map { Result.Success(it ?: 0.0) as Result<Double> }
            .catch { e -> emit(Result.Error(e)) }
            .flowOn(ioDispatcher)

    override fun getExpenseForDate(userId: Int, category: String, date: String): Flow<Result<Double>> =
        moneyTransactionDao.getExpenseForDate(userId, category, date)
            .map { Result.Success(it ?: 0.0) as Result<Double> }
            .catch { e -> emit(Result.Error(e)) }
            .flowOn(ioDispatcher)

    override fun getMostFrequentExpenseItem(userId: Int, category: String): Flow<Result<String>> =
        moneyTransactionDao.getMostFrequentExpenseItem(userId, category)
            .map { Result.Success(it ?: "") as Result<String> }
            .catch { e -> emit(Result.Error(e)) }
            .flowOn(ioDispatcher)

    override fun getUserById(userId: Int): Flow<Result<com.example.loginapp.data.local.entity.UserEntity?>> =
        userDao.getUserById(userId)
            .map { Result.Success(it) as Result<com.example.loginapp.data.local.entity.UserEntity?> }
            .catch { e -> emit(Result.Error(e)) }
            .flowOn(ioDispatcher)
}
