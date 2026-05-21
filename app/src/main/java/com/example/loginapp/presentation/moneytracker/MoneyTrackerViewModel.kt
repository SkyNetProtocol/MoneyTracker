package com.example.loginapp.presentation.moneytracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.MoneyTransaction
import com.example.loginapp.domain.usecase.AddTransactionUseCase
import com.example.loginapp.domain.usecase.DeleteTransactionUseCase
import com.example.loginapp.domain.usecase.GetTransactionsUseCase
import com.example.loginapp.domain.usecase.GetTransactionsByDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoneyTrackerViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getTransactionsByDateUseCase: GetTransactionsByDateUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: com.example.loginapp.domain.usecase.UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    @com.example.loginapp.di.DefaultDispatcher private val defaultDispatcher: kotlinx.coroutines.CoroutineDispatcher
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<TransactionListItem>>(emptyList())
    val transactions: StateFlow<List<TransactionListItem>> = _transactions.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    private var currentUserId: Int = -1
    private var currentCategory: String = "PERSONAL"
    private var currentLimit = 20
    private var selectedDate: Long? = null // null means show all transactions

    fun loadTransactions(userId: Int, category: String = "PERSONAL") {
        currentUserId = userId
        currentCategory = category
        viewModelScope.launch {
            if (selectedDate != null) {
                // Load transactions for specific date
                getTransactionsByDateUseCase(userId, category, selectedDate!!).collect { result ->
                    if (result is Result.Success) {
                        _transactions.value = groupTransactionsByDate(result.data)
                    }
                }
            } else {
                // Load all transactions
                getTransactionsUseCase(userId, category, currentLimit).collect { result ->
                    if (result is Result.Success) {
                        _transactions.value = groupTransactionsByDate(result.data)
                    }
                }
            }
        }
    }

    fun setDateFilter(date: Long?) {
        selectedDate = date
        if (currentUserId != -1) {
            loadTransactions(currentUserId, currentCategory)
        }
    }

    fun getSelectedDate(): Long? = selectedDate

    fun loadMore() {
        if (currentUserId == -1) return
        currentLimit += 20
        loadTransactions(currentUserId, currentCategory)
    }

    private suspend fun groupTransactionsByDate(transactions: List<MoneyTransaction>): List<TransactionListItem> = kotlinx.coroutines.withContext(defaultDispatcher) {
        val groupedList = mutableListOf<TransactionListItem>()
        var lastDate = ""

        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())

        transactions.forEach { transaction ->
            val date = dateFormat.format(java.util.Date(transaction.timestamp))
            if (date != lastDate) {
                groupedList.add(TransactionListItem.DateHeader(date))
                lastDate = date
            }
            groupedList.add(TransactionListItem.TransactionItem(transaction))
        }
        groupedList
    }

    fun addTransaction(title: String, amount: Double, type: String, categoryId: Int? = null, isPendingLiquidation: Boolean = false) {
        if (currentUserId == -1) {
            _operationState.value = OperationState.Error("User not logged in")
            return
        }
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val transaction = MoneyTransaction(
                userId = currentUserId,
                title = title,
                amount = amount,
                type = type,
                category = currentCategory,
                categoryId = categoryId,
                timestamp = selectedDate ?: System.currentTimeMillis(), // Use selected date if available
                isPendingLiquidation = isPendingLiquidation
            )
            val result = addTransactionUseCase(transaction)
            _operationState.value = when (result) {
                is Result.Success -> OperationState.Success("Transaction added successfully")
                is Result.Error -> OperationState.Error(result.exception.message ?: "Failed to add transaction")
                is Result.Loading -> OperationState.Loading
            }
        }
    }

    fun deleteTransaction(transaction: MoneyTransaction) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = deleteTransactionUseCase(transaction)
            _operationState.value = when (result) {
                is Result.Success -> OperationState.Success("Transaction deleted successfully")
                is Result.Error -> OperationState.Error(result.exception.message ?: "Failed to delete transaction")
                is Result.Loading -> OperationState.Loading
            }
        }
    }

    fun updateTransaction(transaction: MoneyTransaction) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val result = updateTransactionUseCase(transaction)
            _operationState.value = when (result) {
                is Result.Success -> OperationState.Success("Transaction updated successfully")
                is Result.Error -> OperationState.Error(result.exception.message ?: "Failed to update transaction")
                is Result.Loading -> OperationState.Loading
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

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}


