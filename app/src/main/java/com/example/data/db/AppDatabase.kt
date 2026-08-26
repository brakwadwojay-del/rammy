package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AppPreferences
import com.example.data.model.BudgetProfile
import com.example.data.model.DailySpendingRecord
import com.example.data.model.ExpenseItem
import com.example.data.model.ExpensePreset

@Database(
    entities = [
        BudgetProfile::class,
        DailySpendingRecord::class,
        ExpenseItem::class,
        ExpensePreset::class,
        AppPreferences::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao
    abstract fun dailySpendingDao(): DailySpendingDao
    abstract fun expenseItemDao(): ExpenseItemDao
    abstract fun expensePresetDao(): ExpensePresetDao
    abstract fun appPreferencesDao(): AppPreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spend_tracker_database"
                )
                    // Never silently delete a user's financial history when the schema changes.
                    // Future schema changes must provide an explicit Room Migration.
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
