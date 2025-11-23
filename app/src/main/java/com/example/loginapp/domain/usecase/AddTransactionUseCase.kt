package com.example.loginapp.domain.usecase

import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.MoneyTransaction
import com.example.loginapp.domain.repository.MoneyRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(private val moneyRepository: MoneyRepository) {
    suspend operator fun invoke(transaction: MoneyTransaction): Result<Unit> {
        return moneyRepository.insertTransaction(transaction)
    }
}
