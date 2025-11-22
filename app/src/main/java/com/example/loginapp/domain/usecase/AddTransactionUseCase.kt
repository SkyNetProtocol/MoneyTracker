package com.example.loginapp.domain.usecase

import com.example.loginapp.common.Result
import com.example.loginapp.data.local.entity.MoneyTransactionEntity
import com.example.loginapp.domain.repository.MoneyRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(private val moneyRepository: MoneyRepository) {
    suspend operator fun invoke(transaction: MoneyTransactionEntity): Result<Unit> {
        return moneyRepository.insertTransaction(transaction)
    }
}
