package com.example.loginapp.data.mapper

import com.example.loginapp.data.local.entity.MoneyTransactionEntity
import com.example.loginapp.domain.model.MoneyTransaction

fun MoneyTransactionEntity.toDomain(): MoneyTransaction {
    return MoneyTransaction(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        type = type,
        category = category,
        categoryId = categoryId,
        timestamp = timestamp
    )
}

fun MoneyTransaction.toEntity(): MoneyTransactionEntity {
    return MoneyTransactionEntity(
        id = id,
        userId = userId,
        title = title,
        amount = amount,
        type = type,
        category = category,
        categoryId = categoryId,
        timestamp = timestamp
    )
}
