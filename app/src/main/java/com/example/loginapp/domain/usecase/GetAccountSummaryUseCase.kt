package com.example.loginapp.domain.usecase

import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.IncomeExpenseSummary
import com.example.loginapp.domain.repository.MoneyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetAccountSummaryUseCase @Inject constructor(
    private val moneyRepository: MoneyRepository
) {
    fun getDistinctDates(userId: Int, category: String = "PERSONAL"): Flow<Result<List<String>>> {
        return moneyRepository.getDistinctDates(userId, category)
    }

    fun getDateSummary(userId: Int, category: String = "PERSONAL", date: String): Flow<Result<IncomeExpenseSummary>> {
        return combine(
            moneyRepository.getIncomeForDate(userId, category, date),
            moneyRepository.getExpenseForDate(userId, category, date)
        ) { incomeResult, expenseResult ->
            if (incomeResult is Result.Success && expenseResult is Result.Success) {
                val income = incomeResult.data
                val expense = expenseResult.data
                Result.Success(IncomeExpenseSummary(income, expense, income - expense))
            } else if (incomeResult is Result.Error) {
                Result.Error(incomeResult.exception)
            } else if (expenseResult is Result.Error) {
                Result.Error(expenseResult.exception)
            } else {
                Result.Loading
            }
        }
    }
}
