package com.example.loginapp.domain.model

data class MoneyTransaction(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String = "PERSONAL", // "PERSONAL" or "COMPANY"
    val timestamp: Long = System.currentTimeMillis()
)
