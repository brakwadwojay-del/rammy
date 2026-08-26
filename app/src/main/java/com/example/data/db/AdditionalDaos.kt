package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppPreferences
import com.example.data.model.ExpensePreset
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpensePresetDao {
    @Query("SELECT * FROM expense_preset ORDER BY orderIndex ASC, id ASC")
    fun getAllPresetsFlow(): Flow<List<ExpensePreset>>

    @Query("SELECT * FROM expense_preset ORDER BY orderIndex ASC, id ASC")
    suspend fun getAllPresets(): List<ExpensePreset>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: ExpensePreset): Long

    @Update
    suspend fun updatePreset(preset: ExpensePreset)

    @Query("DELETE FROM expense_preset WHERE id = :id")
    suspend fun deletePreset(id: Long)

    @Query("DELETE FROM expense_preset")
    suspend fun clearPresets()
}

@Dao
interface AppPreferencesDao {
    @Query("SELECT * FROM app_preferences WHERE id = 1 LIMIT 1")
    fun getPreferencesFlow(): Flow<AppPreferences?>

    @Query("SELECT * FROM app_preferences WHERE id = 1 LIMIT 1")
    suspend fun getPreferences(): AppPreferences?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreferences(preferences: AppPreferences)

    @Query("DELETE FROM app_preferences")
    suspend fun clearPreferences()
}
