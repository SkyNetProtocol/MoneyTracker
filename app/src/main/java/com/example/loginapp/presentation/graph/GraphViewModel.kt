package com.example.loginapp.presentation.graph

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.IncomeExpenseSummary
import com.example.loginapp.domain.usecase.GetIncomeExpenseSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GraphViewModel @Inject constructor(
    private val getIncomeExpenseSummaryUseCase: GetIncomeExpenseSummaryUseCase
) : ViewModel() {

    private val _summary = MutableStateFlow<IncomeExpenseSummary?>(null)
    val summary: StateFlow<IncomeExpenseSummary?> = _summary.asStateFlow()

    fun loadSummary(userId: Int, category: String = "PERSONAL") {
        viewModelScope.launch {
            getIncomeExpenseSummaryUseCase(userId, category).collect { result ->
                if (result is Result.Success) {
                    _summary.value = result.data
                }
            }
        }
    }
}
