package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DailySpendingRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySpendingDao {
    @Query("SELECT * FROM daily_spending_records ORDER BY date DESC")
    fun getAllRecordsFlow(): Flow<List<DailySpendingRecord>>

    @Query("SELECT * FROM daily_spending_records WHERE yearMonth = :yearMonth ORDER BY date ASC")
    fun getRecordsForMonthFlow(yearMonth: String): Flow<List<DailySpendingRecord>>

    @Query("SELECT * FROM daily_spending_records WHERE yearMonth = :yearMonth ORDER BY date ASC")
    suspend fun getRecordsForMonth(yearMonth: String): List<DailySpendingRecord>

    @Query("SELECT * FROM daily_spending_records WHERE date = :date LIMIT 1")
    fun getRecordByDateFlow(date: String): Flow<DailySpendingRecord?>

    @Query("SELECT * FROM daily_spending_records WHERE date = :date LIMIT 1")
    suspend fun getRecordByDate(date: String): DailySpendingRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: DailySpendingRecord)

    @Query("DELETE FROM daily_spending_records WHERE date = :date")
    suspend fun deleteRecordByDate(date: String)

    @Query("DELETE FROM daily_spending_records")
    suspend fun clearAllRecords()
}
