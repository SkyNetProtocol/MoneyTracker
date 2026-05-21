package com.example.loginapp.presentation.liquidation

import app.cash.turbine.test
import com.example.loginapp.MainDispatcherRule
import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.MoneyTransaction
import com.example.loginapp.domain.model.Category
import com.example.loginapp.domain.usecase.GetPendingTransactionsUseCase
import com.example.loginapp.domain.usecase.GetPendingTransactionsTotalUseCase
import com.example.loginapp.domain.usecase.UpdateTransactionUseCase
import com.example.loginapp.domain.usecase.AddTransactionUseCase
import com.example.loginapp.domain.repository.CategoryRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LiquidationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getPendingTransactionsUseCase: GetPendingTransactionsUseCase
    private lateinit var getPendingTransactionsTotalUseCase: GetPendingTransactionsTotalUseCase
    private lateinit var updateTransactionUseCase: UpdateTransactionUseCase
    private lateinit var addTransactionUseCase: AddTransactionUseCase
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var viewModel: LiquidationViewModel

    @Before
    fun setup() {
        getPendingTransactionsUseCase = mockk()
        getPendingTransactionsTotalUseCase = mockk()
        updateTransactionUseCase = mockk()
        addTransactionUseCase = mockk()
        categoryRepository = mockk()
        viewModel = LiquidationViewModel(
            getPendingTransactionsUseCase,
            getPendingTransactionsTotalUseCase,
            updateTransactionUseCase,
            addTransactionUseCase,
            categoryRepository
        )
    }

    @Test
    fun `loadPendingTransactions should emit transactions and sum total when successful`() = runTest {
        // Given
        val userId = 1
        val category = "PERSONAL"
        val pendingList = listOf(
            MoneyTransaction(
                id = 1,
                userId = userId,
                amount = 120.0,
                type = "EXPENSE",
                title = "Dinner",
                timestamp = System.currentTimeMillis(),
                category = category,
                isPendingLiquidation = true
            ),
            MoneyTransaction(
                id = 2,
                userId = userId,
                amount = 80.0,
                type = "EXPENSE",
                title = "Taxi",
                timestamp = System.currentTimeMillis(),
                category = category,
                isPendingLiquidation = true
            )
        )
        val expectedTotal = 200.0

        every { getPendingTransactionsUseCase(userId, category) } returns flowOf(Result.Success(pendingList))
        every { getPendingTransactionsTotalUseCase(userId, category) } returns flowOf(Result.Success(expectedTotal))

        // When
        viewModel.loadPendingTransactions(userId, category)

        // Then
        viewModel.pendingTransactions.test {
            assertThat(awaitItem()).isEqualTo(pendingList)
        }
        viewModel.pendingTotal.test {
            assertThat(awaitItem()).isEqualTo(expectedTotal)
        }
    }

    @Test
    fun `liquidateTransaction should call updateTransactionUseCase with isPendingLiquidation false and isLiquidated true and add corresponding income transaction`() = runTest {
        // Given
        val transaction = MoneyTransaction(
            id = 10,
            userId = 1,
            amount = 150.0,
            type = "EXPENSE",
            title = "Office Supplies",
            timestamp = System.currentTimeMillis(),
            category = "COMPANY",
            isPendingLiquidation = true
        )
        val expectedUpdatedTransaction = transaction.copy(isPendingLiquidation = false, isLiquidated = true)
        val mockBusinessCategory = Category(
            id = 5,
            name = "Business",
            type = "INCOME",
            color = "#2196F3",
            icon = "💼"
        )

        coEvery { updateTransactionUseCase(expectedUpdatedTransaction) } returns Result.Success(Unit)
        every { categoryRepository.getCategoriesByType("INCOME") } returns flowOf(Result.Success(listOf(mockBusinessCategory)))
        
        val capturedIncome = slot<MoneyTransaction>()
        coEvery { addTransactionUseCase(capture(capturedIncome)) } returns Result.Success(Unit)

        // When
        viewModel.liquidateTransaction(transaction)

        // Then
        coVerify(exactly = 1) { updateTransactionUseCase(expectedUpdatedTransaction) }
        coVerify(exactly = 1) { addTransactionUseCase(any()) }
        
        val addedTransaction = capturedIncome.captured
        assertThat(addedTransaction.userId).isEqualTo(transaction.userId)
        assertThat(addedTransaction.title).isEqualTo("Liquidation: Office Supplies")
        assertThat(addedTransaction.amount).isEqualTo(150.0)
        assertThat(addedTransaction.type).isEqualTo("INCOME")
        assertThat(addedTransaction.category).isEqualTo("COMPANY")
        assertThat(addedTransaction.categoryId).isEqualTo(5)
        assertThat(addedTransaction.isPendingLiquidation).isFalse()
        assertThat(addedTransaction.isLiquidated).isFalse()
    }

    @Test
    fun `liquidateAll should call updateTransactionUseCase for every pending transaction and add corresponding income transactions`() = runTest {
        // Given
        val userId = 1
        val category = "COMPANY"
        val pendingList = listOf(
            MoneyTransaction(
                id = 101,
                userId = userId,
                amount = 300.0,
                type = "EXPENSE",
                title = "Hardware",
                timestamp = System.currentTimeMillis(),
                category = category,
                isPendingLiquidation = true
            ),
            MoneyTransaction(
                id = 102,
                userId = userId,
                amount = 150.0,
                type = "EXPENSE",
                title = "Software Subscription",
                timestamp = System.currentTimeMillis(),
                category = category,
                isPendingLiquidation = true
            )
        )
        val mockBusinessCategory = Category(
            id = 5,
            name = "Business",
            type = "INCOME",
            color = "#2196F3",
            icon = "💼"
        )

        every { getPendingTransactionsUseCase(userId, category) } returns flowOf(Result.Success(pendingList))
        every { getPendingTransactionsTotalUseCase(userId, category) } returns flowOf(Result.Success(450.0))
        every { categoryRepository.getCategoriesByType("INCOME") } returns flowOf(Result.Success(listOf(mockBusinessCategory)))

        // Load the transactions into the viewModel first so that it has the list to liquidate
        viewModel.loadPendingTransactions(userId, category)

        val updatedTransaction1 = pendingList[0].copy(isPendingLiquidation = false, isLiquidated = true)
        val updatedTransaction2 = pendingList[1].copy(isPendingLiquidation = false, isLiquidated = true)

        coEvery { updateTransactionUseCase(updatedTransaction1) } returns Result.Success(Unit)
        coEvery { updateTransactionUseCase(updatedTransaction2) } returns Result.Success(Unit)
        
        val capturedIncomes = mutableListOf<MoneyTransaction>()
        coEvery { addTransactionUseCase(capture(capturedIncomes)) } returns Result.Success(Unit)

        // When
        viewModel.liquidateAll()

        // Then
        coVerify(exactly = 1) { updateTransactionUseCase(updatedTransaction1) }
        coVerify(exactly = 1) { updateTransactionUseCase(updatedTransaction2) }
        coVerify(exactly = 2) { addTransactionUseCase(any()) }
        
        assertThat(capturedIncomes).hasSize(2)
        
        val added1 = capturedIncomes[0]
        assertThat(added1.userId).isEqualTo(userId)
        assertThat(added1.title).isEqualTo("Liquidation: Hardware")
        assertThat(added1.amount).isEqualTo(300.0)
        assertThat(added1.type).isEqualTo("INCOME")
        assertThat(added1.category).isEqualTo("COMPANY")
        assertThat(added1.categoryId).isEqualTo(5)
        
        val added2 = capturedIncomes[1]
        assertThat(added2.userId).isEqualTo(userId)
        assertThat(added2.title).isEqualTo("Liquidation: Software Subscription")
        assertThat(added2.amount).isEqualTo(150.0)
        assertThat(added2.type).isEqualTo("INCOME")
        assertThat(added2.category).isEqualTo("COMPANY")
        assertThat(added2.categoryId).isEqualTo(5)
    }
}
