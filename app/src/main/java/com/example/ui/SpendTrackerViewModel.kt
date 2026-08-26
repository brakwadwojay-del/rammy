package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DailyAnalysisSummary
import com.example.data.DailyBreakdown
import com.example.data.MonthProgressSummary
import com.example.data.SalaryCycleSummary
import com.example.data.SpendingCalculator
import com.example.data.WeeklyAnalysisSummary
import com.example.data.WeeklySpendingSummary
import com.example.data.YesterdaySpendingSummary
import com.example.data.model.AppPreferences
import com.example.data.model.BudgetProfile
import com.example.data.model.DailySpendingRecord
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseItem
import com.example.data.repository.SpendingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.max

enum class AppNavTab {
    DAILY,
    INSIGHTS
}

enum class InsightsPeriodType {
    THIS_MONTH,
    PREVIOUS_MONTH,
    CUSTOM
}

data class SpendTrackerUiState(
    val isLoading: Boolean = true,
    val isConfigured: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val savedUserName: String = "",
    val activeTab: AppNavTab = AppNavTab.DAILY,
    val budgetProfile: BudgetProfile? = null,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedMonth: YearMonth = YearMonth.now(),
    val todayRecord: DailySpendingRecord? = null,
    val todayExpenses: List<ExpenseItem> = emptyList(),
    val delayedExpenses: List<ExpenseItem> = emptyList(),
    val todayAllowance: Double = 0.0,
    val todayActualSpent: Double = 0.0,
    val todayLeftToSpend: Double = 0.0,
    val safeToSpendToday: Double = 0.0,
    val tomorrowTarget: Double = 0.0,
    val daysUntilPayday: Int = 0,
    val daysUntilPaydayText: String = "",
    val yesterdaySummary: YesterdaySpendingSummary? = null,
    val needsYesterdayConfirmation: Boolean = false,
    val yesterdayDate: LocalDate = LocalDate.now().minusDays(1),
    val expectedWeeklyExpenditure: Double = 0.0,
    val weeklySpent: Double = 0.0,
    val weeklyRemaining: Double = 0.0,
    val salaryCycleSummary: SalaryCycleSummary? = null,
    val dailyAnalysis: DailyAnalysisSummary? = null,
    val weeklyAnalysis: WeeklyAnalysisSummary? = null,
    val monthProgress: MonthProgressSummary? = null,
    val preferences: AppPreferences = AppPreferences(),
    val categoryBreakdown: List<SpendingCalculator.CategoryBreakdownItem> = emptyList(),
    val spendingMood: SpendingCalculator.SpendingMood? = null,
    val suggestion: SpendingCalculator.ContextualSuggestion? = null,
    val yesterdaySavedDifference: Double? = null,
    val dailyBreakdown: DailyBreakdown? = null,
    val weeklySpending: WeeklySpendingSummary? = null,
    val weekOffset: Int = 0,
    val allHistoryExpenses: List<ExpenseItem> = emptyList(),

    // Insights State
    val insightsPeriodType: InsightsPeriodType = InsightsPeriodType.THIS_MONTH,
    val insightsSelectedMonth: YearMonth = YearMonth.now(),
    val insightsExpenses: List<ExpenseItem> = emptyList(),
    val insightsGoalProgress: SpendingCalculator.GoalProgressData? = null,
    val insightsGoalProjection: SpendingCalculator.GoalProjectionData? = null,
    val insightsCategoryBreakdown: List<SpendingCalculator.CategoryBreakdownItem> = emptyList(),
    val insightsSelectedCategory: ExpenseCategory? = null,
    val insightsIsWeekTrend: Boolean = true,
    val insightsTrendData: SpendingCalculator.SpendingTrendData? = null,
    val insightsComparison: SpendingCalculator.PeriodSpendingComparison? = null,
    val insightsHabits: List<SpendingCalculator.SpendingHabitInsight> = emptyList(),
    val insightsAdvice: List<SpendingCalculator.FinancialAdviceItem> = emptyList(),
    val insightsPerformance: SpendingCalculator.DailyPerformanceData? = null,
    val insightsSummary: SpendingCalculator.MonthlyFinancialSummaryData? = null,
    val insightsHealthOverview: SpendingCalculator.FinancialHealthOverviewData? = null,
    val insightsHasSufficientData: Boolean = false,
    val availableMonths: List<YearMonth> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class SpendTrackerViewModel(
    private val repository: SpendingRepository
) : ViewModel() {

    private val _activeTab = MutableStateFlow(AppNavTab.DAILY)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    private val _weekOffset = MutableStateFlow(0)

    // Insights state controls
    private val _insightsPeriodType = MutableStateFlow(InsightsPeriodType.THIS_MONTH)
    private val _insightsCustomMonth = MutableStateFlow<YearMonth?>(null)
    private val _insightsIsWeekTrend = MutableStateFlow(true)
    private val _insightsSelectedCategory = MutableStateFlow<ExpenseCategory?>(null)

    private val _todayExpensesFlow = _selectedDate.flatMapLatest { date ->
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        repository.getActiveExpensesForDateFlow(dateStr)
    }

    private data class CoreData(
        val profile: BudgetProfile?,
        val allRecords: List<DailySpendingRecord>,
        val allExpenses: List<ExpenseItem>,
        val todayExpenses: List<ExpenseItem>,
        val delayedExpenses: List<ExpenseItem>,
        val preferences: AppPreferences
    )

    private val expensesBundleFlow = combine(
        repository.allActiveExpensesFlow,
        _todayExpensesFlow,
        repository.delayedExpensesFlow
    ) { allExp, todayExp, delayedExp ->
        Triple(allExp, todayExp, delayedExp)
    }

    private val coreDataFlow = combine(
        repository.budgetProfileFlow,
        repository.allRecordsFlow,
        expensesBundleFlow,
        repository.preferencesFlow
    ) { profile, allRecords, (allExpenses, todayExpenses, delayedExpenses), preferences ->
        CoreData(
            profile = profile,
            allRecords = allRecords,
            allExpenses = allExpenses,
            todayExpenses = todayExpenses,
            delayedExpenses = delayedExpenses,
            preferences = preferences ?: AppPreferences()
        )
    }

    private data class ControlState(
        val activeTab: AppNavTab,
        val selectedDate: LocalDate,
        val selectedMonth: YearMonth,
        val weekOffset: Int,
        val insightsPeriodType: InsightsPeriodType,
        val insightsCustomMonth: YearMonth?,
        val insightsIsWeekTrend: Boolean,
        val insightsSelectedCategory: ExpenseCategory?
    )

    private data class InsightsCtrl(
        val periodType: InsightsPeriodType,
        val customMonth: YearMonth?,
        val isWeekTrend: Boolean,
        val selectedCategory: ExpenseCategory?
    )

    private val insightsCtrlFlow = combine(
        _insightsPeriodType,
        _insightsCustomMonth,
        _insightsIsWeekTrend,
        _insightsSelectedCategory
    ) { periodType, customMonth, isWeekTrend, selectedCat ->
        InsightsCtrl(periodType, customMonth, isWeekTrend, selectedCat)
    }

    private val controlStateFlow = combine(
        _activeTab,
        _selectedDate,
        _selectedMonth,
        _weekOffset,
        insightsCtrlFlow
    ) { activeTab, selectedDate, selectedMonth, weekOffset, insightsCtrl ->
        ControlState(
            activeTab = activeTab,
            selectedDate = selectedDate,
            selectedMonth = selectedMonth,
            weekOffset = weekOffset,
            insightsPeriodType = insightsCtrl.periodType,
            insightsCustomMonth = insightsCtrl.customMonth,
            insightsIsWeekTrend = insightsCtrl.isWeekTrend,
            insightsSelectedCategory = insightsCtrl.selectedCategory
        )
    }

    val uiState: StateFlow<SpendTrackerUiState> = combine(
        coreDataFlow,
        controlStateFlow
    ) { core, ctrl ->
        val profile = core.profile
        if (profile == null) {
            SpendTrackerUiState(
                isLoading = false,
                isConfigured = false,
                onboardingCompleted = core.preferences.onboardingCompleted,
                savedUserName = core.preferences.savedUserName,
                activeTab = ctrl.activeTab,
                selectedDate = ctrl.selectedDate,
                selectedMonth = ctrl.selectedMonth,
                todayExpenses = core.todayExpenses,
                delayedExpenses = core.delayedExpenses,
                preferences = core.preferences
            )
        } else {
            val selectedDateStr = ctrl.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val selectedMonthStr = ctrl.selectedMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val monthRecords = core.allRecords.filter { it.yearMonth == selectedMonthStr }
            val todayRecord = monthRecords.find { it.date == selectedDateStr }

            // Salary cycle dates
            val cycleStart = profile.getSalaryReceivedLocalDate()
            val cycleEnd = profile.getNextSalaryLocalDate()
            val cycleStartStr = cycleStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val cycleEndStr = cycleEnd.format(DateTimeFormatter.ISO_LOCAL_DATE)

            // Expenses within the salary cycle
            val cycleExpenses = core.allExpenses.filter { it.date >= cycleStartStr && it.date <= cycleEndStr }
            val pastSpentInCycleBeforeToday = cycleExpenses.filter { it.date < selectedDateStr }.sumOf { it.amount }
            val todayActualSpent = core.todayExpenses.sumOf { it.amount }

            // Dynamic Daily Allowance based on Salary Cycle:
            // (Available spending pool - past spending in this cycle before today) / remaining days until next salary
            val remainingDaysInCycle = SpendingCalculator.calculateDaysRemaining(ctrl.selectedDate, cycleEnd)
            val todayAllowance = SpendingCalculator.calculateDynamicSalaryCycleAllowance(
                salaryAmount = profile.monthlyIncome,
                savingsGoal = profile.monthlySavingsGoal,
                pastSpentInCycleBeforeToday = pastSpentInCycleBeforeToday,
                remainingDaysIncludingToday = remainingDaysInCycle
            )

            val todayLeftToSpend = todayAllowance - todayActualSpent
            val safeToSpendToday = max(0.0, todayLeftToSpend)

            // Dynamic Tomorrow Target using the same dynamic formula
            val tomorrowTarget = SpendingCalculator.calculateTomorrowAllowance(
                salaryAmount = profile.monthlyIncome,
                savingsGoal = profile.monthlySavingsGoal,
                pastSpentBeforeToday = pastSpentInCycleBeforeToday,
                todayActualSpent = todayActualSpent,
                todayDate = ctrl.selectedDate,
                nextSalaryDate = cycleEnd
            )

            // Days until Payday
            val daysUntilPayday = SpendingCalculator.calculateDaysUntilPayday(ctrl.selectedDate, cycleEnd)
            val daysUntilPaydayText = SpendingCalculator.formatDaysUntilPayday(ctrl.selectedDate, cycleEnd)

            // Salary cycle summary
            val salaryCycleSummary = SpendingCalculator.calculateSalaryCycleSummary(
                salaryAmount = profile.monthlyIncome,
                savingsGoal = profile.monthlySavingsGoal,
                salaryReceivedDate = cycleStart,
                nextSalaryDate = cycleEnd,
                todayDate = ctrl.selectedDate,
                cycleExpenses = cycleExpenses,
                todayAllowance = todayAllowance
            )

            val monthSpentSoFar = monthRecords
                .filter { it.date != selectedDateStr }
                .sumOf { it.actualSpent } + todayActualSpent

            val monthProgress = SpendingCalculator.calculateMonthProgress(
                monthlyIncome = profile.monthlyIncome,
                monthlySavingsGoal = profile.monthlySavingsGoal,
                spentSoFarInMonth = monthSpentSoFar,
                date = ctrl.selectedDate,
                hasTodayRecord = todayRecord != null || core.todayExpenses.isNotEmpty(),
                todayAllowance = todayAllowance
            )

            // Yesterday's record & summary processing:
            val yesterdayDate = ctrl.selectedDate.minusDays(1)
            val yesterdayStr = yesterdayDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val yesterdayRecord = core.allRecords.find { it.date == yesterdayStr }
            val yesterdayExpenses = core.allExpenses.filter { it.date == yesterdayStr }
            val yesterdaySavedDifference = yesterdayRecord?.savedDifference

            val hasYesterdayRecord = yesterdayRecord != null
            val hasYesterdayExpenses = yesterdayExpenses.isNotEmpty()
            val isYesterdayInCycle = !yesterdayDate.isBefore(cycleStart) && !yesterdayDate.isAfter(cycleEnd)
            val needsYesterdayConfirmation = isYesterdayInCycle && !hasYesterdayRecord && !hasYesterdayExpenses

            val yesterdayActualSpent = if (hasYesterdayExpenses) {
                yesterdayExpenses.sumOf { it.amount }
            } else {
                yesterdayRecord?.actualSpent ?: 0.0
            }

            val yesterdayAllowance = yesterdayRecord?.dailyAllowance ?: if (isYesterdayInCycle) {
                val pastSpentBeforeYesterday = cycleExpenses.filter { it.date < yesterdayStr }.sumOf { it.amount }
                val remainingDaysFromYesterday = SpendingCalculator.calculateDaysRemaining(yesterdayDate, cycleEnd)
                SpendingCalculator.calculateDynamicSalaryCycleAllowance(
                    salaryAmount = profile.monthlyIncome,
                    savingsGoal = profile.monthlySavingsGoal,
                    pastSpentInCycleBeforeToday = pastSpentBeforeYesterday,
                    remainingDaysIncludingToday = remainingDaysFromYesterday
                )
            } else 0.0

            val yesterdaySummary = if (isYesterdayInCycle && (hasYesterdayRecord || hasYesterdayExpenses)) {
                SpendingCalculator.calculateYesterdaySummary(
                    yesterdayDate = yesterdayDate,
                    yesterdayAllowance = yesterdayAllowance,
                    yesterdaySpent = yesterdayActualSpent,
                    isConfirmedZero = hasYesterdayRecord && yesterdayRecord?.note?.contains("Zero") == true,
                    currencySymbol = profile.currencySymbol
                )
            } else null

            val categoryBreakdown = SpendingCalculator.calculateCategoryBreakdown(core.todayExpenses)
            val dailyBreakdown = SpendingCalculator.calculateDailyBreakdown(
                dailyAllowance = todayAllowance,
                dailyFoodExpense = profile.dailyFoodExpense,
                dailyTransportExpense = profile.dailyTransportExpense
            )

            val baselineDailyAllowance = SpendingCalculator.calculateAvailableSpendingPool(profile.monthlyIncome, profile.monthlySavingsGoal) / SpendingCalculator.calculateCycleDays(cycleStart, cycleEnd).coerceAtLeast(1)

            val weeklySpending = SpendingCalculator.calculateWeeklySpending(
                expenses = core.allExpenses,
                records = core.allRecords,
                referenceDate = ctrl.selectedDate,
                weekOffset = ctrl.weekOffset,
                baselineDailyAllowance = if (todayAllowance > 0) todayAllowance else baselineDailyAllowance
            )

            val expectedWeeklyExpenditure = todayAllowance * 7.0
            val weeklySpent = weeklySpending.weeklySpent
            val weeklyRemaining = expectedWeeklyExpenditure - weeklySpent

            val dailyAnalysis = SpendingCalculator.calculateDailyAnalysis(
                cycleExpenses = cycleExpenses,
                currentDailyAllowance = todayAllowance,
                salaryReceivedDate = cycleStart,
                todayDate = ctrl.selectedDate
            )

            val weeklyAnalysis = SpendingCalculator.calculateWeeklyAnalysis(
                todayAllowance = todayAllowance,
                weeklySummary = weeklySpending
            )

            val streakDays = SpendingCalculator.calculateStreak(core.allRecords, ctrl.selectedDate)

            val spendingMood = SpendingCalculator.evaluateSpendingMood(
                todayLeftToSpend = todayLeftToSpend,
                todayAllowance = todayAllowance,
                todayActualSpent = todayActualSpent,
                salaryCycleSummary = salaryCycleSummary,
                streakDays = streakDays,
                currencySymbol = profile.currencySymbol,
                commentIndex = core.todayExpenses.size
            )

            val suggestion = SpendingCalculator.generateContextualSuggestion(
                todayLeftToSpend = todayLeftToSpend,
                todayAllowance = todayAllowance,
                todayActualSpent = todayActualSpent,
                todayExpenses = core.todayExpenses,
                yesterdaySavedDifference = yesterdaySavedDifference,
                currencySymbol = profile.currencySymbol
            )

            // =================================================================
            // INSIGHTS COMPUTATION (Comprehensive, accurate & single source of truth)
            // =================================================================
            val currentYearMonth = YearMonth.now()
            val effectiveInsightsMonth = when (ctrl.insightsPeriodType) {
                InsightsPeriodType.THIS_MONTH -> currentYearMonth
                InsightsPeriodType.PREVIOUS_MONTH -> currentYearMonth.minusMonths(1)
                InsightsPeriodType.CUSTOM -> ctrl.insightsCustomMonth ?: currentYearMonth
            }

            val effectiveMonthPrefix = effectiveInsightsMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val previousMonthPrefix = effectiveInsightsMonth.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"))

            val insightsExpenses = core.allExpenses.filter { it.date.startsWith(effectiveMonthPrefix) }
            val previousExpenses = core.allExpenses.filter { it.date.startsWith(previousMonthPrefix) }
            val insightsRecords = core.allRecords.filter { it.yearMonth == effectiveMonthPrefix }

            val totalSpentInPeriod = insightsExpenses.sumOf { it.amount }
            val hasSufficientData = insightsExpenses.isNotEmpty() || insightsRecords.isNotEmpty()

            val goalProgress = SpendingCalculator.calculateGoalProgress(
                monthlyIncome = profile.monthlyIncome,
                monthlySavingsGoal = profile.monthlySavingsGoal,
                spentInPeriod = totalSpentInPeriod,
                currencySymbol = profile.currencySymbol
            )

            val goalProjection = SpendingCalculator.calculateGoalProjection(
                monthlyIncome = profile.monthlyIncome,
                monthlySavingsGoal = profile.monthlySavingsGoal,
                spentSoFar = totalSpentInPeriod,
                yearMonth = effectiveInsightsMonth,
                currentDate = LocalDate.now(),
                currencySymbol = profile.currencySymbol
            )

            val insightsCategoryBreakdown = SpendingCalculator.calculateCategoryBreakdown(insightsExpenses)

            val insightsBaselineAllowance = SpendingCalculator.calculateInitialDailyAllowance(
                monthlyIncome = profile.monthlyIncome,
                monthlySavingsGoal = profile.monthlySavingsGoal,
                totalDaysInMonth = effectiveInsightsMonth.lengthOfMonth()
            )

            val trendData = SpendingCalculator.calculateSpendingTrendData(
                records = core.allRecords,
                expenses = core.allExpenses,
                yearMonth = effectiveInsightsMonth,
                isWeekView = ctrl.insightsIsWeekTrend,
                referenceDate = LocalDate.now(),
                baselineDailyAllowance = insightsBaselineAllowance
            )

            val comparison = SpendingCalculator.calculatePeriodComparison(
                currentExpenses = insightsExpenses,
                previousExpenses = previousExpenses
            )

            val habits = SpendingCalculator.analyzeSpendingHabits(
                expenses = insightsExpenses,
                comparison = comparison,
                currencySymbol = profile.currencySymbol
            )

            val advice = SpendingCalculator.generateFinancialAdvice(
                expenses = insightsExpenses,
                records = insightsRecords,
                goalProgress = goalProgress,
                currencySymbol = profile.currencySymbol
            )

            val dailyPerformance = SpendingCalculator.calculateDailyPerformance(
                records = insightsRecords,
                expenses = insightsExpenses,
                yearMonth = effectiveInsightsMonth
            )

            val remainingSpendingMoney = max(0.0, SpendingCalculator.calculateMonthlySpendable(profile.monthlyIncome, profile.monthlySavingsGoal) - totalSpentInPeriod)
            val summary = SpendingCalculator.MonthlyFinancialSummaryData(
                monthlyIncome = profile.monthlyIncome,
                plannedSavings = profile.monthlySavingsGoal,
                spentSoFar = totalSpentInPeriod,
                remainingSpendingMoney = remainingSpendingMoney,
                goalProgressPercent = goalProgress.progressPercent
            )

            val healthOverview = SpendingCalculator.calculateFinancialHealthOverview(
                monthlyIncome = profile.monthlyIncome,
                monthlySavingsGoal = profile.monthlySavingsGoal,
                spentInPeriod = totalSpentInPeriod,
                records = insightsRecords
            )

            // Extract all available months from data
            val availableMonthsSet = mutableSetOf<YearMonth>()
            availableMonthsSet.add(currentYearMonth)
            availableMonthsSet.add(currentYearMonth.minusMonths(1))
            core.allExpenses.forEach {
                try {
                    val d = LocalDate.parse(it.date)
                    availableMonthsSet.add(YearMonth.from(d))
                } catch (_: Exception) {}
            }
            core.allRecords.forEach {
                try {
                    val ym = YearMonth.parse(it.yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"))
                    availableMonthsSet.add(ym)
                } catch (_: Exception) {}
            }
            val availableMonths = availableMonthsSet.sortedDescending()

            SpendTrackerUiState(
                isLoading = false,
                isConfigured = true,
                onboardingCompleted = true,
                savedUserName = profile.userName,
                activeTab = ctrl.activeTab,
                budgetProfile = profile,
                selectedDate = ctrl.selectedDate,
                selectedMonth = ctrl.selectedMonth,
                todayRecord = todayRecord,
                todayExpenses = core.todayExpenses,
                delayedExpenses = core.delayedExpenses,
                todayAllowance = todayAllowance,
                todayActualSpent = todayActualSpent,
                todayLeftToSpend = todayLeftToSpend,
                safeToSpendToday = safeToSpendToday,
                tomorrowTarget = tomorrowTarget,
                daysUntilPayday = daysUntilPayday,
                daysUntilPaydayText = daysUntilPaydayText,
                yesterdaySummary = yesterdaySummary,
                needsYesterdayConfirmation = needsYesterdayConfirmation,
                yesterdayDate = yesterdayDate,
                expectedWeeklyExpenditure = expectedWeeklyExpenditure,
                weeklySpent = weeklySpent,
                weeklyRemaining = weeklyRemaining,
                salaryCycleSummary = salaryCycleSummary,
                dailyAnalysis = dailyAnalysis,
                weeklyAnalysis = weeklyAnalysis,
                monthProgress = monthProgress,
                preferences = core.preferences,
                categoryBreakdown = categoryBreakdown,
                spendingMood = spendingMood,
                suggestion = suggestion,
                yesterdaySavedDifference = yesterdaySavedDifference,
                dailyBreakdown = dailyBreakdown,
                weeklySpending = weeklySpending,
                weekOffset = ctrl.weekOffset,
                allHistoryExpenses = core.allExpenses,

                // Insights properties
                insightsPeriodType = ctrl.insightsPeriodType,
                insightsSelectedMonth = effectiveInsightsMonth,
                insightsExpenses = insightsExpenses,
                insightsGoalProgress = goalProgress,
                insightsGoalProjection = goalProjection,
                insightsCategoryBreakdown = insightsCategoryBreakdown,
                insightsSelectedCategory = ctrl.insightsSelectedCategory,
                insightsIsWeekTrend = ctrl.insightsIsWeekTrend,
                insightsTrendData = trendData,
                insightsComparison = comparison,
                insightsHabits = habits,
                insightsAdvice = advice,
                insightsPerformance = dailyPerformance,
                insightsSummary = summary,
                insightsHealthOverview = healthOverview,
                insightsHasSufficientData = hasSufficientData,
                availableMonths = availableMonths
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SpendTrackerUiState(isLoading = true)
    )

    fun setNavTab(tab: AppNavTab) {
        _activeTab.value = tab
    }

    fun selectWeekOffset(offset: Int) {
        _weekOffset.value = offset
    }

    fun resetWeekOffset() {
        _weekOffset.value = 0
    }

    fun setInsightsPeriodType(periodType: InsightsPeriodType, customMonth: YearMonth? = null) {
        _insightsPeriodType.value = periodType
        if (customMonth != null) {
            _insightsCustomMonth.value = customMonth
        }
    }

    fun setInsightsTrendIsWeek(isWeek: Boolean) {
        _insightsIsWeekTrend.value = isWeek
    }

    fun selectInsightsCategory(category: ExpenseCategory?) {
        _insightsSelectedCategory.value = category
    }

    fun saveSetup(
        userName: String = "Friend",
        monthlyIncome: Double,
        monthlySavingsGoal: Double,
        salaryReceivedDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
        nextSalaryDate: String = LocalDate.now().plusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
        dailyFoodExpense: Double = 0.0,
        dailyTransportExpense: Double = 0.0,
        currencySymbol: String = "GH₵"
    ) {
        viewModelScope.launch {
            repository.saveBudgetProfile(
                userName = userName,
                monthlyIncome = monthlyIncome,
                monthlySavingsGoal = monthlySavingsGoal,
                salaryReceivedDate = salaryReceivedDate,
                nextSalaryDate = nextSalaryDate,
                dailyFoodExpense = dailyFoodExpense,
                dailyTransportExpense = dailyTransportExpense,
                currencySymbol = currencySymbol
            )
        }
    }

    fun updateSalaryDates(salaryReceivedDate: String, nextSalaryDate: String) {
        viewModelScope.launch {
            repository.updateSalaryDates(salaryReceivedDate, nextSalaryDate)
        }
    }

    fun updateUserName(userName: String) {
        viewModelScope.launch {
            repository.updateUserName(userName)
        }
    }

    fun completeOnboarding(userName: String) {
        viewModelScope.launch {
            repository.completeOnboarding(userName)
        }
    }

    // Expense Operations
    fun addExpense(
        amount: Double,
        description: String = "",
        timeFormatted: String = "",
        category: String = "Other"
    ) {
        viewModelScope.launch {
            val time = if (timeFormatted.isNotBlank()) timeFormatted else {
                java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))
            }
            repository.addExpenseItem(
                date = _selectedDate.value,
                amount = amount,
                description = description,
                timeFormatted = time,
                category = category
            )
        }
    }

    fun updateExpense(expense: ExpenseItem) {
        viewModelScope.launch {
            repository.updateExpenseItem(expense, _selectedDate.value)
        }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch {
            repository.deleteExpenseItem(id, _selectedDate.value)
        }
    }

    fun delayExpense(id: Long) {
        viewModelScope.launch {
            repository.delayExpenseItem(id, _selectedDate.value)
        }
    }

    fun restoreExpense(id: Long) {
        viewModelScope.launch {
            repository.restoreExpenseItem(id, _selectedDate.value)
        }
    }

    fun confirmZeroSpendForDate(date: LocalDate) {
        viewModelScope.launch {
            repository.confirmZeroSpending(date)
        }
    }

    fun setDailyReminder(enabled: Boolean, hour: Int = 20, minute: Int = 0) {
        viewModelScope.launch {
            repository.updateDailyReminder(enabled, hour, minute)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    companion object {
        fun provideFactory(repository: SpendingRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SpendTrackerViewModel(repository) as T
                }
            }
    }
}
