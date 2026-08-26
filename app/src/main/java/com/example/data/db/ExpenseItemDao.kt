package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExpenseItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseItemDao {

    @Query("SELECT * FROM expense_items WHERE date = :date AND isDelayed = 0 ORDER BY timestamp ASC")
    fun getActiveExpensesForDateFlow(date: String): Flow<List<ExpenseItem>>

    @Query("SELECT * FROM expense_items WHERE date = :date AND isDelayed = 0 ORDER BY timestamp ASC")
    suspend fun getActiveExpensesForDate(date: String): List<ExpenseItem>

    @Query("SELECT * FROM expense_items WHERE isDelayed = 0 ORDER BY date DESC, timestamp ASC")
    fun getAllActiveExpensesFlow(): Flow<List<ExpenseItem>>

    @Query("SELECT * FROM expense_items WHERE date LIKE :yearMonthPrefix || '%' AND isDelayed = 0 ORDER BY date ASC, timestamp ASC")
    fun getActiveExpensesForMonthFlow(yearMonthPrefix: String): Flow<List<ExpenseItem>>

    @Query("SELECT * FROM expense_items WHERE isDelayed = 0 ORDER BY date DESC, timestamp ASC")
    suspend fun getAllActiveExpenses(): List<ExpenseItem>

    @Query("SELECT * FROM expense_items WHERE isDelayed = 1 ORDER BY timestamp DESC")
    fun getDelayedExpensesFlow(): Flow<List<ExpenseItem>>

    @Query("SELECT * FROM expense_items WHERE isDelayed = 1 ORDER BY timestamp DESC")
    suspend fun getDelayedExpenses(): List<ExpenseItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseItem): Long

    @Update
    suspend fun updateExpense(expense: ExpenseItem)

    @Query("DELETE FROM expense_items WHERE id = :id")
    suspend fun deleteExpense(id: Long)

    @Query("UPDATE expense_items SET isDelayed = 1 WHERE id = :id")
    suspend fun markExpenseDelayed(id: Long)

    @Query("UPDATE expense_items SET isDelayed = 0, date = :newDate WHERE id = :id")
    suspend fun restoreExpense(id: Long, newDate: String)

    @Query("DELETE FROM expense_items")
    suspend fun clearAllExpenses()
}
