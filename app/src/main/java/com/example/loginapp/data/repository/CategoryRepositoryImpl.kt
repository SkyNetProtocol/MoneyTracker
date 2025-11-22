package com.example.loginapp.data.repository

import com.example.loginapp.common.Result
import com.example.loginapp.data.local.dao.CategoryDao
import com.example.loginapp.data.local.entity.CategoryEntity
import com.example.loginapp.di.IoDispatcher
import com.example.loginapp.domain.model.Category
import com.example.loginapp.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CategoryRepository {

    override fun getAllCategories(): Flow<Result<List<Category>>> =
        categoryDao.getAllCategories()
            .map { entities ->
                Result.Success(entities.map { it.toDomainModel() }) as Result<List<Category>>
            }
            .catch { e -> emit(Result.Error(e)) }
            .flowOn(ioDispatcher)

    override fun getCategoriesByType(type: String): Flow<Result<List<Category>>> =
        categoryDao.getCategoriesByType(type)
            .map { entities ->
                Result.Success(entities.map { it.toDomainModel() }) as Result<List<Category>>
            }
            .catch { e -> emit(Result.Error(e)) }
            .flowOn(ioDispatcher)

    override suspend fun getCategoryById(id: Int): Result<Category?> = withContext(ioDispatcher) {
        try {
            val category = categoryDao.getCategoryById(id)
            Result.Success(category?.toDomainModel())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun insertCategory(category: CategoryEntity): Result<Long> = withContext(ioDispatcher) {
        try {
            val id = categoryDao.insertCategory(category)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateCategory(category: CategoryEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            categoryDao.updateCategory(category)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteCategory(category: CategoryEntity): Result<Unit> = withContext(ioDispatcher) {
        try {
            categoryDao.deleteCategory(category)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun seedDefaultCategories(): Result<Unit> = withContext(ioDispatcher) {
        try {
            val defaultCategories = listOf(
                // Expense Categories
                CategoryEntity(name = "Food", icon = "🍔", color = "#FF5722", type = "EXPENSE", isDefault = true),
                CategoryEntity(name = "Transport", icon = "🚗", color = "#2196F3", type = "EXPENSE", isDefault = true),
                CategoryEntity(name = "Bills", icon = "💡", color = "#FFC107", type = "EXPENSE", isDefault = true),
                CategoryEntity(name = "Shopping", icon = "🛍️", color = "#E91E63", type = "EXPENSE", isDefault = true),
                CategoryEntity(name = "Entertainment", icon = "🎬", color = "#9C27B0", type = "EXPENSE", isDefault = true),
                CategoryEntity(name = "Health", icon = "🏥", color = "#4CAF50", type = "EXPENSE", isDefault = true),
                CategoryEntity(name = "Education", icon = "📚", color = "#00BCD4", type = "EXPENSE", isDefault = true),
                CategoryEntity(name = "Other", icon = "📦", color = "#607D8B", type = "EXPENSE", isDefault = true),
                
                // Income Categories
                CategoryEntity(name = "Salary", icon = "💰", color = "#4CAF50", type = "INCOME", isDefault = true),
                CategoryEntity(name = "Business", icon = "💼", color = "#2196F3", type = "INCOME", isDefault = true),
                CategoryEntity(name = "Investment", icon = "📈", color = "#FF9800", type = "INCOME", isDefault = true),
                CategoryEntity(name = "Gift", icon = "🎁", color = "#E91E63", type = "INCOME", isDefault = true),
                CategoryEntity(name = "Other", icon = "💵", color = "#607D8B", type = "INCOME", isDefault = true)
            )
            
            categoryDao.insertCategories(defaultCategories)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun CategoryEntity.toDomainModel() = Category(
        id = id,
        name = name,
        icon = icon,
        color = color,
        type = type,
        isDefault = isDefault
    )
}
