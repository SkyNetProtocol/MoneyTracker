package com.example.loginapp.domain.usecase

import com.example.loginapp.MainDispatcherRule
import com.example.loginapp.TestData
import com.example.loginapp.common.Result
import com.example.loginapp.domain.repository.MoneyRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var moneyRepository: MoneyRepository
    private lateinit var addTransactionUseCase: AddTransactionUseCase

    @Before
    fun setup() {
        moneyRepository = mockk()
        addTransactionUseCase = AddTransactionUseCase(moneyRepository)
    }

    @Test
    fun `when adding valid transaction should return success`() = runTest {
        // Given
        val transaction = TestData.testTransaction
        coEvery { moneyRepository.insertTransaction(transaction) } returns Result.Success(Unit)

        // When
        val result = addTransactionUseCase(transaction)

        // Then
        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify { moneyRepository.insertTransaction(transaction) }
    }

    @Test
    fun `when repository fails should return error`() = runTest {
        // Given
        val transaction = TestData.testTransaction
        val exception = Exception("Database error")
        coEvery { moneyRepository.insertTransaction(transaction) } returns Result.Error(exception)

        // When
        val result = addTransactionUseCase(transaction)

        // Then
        assertThat(result).isInstanceOf(Result.Error::class.java)
        assertThat((result as Result.Error).exception).isEqualTo(exception)
        coVerify { moneyRepository.insertTransaction(transaction) }
    }

    @Test
    fun `when adding expense transaction should call repository`() = runTest {
        // Given
        val expenseTransaction = TestData.testExpenseTransaction
        coEvery { moneyRepository.insertTransaction(expenseTransaction) } returns Result.Success(Unit)

        // When
        val result = addTransactionUseCase(expenseTransaction)

        // Then
        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify { moneyRepository.insertTransaction(expenseTransaction) }
    }
}
