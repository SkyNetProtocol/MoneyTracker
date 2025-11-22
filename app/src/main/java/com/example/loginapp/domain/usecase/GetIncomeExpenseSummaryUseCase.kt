package com.example.loginapp.domain.usecase

import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.IncomeExpenseSummary
import com.example.loginapp.domain.repository.MoneyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetIncomeExpenseSummaryUseCase @Inject constructor(
    private val moneyRepository: MoneyRepository
) {
    operator fun invoke(userId: Int, category: String = "PERSONAL"): Flow<Result<IncomeExpenseSummary>> =
        combine(
            moneyRepository.getTotalIncome(userId, category),
            moneyRepository.getTotalExpense(userId, category)
        ) { incomeResult, expenseResult ->
            when {
                incomeResult is Result.Success && expenseResult is Result.Success -> {
                    val income = incomeResult.data
                    val expense = expenseResult.data
                    Result.Success(
                        IncomeExpenseSummary(
                            totalIncome = income,
                            totalExpense = expense,
                            remaining = income - expense
                        )
                    )
                }
                incomeResult is Result.Error -> Result.Error(incomeResult.exception)
                expenseResult is Result.Error -> Result.Error(expenseResult.exception)
                else -> Result.Error(Exception("Unexpected result state"))
            }
        }
}
