package com.example.loginapp

import com.example.loginapp.domain.model.User
import com.example.loginapp.domain.model.MoneyTransaction
import com.example.loginapp.domain.model.Category

/**
 * Test data objects for use in unit tests
 */
object TestData {

    val testUser = User(
        id = 1,
        username = "testuser",
        email = "test@example.com",
        passwordHash = "password123"
    )

    val testUser2 = User(
        id = 2,
        username = "testuser2",
        email = "test2@example.com",
        passwordHash = "password456"
    )

    val testTransaction = MoneyTransaction(
        id = 1,
        userId = 1,
        amount = 100.0,
        type = "INCOME",
        title = "Salary",
        timestamp = 1704067200000L, // 2024-01-01
        category = "PERSONAL",
        categoryId = 1
    )

    val testExpenseTransaction = MoneyTransaction(
        id = 2,
        userId = 1,
        amount = 50.0,
        type = "EXPENSE",
        title = "Groceries",
        timestamp = 1704153600000L, // 2024-01-02
        category = "PERSONAL",
        categoryId = 2
    )

    val testCategory = Category(
        id = 1,
        name = "Salary",
        type = "INCOME",
        icon = "💰",
        color = "#4CAF50"
    )

    val testExpenseCategory = Category(
        id = 2,
        name = "Food",
        type = "EXPENSE",
        icon = "🍔",
        color = "#F44336"
    )

    fun createTransactions(count: Int, userId: Int = 1): List<MoneyTransaction> {
        return (1..count).map { index ->
            MoneyTransaction(
                id = index,
                userId = userId,
                amount = index * 10.0,
                type = if (index % 2 == 0) "EXPENSE" else "INCOME",
                title = "Transaction $index",
                timestamp = 1704067200000L + (index * 86400000L),
                category = "PERSONAL",
                categoryId = index
            )
        }
    }
}
