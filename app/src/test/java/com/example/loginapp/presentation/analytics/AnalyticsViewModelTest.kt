package com.example.loginapp.presentation.analytics

import app.cash.turbine.test
import com.example.loginapp.MainDispatcherRule
import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.MoneyTransaction
import com.example.loginapp.domain.usecase.GetAnalyticsUseCase
import com.example.loginapp.domain.usecase.GetTransactionsUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getAnalyticsUseCase: GetAnalyticsUseCase
    private lateinit var getTransactionsUseCase: GetTransactionsUseCase
    private lateinit var viewModel: AnalyticsViewModel

    @Before
    fun setup() {
        getAnalyticsUseCase = mockk()
        getTransactionsUseCase = mockk()
        viewModel = AnalyticsViewModel(getAnalyticsUseCase, getTransactionsUseCase)
    }

    @Test
    fun `loadAnalytics should fetch highest income, highest expense, and frequent items`() = runTest {
        // Given
        val userId = 1
        val category = "PERSONAL"
        
        every { getAnalyticsUseCase.getHighestIncome(userId, category) } returns flowOf(Result.Success(5000.0))
        every { getAnalyticsUseCase.getHighestExpense(userId, category) } returns flowOf(Result.Success(1500.0))
        every { getAnalyticsUseCase.getMostFrequentExpenseItem(userId, category) } returns flowOf(Result.Success("Coffee"))
        every { getTransactionsUseCase(userId, category, 1000) } returns flowOf(Result.Success(emptyList()))

        // When
        viewModel.loadAnalytics(userId, category)

        // Then
        viewModel.highestIncome.test {
            assertThat(awaitItem()).isEqualTo(5000.0)
        }
        viewModel.highestExpense.test {
            assertThat(awaitItem()).isEqualTo(1500.0)
        }
        viewModel.mostFrequentItem.test {
            assertThat(awaitItem()).isEqualTo("Coffee")
        }
    }

    @Test
    fun `loadAnalytics should calculate weekly and monthly balances correctly`() = runTest {
        // Given
        val userId = 1
        val category = "PERSONAL"
        val now = System.currentTimeMillis()

        // 1. Transaction in current week (Income & Expense)
        val weekIncome = MoneyTransaction(id = 1, userId = userId, amount = 1000.0, type = "INCOME", title = "Weekly Income", timestamp = now, category = category)
        val weekExpense = MoneyTransaction(id = 2, userId = userId, amount = 200.0, type = "EXPENSE", title = "Weekly Expense", timestamp = now, category = category)

        // 2. Transaction in current month but DIFFERENT week
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val monthOnlyTime = if (currentDay > 8) {
            now - 8L * 24 * 60 * 60 * 1000 // Move back 8 days to guarantee different week but same month
        } else {
            now + 8L * 24 * 60 * 60 * 1000 // Move forward 8 days to guarantee different week but same month
        }
        val monthIncome = MoneyTransaction(id = 3, userId = userId, amount = 500.0, type = "INCOME", title = "Month Income", timestamp = monthOnlyTime, category = category)
        val monthExpense = MoneyTransaction(id = 4, userId = userId, amount = 100.0, type = "EXPENSE", title = "Month Expense", timestamp = monthOnlyTime, category = category)

        // 3. Transaction in previous month (completely unrelated)
        val oldTime = now - 40L * 24 * 60 * 60 * 1000 // 40 days ago is guaranteed to be a different month
        val oldIncome = MoneyTransaction(id = 5, userId = userId, amount = 2000.0, type = "INCOME", title = "Old Income", timestamp = oldTime, category = category)

        val transactions = listOf(weekIncome, weekExpense, monthIncome, monthExpense, oldIncome)

        every { getAnalyticsUseCase.getHighestIncome(userId, category) } returns flowOf(Result.Success(0.0))
        every { getAnalyticsUseCase.getHighestExpense(userId, category) } returns flowOf(Result.Success(0.0))
        every { getAnalyticsUseCase.getMostFrequentExpenseItem(userId, category) } returns flowOf(Result.Success(""))
        every { getTransactionsUseCase(userId, category, 1000) } returns flowOf(Result.Success(transactions))

        // When
        viewModel.loadAnalytics(userId, category)

        // Then
        // Weekly Balance = weekIncome (1000.0) - weekExpense (200.0) = 800.0
        viewModel.weeklyBalance.test {
            assertThat(awaitItem()).isEqualTo(800.0)
        }

        // Monthly Balance = (weekIncome + monthIncome) - (weekExpense + monthExpense) = (1000 + 500) - (200 + 100) = 1500 - 300 = 1200.0
        viewModel.monthlyBalance.test {
            assertThat(awaitItem()).isEqualTo(1200.0)
        }

        // Weekly Balance Range formatting check
        val expectedWeekCalendar = Calendar.getInstance()
        val firstDay = expectedWeekCalendar.firstDayOfWeek
        expectedWeekCalendar.set(Calendar.DAY_OF_WEEK, firstDay)
        val expectedStart = expectedWeekCalendar.time
        expectedWeekCalendar.add(Calendar.DAY_OF_WEEK, 6)
        val expectedEnd = expectedWeekCalendar.time
        val weekFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        val expectedWeekRange = "${weekFormat.format(expectedStart)} - ${weekFormat.format(expectedEnd)}"

        viewModel.weeklyBalanceRange.test {
            assertThat(awaitItem()).isEqualTo(expectedWeekRange)
        }

        // Monthly Balance Range formatting check
        val expectedMonthFormat = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
        val expectedMonthRange = expectedMonthFormat.format(Calendar.getInstance().time)

        viewModel.monthlyBalanceRange.test {
            assertThat(awaitItem()).isEqualTo(expectedMonthRange)
        }
    }
}
