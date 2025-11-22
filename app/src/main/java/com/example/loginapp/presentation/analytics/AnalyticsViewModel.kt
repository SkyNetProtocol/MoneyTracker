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
    private val getAnalyticsUseCase: GetAnalyticsUseCase
) : ViewModel() {

    private val _highestIncome = MutableStateFlow<Double>(0.0)
    val highestIncome: StateFlow<Double> = _highestIncome.asStateFlow()

    private val _highestExpense = MutableStateFlow<Double>(0.0)
    val highestExpense: StateFlow<Double> = _highestExpense.asStateFlow()

    private val _mostFrequentItem = MutableStateFlow<String>("")
    val mostFrequentItem: StateFlow<String> = _mostFrequentItem.asStateFlow()

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
    }
}
