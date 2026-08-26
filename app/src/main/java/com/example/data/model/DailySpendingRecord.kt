package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_spending_records",
    indices = [Index(value = ["yearMonth"])]
)
data class DailySpendingRecord(
    @PrimaryKey val date: String, // Format: YYYY-MM-DD
    val yearMonth: String,        // Format: YYYY-MM
    val dailyAllowance: Double,   // Calculated allowed limit for this day
    val actualSpent: Double,      // Amount actually spent
    val savedDifference: Double,  // dailyAllowance - actualSpent
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
