package com.example.loginapp.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginapp.common.Result
import com.example.loginapp.domain.usecase.GetAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAnalyticsUseCase: GetAnalyticsUseCase,
    private val getTransactionsUseCase: com.example.loginapp.domain.usecase.GetTransactionsUseCase
) : ViewModel() {

    private val _highestIncome = MutableStateFlow<Double>(0.0)
    val highestIncome: StateFlow<Double> = _highestIncome.asStateFlow()

    private val _highestExpense = MutableStateFlow<Double>(0.0)
    val highestExpense: StateFlow<Double> = _highestExpense.asStateFlow()

    private val _mostFrequentItem = MutableStateFlow<String>("")
    val mostFrequentItem: StateFlow<String> = _mostFrequentItem.asStateFlow()

    private val _expenseByCategory = MutableStateFlow<Map<String, Double>>(emptyMap())
    val expenseByCategory: StateFlow<Map<String, Double>> = _expenseByCategory.asStateFlow()

    private val _incomeVsExpense = MutableStateFlow<Pair<Double, Double>>(0.0 to 0.0)
    val incomeVsExpense: StateFlow<Pair<Double, Double>> = _incomeVsExpense.asStateFlow()

    private val _weeklyBalance = MutableStateFlow<Double>(0.0)
    val weeklyBalance: StateFlow<Double> = _weeklyBalance.asStateFlow()

    private val _monthlyBalance = MutableStateFlow<Double>(0.0)
    val monthlyBalance: StateFlow<Double> = _monthlyBalance.asStateFlow()

    private val _weeklyBalanceRange = MutableStateFlow<String>("")
    val weeklyBalanceRange: StateFlow<String> = _weeklyBalanceRange.asStateFlow()

    private val _monthlyBalanceRange = MutableStateFlow<String>("")
    val monthlyBalanceRange: StateFlow<String> = _monthlyBalanceRange.asStateFlow()

    fun loadAnalytics(userId: Int, category: String = "PERSONAL") {
        calculatePeriodRanges()
        viewModelScope.launch {
            getAnalyticsUseCase.getHighestIncome(userId, category).collect { result ->
                if (result is Result.Success) _highestIncome.value = result.data
            }
        }
        viewModelScope.launch {
            getAnalyticsUseCase.getHighestExpense(userId, category).collect { result ->
                if (result is Result.Success) _highestExpense.value = result.data
            }
        }
        viewModelScope.launch {
            getAnalyticsUseCase.getMostFrequentExpenseItem(userId, category).collect { result ->
                if (result is Result.Success) _mostFrequentItem.value = result.data
            }
        }

        // Process data for charts
        viewModelScope.launch {
            // Fetch all transactions for the user (using a large limit for now to get all)
            // Ideally, we should have a repository method to get all transactions without limit
            getTransactionsUseCase(userId, category, 1000).collect { result ->
                if (result is Result.Success) {
                    val transactions = result.data
                    
                    // Calculate Expense by Category
                    val expenses = transactions.filter { it.type == "EXPENSE" }
                    val expenseMap = expenses.groupBy { it.category }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }
                    _expenseByCategory.value = expenseMap

                    // Calculate Total Income vs Expense
                    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val totalExpense = expenses.sumOf { it.amount }
                    _incomeVsExpense.value = totalIncome to totalExpense

                    // Calculate Weekly Balance
                    val currentWeekTransactions = transactions.filter { isCurrentWeek(it.timestamp) }
                    val weeklyIncome = currentWeekTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val weeklyExpense = currentWeekTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                    _weeklyBalance.value = weeklyIncome - weeklyExpense

                    // Calculate Monthly Balance
                    val currentMonthTransactions = transactions.filter { isCurrentMonth(it.timestamp) }
                    val monthlyIncome = currentMonthTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val monthlyExpense = currentMonthTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                    _monthlyBalance.value = monthlyIncome - monthlyExpense
                }
            }
        }
    }

    private fun isCurrentWeek(timestamp: Long): Boolean {
        val calendar = java.util.Calendar.getInstance()
        val currentWeek = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        
        val targetCalendar = java.util.Calendar.getInstance()
        targetCalendar.timeInMillis = timestamp
        
        return targetCalendar.get(java.util.Calendar.WEEK_OF_YEAR) == currentWeek &&
                targetCalendar.get(java.util.Calendar.YEAR) == currentYear
    }

    private fun isCurrentMonth(timestamp: Long): Boolean {
        val calendar = java.util.Calendar.getInstance()
        val currentMonth = calendar.get(java.util.Calendar.MONTH)
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        
        val targetCalendar = java.util.Calendar.getInstance()
        targetCalendar.timeInMillis = timestamp
        
        return targetCalendar.get(java.util.Calendar.MONTH) == currentMonth &&
                targetCalendar.get(java.util.Calendar.YEAR) == currentYear
    }

    private fun calculatePeriodRanges() {
        val calendar = java.util.Calendar.getInstance()
        
        // Calculate week range (e.g. "May 17 - May 23")
        val firstDay = calendar.firstDayOfWeek
        calendar.set(java.util.Calendar.DAY_OF_WEEK, firstDay)
        val startOfWeek = calendar.time
        
        calendar.add(java.util.Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = calendar.time
        
        val weekFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
        _weeklyBalanceRange.value = "${weekFormat.format(startOfWeek)} - ${weekFormat.format(endOfWeek)}"
        
        // Calculate month name (e.g. "May 2026")
        val monthFormat = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
        _monthlyBalanceRange.value = monthFormat.format(java.util.Calendar.getInstance().time)
    }
}
