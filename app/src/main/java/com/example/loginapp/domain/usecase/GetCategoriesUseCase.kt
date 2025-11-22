package com.example.loginapp.domain.usecase

import com.example.loginapp.common.Result
import com.example.loginapp.domain.model.Category
import com.example.loginapp.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<Result<List<Category>>> {
        return categoryRepository.getAllCategories()
    }

    fun byType(type: String): Flow<Result<List<Category>>> {
        return categoryRepository.getCategoriesByType(type)
    }
}
