package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_preferences")
data class AppPreferences(
    @PrimaryKey val id: Int = 1,
    val onboardingCompleted: Boolean = false,
    val savedUserName: String = "",
    val dailyReminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val streakCount: Int = 0,
    val lastStreakDate: String = "" // YYYY-MM-DD
)
