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

    fun loadAnalytics(userId: Int, category: String = "PERSONAL") {
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
                }
            }
        }
    }
}
