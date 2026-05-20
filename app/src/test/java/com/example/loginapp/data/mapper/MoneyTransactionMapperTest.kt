package com.example.loginapp.data.mapper

import com.example.loginapp.TestData
import com.example.loginapp.data.local.entity.MoneyTransactionEntity
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MoneyTransactionMapperTest {

    @Test
    fun `when converting entity to domain should map all fields correctly`() {
        // Given
        val entity = MoneyTransactionEntity(
            id = 1,
            userId = 1,
            title = "Salary",
            amount = 1000.0,
            type = "INCOME",
            category = "PERSONAL",
            categoryId = 1,
            timestamp = 1704067200000L
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertThat(domain.id).isEqualTo(entity.id)
        assertThat(domain.userId).isEqualTo(entity.userId)
        assertThat(domain.title).isEqualTo(entity.title)
        assertThat(domain.amount).isEqualTo(entity.amount)
        assertThat(domain.type).isEqualTo(entity.type)
        assertThat(domain.category).isEqualTo(entity.category)
        assertThat(domain.categoryId).isEqualTo(entity.categoryId)
        assertThat(domain.timestamp).isEqualTo(entity.timestamp)
    }

    @Test
    fun `when converting domain to entity should map all fields correctly`() {
        // Given
        val domain = TestData.testTransaction

        // When
        val entity = domain.toEntity()

        // Then
        assertThat(entity.id).isEqualTo(domain.id)
        assertThat(entity.userId).isEqualTo(domain.userId)
        assertThat(entity.title).isEqualTo(domain.title)
        assertThat(entity.amount).isEqualTo(domain.amount)
        assertThat(entity.type).isEqualTo(domain.type)
        assertThat(entity.category).isEqualTo(domain.category)
        assertThat(entity.categoryId).isEqualTo(domain.categoryId)
        assertThat(entity.timestamp).isEqualTo(domain.timestamp)
    }

    @Test
    fun `when converting entity to domain and back should preserve data`() {
        // Given
        val originalEntity = MoneyTransactionEntity(
            id = 2,
            userId = 1,
            title = "Groceries",
            amount = 50.0,
            type = "EXPENSE",
            category = "PERSONAL",
            categoryId = 2,
            timestamp = 1704153600000L
        )

        // When
        val domain = originalEntity.toDomain()
        val convertedEntity = domain.toEntity()

        // Then
        assertThat(convertedEntity.id).isEqualTo(originalEntity.id)
        assertThat(convertedEntity.userId).isEqualTo(originalEntity.userId)
        assertThat(convertedEntity.title).isEqualTo(originalEntity.title)
        assertThat(convertedEntity.amount).isEqualTo(originalEntity.amount)
        assertThat(convertedEntity.type).isEqualTo(originalEntity.type)
        assertThat(convertedEntity.category).isEqualTo(originalEntity.category)
        assertThat(convertedEntity.categoryId).isEqualTo(originalEntity.categoryId)
        assertThat(convertedEntity.timestamp).isEqualTo(originalEntity.timestamp)
    }

    @Test
    fun `when converting expense transaction should preserve type`() {
        // Given
        val expenseTransaction = TestData.testExpenseTransaction

        // When
        val entity = expenseTransaction.toEntity()
        val convertedDomain = entity.toDomain()

        // Then
        assertThat(convertedDomain.type).isEqualTo("EXPENSE")
        assertThat(convertedDomain.amount).isEqualTo(expenseTransaction.amount)
    }

    @Test
    fun `when converting income transaction should preserve type`() {
        // Given
        val incomeTransaction = TestData.testTransaction

        // When
        val entity = incomeTransaction.toEntity()
        val convertedDomain = entity.toDomain()

        // Then
        assertThat(convertedDomain.type).isEqualTo("INCOME")
        assertThat(convertedDomain.amount).isEqualTo(incomeTransaction.amount)
    }
}
