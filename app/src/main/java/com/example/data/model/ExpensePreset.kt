package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_preset")
data class ExpensePreset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String, // "Food", "Transport", "Other"
    val iconEmoji: String = "💰",
    val orderIndex: Int = 0
)
