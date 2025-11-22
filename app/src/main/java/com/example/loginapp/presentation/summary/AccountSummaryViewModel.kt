package com.example.loginapp.presentation.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.IncomeExpenseSummary
import com.example.loginapp.domain.usecase.GetAccountSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSummaryViewModel @Inject constructor(
    private val getAccountSummaryUseCase: GetAccountSummaryUseCase
) : ViewModel() {

    private val _dates = MutableStateFlow<List<String>>(emptyList())
    val dates: StateFlow<List<String>> = _dates.asStateFlow()

    private val _selectedDateSummary = MutableStateFlow<IncomeExpenseSummary?>(null)
    val selectedDateSummary: StateFlow<IncomeExpenseSummary?> = _selectedDateSummary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadDates(userId: Int, category: String = "PERSONAL") {
        viewModelScope.launch {
            _isLoading.value = true
            getAccountSummaryUseCase.getDistinctDates(userId, category).collect { result ->
                _isLoading.value = false
                if (result is Result.Success) {
                    _dates.value = result.data
                }
            }
        }
    }

    fun loadDateSummary(userId: Int, category: String = "PERSONAL", date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            getAccountSummaryUseCase.getDateSummary(userId, category, date).collect { result ->
                _isLoading.value = false
                if (result is Result.Success) {
                    _selectedDateSummary.value = result.data
                }
            }
        }
    }
}
