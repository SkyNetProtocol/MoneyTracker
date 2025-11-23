package com.example.loginapp.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loginapp.common.Result
import com.example.loginapp.data.local.entity.CategoryEntity
import com.example.loginapp.domain.model.Category
import com.example.loginapp.domain.usecase.GetCategoriesUseCase
import com.example.loginapp.domain.usecase.ManageCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val manageCategoryUseCase: ManageCategoryUseCase
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _expenseCategories = MutableStateFlow<List<Category>>(emptyList())
    val expenseCategories: StateFlow<List<Category>> = _expenseCategories.asStateFlow()

    private val _incomeCategories = MutableStateFlow<List<Category>>(emptyList())
    val incomeCategories: StateFlow<List<Category>> = _incomeCategories.asStateFlow()

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    init {
        loadCategories()
        seedDefaultCategoriesIfNeeded()
    }

    fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { result ->
                if (result is Result.Success) {
                    _categories.value = result.data
                }
            }
        }
    }

    fun loadCategoriesByType(type: String) {
        viewModelScope.launch {
            getCategoriesUseCase.byType(type).collect { result ->
                if (result is Result.Success) {
                    when (type) {
                        "EXPENSE" -> _expenseCategories.value = result.data
                        "INCOME" -> _incomeCategories.value = result.data
                    }
                }
            }
        }
    }

    fun addCategory(name: String, icon: String, color: String, type: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val category = CategoryEntity(
                name = name,
                icon = icon,
                color = color,
                type = type,
                isDefault = false
            )
            val result = manageCategoryUseCase.addCategory(category)
            _operationState.value = when (result) {
                is Result.Success -> OperationState.Success("Category added successfully")
                is Result.Error -> OperationState.Error(result.exception.message ?: "Failed to add category")
                else -> OperationState.Idle
            }
        }
    }

    fun deleteCategory(category: Category) {
        if (category.isDefault) {
            _operationState.value = OperationState.Error("Cannot delete default categories")
            return
        }
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            val entity = CategoryEntity(
                id = category.id,
                name = category.name,
                icon = category.icon,
                color = category.color,
                type = category.type,
                isDefault = category.isDefault
            )
            val result = manageCategoryUseCase.deleteCategory(entity)
            _operationState.value = when (result) {
                is Result.Success -> OperationState.Success("Category deleted successfully")
                is Result.Error -> OperationState.Error(result.exception.message ?: "Failed to delete category")
                else -> OperationState.Idle
            }
        }
    }

    private fun seedDefaultCategoriesIfNeeded() {
        viewModelScope.launch {
            // Only seed if no categories exist
            if (_categories.value.isEmpty()) {
                manageCategoryUseCase.seedDefaultCategories()
                loadCategories()
            }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }
}


