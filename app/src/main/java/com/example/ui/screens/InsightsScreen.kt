package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SpendingCalculator
import com.example.data.model.ExpenseCategory
import com.example.ui.InsightsPeriodType
import com.example.ui.SpendTrackerUiState
import com.example.ui.components.DailyPerformanceSection
import com.example.ui.components.FinancialHealthOverviewBanner
import com.example.ui.components.FinancialSuggestionsSection
import com.example.ui.components.GoalProgressCard
import com.example.ui.components.InsightsEmptyState
import com.example.ui.components.InsightsPeriodSelector
import com.example.ui.components.MonthlySummaryGrid
import com.example.ui.components.PeriodComparisonSection
import com.example.ui.components.SpendingBreakdownSection
import com.example.ui.components.SpendingHabitsSection
import com.example.ui.components.SpendingTrendsSection
import com.example.ui.theme.BentoDeepPurple
import com.example.ui.theme.BentoLavenderCard
import com.example.ui.theme.BentoOnSurfaceVariantLight
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    uiState: SpendTrackerUiState,
    onSelectPeriodType: (InsightsPeriodType, YearMonth?) -> Unit,
    onToggleTrendView: (Boolean) -> Unit,
    onSelectCategory: (ExpenseCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencySymbol = uiState.budgetProfile?.currencySymbol ?: "GH₵"
    val formattedPeriodTitle = uiState.insightsSelectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Insights",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("insights_title_text")
                        )
                        Text(
                            text = formattedPeriodTitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple,
                            modifier = Modifier.testTag("insights_period_subtitle")
                        )
                    }
                },
                actions = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BentoLavenderCard)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoGraph,
                            contentDescription = "Insights Analysis",
                            tint = BentoDeepPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("insights_scroll_column"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Period Selector Segmented Switcher
            item {
                InsightsPeriodSelector(
                    periodType = uiState.insightsPeriodType,
                    selectedMonth = uiState.insightsSelectedMonth,
                    availableMonths = uiState.availableMonths,
                    onSelectPeriod = onSelectPeriodType,
                    modifier = Modifier.testTag("insights_period_selector")
                )
            }

            if (!uiState.insightsHasSufficientData) {
                // Section 16: Graceful Empty State
                item {
                    InsightsEmptyState()
                }
            } else {
                // Section: Expected Weekly Expenditure
                item {
                    WeeklyExpenditureCard(
                        expectedWeeklyExpenditure = uiState.expectedWeeklyExpenditure,
                        weeklySpent = uiState.weeklySpent,
                        weeklyRemaining = uiState.weeklyRemaining,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.testTag("insights_weekly_expenditure_card")
                    )
                }

                // Section: Daily Budget Expectations (Food & Transport)
                uiState.dailyBreakdown?.let { breakdown ->
                    if (breakdown.food > 0 || breakdown.transport > 0) {
                        item {
                            DailyBreakdownCard(
                                breakdown = breakdown,
                                dailyAllowance = uiState.todayAllowance,
                                currencySymbol = currencySymbol,
                                modifier = Modifier.testTag("insights_daily_breakdown_card")
                            )
                        }
                    }
                }

                // Section 12: Financial Health Overview
                item {
                    FinancialHealthOverviewBanner(
                        health = uiState.insightsHealthOverview,
                        modifier = Modifier.testTag("insights_health_banner")
                    )
                }

                // Section 3, 4, 13: Goal Progress & Projections
                if (uiState.insightsGoalProgress != null) {
                    item {
                        GoalProgressCard(
                            goalProgress = uiState.insightsGoalProgress,
                            goalProjection = uiState.insightsGoalProjection,
                            currencySymbol = currencySymbol
                        )
                    }
                }

                // Section 5: Spending Breakdown Donut Chart
                item {
                    SpendingBreakdownSection(
                        breakdown = uiState.insightsCategoryBreakdown,
                        totalSpent = uiState.insightsGoalProgress?.currentMonthSpent ?: 0.0,
                        selectedCategory = uiState.insightsSelectedCategory,
                        onSelectCategory = onSelectCategory,
                        currencySymbol = currencySymbol
                    )
                }

                // Section 6: Spending Trends Graph (Week vs Month)
                item {
                    SpendingTrendsSection(
                        trendData = uiState.insightsTrendData,
                        isWeekView = uiState.insightsIsWeekTrend,
                        onToggleTrendView = onToggleTrendView,
                        currencySymbol = currencySymbol
                    )
                }

                // Section 7: Your Spending Habits
                if (uiState.insightsHabits.isNotEmpty()) {
                    item {
                        SpendingHabitsSection(
                            habits = uiState.insightsHabits,
                            modifier = Modifier.testTag("insights_habits_section")
                        )
                    }
                }

                // Section 8: Compare With Previous Period
                if (uiState.insightsComparison != null && uiState.insightsComparison.previousPeriodTotal > 0.0) {
                    item {
                        PeriodComparisonSection(
                            comparison = uiState.insightsComparison,
                            currencySymbol = currencySymbol,
                            modifier = Modifier.testTag("insights_comparison_section")
                        )
                    }
                }

                // Section 9: Financial Advice / Suggestions
                if (uiState.insightsAdvice.isNotEmpty()) {
                    item {
                        FinancialSuggestionsSection(
                            suggestions = uiState.insightsAdvice,
                            modifier = Modifier.testTag("insights_suggestions_section")
                        )
                    }
                }

                // Section 10: Daily Budget Performance
                if (uiState.insightsPerformance != null) {
                    item {
                        DailyPerformanceSection(
                            performance = uiState.insightsPerformance,
                            currencySymbol = currencySymbol,
                            modifier = Modifier.testTag("insights_daily_performance_section")
                        )
                    }
                }

                // Section 11: Monthly Financial Summary Grid
                if (uiState.insightsSummary != null) {
                    item {
                        MonthlySummaryGrid(
                            summary = uiState.insightsSummary,
                            currencySymbol = currencySymbol,
                            modifier = Modifier.testTag("insights_monthly_summary_grid")
                        )
                    }
                }

                // Section 12: Expense History for Period
                if (uiState.insightsExpenses.isNotEmpty()) {
                    item {
                        ExpenseHistoryListSection(
                            expenses = uiState.insightsExpenses,
                            currencySymbol = currencySymbol,
                            modifier = Modifier.testTag("insights_expense_history_section")
                        )
                    }
                }
            }

            // Bottom Spacing for smooth scroll above navigation bar
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Section displaying historical expenses grouped by date
 */
@Composable
fun ExpenseHistoryListSection(
    expenses: List<com.example.data.model.ExpenseItem>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val groupedExpenses = remember(expenses) {
        expenses.groupBy { it.date }
            .toList()
            .sortedByDescending { it.first }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFE2DDF8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expense History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoDeepPurple
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BentoLavenderCard
                ) {
                    Text(
                        text = "${expenses.size} items",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            for (entry in groupedExpenses) {
                val dateStr = entry.first
                val dayExpenses = entry.second
                val dayTotal = dayExpenses.sumOf { it.amount }
                val parsedDate = try {
                    java.time.LocalDate.parse(dateStr)
                } catch (_: Exception) {
                    null
                }
                val formattedHeaderDate = parsedDate?.let { date ->
                    val today = java.time.LocalDate.now()
                    when {
                        date == today -> "Today, ${date.format(DateTimeFormatter.ofPattern("MMM d"))}"
                        date == today.minusDays(1) -> "Yesterday, ${date.format(DateTimeFormatter.ofPattern("MMM d"))}"
                        else -> date.format(DateTimeFormatter.ofPattern("EEE, MMMM d, yyyy"))
                    }
                } ?: dateStr

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedHeaderDate,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple
                        )
                        Text(
                            text = "$currencySymbol${SpendingCalculator.formatExactDecimal(dayTotal)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = BentoDeepPurple
                        )
                    }

                    for (exp in dayExpenses) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = BentoLavenderCard.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exp.description.ifBlank { exp.getEffectiveCategory().title },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BentoDeepPurple
                                    )
                                    Text(
                                        text = exp.timeFormatted,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BentoOnSurfaceVariantLight
                                    )
                                }
                                Text(
                                    text = "$currencySymbol${SpendingCalculator.formatExactDecimal(exp.amount)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoDeepPurple
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
