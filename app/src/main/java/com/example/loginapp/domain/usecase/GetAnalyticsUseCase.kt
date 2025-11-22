package com.example.loginapp.domain.usecase

import com.example.loginapp.common.Result
import com.example.loginapp.domain.repository.MoneyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAnalyticsUseCase @Inject constructor(private val moneyRepository: MoneyRepository) {
    fun getHighestIncome(userId: Int, category: String = "PERSONAL"): Flow<Result<Double>> {
        return moneyRepository.getHighestIncome(userId, category)
    }

    fun getHighestExpense(userId: Int, category: String = "PERSONAL"): Flow<Result<Double>> {
        return moneyRepository.getHighestExpense(userId, category)
    }

    fun getMostFrequentExpenseItem(userId: Int, category: String = "PERSONAL"): Flow<Result<String>> {
        return moneyRepository.getMostFrequentExpenseItem(userId, category)
    }
}
