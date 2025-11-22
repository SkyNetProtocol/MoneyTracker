package com.example.loginapp.presentation.moneytracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginapp.common.Result
import com.example.loginapp.data.local.entity.MoneyTransactionEntity
import com.example.loginapp.domain.model.MoneyTransaction
import com.example.loginapp.domain.usecase.AddTransactionUseCase
import com.example.loginapp.domain.usecase.DeleteTransactionUseCase
import com.example.loginapp.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoneyTrackerViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<TransactionListItem>>(emptyList())
    val transactions: StateFlow<List<TransactionListItem>> = _transactions.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    private var currentUserId: Int = -1
    private var currentCategory: String = "PERSONAL"
    private var currentLimit = 20

    fun loadTransactions(userId: Int, category: String = "PERSONAL") {
        currentUserId = userId
        currentCategory = category
        viewModelScope.launch {
            getTransactionsUseCase(userId, category, currentLimit).collect { result ->
                if (result is Result.Success) {
                    _transactions.value = groupTransactionsByDate(result.data)
                }
            }
        }
    }

    fun loadMore() {
        if (currentUserId == -1) return
        currentLimit += 20
        loadTransactions(currentUserId, currentCategory)
    }

    private fun groupTransactionsByDate(transactions: List<MoneyTransaction>): List<TransactionListItem> {
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
        return groupedList
    }

    fun addTransaction(title: String, amount: Double, type: String, categoryId: Int? = null) {
        if (currentUserId == -1) {
            _operationState.value = OperationState.Error("User not logged in")
            return
        }
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val transaction = MoneyTransactionEntity(
                userId = currentUserId,
                title = title,
                amount = amount,
                type = type,
                category = currentCategory,
                categoryId = categoryId
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
            val entity = MoneyTransactionEntity(
                id = transaction.id,
                userId = transaction.userId,
                title = transaction.title,
                amount = transaction.amount,
                type = transaction.type,
                category = transaction.category,
                timestamp = transaction.timestamp
            )
            val result = deleteTransactionUseCase(entity)
            _operationState.value = when (result) {
                is Result.Success -> OperationState.Success("Transaction deleted successfully")
                is Result.Error -> OperationState.Error(result.exception.message ?: "Failed to delete transaction")
                is Result.Loading -> OperationState.Loading
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}

sealed class OperationState {
    object Idle : OperationState()
    object Loading : OperationState()
    data class Success(val message: String) : OperationState()
    data class Error(val message: String) : OperationState()
}
