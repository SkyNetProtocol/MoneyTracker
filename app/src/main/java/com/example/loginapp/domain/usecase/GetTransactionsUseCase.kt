package com.example.loginapp.domain.usecase

import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.MoneyTransaction
import com.example.loginapp.domain.repository.MoneyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(private val moneyRepository: MoneyRepository) {
    operator fun invoke(userId: Int, category: String = "PERSONAL", limit: Int = Int.MAX_VALUE): Flow<Result<List<MoneyTransaction>>> {
        return moneyRepository.getTransactionsForUser(userId, category, limit)
    }
}
