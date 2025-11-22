package com.example.loginapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.loginapp.data.local.entity.MoneyTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoneyTransactionDao {
    @Query("SELECT * FROM money_transactions WHERE userId = :userId AND category = :category ORDER BY timestamp DESC LIMIT :limit")
    fun getTransactionsForUser(userId: Int, category: String, limit: Int): Flow<List<MoneyTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: MoneyTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: MoneyTransactionEntity)

    @Query("SELECT MAX(amount) FROM money_transactions WHERE userId = :userId AND category = :category AND type = 'INCOME'")
    fun getHighestIncome(userId: Int, category: String): Flow<Double?>

    @Query("SELECT MAX(amount) FROM money_transactions WHERE userId = :userId AND category = :category AND type = 'EXPENSE'")
    fun getHighestExpense(userId: Int, category: String): Flow<Double?>

    @Query("SELECT title FROM money_transactions WHERE userId = :userId AND category = :category AND type = 'EXPENSE' GROUP BY title ORDER BY COUNT(*) DESC LIMIT 1")
    fun getMostFrequentExpenseItem(userId: Int, category: String): Flow<String?>

    @Query("SELECT SUM(amount) FROM money_transactions WHERE userId = :userId AND category = :category AND type = 'INCOME'")
    fun getTotalIncome(userId: Int, category: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM money_transactions WHERE userId = :userId AND category = :category AND type = 'EXPENSE'")
    fun getTotalExpense(userId: Int, category: String): Flow<Double?>

    @Query("SELECT DISTINCT date(timestamp/1000, 'unixepoch') as date FROM money_transactions WHERE userId = :userId AND category = :category ORDER BY date DESC")
    fun getDistinctDates(userId: Int, category: String): Flow<List<String>>

    @Query("SELECT SUM(amount) FROM money_transactions WHERE userId = :userId AND category = :category AND type = 'INCOME' AND date(timestamp/1000, 'unixepoch') = :date")
    fun getIncomeForDate(userId: Int, category: String, date: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM money_transactions WHERE userId = :userId AND category = :category AND type = 'EXPENSE' AND date(timestamp/1000, 'unixepoch') = :date")
    fun getExpenseForDate(userId: Int, category: String, date: String): Flow<Double?>
}

data class ItemCount(val title: String, val count: Int)
