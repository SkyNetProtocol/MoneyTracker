package com.example.loginapp.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginapp.common.Result
import com.example.loginapp.domain.usecase.GetTransactionsByDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.loginapp.domain.model.MoneyTransaction
import javax.inject.Inject

import com.example.loginapp.domain.usecase.UpdateTransactionUseCase

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getTransactionsByDateUseCase: GetTransactionsByDateUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _transactions = MutableStateFlow<List<MoneyTransaction>>(emptyList())
    val transactions: StateFlow<List<MoneyTransaction>> = _transactions.asStateFlow()

    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome.asStateFlow()

    private val _totalExpense = MutableStateFlow(0.0)
    val totalExpense: StateFlow<Double> = _totalExpense.asStateFlow()

    private val _netBalance = MutableStateFlow(0.0)
    val netBalance: StateFlow<Double> = _netBalance.asStateFlow()

    private var currentUserId: Int = -1
    private var currentCategory: String = "PERSONAL"

    fun initialize(userId: Int, category: String) {
        currentUserId = userId
        currentCategory = category
        loadSummaryForSelectedDate()
    }

    fun selectDate(dateInMillis: Long) {
        _selectedDate.value = dateInMillis
        loadSummaryForSelectedDate()
    }

    private fun loadSummaryForSelectedDate() {
        if (currentUserId == -1) return
        
        viewModelScope.launch {
            getTransactionsByDateUseCase(currentUserId, currentCategory, _selectedDate.value).collect { result ->
                if (result is Result.Success) {
                    val transactionsData = result.data
                    
                    _transactions.value = transactionsData
                    
                    // Calculate totals
                    val income = transactionsData.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val expense = transactionsData.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                    
                    _totalIncome.value = income
                    _totalExpense.value = expense
                    _netBalance.value = income - expense
                }
            }
        }
    }

    fun togglePendingLiquidation(transaction: MoneyTransaction, isChecked: Boolean) {
        viewModelScope.launch {
            val updated = transaction.copy(
                isPendingLiquidation = isChecked,
                isLiquidated = false
            )
            updateTransactionUseCase(updated)
        }
    }
}

