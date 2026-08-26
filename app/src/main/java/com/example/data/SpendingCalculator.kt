package com.example.data

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.abs

data class DailyBreakdown(
    val food: Double,
    val transport: Double,
    val other: Double,
    val totalAllowance: Double
)

data class SalaryCycleSummary(
    val salaryAmount: Double,
    val savingsGoal: Double,
    val availableSpendingPool: Double,
    val salaryReceivedDate: LocalDate,
    val nextSalaryDate: LocalDate,
    val totalCycleDays: Int,
    val daysRemaining: Int,
    val daysElapsed: Int,
    val totalSpentInCycle: Double,
    val remainingSpendingMoney: Double,
    val projectedSavings: Double,
    val isOnTrack: Boolean,
    val statusMessage: String
)

data class DailyAnalysisSummary(
    val averageDailySpent: Double,
    val highestSpendingDay: Pair<LocalDate, Double>?,
    val lowestSpendingDay: Pair<LocalDate, Double>?,
    val currentDailyAllowance: Double
)

data class WeeklyAnalysisSummary(
    val expectedWeeklyExpenditure: Double,
    val actualWeeklyExpenditure: Double,
    val difference: Double,
    val averageDailySpent: Double
)

data class MonthProgressSummary(
    val monthlyIncome: Double,
    val monthlySavingsGoal: Double,
    val totalSpendableBudget: Double,
    val totalSpentSoFar: Double,
    val remainingSpendableBudget: Double,
    val projectedSavings: Double,
    val daysRemainingInMonth: Int,
    val totalDaysInMonth: Int,
    val dayOfMonth: Int,
    val isOnTrack: Boolean,
    val statusMessage: String
)

data class WeeklySpendingSummary(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val weekLabel: String,
    val isCurrentWeek: Boolean,
    val weeklyAllowance: Double,
    val weeklySpent: Double,
    val weeklyRemaining: Double,
    val averageDailySpent: Double,
    val daysElapsed: Int,
    val weekExpenses: List<com.example.data.model.ExpenseItem> = emptyList()
)

data class YesterdaySpendingSummary(
    val hasData: Boolean,
    val isZeroConfirmed: Boolean,
    val date: LocalDate,
    val allowance: Double,
    val actualSpent: Double,
    val difference: Double, // allowance - actualSpent (positive = saved/under budget, negative = over budget)
    val moodEmoji: String,
    val statusTitle: String,
    val statusDescription: String
)

object SpendingCalculator {

    /**
     * Calculates total days until expected payday (next salary date).
     */
    fun calculateDaysUntilPayday(today: LocalDate, nextSalaryDate: LocalDate): Int {
        val days = ChronoUnit.DAYS.between(today, nextSalaryDate).toInt()
        return max(0, days)
    }

    /**
     * Formats the unobtrusive "Days until payday" indicator.
     */
    fun formatDaysUntilPayday(today: LocalDate, nextSalaryDate: LocalDate): String {
        val days = ChronoUnit.DAYS.between(today, nextSalaryDate).toInt()
        return when {
            days == 0 || today.isEqual(nextSalaryDate) -> "Payday is today 🎉"
            days == 1 -> "1 day until payday"
            days > 1 -> "$days days until payday"
            else -> "Payday reached"
        }
    }

    /**
     * Dynamically calculates tomorrow's recommended spending target.
     * Uses the exact same dynamic formula:
     * (Remaining spending pool after today's actual spending) ÷ (remaining days from tomorrow until next salary)
     */
    fun calculateTomorrowAllowance(
        salaryAmount: Double,
        savingsGoal: Double,
        pastSpentBeforeToday: Double,
        todayActualSpent: Double,
        todayDate: LocalDate,
        nextSalaryDate: LocalDate
    ): Double {
        val tomorrow = todayDate.plusDays(1)
        if (!tomorrow.isBefore(nextSalaryDate) && !todayDate.isBefore(nextSalaryDate)) {
            // Already at or past salary cycle end
            return 0.0
        }
        val daysRemainingFromTomorrow = calculateDaysRemaining(tomorrow, nextSalaryDate)
        if (daysRemainingFromTomorrow <= 0) return 0.0

        val totalSpentThroughToday = pastSpentBeforeToday + todayActualSpent
        val availableSpending = calculateAvailableSpendingPool(salaryAmount, savingsGoal)
        val remainingSpendableAfterToday = max(0.0, availableSpending - totalSpentThroughToday)

        return remainingSpendableAfterToday / daysRemainingFromTomorrow
    }

    /**
     * Calculates simple end-of-day / yesterday summary.
     */
    fun calculateYesterdaySummary(
        yesterdayDate: LocalDate,
        yesterdayAllowance: Double,
        yesterdaySpent: Double,
        isConfirmedZero: Boolean = false,
        currencySymbol: String = "GH₵"
    ): YesterdaySpendingSummary {
        val diff = yesterdayAllowance - yesterdaySpent

        return when {
            diff > 0.50 -> {
                // User spent less than planned
                YesterdaySpendingSummary(
                    hasData = true,
                    isZeroConfirmed = isConfirmedZero,
                    date = yesterdayDate,
                    allowance = yesterdayAllowance,
                    actualSpent = yesterdaySpent,
                    difference = diff,
                    moodEmoji = "😄",
                    statusTitle = "Good day",
                    statusDescription = "You spent $currencySymbol${formatExactDecimal(diff)} less than planned."
                )
            }
            diff < -0.50 -> {
                // User overspent
                val over = abs(diff)
                YesterdaySpendingSummary(
                    hasData = true,
                    isZeroConfirmed = isConfirmedZero,
                    date = yesterdayDate,
                    allowance = yesterdayAllowance,
                    actualSpent = yesterdaySpent,
                    difference = diff,
                    moodEmoji = "😐",
                    statusTitle = "A little over yesterday",
                    statusDescription = "You spent $currencySymbol${formatExactDecimal(over)} more than planned."
                )
            }
            else -> {
                // On track
                YesterdaySpendingSummary(
                    hasData = true,
                    isZeroConfirmed = isConfirmedZero,
                    date = yesterdayDate,
                    allowance = yesterdayAllowance,
                    actualSpent = yesterdaySpent,
                    difference = diff,
                    moodEmoji = "🙂",
                    statusTitle = "You stayed on track.",
                    statusDescription = "You spent $currencySymbol${formatExactDecimal(yesterdaySpent)} out of $currencySymbol${formatExactDecimal(yesterdayAllowance)}."
                )
            }
        }
    }

    /**
     * Calculates total days in the salary cycle between salaryReceivedDate and nextSalaryDate.
     */
    fun calculateCycleDays(salaryReceivedDate: LocalDate, nextSalaryDate: LocalDate): Int {
        val days = ChronoUnit.DAYS.between(salaryReceivedDate, nextSalaryDate).toInt()
        return if (days <= 0) 1 else days
    }

    /**
     * Calculates days remaining in the cycle from today until next salary date (inclusive of today).
     */
    fun calculateDaysRemaining(today: LocalDate, nextSalaryDate: LocalDate): Int {
        if (!today.isBefore(nextSalaryDate)) return 1
        val days = ChronoUnit.DAYS.between(today, nextSalaryDate).toInt()
        return max(1, days)
    }

    /**
     * Money available for spending in salary cycle = Salary Amount - Savings Goal
     */
    fun calculateAvailableSpendingPool(salaryAmount: Double, savingsGoal: Double): Double {
        return max(0.0, salaryAmount - savingsGoal)
    }

    /**
     * Dynamically calculates today's recommended daily spending.
     *
     * Total Salary - Savings Target = Available Spending Pool
     * Available Spending Pool - Past Spending in this Cycle before today = Remaining Spendable Money
     * Remaining Days (from today until next salary) = Days remaining
     * Recommended Daily Spending = Remaining Spendable Money ÷ Remaining Days
     *
     * Automatically adjusts for past surpluses or overspending and strictly protects the savings goal.
     */
    fun calculateDynamicSalaryCycleAllowance(
        salaryAmount: Double,
        savingsGoal: Double,
        pastSpentInCycleBeforeToday: Double,
        remainingDaysIncludingToday: Int
    ): Double {
        if (remainingDaysIncludingToday <= 0) return 0.0
        val availableSpending = calculateAvailableSpendingPool(salaryAmount, savingsGoal)
        val remainingSpendable = max(0.0, availableSpending - pastSpentInCycleBeforeToday)
        return remainingSpendable / remainingDaysIncludingToday
    }

    /**
     * Comprehensive Salary Cycle Summary and Projection
     */
    fun calculateSalaryCycleSummary(
        salaryAmount: Double,
        savingsGoal: Double,
        salaryReceivedDate: LocalDate,
        nextSalaryDate: LocalDate,
        todayDate: LocalDate,
        cycleExpenses: List<com.example.data.model.ExpenseItem>,
        todayAllowance: Double
    ): SalaryCycleSummary {
        val totalCycleDays = calculateCycleDays(salaryReceivedDate, nextSalaryDate)
        val daysRemaining = calculateDaysRemaining(todayDate, nextSalaryDate)
        val daysElapsed = max(0, ChronoUnit.DAYS.between(salaryReceivedDate, todayDate).toInt())

        val availableSpending = calculateAvailableSpendingPool(salaryAmount, savingsGoal)
        val totalSpentInCycle = cycleExpenses.sumOf { it.amount }
        val remainingSpendingMoney = max(0.0, availableSpending - totalSpentInCycle)

        // Projected savings:
        // If user continues within the dynamically recommended daily limit for the remaining days,
        // projected savings equals exactly the savings goal plus any positive leftover buffer.
        val projectedTotalSpend = totalSpentInCycle + (todayAllowance * (daysRemaining - 1).coerceAtLeast(0))
        val projectedSavings = max(0.0, salaryAmount - projectedTotalSpend)

        val isOnTrack = totalSpentInCycle <= (availableSpending * ((daysElapsed + 1).toDouble() / totalCycleDays.toDouble())) ||
                totalSpentInCycle <= availableSpending

        val statusMessage = if (totalSpentInCycle > availableSpending) {
            val overspent = totalSpentInCycle - availableSpending
            "Spending pool exceeded by GH₵${formatExactDecimal(overspent)}. Reduced daily allowance active."
        } else if (isOnTrack) {
            "On track to save GH₵${formatAmount(savingsGoal)} before next salary."
        } else {
            "Pacing slightly high. Stick to recommended daily spending."
        }

        return SalaryCycleSummary(
            salaryAmount = salaryAmount,
            savingsGoal = savingsGoal,
            availableSpendingPool = availableSpending,
            salaryReceivedDate = salaryReceivedDate,
            nextSalaryDate = nextSalaryDate,
            totalCycleDays = totalCycleDays,
            daysRemaining = daysRemaining,
            daysElapsed = daysElapsed,
            totalSpentInCycle = totalSpentInCycle,
            remainingSpendingMoney = remainingSpendingMoney,
            projectedSavings = projectedSavings,
            isOnTrack = isOnTrack,
            statusMessage = statusMessage
        )
    }

    /**
     * Daily Analysis summary for Analysis screen
     */
    fun calculateDailyAnalysis(
        cycleExpenses: List<com.example.data.model.ExpenseItem>,
        currentDailyAllowance: Double,
        salaryReceivedDate: LocalDate,
        todayDate: LocalDate
    ): DailyAnalysisSummary {
        val expensesByDate = cycleExpenses.groupBy { it.date }
        val daysElapsed = max(1, ChronoUnit.DAYS.between(salaryReceivedDate, todayDate).toInt() + 1)
        val totalSpent = cycleExpenses.sumOf { it.amount }
        val avgDaily = totalSpent / daysElapsed

        var highest: Pair<LocalDate, Double>? = null
        var lowest: Pair<LocalDate, Double>? = null

        expensesByDate.forEach { (dateStr, list) ->
            try {
                val d = LocalDate.parse(dateStr)
                val daySum = list.sumOf { it.amount }
                if (highest == null || daySum > (highest?.second ?: 0.0)) {
                    highest = Pair(d, daySum)
                }
                if (lowest == null || daySum < (lowest?.second ?: Double.MAX_VALUE)) {
                    lowest = Pair(d, daySum)
                }
            } catch (_: Exception) {}
        }

        return DailyAnalysisSummary(
            averageDailySpent = avgDaily,
            highestSpendingDay = highest,
            lowestSpendingDay = lowest,
            currentDailyAllowance = currentDailyAllowance
        )
    }

    /**
     * Weekly Analysis summary for Analysis screen
     */
    fun calculateWeeklyAnalysis(
        todayAllowance: Double,
        weeklySummary: WeeklySpendingSummary
    ): WeeklyAnalysisSummary {
        val expectedWeekly = todayAllowance * 7.0
        val actualWeekly = weeklySummary.weeklySpent
        val difference = expectedWeekly - actualWeekly

        return WeeklyAnalysisSummary(
            expectedWeeklyExpenditure = expectedWeekly,
            actualWeeklyExpenditure = actualWeekly,
            difference = difference,
            averageDailySpent = weeklySummary.averageDailySpent
        )
    }

    /**
     * Monthly money available for spending = Monthly Income - Monthly Savings Goal
     */
    fun calculateMonthlySpendable(monthlyIncome: Double, monthlySavingsGoal: Double): Double {
        return max(0.0, monthlyIncome - monthlySavingsGoal)
    }

    /**
     * Calculates the initial baseline daily allowance based on full month days.
     */
    fun calculateInitialDailyAllowance(
        monthlyIncome: Double,
        monthlySavingsGoal: Double,
        totalDaysInMonth: Int
    ): Double {
        if (totalDaysInMonth <= 0) return 0.0
        val spendable = calculateMonthlySpendable(monthlyIncome, monthlySavingsGoal)
        return spendable / totalDaysInMonth
    }

    /**
     * Dynamically calculates today's recommended spending limit.
     */
    fun calculateDynamicDailyAllowance(
        monthlyIncome: Double,
        monthlySavingsGoal: Double,
        pastSpentInMonth: Double,
        remainingDaysIncludingToday: Int
    ): Double {
        if (remainingDaysIncludingToday <= 0) return 0.0
        val totalSpendable = calculateMonthlySpendable(monthlyIncome, monthlySavingsGoal)
        val remainingSpendable = totalSpendable - pastSpentInMonth
        return max(0.0, remainingSpendable / remainingDaysIncludingToday)
    }

    /**
     * Breakdown of daily budget into Food, Transport, and Other spending.
     * Food and transport are part of the daily allowance, not additional money.
     */
    fun calculateDailyBreakdown(
        dailyAllowance: Double,
        dailyFoodExpense: Double,
        dailyTransportExpense: Double
    ): DailyBreakdown {
        val fixedExpenses = dailyFoodExpense + dailyTransportExpense
        val other = max(0.0, dailyAllowance - fixedExpenses)
        return DailyBreakdown(
            food = dailyFoodExpense,
            transport = dailyTransportExpense,
            other = other,
            totalAllowance = dailyAllowance
        )
    }

    /**
     * Calculates weekly spending summary for any given week (supports offset for past weeks).
     */
    fun calculateWeeklySpending(
        expenses: List<com.example.data.model.ExpenseItem>,
        records: List<com.example.data.model.DailySpendingRecord>,
        referenceDate: LocalDate,
        weekOffset: Int = 0,
        baselineDailyAllowance: Double
    ): WeeklySpendingSummary {
        val currentMonday = referenceDate.minusDays((referenceDate.dayOfWeek.value - 1).toLong())
        val targetMonday = currentMonday.plusWeeks(weekOffset.toLong())
        val targetSunday = targetMonday.plusDays(6)
        val isCurrent = (weekOffset == 0)

        val expenseMap = expenses.groupBy { it.date }
        val recordMap = records.associateBy { it.date }

        var totalAllowance = 0.0
        var totalSpent = 0.0
        var activeDays = 0

        val weekExpensesList = mutableListOf<com.example.data.model.ExpenseItem>()

        for (i in 0 until 7) {
            val d = targetMonday.plusDays(i.toLong())
            val dStr = d.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            val dayExpenses = expenseMap[dStr] ?: emptyList()
            weekExpensesList.addAll(dayExpenses)

            val daySpent = if (dayExpenses.isNotEmpty()) {
                dayExpenses.sumOf { it.amount }
            } else {
                recordMap[dStr]?.actualSpent ?: 0.0
            }
            val dayAllowance = recordMap[dStr]?.dailyAllowance ?: baselineDailyAllowance

            totalAllowance += dayAllowance
            totalSpent += daySpent

            if (!d.isAfter(referenceDate)) {
                activeDays++
            }
        }

        val remaining = totalAllowance - totalSpent
        val avgDaily = if (activeDays > 0) totalSpent / activeDays else if (totalSpent > 0) totalSpent / 7 else 0.0

        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d")
        val weekLabel = if (isCurrent) "This Week" else "${targetMonday.format(formatter)} – ${targetSunday.format(formatter)}"

        return WeeklySpendingSummary(
            weekStart = targetMonday,
            weekEnd = targetSunday,
            weekLabel = weekLabel,
            isCurrentWeek = isCurrent,
            weeklyAllowance = totalAllowance,
            weeklySpent = totalSpent,
            weeklyRemaining = remaining,
            averageDailySpent = avgDaily,
            daysElapsed = activeDays,
            weekExpenses = weekExpensesList.sortedByDescending { it.date + " " + it.timeFormatted }
        )
    }

    /**
     * Calculates monthly progress and projected savings.
     */
    fun calculateMonthProgress(
        monthlyIncome: Double,
        monthlySavingsGoal: Double,
        spentSoFarInMonth: Double,
        date: LocalDate,
        hasTodayRecord: Boolean,
        todayAllowance: Double
    ): MonthProgressSummary {
        val yearMonth = YearMonth.from(date)
        val totalDays = yearMonth.lengthOfMonth()
        val currentDay = date.dayOfMonth
        
        // Days remaining in month
        // If today is day 23 of 31:
        // If today's spending has been recorded, remaining days to spend is (31 - 23) = 8 days.
        // If today's spending is not yet recorded, remaining days to spend is (31 - 23 + 1) = 9 days.
        val remainingDays = if (hasTodayRecord) {
            max(0, totalDays - currentDay)
        } else {
            max(1, totalDays - currentDay + 1)
        }

        val totalSpendable = calculateMonthlySpendable(monthlyIncome, monthlySavingsGoal)
        val remainingSpendable = max(0.0, totalSpendable - spentSoFarInMonth)

        // Projected savings:
        // Monthly Income - (Total spent so far + projected remaining spend at current allowance)
        val projectedRemainingSpend = if (hasTodayRecord) {
            val futureDays = max(0, totalDays - currentDay)
            if (futureDays > 0) {
                remainingSpendable
            } else {
                0.0
            }
        } else {
            remainingSpendable
        }

        val projectedTotalSpend = spentSoFarInMonth + projectedRemainingSpend
        val projectedSavings = max(0.0, monthlyIncome - projectedTotalSpend)

        val isOnTrack = (spentSoFarInMonth <= totalSpendable)
        val statusMessage = if (isOnTrack) {
            "You're on track to save GH₵${formatAmount(monthlySavingsGoal)} this month."
        } else {
            val overAmount = spentSoFarInMonth - totalSpendable
            "Budget exceeded by GH₵${formatAmount(overAmount)}. Reduced allowance recommended."
        }

        return MonthProgressSummary(
            monthlyIncome = monthlyIncome,
            monthlySavingsGoal = monthlySavingsGoal,
            totalSpendableBudget = totalSpendable,
            totalSpentSoFar = spentSoFarInMonth,
            remainingSpendableBudget = remainingSpendable,
            projectedSavings = projectedSavings,
            daysRemainingInMonth = remainingDays,
            totalDaysInMonth = totalDays,
            dayOfMonth = currentDay,
            isOnTrack = isOnTrack,
            statusMessage = statusMessage
        )
    }

    fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format("%,d", amount.toLong())
        } else {
            String.format("%,.2f", amount)
        }
    }

    fun formatExactDecimal(amount: Double): String {
        return String.format("%.2f", amount)
    }

    /**
     * Calculates consecutive days of meeting daily spending target (actualSpent <= dailyAllowance).
     */
    fun calculateStreak(records: List<com.example.data.model.DailySpendingRecord>, todayDate: LocalDate): Int {
        if (records.isEmpty()) return 0
        val recordMap = records.associateBy { it.date }
        var streak = 0
        var checkDate = todayDate

        // If today has a record:
        val todayStr = checkDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        val todayRecord = recordMap[todayStr]
        if (todayRecord != null) {
            if (todayRecord.actualSpent <= todayRecord.dailyAllowance) {
                streak++
            } else {
                return 0
            }
            checkDate = checkDate.minusDays(1)
        } else {
            // Check starting from yesterday
            checkDate = checkDate.minusDays(1)
        }

        while (true) {
            val dateStr = checkDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
            val record = recordMap[dateStr] ?: break
            if (record.actualSpent <= record.dailyAllowance) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    data class HealthScoreBreakdown(
        val score: Int, // 0..100
        val rating: String, // "Excellent", "Good", "Fair", "Needs Attention"
        val explanation: String
    )

    enum class SpendingMoodType {
        AHEAD_OF_GOAL,
        EXCELLENT,
        ON_TRACK,
        GETTING_CLOSE,
        OVER_BUDGET
    }

    data class SpendingMood(
        val type: SpendingMoodType,
        val emoji: String,
        val headline: String,
        val message: String,
        val badgeLabel: String,
        val ghanaianComment: String = headline,
        val allGhanaianComments: List<String> = listOf(headline),
        val spentPctOfAllowance: Float = 0f
    )

    // Universal, friendly reaction comment libraries
    val EXCELLENT_COMMENTS = listOf(
        "You're doing great!",
        "You're ahead of schedule.",
        "Great job staying under budget."
    )

    val ON_TRACK_COMMENTS = listOf(
        "You're on track.",
        "Keep it up.",
        "You're managing your spending well."
    )

    val NEAR_LIMIT_COMMENTS = listOf(
        "I think you should slow down.",
        "You're getting close to today's limit.",
        "Maybe save some for later."
    )

    val OVER_BUDGET_COMMENTS = listOf(
        "You've gone over today's limit.",
        "Let's slow down a little tomorrow.",
        "You'll need to adjust your spending."
    )

    val AHEAD_COMMENTS = listOf(
        "You're ahead of your savings goal!",
        "You're building a healthy buffer."
    )

    // Backward-compatibility aliases if referenced elsewhere
    val GHANAIAN_EXCELLENT_COMMENTS = EXCELLENT_COMMENTS
    val GHANAIAN_ON_TRACK_COMMENTS = ON_TRACK_COMMENTS
    val GHANAIAN_NEAR_LIMIT_COMMENTS = NEAR_LIMIT_COMMENTS
    val GHANAIAN_OVER_BUDGET_COMMENTS = OVER_BUDGET_COMMENTS
    val GHANAIAN_AHEAD_COMMENTS = AHEAD_COMMENTS

    /**
     * Evaluates the spending mood character taking into account both today's
     * spending pace and overall position in the salary cycle, equipped with
     * lively universal personality reactions.
     */
    fun evaluateSpendingMood(
        todayLeftToSpend: Double,
        todayAllowance: Double,
        todayActualSpent: Double,
        salaryCycleSummary: SalaryCycleSummary? = null,
        streakDays: Int = 0,
        currencySymbol: String = "GH₵",
        commentIndex: Int = 0
    ): SpendingMood {
        val spentRatio = if (todayAllowance > 0) (todayActualSpent / todayAllowance).toFloat() else 0f
        val isSignificantlyAhead = salaryCycleSummary != null && 
            salaryCycleSummary.projectedSavings >= (salaryCycleSummary.savingsGoal * 1.03) &&
            salaryCycleSummary.daysRemaining > 0

        // 1. Over budget today: 😟 "You've gone over today's limit."
        if (todayLeftToSpend < 0) {
            val overAmount = kotlin.math.abs(todayLeftToSpend)
            val comments = OVER_BUDGET_COMMENTS
            val selectedComment = comments[kotlin.math.abs(commentIndex) % comments.size]
            return SpendingMood(
                type = SpendingMoodType.OVER_BUDGET,
                emoji = "😟",
                headline = selectedComment,
                message = "You're $currencySymbol${formatExactDecimal(overAmount)} over today. We'll adjust tomorrow to protect your savings.",
                badgeLabel = "Over Budget",
                ghanaianComment = selectedComment,
                allGhanaianComments = comments,
                spentPctOfAllowance = spentRatio
            )
        }

        // 2. Significantly ahead of goal across cycle: 😎 "You're ahead of your savings goal!"
        if (isSignificantlyAhead && todayLeftToSpend >= 0 && spentRatio <= 0.65f) {
            val comments = AHEAD_COMMENTS
            val selectedComment = comments[kotlin.math.abs(commentIndex) % comments.size]
            return SpendingMood(
                type = SpendingMoodType.AHEAD_OF_GOAL,
                emoji = "😎",
                headline = selectedComment,
                message = "You're comfortably pacing ahead of your savings target of $currencySymbol${formatAmount(salaryCycleSummary?.savingsGoal ?: 0.0)}!",
                badgeLabel = "Ahead of Goal",
                ghanaianComment = selectedComment,
                allGhanaianComments = comments,
                spentPctOfAllowance = spentRatio
            )
        }

        // 3. Approaching daily limit (75% or more spent) -> Side-eye emoji 🙄 with primary "I think you should slow down."
        if (spentRatio >= 0.75f) {
            val comments = NEAR_LIMIT_COMMENTS
            val selectedComment = comments[kotlin.math.abs(commentIndex) % comments.size]
            return SpendingMood(
                type = SpendingMoodType.GETTING_CLOSE,
                emoji = "🙄",
                headline = selectedComment,
                message = "You have $currencySymbol${formatExactDecimal(todayLeftToSpend)} left today. Consider slowing down to stay on track.",
                badgeLabel = "Near Limit",
                ghanaianComment = selectedComment,
                allGhanaianComments = comments,
                spentPctOfAllowance = spentRatio
            )
        }

        // 4. Doing Very Well / Excellent (Under 45% spent or zero spent, or on a good streak): 😄 "You're doing great!"
        if (todayActualSpent == 0.0 || spentRatio <= 0.45f || streakDays >= 2) {
            val comments = EXCELLENT_COMMENTS
            val selectedComment = comments[kotlin.math.abs(commentIndex) % comments.size]
            return SpendingMood(
                type = SpendingMoodType.EXCELLENT,
                emoji = "😄",
                headline = selectedComment,
                message = if (todayActualSpent == 0.0) {
                    "Full $currencySymbol${formatExactDecimal(todayAllowance)} ready for today. Great job staying under budget!"
                } else {
                    "$currencySymbol${formatExactDecimal(todayLeftToSpend)} remaining from today's target. You're doing great!"
                },
                badgeLabel = "Doing Well",
                ghanaianComment = selectedComment,
                allGhanaianComments = comments,
                spentPctOfAllowance = spentRatio
            )
        }

        // 5. On Track (45% to 75% spent) -> 🙂 "You're on track."
        val comments = ON_TRACK_COMMENTS
        val selectedComment = comments[kotlin.math.abs(commentIndex) % comments.size]
        return SpendingMood(
            type = SpendingMoodType.ON_TRACK,
            emoji = "🙂",
            headline = selectedComment,
            message = "Right on track with $currencySymbol${formatExactDecimal(todayLeftToSpend)} left to spend today.",
            badgeLabel = "On Track",
            ghanaianComment = selectedComment,
            allGhanaianComments = comments,
            spentPctOfAllowance = spentRatio
        )
    }

    data class ContextualSuggestion(
        val iconEmoji: String,
        val title: String,
        val message: String,
        val isWin: Boolean = false
    )

    data class CategoryBreakdownItem(
        val category: com.example.data.model.ExpenseCategory,
        val amount: Double,
        val percentage: Float, // 0..100
        val count: Int
    )

    /**
     * Groups expenses by category and calculates percentages.
     */
    fun calculateCategoryBreakdown(expenses: List<com.example.data.model.ExpenseItem>): List<CategoryBreakdownItem> {
        if (expenses.isEmpty()) return emptyList()
        val total = expenses.sumOf { it.amount }
        if (total <= 0.0) return emptyList()

        val grouped = expenses.groupBy { it.getEffectiveCategory() }
        return grouped.map { (cat, items) ->
            val sum = items.sumOf { it.amount }
            val pct = ((sum / total) * 100.0).toFloat()
            CategoryBreakdownItem(
                category = cat,
                amount = sum,
                percentage = pct,
                count = items.size
            )
        }.sortedByDescending { it.amount }
    }

    /**
     * Generates a single, highly relevant contextual suggestion based on live spending data.
     */
    fun generateContextualSuggestion(
        todayLeftToSpend: Double,
        todayAllowance: Double,
        todayActualSpent: Double,
        todayExpenses: List<com.example.data.model.ExpenseItem>,
        yesterdaySavedDifference: Double? = null,
        currencySymbol: String = "GH₵"
    ): ContextualSuggestion {
        val breakdown = calculateCategoryBreakdown(todayExpenses)

        // Case 1: Over budget
        if (todayLeftToSpend < 0) {
            val overAmount = -todayLeftToSpend
            return ContextualSuggestion(
                iconEmoji = "⚠️",
                title = "Over daily limit",
                message = "You're $currencySymbol${formatAmount(overAmount)} over today's allowance. Tomorrow's allowance will automatically rebalance.",
                isWin = false
            )
        }

        // Case 2: No spending today yet, but yesterday had savings
        if (todayActualSpent == 0.0) {
            if (yesterdaySavedDifference != null && yesterdaySavedDifference > 0.0) {
                return ContextualSuggestion(
                    iconEmoji = "🎯",
                    title = "Yesterday's win",
                    message = "You spent $currencySymbol${formatAmount(yesterdaySavedDifference)} less than your daily allowance yesterday.",
                    isWin = true
                )
            }
            return ContextualSuggestion(
                iconEmoji = "✨",
                title = "Clean slate",
                message = "You have your full allowance of $currencySymbol${formatAmount(todayAllowance)} ready for today.",
                isWin = true
            )
        }

        // Case 3: Dominant expense category (> 45% of today's spending and at least 2 expenses logged)
        val dominant = breakdown.firstOrNull()
        if (dominant != null && dominant.percentage >= 45f && todayExpenses.size >= 2) {
            return ContextualSuggestion(
                iconEmoji = dominant.category.emoji,
                title = "${dominant.category.title} is your biggest expense today",
                message = "${dominant.category.title} accounts for $currencySymbol${formatAmount(dominant.amount)} (${dominant.percentage.toInt()}% of today's spending).",
                isWin = false
            )
        }

        // Case 4: Positive remaining with spending logged
        if (todayLeftToSpend > 0) {
            return ContextualSuggestion(
                iconEmoji = "💡",
                title = "Small win",
                message = "You have $currencySymbol${formatAmount(todayLeftToSpend)} left today. Staying under your limit puts you ahead of your plan.",
                isWin = true
            )
        }

        return ContextualSuggestion(
            iconEmoji = "🌱",
            title = "On track",
            message = "Keep logging each spend today to stay mindful of your daily allowance.",
            isWin = true
        )
    }

    /**
     * Calculates financial health score (0-100) based on savings progress, spending consistency, and daily discipline.
     */
    fun calculateHealthScore(
        monthlyIncome: Double,
        monthlySavingsGoal: Double,
        monthRecords: List<com.example.data.model.DailySpendingRecord>,
        progress: MonthProgressSummary?
    ): HealthScoreBreakdown {
        if (monthlyIncome <= 0) return HealthScoreBreakdown(50, "Neutral", "Configure your budget profile to calculate your score.")

        // 1. Savings target ratio factor (max 40 pts)
        val savingsTargetRatio = (monthlySavingsGoal / monthlyIncome).coerceIn(0.0, 0.5)
        val savingsPts = (savingsTargetRatio / 0.2 * 40).coerceIn(10.0, 40.0)

        // 2. Budget adherence so far (max 40 pts)
        val adherencePts = if (progress != null) {
            if (progress.isOnTrack) 40.0 else 15.0
        } else {
            30.0
        }

        // 3. Daily discipline consistency (max 20 pts)
        val disciplinePts = if (monthRecords.isNotEmpty()) {
            val underBudgetCount = monthRecords.count { it.actualSpent <= it.dailyAllowance }
            (underBudgetCount.toDouble() / monthRecords.size) * 20.0
        } else {
            15.0
        }

        val totalScore = (savingsPts + adherencePts + disciplinePts).toInt().coerceIn(0, 100)
        val rating = when {
            totalScore >= 80 -> "Excellent"
            totalScore >= 65 -> "Good"
            totalScore >= 50 -> "Fair"
            else -> "Needs Attention"
        }

        val explanation = when (rating) {
            "Excellent" -> "You're consistently staying within daily targets and on track with your GH₵${formatAmount(monthlySavingsGoal)} monthly savings goal."
            "Good" -> "Your spending is well-managed. Keep daily expenses within limits to maximize your end-of-month savings."
            "Fair" -> "A few days exceeded the daily allowance. Dynamic rebalancing helps you recover smoothly."
            else -> "Spending has exceeded planned targets this month. Focus on essential needs for remaining days to rebuild your savings."
        }

        return HealthScoreBreakdown(totalScore, rating, explanation)
    }

    // =========================================================================
    // INSIGHTS & FINANCIAL ANALYSIS MODELS & CALCULATIONS
    // =========================================================================

    enum class ProjectionStatus {
        ON_TRACK,
        SLIGHTLY_BEHIND,
        DIFFICULT
    }

    enum class DayBudgetStatus {
        UNDER,
        CLOSE,
        OVER
    }

    data class GoalProgressData(
        val monthlySavingsGoal: Double,
        val currentMonthSpent: Double,
        val monthlyIncome: Double,
        val monthlySpendable: Double,
        val savedAmountSoFar: Double,
        val remainingToGoal: Double,
        val progressPercent: Float, // 0..100
        val isGoalReached: Boolean,
        val explanation: String
    )

    data class GoalProjectionData(
        val status: ProjectionStatus,
        val headline: String,
        val message: String,
        val projectedCompletionDateFormatted: String? = null,
        val recommendedDailyAdjustment: Double? = null
    )

    data class CategoryComparisonItem(
        val category: com.example.data.model.ExpenseCategory,
        val currentAmount: Double,
        val previousAmount: Double,
        val difference: Double,
        val percentageChange: Float?, // null if previous was 0
        val isIncreased: Boolean
    )

    data class PeriodSpendingComparison(
        val currentPeriodTotal: Double,
        val previousPeriodTotal: Double,
        val totalDifference: Double,
        val totalPercentageChange: Float?,
        val isTotalDecreased: Boolean,
        val categoryComparisons: List<CategoryComparisonItem>
    )

    data class SpendingHabitInsight(
        val icon: String,
        val title: String,
        val description: String,
        val tag: String = "Insight"
    )

    data class FinancialAdviceItem(
        val iconEmoji: String,
        val title: String,
        val message: String,
        val actionTag: String? = null
    )

    data class DailyStatusItem(
        val date: LocalDate,
        val dayNumber: Int,
        val dayOfWeekLetter: String, // "M", "T", "W", etc.
        val actualSpent: Double,
        val allowance: Double,
        val status: DayBudgetStatus
    )

    data class DailyPerformanceData(
        val daysUnderBudgetCount: Int,
        val totalDaysEvaluated: Int,
        val percentageUnderBudget: Float,
        val dayStatuses: List<DailyStatusItem>
    )

    data class TrendPoint(
        val label: String, // e.g. "Monday" or "January"
        val shortLabel: String = label, // e.g. "Mon" or "Jan"
        val date: LocalDate,
        val actualSpent: Double,
        val allowance: Double,
        val hasRecordedSpending: Boolean
    )

    data class SpendingTrendData(
        val points: List<TrendPoint>,
        val plannedDailyAllowance: Double,
        val averageDailySpent: Double,
        val maxAmount: Double
    )

    data class MonthlyFinancialSummaryData(
        val monthlyIncome: Double,
        val plannedSavings: Double,
        val spentSoFar: Double,
        val remainingSpendingMoney: Double,
        val goalProgressPercent: Float
    )

    data class FinancialHealthOverviewData(
        val title: String,
        val message: String,
        val isOnTrack: Boolean
    )

    /**
     * Calculates Goal Progress for the given period.
     */
    fun calculateGoalProgress(
        monthlyIncome: Double,
        monthlySavingsGoal: Double,
        spentInPeriod: Double,
        currencySymbol: String = "GH₵"
    ): GoalProgressData {
        val totalSpendable = calculateMonthlySpendable(monthlyIncome, monthlySavingsGoal)
        val remainingSpendable = max(0.0, totalSpendable - spentInPeriod)
        // Current saved amount is income minus what has been spent
        val savedAmountSoFar = max(0.0, monthlyIncome - spentInPeriod)
        
        val progressPercent = if (monthlySavingsGoal > 0.0) {
            ((savedAmountSoFar / monthlySavingsGoal) * 100.0).toFloat().coerceIn(0f, 100f)
        } else {
            100f
        }

        val remainingToGoal = max(0.0, monthlySavingsGoal - savedAmountSoFar)
        val isGoalReached = remainingToGoal <= 0.0 && savedAmountSoFar >= monthlySavingsGoal

        val explanation = when {
            isGoalReached -> "🎉 Goal reached! You've protected your $currencySymbol${formatAmount(monthlySavingsGoal)} target."
            remainingToGoal > 0.0 -> "$currencySymbol${formatAmount(remainingToGoal)} left to reach your goal"
            else -> "You're on track to reach your $currencySymbol${formatAmount(monthlySavingsGoal)} goal."
        }

        return GoalProgressData(
            monthlySavingsGoal = monthlySavingsGoal,
            currentMonthSpent = spentInPeriod,
            monthlyIncome = monthlyIncome,
            monthlySpendable = totalSpendable,
            savedAmountSoFar = savedAmountSoFar,
            remainingToGoal = remainingToGoal,
            progressPercent = progressPercent,
            isGoalReached = isGoalReached,
            explanation = explanation
        )
    }

    /**
     * Calculates mathematically sound Goal Projection based on actual daily burn rate and days left.
     */
    fun calculateGoalProjection(
        monthlyIncome: Double,
        monthlySavingsGoal: Double,
        spentSoFar: Double,
        yearMonth: YearMonth,
        currentDate: LocalDate,
        currencySymbol: String = "GH₵"
    ): GoalProjectionData {
        val totalDays = yearMonth.lengthOfMonth()
        val currentDay = if (currentDate.year == yearMonth.year && currentDate.month == yearMonth.month) {
            currentDate.dayOfMonth
        } else {
            totalDays
        }
        val daysElapsed = max(1, currentDay)
        val daysRemaining = max(0, totalDays - currentDay)

        val totalSpendable = calculateMonthlySpendable(monthlyIncome, monthlySavingsGoal)
        val averageDailySpent = spentSoFar / daysElapsed

        // If no expenses yet or at the very beginning of the month
        if (spentSoFar <= 0.0) {
            return GoalProjectionData(
                status = ProjectionStatus.ON_TRACK,
                headline = "🟢 You're on track",
                message = "At your planned saving pace, you're on track to secure your $currencySymbol${formatAmount(monthlySavingsGoal)} goal this month."
            )
        }

        val projectedTotalSpend = spentSoFar + (averageDailySpent * daysRemaining)
        val projectedSavings = max(0.0, monthlyIncome - projectedTotalSpend)

        return if (projectedSavings >= monthlySavingsGoal) {
            // Projected date when goal is safely secured
            val completionDay = if (averageDailySpent > 0) {
                val allowedSpend = totalSpendable
                val remainingSpendAllowed = max(0.0, allowedSpend - spentSoFar)
                val daysToExhaustSpendable = (remainingSpendAllowed / averageDailySpent).toInt()
                (currentDay + daysToExhaustSpendable).coerceIn(1, totalDays)
            } else {
                totalDays
            }
            val projectedDate = yearMonth.atDay(completionDay).format(java.time.format.DateTimeFormatter.ofPattern("MMMM d"))

            GoalProjectionData(
                status = ProjectionStatus.ON_TRACK,
                headline = "🟢 You're on track",
                message = "At your current saving rate, you're projected to reach your $currencySymbol${formatAmount(monthlySavingsGoal)} goal by $projectedDate.",
                projectedCompletionDateFormatted = projectedDate
            )
        } else if (projectedSavings >= monthlySavingsGoal * 0.75) {
            val deficit = monthlySavingsGoal - projectedSavings
            val dailyAdjustment = if (daysRemaining > 0) deficit / daysRemaining else deficit

            GoalProjectionData(
                status = ProjectionStatus.SLIGHTLY_BEHIND,
                headline = "🟠 You're slightly behind",
                message = "You need to save approximately $currencySymbol${formatAmount(dailyAdjustment)} more per day to reach your goal.",
                recommendedDailyAdjustment = dailyAdjustment
            )
        } else {
            val deficit = monthlySavingsGoal - projectedSavings
            val dailyReductionNeeded = if (daysRemaining > 0) deficit / daysRemaining else deficit

            GoalProjectionData(
                status = ProjectionStatus.DIFFICULT,
                headline = "🔴 Current spending makes the goal difficult to reach",
                message = "Reducing daily spending by approximately $currencySymbol${formatAmount(dailyReductionNeeded)} could put you back on track.",
                recommendedDailyAdjustment = dailyReductionNeeded
            )
        }
    }

    /**
     * Calculates spending trends for Week (last 7 days) and Month (all days).
     */
    fun calculateSpendingTrendData(
        records: List<com.example.data.model.DailySpendingRecord>,
        expenses: List<com.example.data.model.ExpenseItem>,
        yearMonth: YearMonth,
        isWeekView: Boolean,
        referenceDate: LocalDate,
        baselineDailyAllowance: Double
    ): SpendingTrendData {
        val expenseSumByDate = expenses.groupBy { it.date }.mapValues { (_, items) -> items.sumOf { it.amount } }
        val recordMap = records.associateBy { it.date }

        val points = mutableListOf<TrendPoint>()

        if (isWeekView) {
            // Last 7 days ending at referenceDate or current week Monday..Sunday
            val startOfWeek = referenceDate.minusDays(referenceDate.dayOfWeek.value.toLong() - 1)
            for (i in 0 until 7) {
                val d = startOfWeek.plusDays(i.toLong())
                val dStr = d.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                val spent = expenseSumByDate[dStr] ?: recordMap[dStr]?.actualSpent ?: 0.0
                val hasRecorded = expenseSumByDate.containsKey(dStr) || recordMap.containsKey(dStr)
                val allowance = recordMap[dStr]?.dailyAllowance ?: baselineDailyAllowance
                val shortLabel = d.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH)
                val fullLabel = d.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
                points.add(TrendPoint(label = fullLabel, shortLabel = shortLabel, date = d, actualSpent = spent, allowance = allowance, hasRecordedSpending = hasRecorded))
            }
        } else {
            // Month view: 12 months of the year (January..December)
            val year = yearMonth.year
            for (m in 1..12) {
                val ym = YearMonth.of(year, m)
                val monthPrefix = String.format("%04d-%02d", year, m)
                val monthExpenses = expenses.filter { it.date.startsWith(monthPrefix) }.sumOf { it.amount }
                val monthRecords = records.filter { it.date.startsWith(monthPrefix) }
                val hasRecorded = monthExpenses > 0 || monthRecords.any { it.actualSpent > 0 }
                val allowance = if (monthRecords.isNotEmpty()) monthRecords.sumOf { it.dailyAllowance } else baselineDailyAllowance * ym.lengthOfMonth()
                val fullMonth = ym.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
                val shortMonth = ym.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH)
                points.add(TrendPoint(label = fullMonth, shortLabel = shortMonth, date = ym.atDay(1), actualSpent = monthExpenses, allowance = allowance, hasRecordedSpending = hasRecorded))
            }
        }

        val maxSpent = points.maxOfOrNull { it.actualSpent } ?: 0.0
        val maxAllowance = points.maxOfOrNull { it.allowance } ?: 0.0
        val maxAmount = max(1.0, max(maxSpent, maxAllowance) * 1.15)
        val nonZeroPoints = points.filter { it.hasRecordedSpending }
        val avgSpent = if (nonZeroPoints.isNotEmpty()) nonZeroPoints.sumOf { it.actualSpent } / nonZeroPoints.size else 0.0

        return SpendingTrendData(
            points = points,
            plannedDailyAllowance = baselineDailyAllowance,
            averageDailySpent = avgSpent,
            maxAmount = maxAmount
        )
    }

    /**
     * Compares current period with previous period.
     */
    fun calculatePeriodComparison(
        currentExpenses: List<com.example.data.model.ExpenseItem>,
        previousExpenses: List<com.example.data.model.ExpenseItem>
    ): PeriodSpendingComparison {
        val currentTotal = currentExpenses.sumOf { it.amount }
        val previousTotal = previousExpenses.sumOf { it.amount }
        val totalDiff = currentTotal - previousTotal
        val totalPctChange = if (previousTotal > 0.0) {
            (((currentTotal - previousTotal) / previousTotal) * 100.0).toFloat()
        } else null

        val currentGrouped = currentExpenses.groupBy { it.getEffectiveCategory() }
        val prevGrouped = previousExpenses.groupBy { it.getEffectiveCategory() }

        val allCategories = (currentGrouped.keys + prevGrouped.keys).distinct()
        val catComparisons = allCategories.map { cat ->
            val curAmt = currentGrouped[cat]?.sumOf { it.amount } ?: 0.0
            val prevAmt = prevGrouped[cat]?.sumOf { it.amount } ?: 0.0
            val diff = curAmt - prevAmt
            val pct = if (prevAmt > 0.0) {
                (((curAmt - prevAmt) / prevAmt) * 100.0).toFloat()
            } else null
            CategoryComparisonItem(
                category = cat,
                currentAmount = curAmt,
                previousAmount = prevAmt,
                difference = diff,
                percentageChange = pct,
                isIncreased = curAmt > prevAmt
            )
        }.sortedByDescending { it.currentAmount }

        return PeriodSpendingComparison(
            currentPeriodTotal = currentTotal,
            previousPeriodTotal = previousTotal,
            totalDifference = totalDiff,
            totalPercentageChange = totalPctChange,
            isTotalDecreased = currentTotal <= previousTotal,
            categoryComparisons = catComparisons
        )
    }

    /**
     * Generates real data-backed Spending Habits.
     */
    fun analyzeSpendingHabits(
        expenses: List<com.example.data.model.ExpenseItem>,
        comparison: PeriodSpendingComparison?,
        currencySymbol: String = "GH₵"
    ): List<SpendingHabitInsight> {
        val habits = mutableListOf<SpendingHabitInsight>()
        if (expenses.size < 2) return emptyList()

        val breakdown = calculateCategoryBreakdown(expenses)

        // 1. Largest spending category
        val topCategory = breakdown.firstOrNull()
        if (topCategory != null && topCategory.amount > 0.0) {
            habits.add(
                SpendingHabitInsight(
                    icon = topCategory.category.emoji,
                    title = topCategory.category.title,
                    description = "${topCategory.category.title} is your largest spending category this month ($currencySymbol${formatAmount(topCategory.amount)}, ${topCategory.percentage.toInt()}%).",
                    tag = "Top Category"
                )
            )
        }

        // 2. Month-over-month category trend (if previous data exists)
        if (comparison != null && comparison.categoryComparisons.isNotEmpty()) {
            val biggestIncrease = comparison.categoryComparisons
                .filter { it.percentageChange != null && it.percentageChange > 5f && it.previousAmount > 0.0 }
                .maxByOrNull { it.percentageChange ?: 0f }

            if (biggestIncrease != null && biggestIncrease.percentageChange != null) {
                habits.add(
                    SpendingHabitInsight(
                        icon = biggestIncrease.category.emoji,
                        title = biggestIncrease.category.title,
                        description = "You spent ${biggestIncrease.percentageChange.toInt()}% more on ${biggestIncrease.category.title.lowercase()} this month than last month.",
                        tag = "Trend"
                    )
                )
            }
        }

        // 3. Weekend vs Weekday analysis
        val weekendExpenses = expenses.filter {
            val d = try { LocalDate.parse(it.date) } catch (e: Exception) { null }
            d != null && (d.dayOfWeek == java.time.DayOfWeek.SATURDAY || d.dayOfWeek == java.time.DayOfWeek.SUNDAY)
        }
        val weekdayExpenses = expenses.filter {
            val d = try { LocalDate.parse(it.date) } catch (e: Exception) { null }
            d != null && d.dayOfWeek != java.time.DayOfWeek.SATURDAY && d.dayOfWeek != java.time.DayOfWeek.SUNDAY
        }

        if (weekendExpenses.isNotEmpty() && weekdayExpenses.isNotEmpty()) {
            val weekendTotal = weekendExpenses.sumOf { it.amount }
            val weekdayTotal = weekdayExpenses.sumOf { it.amount }
            val weekendAvg = weekendTotal / weekendExpenses.size
            val weekdayAvg = weekdayTotal / weekdayExpenses.size

            if (weekendAvg > weekdayAvg * 1.25) {
                habits.add(
                    SpendingHabitInsight(
                        icon = "📅",
                        title = "Weekend spending",
                        description = "You tend to spend more on weekends ($currencySymbol${formatAmount(weekendAvg)} avg vs $currencySymbol${formatAmount(weekdayAvg)} weekday).",
                        tag = "Timing"
                    )
                )
            }
        }

        // 4. Evening spending analysis (after 6 PM)
        val eveningExpenses = expenses.filter {
            val time = try { java.time.LocalTime.parse(it.timeFormatted) } catch (e: Exception) { null }
            time != null && time.hour >= 18
        }
        if (expenses.size >= 4 && eveningExpenses.size.toFloat() / expenses.size >= 0.5f) {
            val eveningPct = ((eveningExpenses.size.toFloat() / expenses.size) * 100).toInt()
            habits.add(
                SpendingHabitInsight(
                    icon = "🌙",
                    title = "Evening spending",
                    description = "Most of your recorded expenses ($eveningPct%) happen after 6 PM.",
                    tag = "Habit"
                )
            )
        }

        return habits
    }

    /**
     * Generates actionable, data-backed Financial Suggestions.
     */
    fun generateFinancialAdvice(
        expenses: List<com.example.data.model.ExpenseItem>,
        records: List<com.example.data.model.DailySpendingRecord>,
        goalProgress: GoalProgressData,
        currencySymbol: String = "GH₵"
    ): List<FinancialAdviceItem> {
        val suggestions = mutableListOf<FinancialAdviceItem>()
        if (expenses.isEmpty()) return emptyList()

        val breakdown = calculateCategoryBreakdown(expenses)

        // 1. High category proportion advice
        val highSpendCat = breakdown.firstOrNull { it.percentage >= 20f }
        if (highSpendCat != null && goalProgress.remainingToGoal > 0.0) {
            val suggestedDailyCut = 5.0
            suggestions.add(
                FinancialAdviceItem(
                    iconEmoji = "💡",
                    title = "You could improve your savings",
                    message = "${highSpendCat.category.title} accounts for ${highSpendCat.percentage.toInt()}% of your spending this month. Reducing ${highSpendCat.category.title.lowercase()} spending by $currencySymbol${formatAmount(suggestedDailyCut)} per day could accelerate your savings.",
                    actionTag = "Saving Tip"
                )
            )
        }

        // 2. Discipline positive reinforcement
        if (records.size >= 5) {
            val underBudgetCount = records.count { it.actualSpent <= it.dailyAllowance }
            if (underBudgetCount >= records.size * 0.7) {
                suggestions.add(
                    FinancialAdviceItem(
                        iconEmoji = "🎯",
                        title = "You're doing well",
                        message = "You've stayed below your daily spending target for $underBudgetCount of the last ${records.size} days.",
                        actionTag = "Consistency"
                    )
                )
            }
        }

        // 3. Weekend caution advice
        val weekendExpenses = expenses.filter {
            val d = try { LocalDate.parse(it.date) } catch (e: Exception) { null }
            d != null && (d.dayOfWeek == java.time.DayOfWeek.SATURDAY || d.dayOfWeek == java.time.DayOfWeek.SUNDAY)
        }
        val weekdayExpenses = expenses.filter {
            val d = try { LocalDate.parse(it.date) } catch (e: Exception) { null }
            d != null && d.dayOfWeek != java.time.DayOfWeek.SATURDAY && d.dayOfWeek != java.time.DayOfWeek.SUNDAY
        }
        if (weekendExpenses.size >= 2 && weekdayExpenses.size >= 2) {
            val weekendAvg = weekendExpenses.sumOf { it.amount } / weekendExpenses.size
            val weekdayAvg = weekdayExpenses.sumOf { it.amount } / weekdayExpenses.size
            val diff = weekendAvg - weekdayAvg
            if (diff >= 10.0) {
                suggestions.add(
                    FinancialAdviceItem(
                        iconEmoji = "💡",
                        title = "Watch your weekends",
                        message = "Your average weekend spending is $currencySymbol${formatAmount(diff)} higher than your weekday average.",
                        actionTag = "Awareness"
                    )
                )
            }
        }

        return suggestions
    }

    /**
     * Calculates Daily Budget Performance (Days under budget and day-by-day indicators).
     */
    fun calculateDailyPerformance(
        records: List<com.example.data.model.DailySpendingRecord>,
        expenses: List<com.example.data.model.ExpenseItem>,
        yearMonth: YearMonth
    ): DailyPerformanceData {
        val expenseSumByDate = expenses.groupBy { it.date }.mapValues { (_, items) -> items.sumOf { it.amount } }
        val recordMap = records.associateBy { it.date }

        val activeDates = (expenseSumByDate.keys + recordMap.keys)
            .filter { it.startsWith(yearMonth.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))) }
            .distinct()
            .sorted()

        val statuses = mutableListOf<DailyStatusItem>()
        var underCount = 0

        for (dateStr in activeDates) {
            val date = try { LocalDate.parse(dateStr) } catch (e: Exception) { continue }
            val spent = expenseSumByDate[dateStr] ?: recordMap[dateStr]?.actualSpent ?: 0.0
            val allowance = recordMap[dateStr]?.dailyAllowance ?: 0.0

            val status = when {
                spent <= allowance -> {
                    underCount++
                    DayBudgetStatus.UNDER
                }
                spent <= allowance * 1.10 -> DayBudgetStatus.CLOSE
                else -> DayBudgetStatus.OVER
            }

            val dayLetter = date.dayOfWeek.name.take(1) // "M", "T", "W", "T", "F", "S", "S"

            statuses.add(
                DailyStatusItem(
                    date = date,
                    dayNumber = date.dayOfMonth,
                    dayOfWeekLetter = dayLetter,
                    actualSpent = spent,
                    allowance = allowance,
                    status = status
                )
            )
        }

        val totalEvaluated = statuses.size
        val pct = if (totalEvaluated > 0) (underCount.toFloat() / totalEvaluated) * 100f else 0f

        return DailyPerformanceData(
            daysUnderBudgetCount = underCount,
            totalDaysEvaluated = totalEvaluated,
            percentageUnderBudget = pct,
            dayStatuses = statuses
        )
    }

    /**
     * Generates plain-language Financial Health Overview.
     */
    fun calculateFinancialHealthOverview(
        monthlyIncome: Double,
        monthlySavingsGoal: Double,
        spentInPeriod: Double,
        records: List<com.example.data.model.DailySpendingRecord>
    ): FinancialHealthOverviewData {
        val totalSpendable = calculateMonthlySpendable(monthlyIncome, monthlySavingsGoal)
        val isOnTrack = spentInPeriod <= totalSpendable

        return if (isOnTrack) {
            FinancialHealthOverviewData(
                title = "You're doing well 👍",
                message = "You're currently spending below your planned monthly rate and are on track to reach your savings goal.",
                isOnTrack = true
            )
        } else {
            FinancialHealthOverviewData(
                title = "Watch your spending ⚠️",
                message = "Your spending is currently above the rate needed to reach your savings goal. Moderating daily expenses will help restore balance.",
                isOnTrack = false
            )
        }
    }
}
