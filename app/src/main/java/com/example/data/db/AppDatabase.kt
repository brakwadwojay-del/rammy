package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao
    abstract fun dailySpendingDao(): DailySpendingDao
    abstract fun expenseItemDao(): ExpenseItemDao
    abstract fun expensePresetDao(): ExpensePresetDao
    abstract fun appPreferencesDao(): AppPreferencesDao

    companion object {
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 8 does not change the database schema. This explicit migration
                // preserves existing local financial data without destructive fallback.
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spend_tracker_database"
                )
                    .addMigrations(MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
