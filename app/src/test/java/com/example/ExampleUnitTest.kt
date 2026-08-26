package com.example

import com.example.data.SpendingCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {

  @Test
  fun testMonthlySpendableCalculation() {
    val spendable = SpendingCalculator.calculateMonthlySpendable(
      monthlyIncome = 2100.0,
      monthlySavingsGoal = 600.0
    )
    assertEquals(1500.0, spendable, 0.001)
  }

  @Test
  fun testInitialDailyAllowanceIn30DayMonth() {
    val allowance = SpendingCalculator.calculateInitialDailyAllowance(
      monthlyIncome = 2100.0,
      monthlySavingsGoal = 600.0,
      totalDaysInMonth = 30
    )
    assertEquals(50.0, allowance, 0.001)
  }

  @Test
  fun testDailyBreakdown() {
    val breakdown = SpendingCalculator.calculateDailyBreakdown(
      dailyAllowance = 50.0,
      dailyFoodExpense = 20.0,
      dailyTransportExpense = 15.0
    )
    assertEquals(20.0, breakdown.food, 0.001)
    assertEquals(15.0, breakdown.transport, 0.001)
    assertEquals(15.0, breakdown.other, 0.001)
    assertEquals(50.0, breakdown.totalAllowance, 0.001)
  }

  @Test
  fun testDynamicDailyAllowance_UnderSpending() {
    // Day 1: Allowed 50, Spent 38 (Saved 12)
    // Money available initially: 1500
    // Remaining spendable: 1500 - 38 = 1462
    // Remaining days in 30-day month: 29 days
    // Day 2 new allowance: 1462 / 29 = 50.4137...
    val day2Allowance = SpendingCalculator.calculateDynamicDailyAllowance(
      monthlyIncome = 2100.0,
      monthlySavingsGoal = 600.0,
      pastSpentInMonth = 38.0,
      remainingDaysIncludingToday = 29
    )
    assertEquals(1462.0 / 29.0, day2Allowance, 0.001)
    assertTrue(day2Allowance > 50.0)
  }

  @Test
  fun testDynamicDailyAllowance_OverSpending() {
    // Day 1: Allowed 50, Spent 65 (Over by 15)
    // Remaining spendable: 1500 - 65 = 1435
    // Remaining days: 29 days
    // Day 2 new allowance: 1435 / 29 = 49.4827...
    val day2Allowance = SpendingCalculator.calculateDynamicDailyAllowance(
      monthlyIncome = 2100.0,
      monthlySavingsGoal = 600.0,
      pastSpentInMonth = 65.0,
      remainingDaysIncludingToday = 29
    )
    assertEquals(1435.0 / 29.0, day2Allowance, 0.001)
    assertTrue(day2Allowance < 50.0)
  }

  @Test
  fun testMonthProgressCalculation() {
    val date = LocalDate.of(2026, 8, 23)
    val progress = SpendingCalculator.calculateMonthProgress(
      monthlyIncome = 2100.0,
      monthlySavingsGoal = 600.0,
      spentSoFarInMonth = 38.0,
      date = date,
      hasTodayRecord = true,
      todayAllowance = 50.0
    )
    assertEquals(2100.0, progress.monthlyIncome, 0.001)
    assertEquals(600.0, progress.monthlySavingsGoal, 0.001)
    assertEquals(38.0, progress.totalSpentSoFar, 0.001)
    assertEquals(31, progress.totalDaysInMonth)
    assertTrue(progress.isOnTrack)
  }

  @Test
  fun testSalaryCycleDynamicAllowanceCalculation() {
    val receivedDate = LocalDate.of(2026, 8, 25)
    val nextDate = LocalDate.of(2026, 9, 25) // 31 days total
    val today = LocalDate.of(2026, 8, 25) // day 1: 31 days remaining

    // Salary: 3000, Goal: 600 -> Spendable: 2400
    // Daily allowance = 2400 / 31 = 77.419...
    val allowance = SpendingCalculator.calculateDynamicSalaryCycleAllowance(
      salaryAmount = 3000.0,
      savingsGoal = 600.0,
      pastSpentInCycleBeforeToday = 0.0,
      remainingDaysIncludingToday = 31
    )
    assertEquals(2400.0 / 31.0, allowance, 0.001)

    // Summary test
    val summary = SpendingCalculator.calculateSalaryCycleSummary(
      salaryAmount = 3000.0,
      savingsGoal = 600.0,
      salaryReceivedDate = receivedDate,
      nextSalaryDate = nextDate,
      todayDate = today,
      cycleExpenses = emptyList(),
      todayAllowance = allowance
    )
    assertEquals(31, summary.totalCycleDays)
    assertEquals(31, summary.daysRemaining)
    assertEquals(2400.0, summary.remainingSpendingMoney, 0.001)
    assertTrue(summary.isOnTrack)
  }

  @Test
  fun testSpendingMoodDynamicExpressions() {
    // 1. Excellent (0 spent)
    val moodExcellent = SpendingCalculator.evaluateSpendingMood(
      todayLeftToSpend = 50.0,
      todayAllowance = 50.0,
      todayActualSpent = 0.0,
      salaryCycleSummary = null,
      currencySymbol = "GH₵"
    )
    assertEquals(SpendingCalculator.SpendingMoodType.EXCELLENT, moodExcellent.type)
    assertEquals("😄", moodExcellent.emoji)
    assertTrue(moodExcellent.headline.contains("great", ignoreCase = true))

    // 2. On Track (moderate spending)
    val moodOnTrack = SpendingCalculator.evaluateSpendingMood(
      todayLeftToSpend = 20.0,
      todayAllowance = 50.0,
      todayActualSpent = 30.0, // 60% spent
      salaryCycleSummary = null,
      currencySymbol = "GH₵"
    )
    assertEquals(SpendingCalculator.SpendingMoodType.ON_TRACK, moodOnTrack.type)
    assertEquals("🙂", moodOnTrack.emoji)
    assertTrue(moodOnTrack.headline.contains("track", ignoreCase = true))

    // 3. Getting Close (approaching limit >= 75%)
    val moodGettingClose = SpendingCalculator.evaluateSpendingMood(
      todayLeftToSpend = 8.0,
      todayAllowance = 50.0,
      todayActualSpent = 42.0, // 84% spent
      salaryCycleSummary = null,
      currencySymbol = "GH₵"
    )
    assertEquals(SpendingCalculator.SpendingMoodType.GETTING_CLOSE, moodGettingClose.type)
    assertEquals("🙄", moodGettingClose.emoji)
    assertTrue(moodGettingClose.headline.contains("slow down", ignoreCase = true) || moodGettingClose.headline.contains("limit", ignoreCase = true))

    // 4. Over Budget (exceeded limit)
    val moodOverBudget = SpendingCalculator.evaluateSpendingMood(
      todayLeftToSpend = -15.0,
      todayAllowance = 50.0,
      todayActualSpent = 65.0,
      salaryCycleSummary = null,
      currencySymbol = "GH₵"
    )
    assertEquals(SpendingCalculator.SpendingMoodType.OVER_BUDGET, moodOverBudget.type)
    assertEquals("😟", moodOverBudget.emoji)
    assertTrue(moodOverBudget.headline.contains("over", ignoreCase = true))

    // 5. Ahead of Goal (cycle is ahead)
    val summaryAhead = SpendingCalculator.calculateSalaryCycleSummary(
      salaryAmount = 3000.0,
      savingsGoal = 500.0,
      salaryReceivedDate = LocalDate.of(2026, 8, 25),
      nextSalaryDate = LocalDate.of(2026, 9, 25),
      todayDate = LocalDate.of(2026, 8, 25),
      cycleExpenses = emptyList(),
      todayAllowance = 80.0
    )
    val moodAhead = SpendingCalculator.evaluateSpendingMood(
      todayLeftToSpend = 70.0,
      todayAllowance = 80.0,
      todayActualSpent = 10.0,
      salaryCycleSummary = summaryAhead,
      currencySymbol = "GH₵"
    )
    assertEquals(SpendingCalculator.SpendingMoodType.AHEAD_OF_GOAL, moodAhead.type)
    assertEquals("😎", moodAhead.emoji)
  }
}

