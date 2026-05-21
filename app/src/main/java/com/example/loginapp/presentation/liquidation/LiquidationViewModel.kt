package com.example.loginapp.presentation.liquidation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.MoneyTransaction
import com.example.loginapp.domain.usecase.GetPendingTransactionsUseCase
import com.example.loginapp.domain.usecase.GetPendingTransactionsTotalUseCase
import com.example.loginapp.domain.usecase.UpdateTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.loginapp.domain.usecase.AddTransactionUseCase
import com.example.loginapp.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first

@HiltViewModel
class LiquidationViewModel @Inject constructor(
    private val getPendingTransactionsUseCase: GetPendingTransactionsUseCase,
    private val getPendingTransactionsTotalUseCase: GetPendingTransactionsTotalUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _pendingTransactions = MutableStateFlow<List<MoneyTransaction>>(emptyList())
    val pendingTransactions: StateFlow<List<MoneyTransaction>> = _pendingTransactions.asStateFlow()

    private val _pendingTotal = MutableStateFlow<Double>(0.0)
    val pendingTotal: StateFlow<Double> = _pendingTotal.asStateFlow()

    private var currentUserId: Int = -1
    private var currentCategory: String = "PERSONAL"

    fun loadPendingTransactions(userId: Int, category: String = "PERSONAL") {
        currentUserId = userId
        currentCategory = category
        if (userId == -1) return

        viewModelScope.launch {
            getPendingTransactionsUseCase(userId, category).collectLatest { result ->
                if (result is Result.Success) {
                    _pendingTransactions.value = result.data
                }
            }
        }

        viewModelScope.launch {
            getPendingTransactionsTotalUseCase(userId, category).collectLatest { result ->
                if (result is Result.Success) {
                    _pendingTotal.value = result.data
                }
            }
        }
    }

    fun liquidateTransaction(transaction: MoneyTransaction) {
        viewModelScope.launch {
            // Mark the expense as liquidated
            val updated = transaction.copy(isPendingLiquidation = false, isLiquidated = true)
            updateTransactionUseCase(updated)

            // Find the "Business" category ID under INCOME type
            val incomeCategoriesResult = categoryRepository.getCategoriesByType("INCOME").first()
            val businessCategoryId = if (incomeCategoriesResult is Result.Success) {
                incomeCategoriesResult.data.find { it.name.equals("Business", ignoreCase = true) }?.id
            } else null

            // Create corresponding income transaction representing liquidation reimbursement
            val liquidationIncome = MoneyTransaction(
                userId = transaction.userId,
                title = "Liquidation: ${transaction.title}",
                amount = transaction.amount,
                type = "INCOME",
                category = transaction.category, // PERSONAL or COMPANY
                categoryId = businessCategoryId,
                timestamp = System.currentTimeMillis(),
                isPendingLiquidation = false,
                isLiquidated = false
            )
            addTransactionUseCase(liquidationIncome)
        }
    }

    fun liquidateAll() {
        val currentList = _pendingTransactions.value
        if (currentList.isEmpty()) return
        viewModelScope.launch {
            // Fetch "Business" category ID once for performance
            val incomeCategoriesResult = categoryRepository.getCategoriesByType("INCOME").first()
            val businessCategoryId = if (incomeCategoriesResult is Result.Success) {
                incomeCategoriesResult.data.find { it.name.equals("Business", ignoreCase = true) }?.id
            } else null

            currentList.forEach { transaction ->
                // Mark the expense as liquidated
                val updated = transaction.copy(isPendingLiquidation = false, isLiquidated = true)
                updateTransactionUseCase(updated)

                // Create corresponding income transaction representing liquidation reimbursement
                val liquidationIncome = MoneyTransaction(
                    userId = transaction.userId,
                    title = "Liquidation: ${transaction.title}",
                    amount = transaction.amount,
                    type = "INCOME",
                    category = transaction.category, // PERSONAL or COMPANY
                    categoryId = businessCategoryId,
                    timestamp = System.currentTimeMillis(),
                    isPendingLiquidation = false,
                    isLiquidated = false
                )
                addTransactionUseCase(liquidationIncome)
            }
        }
    }
}
