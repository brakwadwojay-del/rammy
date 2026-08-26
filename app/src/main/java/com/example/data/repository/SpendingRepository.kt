package com.example.data.repository

import com.example.data.SpendingCalculator
import com.example.data.db.AppPreferencesDao
import com.example.data.db.BudgetDao
import com.example.data.db.DailySpendingDao
import com.example.data.db.ExpenseItemDao
import com.example.data.db.ExpensePresetDao
import com.example.data.model.AppPreferences
import com.example.data.model.BudgetProfile
import com.example.data.model.DailySpendingRecord
import com.example.data.model.ExpenseItem
import com.example.data.model.ExpensePreset
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.max

class SpendingRepository(
    private val budgetDao: BudgetDao,
    private val dailySpendingDao: DailySpendingDao,
    private val expenseItemDao: ExpenseItemDao,
    private val expensePresetDao: ExpensePresetDao,
    private val appPreferencesDao: AppPreferencesDao
) {
    val budgetProfileFlow: Flow<BudgetProfile?> = budgetDao.getBudgetProfileFlow()
    val allRecordsFlow: Flow<List<DailySpendingRecord>> = dailySpendingDao.getAllRecordsFlow()
    val presetsFlow: Flow<List<ExpensePreset>> = expensePresetDao.getAllPresetsFlow()
    val preferencesFlow: Flow<AppPreferences?> = appPreferencesDao.getPreferencesFlow()
    val delayedExpensesFlow: Flow<List<ExpenseItem>> = expenseItemDao.getDelayedExpensesFlow()
    val allActiveExpensesFlow: Flow<List<ExpenseItem>> = expenseItemDao.getAllActiveExpensesFlow()

    fun getActiveExpensesForDateFlow(dateStr: String): Flow<List<ExpenseItem>> {
        return expenseItemDao.getActiveExpensesForDateFlow(dateStr)
    }

    fun getRecordsForMonthFlow(yearMonth: String): Flow<List<DailySpendingRecord>> {
        return dailySpendingDao.getRecordsForMonthFlow(yearMonth)
    }

    suspend fun getBudgetProfile(): BudgetProfile? {
        return budgetDao.getBudgetProfile()
    }

    suspend fun getPreferences(): AppPreferences {
        return appPreferencesDao.getPreferences() ?: AppPreferences()
    }

    suspend fun saveBudgetProfile(
        userName: String = "Friend",
        monthlyIncome: Double,
        monthlySavingsGoal: Double,
        salaryReceivedDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
        nextSalaryDate: String = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
        dailyFoodExpense: Double = 0.0,
        dailyTransportExpense: Double = 0.0,
        currencySymbol: String = "GH₵"
    ) {
        val sanitizedName = userName.trim().ifBlank { "Friend" }
        val profile = BudgetProfile(
            id = 1,
            userName = sanitizedName,
            monthlyIncome = monthlyIncome,
            monthlySavingsGoal = monthlySavingsGoal,
            salaryReceivedDate = salaryReceivedDate,
            nextSalaryDate = nextSalaryDate,
            dailyFoodExpense = dailyFoodExpense,
            dailyTransportExpense = dailyTransportExpense,
            currencySymbol = currencySymbol,
            updatedAt = System.currentTimeMillis()
        )
        budgetDao.insertOrUpdateProfile(profile)
    }

    suspend fun updateSalaryDates(
        salaryReceivedDate: String,
        nextSalaryDate: String
    ) {
        val current = getBudgetProfile() ?: return
        val updated = current.copy(
            salaryReceivedDate = salaryReceivedDate,
            nextSalaryDate = nextSalaryDate,
            updatedAt = System.currentTimeMillis()
        )
        budgetDao.insertOrUpdateProfile(updated)
    }

    suspend fun updateUserName(newUserName: String) {
        val sanitized = newUserName.trim().ifBlank { "Friend" }
        val currentPrefs = getPreferences()
        appPreferencesDao.insertOrUpdatePreferences(
            currentPrefs.copy(savedUserName = sanitized)
        )
        val current = getBudgetProfile()
        if (current != null) {
            saveBudgetProfile(
                userName = sanitized,
                monthlyIncome = current.monthlyIncome,
                monthlySavingsGoal = current.monthlySavingsGoal,
                salaryReceivedDate = current.salaryReceivedDate,
                nextSalaryDate = current.nextSalaryDate,
                dailyFoodExpense = current.dailyFoodExpense,
                dailyTransportExpense = current.dailyTransportExpense,
                currencySymbol = current.currencySymbol
            )
        }
    }

    suspend fun completeOnboarding(userName: String) {
        val sanitized = userName.trim().ifBlank { "Friend" }
        val current = getPreferences()
        appPreferencesDao.insertOrUpdatePreferences(
            current.copy(
                onboardingCompleted = true,
                savedUserName = sanitized
            )
        )
    }

    /**
     * Calculates the dynamic allowance for a specific date given the profile and all past records in the salary cycle.
     */
    suspend fun calculateAllowanceForDate(
        date: LocalDate,
        profile: BudgetProfile
    ): Double {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val cycleStart = profile.getSalaryReceivedLocalDate()
        val cycleEnd = profile.getNextSalaryLocalDate()
        val cycleStartStr = cycleStart.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val allExpenses = expenseItemDao.getAllActiveExpenses()
        val pastSpentInCycle = allExpenses
            .filter { it.date >= cycleStartStr && it.date < dateStr }
            .sumOf { it.amount }

        val remainingDays = SpendingCalculator.calculateDaysRemaining(date, cycleEnd)

        return SpendingCalculator.calculateDynamicSalaryCycleAllowance(
            salaryAmount = profile.monthlyIncome,
            savingsGoal = profile.monthlySavingsGoal,
            pastSpentInCycleBeforeToday = pastSpentInCycle,
            remainingDaysIncludingToday = remainingDays
        )
    }

    /**
     * Individual Expense Item CRUD & Sync
     */
    suspend fun addExpenseItem(
        date: LocalDate,
        amount: Double,
        description: String,
        timeFormatted: String,
        category: String = "Other"
    ): Long {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val expense = ExpenseItem(
            date = dateStr,
            amount = amount,
            description = description.ifBlank { "Expense" },
            category = category,
            timeFormatted = timeFormatted,
            timestamp = System.currentTimeMillis(),
            isDelayed = false
        )
        val id = expenseItemDao.insertExpense(expense)
        syncDailyRecordForDate(date)
        return id
    }

    suspend fun updateExpenseItem(
        expense: ExpenseItem,
        date: LocalDate
    ) {
        expenseItemDao.updateExpense(expense)
        syncDailyRecordForDate(date)
    }

    suspend fun deleteExpenseItem(
        id: Long,
        date: LocalDate
    ) {
        expenseItemDao.deleteExpense(id)
        syncDailyRecordForDate(date)
    }

    suspend fun delayExpenseItem(
        id: Long,
        date: LocalDate
    ) {
        expenseItemDao.markExpenseDelayed(id)
        syncDailyRecordForDate(date)
    }

    suspend fun restoreExpenseItem(
        id: Long,
        targetDate: LocalDate
    ) {
        val targetDateStr = targetDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        expenseItemDao.restoreExpense(id, targetDateStr)
        syncDailyRecordForDate(targetDate)
    }

    /**
     * Re-aggregates all active (non-delayed) expense items for a date
     * and updates the corresponding DailySpendingRecord.
     */
    private suspend fun syncDailyRecordForDate(date: LocalDate) {
        val profile = budgetDao.getBudgetProfile() ?: return
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val activeExpenses = expenseItemDao.getActiveExpensesForDate(dateStr)
        val totalActualSpent = activeExpenses.sumOf { it.amount }

        saveDailySpending(
            date = date,
            actualSpent = totalActualSpent,
            note = if (activeExpenses.isNotEmpty()) "${activeExpenses.size} items" else ""
        )
    }

    /**
     * Saves or updates a daily spending entry.
     * Computes the dailyAllowance (if not provided) and the savedDifference (dailyAllowance - actualSpent).
     */
    suspend fun saveDailySpending(
        date: LocalDate,
        actualSpent: Double,
        note: String = ""
    ) {
        val profile = budgetDao.getBudgetProfile() ?: return
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val yearMonthStr = YearMonth.from(date).format(DateTimeFormatter.ofPattern("yyyy-MM"))

        // Check if there is an existing record for this date
        val existing = dailySpendingDao.getRecordByDate(dateStr)
        val dailyAllowance = if (existing != null) {
            existing.dailyAllowance
        } else {
            calculateAllowanceForDate(date, profile)
        }

        val savedDifference = dailyAllowance - actualSpent

        val record = DailySpendingRecord(
            date = dateStr,
            yearMonth = yearMonthStr,
            dailyAllowance = dailyAllowance,
            actualSpent = actualSpent,
            savedDifference = savedDifference,
            note = note,
            updatedAt = System.currentTimeMillis()
        )
        dailySpendingDao.insertOrUpdateRecord(record)
    }

    suspend fun confirmZeroSpending(date: LocalDate) {
        saveDailySpending(
            date = date,
            actualSpent = 0.0,
            note = "Zero spending confirmed"
        )
    }

    suspend fun deleteRecord(dateStr: String) {
        dailySpendingDao.deleteRecordByDate(dateStr)
    }

    // End-of-day remaining money options:
    suspend fun applySaveRemainingToGoal(savedAmount: Double) {
        val profile = budgetDao.getBudgetProfile() ?: return
        val updatedSavingsGoal = profile.monthlySavingsGoal + savedAmount
        budgetDao.insertOrUpdateProfile(
            profile.copy(
                monthlySavingsGoal = updatedSavingsGoal,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // Preset operations
    suspend fun addPreset(title: String, amount: Double, category: String, iconEmoji: String) {
        val preset = ExpensePreset(
            title = title,
            amount = amount,
            category = category,
            iconEmoji = iconEmoji
        )
        expensePresetDao.insertPreset(preset)
    }

    suspend fun updatePreset(preset: ExpensePreset) {
        expensePresetDao.updatePreset(preset)
    }

    suspend fun deletePreset(id: Long) {
        expensePresetDao.deletePreset(id)
    }

    // Preferences & Daily Reminder
    suspend fun updateDailyReminder(enabled: Boolean, hour: Int = 20, minute: Int = 0) {
        val current = getPreferences()
        appPreferencesDao.insertOrUpdatePreferences(
            current.copy(
                dailyReminderEnabled = enabled,
                reminderHour = hour,
                reminderMinute = minute
            )
        )
    }

    suspend fun clearAllData() {
        budgetDao.clearBudgetProfile()
        dailySpendingDao.clearAllRecords()
        expenseItemDao.clearAllExpenses()
        expensePresetDao.clearPresets()
        appPreferencesDao.clearPreferences()
    }
}
