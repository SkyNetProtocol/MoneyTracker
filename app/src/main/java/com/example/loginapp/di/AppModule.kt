package com.example.loginapp.di

import android.content.Context
import androidx.room.Room
import com.example.loginapp.data.local.AppDatabase
import com.example.loginapp.data.local.dao.CategoryDao
import com.example.loginapp.data.local.dao.MoneyTransactionDao
import com.example.loginapp.data.local.dao.UserDao
import com.example.loginapp.data.repository.CategoryRepositoryImpl
import com.example.loginapp.data.repository.MoneyRepositoryImpl
import com.example.loginapp.data.repository.UserRepositoryImpl
import com.example.loginapp.domain.repository.CategoryRepository
import com.example.loginapp.domain.repository.MoneyRepository
import com.example.loginapp.domain.repository.UserRepository
import com.example.loginapp.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideMoneyTransactionDao(database: AppDatabase): MoneyTransactionDao {
        return database.moneyTransactionDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): UserRepository {
        return UserRepositoryImpl(userDao, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideMoneyRepository(
        userDao: UserDao,
        moneyTransactionDao: MoneyTransactionDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): MoneyRepository {
        return MoneyRepositoryImpl(userDao, moneyTransactionDao, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(
        categoryDao: CategoryDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): CategoryRepository {
        return CategoryRepositoryImpl(categoryDao, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(userRepository: UserRepository): LoginUseCase {
        return LoginUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideRegisterUseCase(userRepository: UserRepository): RegisterUseCase {
        return RegisterUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideGetTransactionsUseCase(moneyRepository: MoneyRepository): GetTransactionsUseCase {
        return GetTransactionsUseCase(moneyRepository)
    }

    @Provides
    @Singleton
    fun provideAddTransactionUseCase(moneyRepository: MoneyRepository): AddTransactionUseCase {
        return AddTransactionUseCase(moneyRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteTransactionUseCase(moneyRepository: MoneyRepository): DeleteTransactionUseCase {
        return DeleteTransactionUseCase(moneyRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateTransactionUseCase(moneyRepository: MoneyRepository): UpdateTransactionUseCase {
        return UpdateTransactionUseCase(moneyRepository)
    }

    @Provides
    @Singleton
    fun provideGetAnalyticsUseCase(moneyRepository: MoneyRepository): GetAnalyticsUseCase {
        return GetAnalyticsUseCase(moneyRepository)
    }

    @Provides
    @Singleton
    fun provideGetCategoriesUseCase(categoryRepository: CategoryRepository): GetCategoriesUseCase {
        return GetCategoriesUseCase(categoryRepository)
    }

    @Provides
    @Singleton
    fun provideManageCategoryUseCase(categoryRepository: CategoryRepository): ManageCategoryUseCase {
        return ManageCategoryUseCase(categoryRepository)
    }
}
