package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity(tableName = "budget_profile")
data class BudgetProfile(
    @PrimaryKey val id: Int = 1,
    val userName: String = "Friend",
    val monthlyIncome: Double, // Salary Amount
    val monthlySavingsGoal: Double, // Target savings from this salary
    val salaryReceivedDate: String = "", // e.g. "2026-08-25"
    val nextSalaryDate: String = "", // e.g. "2026-09-25"
    val dailyFoodExpense: Double = 0.0, // Expected daily food estimate
    val dailyTransportExpense: Double = 0.0, // Expected daily transport estimate
    val currencySymbol: String = "GH₵",
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getSalaryReceivedLocalDate(): LocalDate {
        return try {
            if (salaryReceivedDate.isNotBlank()) LocalDate.parse(salaryReceivedDate) else LocalDate.now()
        } catch (_: Exception) {
            LocalDate.now()
        }
    }

    fun getNextSalaryLocalDate(): LocalDate {
        return try {
            if (nextSalaryDate.isNotBlank()) LocalDate.parse(nextSalaryDate) else LocalDate.now().plusMonths(1)
        } catch (_: Exception) {
            LocalDate.now().plusMonths(1)
        }
    }

    /**
     * Total available spending money = Salary - Savings Goal
     */
    val availableSpendingMoney: Double
        get() = kotlin.math.max(0.0, monthlyIncome - monthlySavingsGoal)
}


