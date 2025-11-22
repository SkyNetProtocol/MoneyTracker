package com.example.loginapp.domain.repository

import com.example.loginapp.common.Result
import com.example.loginapp.data.local.entity.MoneyTransactionEntity
import com.example.loginapp.domain.model.MoneyTransaction
import kotlinx.coroutines.flow.Flow

interface MoneyRepository {
    fun getTransactionsForUser(userId: Int, category: String, limit: Int): Flow<Result<List<MoneyTransaction>>>
    suspend fun insertTransaction(transaction: MoneyTransactionEntity): Result<Unit>
    suspend fun deleteTransaction(transaction: MoneyTransactionEntity): Result<Unit>
    fun getTotalIncome(userId: Int, category: String): Flow<Result<Double>>
    fun getTotalExpense(userId: Int, category: String): Flow<Result<Double>>
    fun getHighestIncome(userId: Int, category: String): Flow<Result<Double>>
    fun getHighestExpense(userId: Int, category: String): Flow<Result<Double>>
    fun getMostFrequentExpenseItem(userId: Int, category: String): Flow<Result<String>>
    fun getDistinctDates(userId: Int, category: String): Flow<Result<List<String>>>
    fun getIncomeForDate(userId: Int, category: String, date: String): Flow<Result<Double>>
    fun getExpenseForDate(userId: Int, category: String, date: String): Flow<Result<Double>>
    fun getUserById(userId: Int): Flow<Result<com.example.loginapp.data.local.entity.UserEntity?>>
}
