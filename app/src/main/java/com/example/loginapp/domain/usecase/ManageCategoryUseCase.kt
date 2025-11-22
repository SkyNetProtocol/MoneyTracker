package com.example.loginapp.domain.usecase

import com.example.loginapp.common.Result
import com.example.loginapp.data.local.entity.CategoryEntity
import com.example.loginapp.domain.repository.CategoryRepository
import javax.inject.Inject

class ManageCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend fun addCategory(category: CategoryEntity): Result<Long> {
        return categoryRepository.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity): Result<Unit> {
        return categoryRepository.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity): Result<Unit> {
        return categoryRepository.deleteCategory(category)
    }

    suspend fun seedDefaultCategories(): Result<Unit> {
        return categoryRepository.seedDefaultCategories()
    }
}
