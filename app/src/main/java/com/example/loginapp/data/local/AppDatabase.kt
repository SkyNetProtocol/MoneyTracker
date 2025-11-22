package com.example.loginapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.loginapp.data.local.dao.UserDao
import com.example.loginapp.data.local.entity.UserEntity
import com.example.loginapp.data.local.dao.MoneyTransactionDao
import com.example.loginapp.data.local.entity.MoneyTransactionEntity
import com.example.loginapp.data.local.dao.CategoryDao
import com.example.loginapp.data.local.entity.CategoryEntity

@Database(entities = [UserEntity::class, MoneyTransactionEntity::class, CategoryEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun moneyTransactionDao(): MoneyTransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
