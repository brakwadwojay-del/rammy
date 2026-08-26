package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.AppDatabase
import com.example.data.repository.SpendingRepository
import com.example.ui.AppNavTab
import com.example.ui.SpendTrackerUiState
import com.example.ui.SpendTrackerViewModel
import com.example.ui.components.ShimmerSkeleton
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.BentoDeepPurple
import com.example.ui.theme.BentoLavenderAccent
import com.example.ui.theme.BentoLavenderCard
import com.example.ui.theme.BentoOnSurfaceVariantLight
import com.example.ui.theme.BentoOutlineLight
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val database by lazy { AppDatabase.getDatabase(applicationContext) }
  private val repository by lazy {
    SpendingRepository(
      budgetDao = database.budgetDao(),
      dailySpendingDao = database.dailySpendingDao(),
      expenseItemDao = database.expenseItemDao(),
      expensePresetDao = database.expensePresetDao(),
      appPreferencesDao = database.appPreferencesDao()
    )
  }
  private val viewModel: SpendTrackerViewModel by viewModels {
    SpendTrackerViewModel.provideFactory(repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          SpendTrackerApp(viewModel = viewModel)
        }
      }
    }
  }
}

enum class AppScreenStage {
  WELCOME,
  SETUP,
  MAIN_APP
}

@Composable
fun SpendTrackerApp(viewModel: SpendTrackerViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var isEditingBudget by remember { mutableStateOf(false) }

  if (uiState.isLoading) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      ShimmerSkeleton(height = 36.dp, shape = RoundedCornerShape(12.dp))
      ShimmerSkeleton(height = 160.dp, shape = RoundedCornerShape(28.dp))
      ShimmerSkeleton(height = 70.dp, shape = RoundedCornerShape(20.dp))
      ShimmerSkeleton(height = 140.dp, shape = RoundedCornerShape(24.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        ShimmerSkeleton(modifier = Modifier.weight(1f), height = 100.dp, shape = RoundedCornerShape(18.dp))
        ShimmerSkeleton(modifier = Modifier.weight(1f), height = 100.dp, shape = RoundedCornerShape(18.dp))
        ShimmerSkeleton(modifier = Modifier.weight(1f), height = 100.dp, shape = RoundedCornerShape(18.dp))
      }
    }
  } else {
    val currentStage = when {
      !uiState.onboardingCompleted -> AppScreenStage.WELCOME
      !uiState.isConfigured || isEditingBudget -> AppScreenStage.SETUP
      else -> AppScreenStage.MAIN_APP
    }

    Crossfade(
      targetState = currentStage,
      label = "screen_stage_transition"
    ) { stage ->
      when (stage) {
        AppScreenStage.WELCOME -> {
          WelcomeScreen(
            onContinue = { enteredName ->
              viewModel.completeOnboarding(enteredName)
            }
          )
        }
        AppScreenStage.SETUP -> {
          SetupScreen(
            initialProfile = if (isEditingBudget) uiState.budgetProfile else null,
            initialUserName = uiState.savedUserName,
            onSave = { userName, salaryAmount, savingsGoal, salaryReceivedDate, nextSalaryDate, food, transport ->
              viewModel.saveSetup(
                userName = userName,
                monthlyIncome = salaryAmount,
                monthlySavingsGoal = savingsGoal,
                salaryReceivedDate = salaryReceivedDate,
                nextSalaryDate = nextSalaryDate,
                dailyFoodExpense = food,
                dailyTransportExpense = transport
              )
              isEditingBudget = false
            },
            onCancel = if (isEditingBudget) {
              { isEditingBudget = false }
            } else null
          )
        }
        AppScreenStage.MAIN_APP -> {
          Scaffold(
            bottomBar = {
              NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              tonalElevation = 8.dp,
              modifier = Modifier.testTag("app_bottom_nav_bar")
            ) {
              NavigationBarItem(
                selected = uiState.activeTab == AppNavTab.DAILY,
                onClick = { viewModel.setNavTab(AppNavTab.DAILY) },
                icon = {
                  Text(
                    text = "🏠",
                    fontSize = 20.sp
                  )
                },
                label = {
                  Text(
                    text = "Daily",
                    fontWeight = if (uiState.activeTab == AppNavTab.DAILY) FontWeight.Bold else FontWeight.Medium
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = BentoDeepPurple,
                  selectedTextColor = BentoDeepPurple,
                  unselectedIconColor = BentoOnSurfaceVariantLight,
                  unselectedTextColor = BentoOnSurfaceVariantLight,
                  indicatorColor = BentoLavenderCard
                ),
                modifier = Modifier.testTag("nav_daily_tab")
              )

              NavigationBarItem(
                selected = uiState.activeTab == AppNavTab.INSIGHTS,
                onClick = { viewModel.setNavTab(AppNavTab.INSIGHTS) },
                icon = {
                  Text(
                    text = "📊",
                    fontSize = 20.sp
                  )
                },
                label = {
                  Text(
                    text = "Insights",
                    fontWeight = if (uiState.activeTab == AppNavTab.INSIGHTS) FontWeight.Bold else FontWeight.Medium
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = BentoDeepPurple,
                  selectedTextColor = BentoDeepPurple,
                  unselectedIconColor = BentoOnSurfaceVariantLight,
                  unselectedTextColor = BentoOnSurfaceVariantLight,
                  indicatorColor = BentoLavenderCard
                ),
                modifier = Modifier.testTag("nav_insights_tab")
              )
            }
          }
        ) { paddingValues ->
          Crossfade(
            targetState = uiState.activeTab,
            label = "tab_switch",
            modifier = Modifier.padding(paddingValues)
          ) { activeTab ->
            when (activeTab) {
              AppNavTab.DAILY -> {
                HomeScreen(
                  uiState = uiState,
                  onAddExpense = { amount, description, timeFormatted, category ->
                    viewModel.addExpense(amount, description, timeFormatted, category)
                  },
                  onUpdateExpense = { expense ->
                    viewModel.updateExpense(expense)
                  },
                  onDeleteExpense = { id ->
                    viewModel.deleteExpense(id)
                  },
                  onDelayExpense = { id ->
                    viewModel.delayExpense(id)
                  },
                  onRestoreExpense = { id ->
                    viewModel.restoreExpense(id)
                  },
                  onOpenEditBudget = {
                    isEditingBudget = true
                  },
                  onResetAllData = {
                    viewModel.resetAllData()
                    isEditingBudget = false
                  },
                  onSetDailyReminder = { enabled ->
                    viewModel.setDailyReminder(enabled)
                  },
                  onUpdateUserName = { name ->
                    viewModel.updateUserName(name)
                  },
                  onConfirmZeroSpend = { date ->
                    viewModel.confirmZeroSpendForDate(date)
                  }
                )
              }
              AppNavTab.INSIGHTS -> {
                InsightsScreen(
                  uiState = uiState,
                  onSelectPeriodType = { periodType, customMonth ->
                    viewModel.setInsightsPeriodType(periodType, customMonth)
                  },
                  onToggleTrendView = { isWeek ->
                    viewModel.setInsightsTrendIsWeek(isWeek)
                  },
                  onSelectCategory = { category ->
                    viewModel.selectInsightsCategory(category)
                  }
                )
              }
            }
          }
        }
      }
    }
  }
}
}
