package com.example.loginapp.domain.repository

import com.example.loginapp.common.Result
import com.example.loginapp.data.local.entity.CategoryEntity
import com.example.loginapp.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<Result<List<Category>>>
    fun getCategoriesByType(type: String): Flow<Result<List<Category>>>
    suspend fun getCategoryById(id: Int): Result<Category?>
    suspend fun insertCategory(category: CategoryEntity): Result<Long>
    suspend fun updateCategory(category: CategoryEntity): Result<Unit>
    suspend fun deleteCategory(category: CategoryEntity): Result<Unit>
    suspend fun seedDefaultCategories(): Result<Unit>
}
