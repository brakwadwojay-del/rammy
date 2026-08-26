package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BudgetProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_profile WHERE id = 1 LIMIT 1")
    fun getBudgetProfileFlow(): Flow<BudgetProfile?>

    @Query("SELECT * FROM budget_profile WHERE id = 1 LIMIT 1")
    suspend fun getBudgetProfile(): BudgetProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: BudgetProfile)

    @Query("DELETE FROM budget_profile")
    suspend fun clearBudgetProfile()
}
