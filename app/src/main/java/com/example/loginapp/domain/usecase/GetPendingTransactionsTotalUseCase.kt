package com.example.loginapp.domain.usecase

import com.example.loginapp.common.Result
import com.example.loginapp.domain.repository.MoneyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPendingTransactionsTotalUseCase @Inject constructor(
    private val moneyRepository: MoneyRepository
) {
    operator fun invoke(userId: Int, category: String = "PERSONAL"): Flow<Result<Double>> {
        return moneyRepository.getPendingTransactionsTotal(userId, category)
    }
}
