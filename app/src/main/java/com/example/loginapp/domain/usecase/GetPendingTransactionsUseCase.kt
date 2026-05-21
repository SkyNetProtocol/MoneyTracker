package com.example.loginapp.domain.usecase

import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.MoneyTransaction
import com.example.loginapp.domain.repository.MoneyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPendingTransactionsUseCase @Inject constructor(
    private val moneyRepository: MoneyRepository
) {
    operator fun invoke(userId: Int, category: String = "PERSONAL"): Flow<Result<List<MoneyTransaction>>> {
        return moneyRepository.getPendingTransactions(userId, category)
    }
}
