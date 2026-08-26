package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyBreakdown
import com.example.data.MonthProgressSummary
import com.example.data.SpendingCalculator
import com.example.data.WeeklySpendingSummary
import com.example.data.YesterdaySpendingSummary
import com.example.data.model.ExpenseCategory
import com.example.data.model.ExpenseItem
import com.example.ui.SpendTrackerUiState
import com.example.ui.components.AllowanceProgressBar
import com.example.ui.components.AnimatedAmountText
import com.example.ui.components.DonutChart
import com.example.ui.components.bounceClick
import com.example.ui.components.bringIntoViewOnFocus
import com.example.ui.theme.BentoAmberText
import com.example.ui.theme.BentoDeepPurple
import com.example.ui.theme.BentoLavenderAccent
import com.example.ui.theme.BentoLavenderCard
import com.example.ui.theme.BentoOnSurfaceLight
import com.example.ui.theme.BentoOnSurfaceVariantLight
import com.example.ui.theme.BentoOutlineLight
import com.example.ui.theme.BentoOutlineVariantLight
import com.example.ui.theme.BentoPrimaryLight
import com.example.ui.theme.BentoSecondaryContainerLight
import com.example.ui.theme.BentoSecondaryLight
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: SpendTrackerUiState,
    onAddExpense: (Double, String, String, String) -> Unit,
    onUpdateExpense: (ExpenseItem) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    onDelayExpense: (Long) -> Unit,
    onRestoreExpense: (Long) -> Unit,
    onOpenEditBudget: () -> Unit,
    onResetAllData: () -> Unit,
    onSetDailyReminder: (Boolean) -> Unit = {},
    onUpdateUserName: (String) -> Unit = {},
    onConfirmZeroSpend: (LocalDate) -> Unit = {}
) {
    val profile = uiState.budgetProfile ?: return
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var selectedExpenseForEdit by remember { mutableStateOf<ExpenseItem?>(null) }
    var showBreakdownSheet by remember { mutableStateOf(false) }
    var showDelayedSheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editingNameInput by remember { mutableStateOf(profile.userName) }

    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val breakdownSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val delayedSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Formatted Date: e.g. "Monday, August 24"
    val formattedDate = remember(uiState.selectedDate) {
        uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rammys Spend Tracker",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = BentoDeepPurple,
                        letterSpacing = (-0.5).sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = BentoDeepPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 40.dp)
        ) {
            // 1. Compact Header: Greeting and Date (Not inside a large card)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Hello, ${profile.userName.trim().ifBlank { "Friend" }} 👋",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = BentoSecondaryLight
                        )
                    }

                    // Quick User Profile / Edit Name Button
                    Surface(
                        onClick = {
                            editingNameInput = profile.userName
                            showEditNameDialog = true
                        },
                        shape = CircleShape,
                        color = BentoLavenderCard,
                        border = BorderStroke(1.dp, BentoLavenderAccent),
                        modifier = Modifier.testTag("edit_user_name_button")
                    ) {
                        Box(
                            modifier = Modifier.size(38.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.userName.trim().take(1).uppercase().ifBlank { "U" },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BentoDeepPurple
                            )
                        }
                    }
                }
            }

            // 2. Hero Daily Spending Card
            item {
                HeroDailyBalanceSection(
                    leftToSpend = uiState.todayLeftToSpend,
                    safeToSpendToday = uiState.safeToSpendToday,
                    dailyAllowance = uiState.todayAllowance,
                    actualSpent = uiState.todayActualSpent,
                    currencySymbol = profile.currencySymbol,
                    spendingMood = uiState.spendingMood,
                    tomorrowTarget = uiState.tomorrowTarget,
                    daysUntilPaydayText = uiState.daysUntilPaydayText,
                    formattedDate = formattedDate
                )
            }

            // 3. Prominent "+ Add Expense" Button
            item {
                Button(
                    onClick = { showAddExpenseSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .bounceClick()
                        .testTag("add_expense_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoDeepPurple,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Expense",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 4. Tomorrow & Payday Compact Secondary Information
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = BentoSecondaryContainerLight.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, BentoOutlineLight.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tomorrow: ${profile.currencySymbol}${SpendingCalculator.formatExactDecimal(uiState.tomorrowTarget)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoSecondaryLight
                        )

                        if (uiState.daysUntilPaydayText.isNotBlank()) {
                            Text(
                                text = "  •  ",
                                style = MaterialTheme.typography.bodySmall,
                                color = BentoSecondaryLight.copy(alpha = 0.6f)
                            )
                            Text(
                                text = uiState.daysUntilPaydayText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoSecondaryLight
                            )
                        }
                    }
                }
            }

            // 5. Yesterday Zero Spend Confirmation (if needed)
            if (uiState.needsYesterdayConfirmation) {
                item {
                    YesterdayZeroSpendPromptCard(
                        yesterdayDate = uiState.yesterdayDate,
                        currencySymbol = profile.currencySymbol,
                        onConfirm = { onConfirmZeroSpend(uiState.yesterdayDate) }
                    )
                }
            }

            // 6. Today's Expenses Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 2.dp, end = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Expenses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple
                    )

                    if (uiState.todayExpenses.isNotEmpty()) {
                        Surface(
                            onClick = { showBreakdownSheet = true },
                            shape = RoundedCornerShape(10.dp),
                            color = BentoLavenderCard,
                            border = BorderStroke(1.dp, BentoLavenderAccent),
                            modifier = Modifier
                                .bounceClick()
                                .testTag("view_breakdown_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PieChart,
                                    contentDescription = null,
                                    tint = BentoDeepPurple,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${uiState.todayExpenses.size} logged",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoDeepPurple
                                )
                            }
                        }
                    }
                }
            }

            // 7. Today's Expense List or Inline Empty State
            if (uiState.todayExpenses.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "No expenses recorded today yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = BentoSecondaryLight,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tap + Add Expense above to record your first spend.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoOnSurfaceVariantLight,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(
                    items = uiState.todayExpenses,
                    key = { it.id }
                ) { expense ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(200))
                    ) {
                        CompactExpenseRow(
                            expense = expense,
                            currencySymbol = profile.currencySymbol,
                            onClick = { selectedExpenseForEdit = expense },
                            onDelay = { onDelayExpense(expense.id) }
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // 8. ADD EXPENSE BOTTOM SHEET
    // ==========================================
    if (showAddExpenseSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                keyboardController?.hide()
                showAddExpenseSheet = false
            },
            sheetState = addSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            ExpenseFormContent(
                title = "Add Expense",
                confirmButtonText = "Add Expense",
                currencySymbol = profile.currencySymbol,
                onDismiss = {
                    keyboardController?.hide()
                    coroutineScope.launch {
                        addSheetState.hide()
                        showAddExpenseSheet = false
                    }
                },
                onSave = { amount, description, timeFormatted, category ->
                    keyboardController?.hide()
                    onAddExpense(amount, description, timeFormatted, category)
                    coroutineScope.launch {
                        addSheetState.hide()
                        showAddExpenseSheet = false
                    }
                }
            )
        }
    }

    // ==========================================
    // 9. EDIT EXPENSE BOTTOM SHEET
    // ==========================================
    selectedExpenseForEdit?.let { expense ->
        ModalBottomSheet(
            onDismissRequest = {
                keyboardController?.hide()
                selectedExpenseForEdit = null
            },
            sheetState = editSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            ExpenseFormContent(
                title = "Edit Expense",
                confirmButtonText = "Save Changes",
                initialAmount = expense.amount,
                initialDescription = expense.description,
                initialCategory = expense.category,
                initialTime = expense.timeFormatted,
                currencySymbol = profile.currencySymbol,
                showExtraActions = true,
                onDelay = {
                    keyboardController?.hide()
                    onDelayExpense(expense.id)
                    coroutineScope.launch {
                        editSheetState.hide()
                        selectedExpenseForEdit = null
                    }
                },
                onDelete = {
                    keyboardController?.hide()
                    onDeleteExpense(expense.id)
                    coroutineScope.launch {
                        editSheetState.hide()
                        selectedExpenseForEdit = null
                    }
                },
                onDismiss = {
                    keyboardController?.hide()
                    coroutineScope.launch {
                        editSheetState.hide()
                        selectedExpenseForEdit = null
                    }
                },
                onSave = { amount, description, timeFormatted, category ->
                    keyboardController?.hide()
                    onUpdateExpense(
                        expense.copy(
                            amount = amount,
                            description = description,
                            category = category,
                            timeFormatted = timeFormatted
                        )
                    )
                    coroutineScope.launch {
                        editSheetState.hide()
                        selectedExpenseForEdit = null
                    }
                }
            )
        }
    }

    // ==========================================
    // 10. DAILY SPENDING BREAKDOWN BOTTOM SHEET
    // ==========================================
    if (showBreakdownSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBreakdownSheet = false },
            sheetState = breakdownSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            BreakdownSheetContent(
                breakdown = uiState.categoryBreakdown,
                totalSpent = uiState.todayActualSpent,
                currencySymbol = profile.currencySymbol,
                onClose = {
                    coroutineScope.launch {
                        breakdownSheetState.hide()
                        showBreakdownSheet = false
                    }
                }
            )
        }
    }

    // ==========================================
    // 11. DELAYED EXPENSES BOTTOM SHEET
    // ==========================================
    if (showDelayedSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDelayedSheet = false },
            sheetState = delayedSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            DelayedExpensesSheetContent(
                delayedExpenses = uiState.delayedExpenses,
                currencySymbol = profile.currencySymbol,
                onRestore = { id ->
                    onRestoreExpense(id)
                },
                onDelete = { id ->
                    onDeleteExpense(id)
                },
                onClose = {
                    coroutineScope.launch {
                        delayedSheetState.hide()
                        showDelayedSheet = false
                    }
                }
            )
        }
    }

    // ==========================================
    // 12. SETTINGS & BUDGET PROFILE DIALOG
    // ==========================================
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = BentoDeepPurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rammys Spend Tracker",
                        fontWeight = FontWeight.Black,
                        color = BentoDeepPurple
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Know your money. Own your day.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimaryLight
                    )

                    HorizontalDivider(color = BentoOutlineVariantLight)

                    // User Profile Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Your Name",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BentoSecondaryLight
                            )
                            Text(
                                text = profile.userName.ifBlank { "Friend" },
                                fontWeight = FontWeight.Bold,
                                color = BentoDeepPurple,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        TextButton(
                            onClick = {
                                editingNameInput = profile.userName
                                showSettingsDialog = false
                                showEditNameDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = BentoDeepPurple)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change", color = BentoDeepPurple, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = BentoOutlineVariantLight)

                    // Financial Parameters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Monthly Income",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BentoSecondaryLight
                        )
                        Text(
                            text = "GH₵${SpendingCalculator.formatAmount(profile.monthlyIncome)}",
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Planned Savings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BentoSecondaryLight
                        )
                        Text(
                            text = "GH₵${SpendingCalculator.formatAmount(profile.monthlySavingsGoal)}",
                            fontWeight = FontWeight.Bold,
                            color = BentoAmberText
                        )
                    }

                    val spendablePool = SpendingCalculator.calculateMonthlySpendable(
                        profile.monthlyIncome,
                        profile.monthlySavingsGoal
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Monthly Spending Pool",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BentoSecondaryLight
                        )
                        Text(
                            text = "GH₵${SpendingCalculator.formatAmount(spendablePool)}",
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryLight
                        )
                    }

                    HorizontalDivider(color = BentoOutlineVariantLight)

                    // Daily 8:00 PM Reminder Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily 8:00 PM Check-In",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoDeepPurple
                            )
                            Text(
                                text = "Evening prompt to log spending",
                                style = MaterialTheme.typography.labelSmall,
                                color = BentoSecondaryLight
                            )
                        }
                        Switch(
                            checked = uiState.preferences.dailyReminderEnabled,
                            onCheckedChange = { onSetDailyReminder(it) }
                        )
                    }

                    HorizontalDivider(color = BentoOutlineVariantLight)

                    // Action buttons in settings
                    Button(
                        onClick = {
                            showSettingsDialog = false
                            onOpenEditBudget()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoDeepPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Monthly Parameters")
                    }

                    OutlinedButton(
                        onClick = {
                            showSettingsDialog = false
                            showResetConfirmDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                        border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset All Data")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Done", color = BentoDeepPurple, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = BentoDeepPurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Your Name",
                        fontWeight = FontWeight.Black,
                        color = BentoDeepPurple
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter how you want your name to appear on the dashboard.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BentoOnSurfaceVariantLight
                    )

                    OutlinedTextField(
                        value = editingNameInput,
                        onValueChange = { editingNameInput = it },
                        label = { Text("Your Name") },
                        placeholder = { Text("e.g. Alex, Maya, or Rammy") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val cleanName = editingNameInput.trim().ifBlank { "Friend" }
                                onUpdateUserName(cleanName)
                                showEditNameDialog = false
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewOnFocus()
                            .testTag("dialog_user_name_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoDeepPurple,
                            unfocusedBorderColor = BentoOutlineLight
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanName = editingNameInput.trim().ifBlank { "Friend" }
                        onUpdateUserName(cleanName)
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoDeepPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("save_user_name_button")
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = BentoSecondaryLight)
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = "Reset All Data?",
                    fontWeight = FontWeight.Bold,
                    color = BentoDeepPurple
                )
            },
            text = {
                Text(
                    text = "This will permanently remove all your budget profile, daily spending logs, and delayed items. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoOnSurfaceLight
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        onResetAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Yes, Reset Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = BentoSecondaryLight)
                }
            }
        )
    }
}

/**
 * Dominant Hero Daily Balance Section with Prominent Spending Mood Character,
 * Safe to Spend Indicator, Tomorrow Target Preview, and Payday Countdown.
 */
@Composable
private fun HeroDailyBalanceSection(
    leftToSpend: Double,
    safeToSpendToday: Double,
    dailyAllowance: Double,
    actualSpent: Double,
    currencySymbol: String,
    spendingMood: SpendingCalculator.SpendingMood?,
    tomorrowTarget: Double,
    daysUntilPaydayText: String,
    formattedDate: String
) {
    val isNegative = leftToSpend < 0.0
    val mood = spendingMood ?: SpendingCalculator.evaluateSpendingMood(
        todayLeftToSpend = leftToSpend,
        todayAllowance = dailyAllowance,
        todayActualSpent = actualSpent,
        currencySymbol = currencySymbol
    )

    // Dynamic background colors depending on mood status
    val cardBg: Color
    val cardBorder: Color
    val moodBadgeBg: Color
    val moodBadgeColor: Color

    when (mood.type) {
        SpendingCalculator.SpendingMoodType.AHEAD_OF_GOAL,
        SpendingCalculator.SpendingMoodType.EXCELLENT -> {
            cardBg = BentoLavenderCard
            cardBorder = BentoLavenderAccent
            moodBadgeBg = Color.White.copy(alpha = 0.9f)
            moodBadgeColor = BentoDeepPurple
        }
        SpendingCalculator.SpendingMoodType.ON_TRACK -> {
            cardBg = BentoLavenderCard
            cardBorder = BentoLavenderAccent
            moodBadgeBg = Color.White.copy(alpha = 0.9f)
            moodBadgeColor = BentoDeepPurple
        }
        SpendingCalculator.SpendingMoodType.GETTING_CLOSE -> {
            cardBg = Color(0xFFFFFBEB)
            cardBorder = Color(0xFFFDE68A)
            moodBadgeBg = Color(0xFFFEF3C7)
            moodBadgeColor = Color(0xFF92400E)
        }
        SpendingCalculator.SpendingMoodType.OVER_BUDGET -> {
            cardBg = ExpenseRedLight
            cardBorder = ExpenseRed.copy(alpha = 0.4f)
            moodBadgeBg = ExpenseRed.copy(alpha = 0.15f)
            moodBadgeColor = ExpenseRed
        }
    }

    // Gentle ambient breathing scale & floating offset for the mood character
    val infiniteTransition = rememberInfiniteTransition(label = "moodBreathing")
    val characterScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "characterBreathingScale"
    )
    val floatingOffsetY by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "characterFloatingOffsetY"
    )
    // Subtle side-to-side shift for side-eye mood
    val sideEyeShiftX by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sideEyeShiftX"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_spending_hero_card"),
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Top Header: Title & Mood Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = BentoDeepPurple,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Today's Spending",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = moodBadgeBg,
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Text(
                        text = mood.badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = moodBadgeColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // 2. Prominent Mood Character (Centerpiece)
            Surface(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.92f),
                border = BorderStroke(2.dp, cardBorder),
                shadowElevation = 3.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = mood.emoji,
                        transitionSpec = {
                            (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith
                                    (scaleOut() + fadeOut())
                        },
                        label = "moodEmojiAnimation"
                    ) { emojiChar ->
                        val isSideEye = emojiChar == "🙄"
                        val isOverBudget = emojiChar == "😟"
                        Text(
                            text = emojiChar,
                            fontSize = 48.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .scale(if (isOverBudget) characterScale * 0.96f else characterScale)
                                .then(
                                    if (isSideEye) {
                                        Modifier.offset(
                                            x = sideEyeShiftX.dp,
                                            y = floatingOffsetY.dp
                                        )
                                    } else {
                                        Modifier.offset(
                                            y = floatingOffsetY.dp
                                        )
                                    }
                                )
                        )
                    }
                }
            }

            // 3. Recommended Spending Today (Animated Numbers - Dominant Header)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                AnimatedAmountText(
                    targetAmount = dailyAllowance,
                    prefix = "$currencySymbol",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 38.sp,
                        lineHeight = 42.sp
                    ),
                    fontWeight = FontWeight.Bold,
                    color = BentoDeepPurple
                )
                Text(
                    text = "Recommended today",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = BentoSecondaryLight
                )
            }

            // 4. Safe to Spend & Spent Today Stat Row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, BentoOutlineLight.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Safe to spend",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = BentoSecondaryLight
                        )
                        AnimatedAmountText(
                            targetAmount = safeToSpendToday,
                            prefix = "$currencySymbol",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (safeToSpendToday <= 0.0 && isNegative) ExpenseRed else BentoDeepPurple
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(BentoOutlineLight.copy(alpha = 0.6f))
                    )

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Spent today",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = BentoSecondaryLight
                        )
                        AnimatedAmountText(
                            targetAmount = actualSpent,
                            prefix = "$currencySymbol",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isNegative) ExpenseRed else BentoDeepPurple
                        )
                    }
                }
            }

            // 5. Short Status Comment
            Text(
                text = "\"${mood.headline}\"",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isNegative) ExpenseRed else BentoSecondaryLight,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * End-of-Day Zero Spending Confirmation Prompt Card
 */
@Composable
private fun YesterdayZeroSpendPromptCard(
    yesterdayDate: LocalDate,
    currencySymbol: String,
    onConfirm: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("yesterday_zero_spend_prompt_card"),
        shape = RoundedCornerShape(18.dp),
        color = BentoLavenderCard,
        border = BorderStroke(1.dp, BentoLavenderAccent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "✨", fontSize = 22.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Didn't spend anything yesterday?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple
                    )
                    Text(
                        text = "No expenses recorded for ${yesterdayDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoSecondaryLight
                    )
                }
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .bounceClick()
                    .testTag("confirm_zero_spend_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoDeepPurple,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Yes, I didn't spend anything",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Supportive End-of-Day Summary Card for the previous day
 */
@Composable
private fun YesterdaySummaryCard(
    summary: YesterdaySpendingSummary,
    currencySymbol: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("yesterday_summary_card"),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, BentoLavenderAccent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = BentoLavenderCard,
                border = BorderStroke(1.dp, BentoLavenderAccent)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = summary.moodEmoji,
                        fontSize = 20.sp
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Yesterday",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoSecondaryLight
                    )
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSecondaryLight
                    )
                    Text(
                        text = summary.statusTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple
                    )
                }
                Text(
                    text = summary.statusDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoSecondaryLight
                )
            }
        }
    }
}

/**
 * Single Contextual Suggestion Area Card
 */
@Composable
private fun ContextualSuggestionCard(
    suggestion: SpendingCalculator.ContextualSuggestion
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = BentoSecondaryContainerLight.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, BentoLavenderAccent.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = suggestion.iconEmoji,
                        fontSize = 18.sp
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoDeepPurple
                )
                Text(
                    text = suggestion.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoOnSurfaceVariantLight,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * Compact Expense Row with Category Icons
 */
@Composable
private fun CompactExpenseRow(
    expense: ExpenseItem,
    currencySymbol: String,
    onClick: () -> Unit,
    onDelay: () -> Unit
) {
    val category = expense.getEffectiveCategory()

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick()
            .testTag("expense_row_${expense.id}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BentoOutlineLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Emoji Badge
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = category.color.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, category.color.copy(alpha = 0.25f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = category.emoji,
                            fontSize = 20.sp
                        )
                    }
                }

                // Description & Category/Time subtitle
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = category.color
                        )
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoSecondaryLight
                        )
                        Text(
                            text = expense.timeFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoSecondaryLight
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "$currencySymbol${SpendingCalculator.formatExactDecimal(expense.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = BentoDeepPurple
                )

                // Quick Action: Delay button
                Surface(
                    onClick = onDelay,
                    shape = RoundedCornerShape(10.dp),
                    color = BentoSecondaryContainerLight,
                    border = BorderStroke(1.dp, BentoOutlineLight),
                    modifier = Modifier.testTag("delay_expense_${expense.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Delay",
                            modifier = Modifier.size(13.dp),
                            tint = BentoDeepPurple
                        )
                        Text(
                            text = "Delay",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple
                        )
                    }
                }
            }
        }
    }
}

/**
 * Daily Spending Breakdown Bottom Sheet Content
 */
@Composable
private fun BreakdownSheetContent(
    breakdown: List<SpendingCalculator.CategoryBreakdownItem>,
    totalSpent: Double,
    currencySymbol: String,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Today's Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = BentoDeepPurple
                )
                Text(
                    text = "Real spending by category",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoSecondaryLight
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = BentoSecondaryLight)
            }
        }

        // Animated Donut Chart
        DonutChart(
            breakdown = breakdown,
            totalAmount = totalSpent,
            currencySymbol = currencySymbol
        )

        // Category breakdown list
        if (breakdown.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                breakdown.forEach { item ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = BentoSecondaryContainerLight.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, BentoOutlineLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = item.category.color.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = item.category.emoji, fontSize = 18.sp)
                                    }
                                }

                                Column {
                                    Text(
                                        text = item.category.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoDeepPurple
                                    )
                                    Text(
                                        text = "${item.count} ${if (item.count == 1) "item" else "items"} · ${item.percentage.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BentoSecondaryLight
                                    )
                                }
                            }

                            Text(
                                text = "$currencySymbol${SpendingCalculator.formatExactDecimal(item.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoDeepPurple
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Add / Edit Expense Form Sheet Content with Category Selection
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpenseFormContent(
    title: String,
    confirmButtonText: String,
    initialAmount: Double? = null,
    initialDescription: String? = null,
    initialCategory: String? = null,
    initialTime: String? = null,
    currencySymbol: String = "GH₵",
    showExtraActions: Boolean = false,
    onDelay: () -> Unit = {},
    onDelete: () -> Unit = {},
    onDismiss: () -> Unit,
    onSave: (Double, String, String, String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val currentTime = remember {
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
        now.format(formatter)
    }

    var amountText by remember {
        mutableStateOf(initialAmount?.let { SpendingCalculator.formatExactDecimal(it) } ?: "")
    }
    var descriptionText by remember {
        mutableStateOf(initialDescription ?: "")
    }
    var selectedCategory by remember {
        mutableStateOf(
            if (!initialCategory.isNullOrBlank()) {
                ExpenseCategory.fromString(initialCategory)
            } else if (!initialDescription.isNullOrBlank()) {
                ExpenseCategory.inferFromDescription(initialDescription)
            } else {
                ExpenseCategory.FOOD
            }
        )
    }
    var timeText by remember {
        mutableStateOf(initialTime ?: currentTime)
    }

    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        if (initialAmount == null) {
            delay(150)
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = BentoDeepPurple
            )
            IconButton(onClick = {
                keyboardController?.hide()
                onDismiss()
            }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = BentoSecondaryLight)
            }
        }

        // Category Picker (🍛 Food, 🚕 Transport, 🛍 Shopping, ☕ Drinks, 💡 Bills, ✦ Other)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Category",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = BentoSecondaryLight
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpenseCategory.entries.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = {
                            selectedCategory = cat
                            if (descriptionText.isBlank()) {
                                descriptionText = cat.title
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) cat.color.copy(alpha = 0.2f) else BentoSecondaryContainerLight,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) cat.color else BentoOutlineLight
                        ),
                        modifier = Modifier.bounceClick()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = cat.emoji, fontSize = 14.sp)
                            Text(
                                text = cat.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BentoDeepPurple else BentoSecondaryLight
                            )
                        }
                    }
                }
            }
        }

        // Quick Amount Selection Buttons (5, 10, 20, 50)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Quick amount",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = BentoSecondaryLight
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val quickAmounts = listOf(5.0, 10.0, 20.0, 50.0)
                quickAmounts.forEach { quickVal ->
                    val isCurrent = amountText.toDoubleOrNull() == quickVal
                    Surface(
                        onClick = {
                            amountText = SpendingCalculator.formatExactDecimal(quickVal)
                            keyboardController?.hide()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrent) BentoLavenderAccent else BentoSecondaryContainerLight,
                        border = BorderStroke(
                            1.dp,
                            if (isCurrent) BentoDeepPurple else BentoOutlineLight
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .bounceClick()
                            .testTag("quick_amount_${quickVal.toInt()}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$currencySymbol${quickVal.toInt()}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                                color = if (isCurrent) BentoDeepPurple else BentoSecondaryLight
                            )
                        }
                    }
                }
            }
        }

        // Amount Input Field
        OutlinedTextField(
            value = amountText,
            onValueChange = { input ->
                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                    amountText = input
                }
            },
            label = { Text("Amount ($currencySymbol)") },
            placeholder = { Text("0.00") },
            prefix = {
                Text(
                    text = "$currencySymbol ",
                    fontWeight = FontWeight.Bold,
                    color = BentoDeepPurple
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .bringIntoViewOnFocus()
                .testTag("expense_amount_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BentoDeepPurple,
                unfocusedBorderColor = BentoOutlineLight
            )
        )

        // Description Input Field
        OutlinedTextField(
            value = descriptionText,
            onValueChange = { input ->
                descriptionText = input
                // Auto-infer category if matching keywords
                val inferred = ExpenseCategory.inferFromDescription(input)
                if (inferred != ExpenseCategory.OTHER) {
                    selectedCategory = inferred
                }
            },
            label = { Text("What was it?") },
            placeholder = { Text("e.g. Lunch, taxi, groceries") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewOnFocus()
                .testTag("expense_description_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BentoDeepPurple,
                unfocusedBorderColor = BentoOutlineLight
            )
        )

        // Time Input Field
        OutlinedTextField(
            value = timeText,
            onValueChange = { timeText = it },
            label = { Text("Time") },
            trailingIcon = {
                IconButton(onClick = { timeText = currentTime }) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Now",
                        tint = BentoDeepPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewOnFocus(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BentoDeepPurple,
                unfocusedBorderColor = BentoOutlineLight
            )
        )

        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
        val isValid = parsedAmount > 0.0

        // Primary Confirm Button
        Button(
            onClick = {
                if (isValid) {
                    keyboardController?.hide()
                    val desc = descriptionText.ifBlank { selectedCategory.title }
                    val time = timeText.ifBlank { currentTime }
                    onSave(parsedAmount, desc, time, selectedCategory.title)
                }
            },
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .bringIntoViewOnFocus()
                .testTag("save_expense_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BentoDeepPurple,
                disabledContainerColor = BentoDeepPurple.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = confirmButtonText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Extra Actions for Edit Mode: Delay & Delete
        if (showExtraActions) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        keyboardController?.hide()
                        onDelay()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("modal_delay_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoDeepPurple),
                    border = BorderStroke(1.dp, BentoLavenderAccent)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delay")
                }

                OutlinedButton(
                    onClick = {
                        keyboardController?.hide()
                        onDelete()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("modal_delete_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                    border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete")
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

/**
 * Delayed Expenses Sheet Content
 */
@Composable
private fun DelayedExpensesSheetContent(
    delayedExpenses: List<ExpenseItem>,
    currencySymbol: String,
    onRestore: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Delayed Expenses",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = BentoDeepPurple
                )
                Text(
                    text = "Not counted against today's spending",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoSecondaryLight
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = BentoSecondaryLight)
            }
        }

        if (delayedExpenses.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = BentoSecondaryContainerLight,
                border = BorderStroke(1.dp, BentoOutlineLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "No delayed expenses",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple
                    )
                    Text(
                        text = "You can delay any expense from today's list if you don't want it counted today.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoSecondaryLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(delayedExpenses, key = { it.id }) { item ->
                    val category = item.getEffectiveCategory()
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = BentoSecondaryContainerLight,
                        border = BorderStroke(1.dp, BentoOutlineLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = category.color.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = category.emoji, fontSize = 16.sp)
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoDeepPurple
                                    )
                                    Text(
                                        text = "$currencySymbol${SpendingCalculator.formatExactDecimal(item.amount)} · ${item.timeFormatted}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BentoSecondaryLight
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Restore Button
                                Button(
                                    onClick = { onRestore(item.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoDeepPurple),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.RotateLeft,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Restore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                // Delete Button
                                IconButton(
                                    onClick = { onDelete(item.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Weekly Spending Summary Card
 */
@Composable
fun WeeklySpendingCard(
    weeklySpending: WeeklySpendingSummary,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val isOverBudget = weeklySpending.weeklySpent > weeklySpending.weeklyAllowance
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_spending_card"),
        shape = RoundedCornerShape(20.dp),
        color = BentoLavenderCard,
        border = BorderStroke(1.dp, BentoLavenderAccent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = BentoDeepPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Weekly Spending",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, BentoLavenderAccent)
                ) {
                    Text(
                        text = weeklySpending.weekLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoDeepPurple,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Spent this week",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSecondaryLight
                    )
                    Text(
                        text = "$currencySymbol${SpendingCalculator.formatExactDecimal(weeklySpending.weeklySpent)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isOverBudget) ExpenseRed else BentoDeepPurple
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Weekly allowance",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSecondaryLight
                    )
                    Text(
                        text = "$currencySymbol${SpendingCalculator.formatExactDecimal(weeklySpending.weeklyAllowance)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple
                    )
                }
            }

            // Progress bar
            AllowanceProgressBar(
                spent = weeklySpending.weeklySpent,
                allowance = weeklySpending.weeklyAllowance,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Daily avg: $currencySymbol${SpendingCalculator.formatExactDecimal(weeklySpending.averageDailySpent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoSecondaryLight
                )
                val remaining = weeklySpending.weeklyRemaining
                val daysLeft = maxOf(0, 7 - weeklySpending.daysElapsed)
                Text(
                    text = if (remaining >= 0) {
                        "$currencySymbol${SpendingCalculator.formatExactDecimal(remaining)} left (${daysLeft}d left)"
                    } else {
                        "$currencySymbol${SpendingCalculator.formatExactDecimal(kotlin.math.abs(remaining))} over"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (remaining < 0) ExpenseRed else BentoDeepPurple
                )
            }
        }
    }
}

/**
 * Expected Weekly Expenditure Card
 */
@Composable
fun WeeklyExpenditureCard(
    expectedWeeklyExpenditure: Double,
    weeklySpent: Double,
    weeklyRemaining: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val isOverBudget = weeklyRemaining < 0.0

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_expenditure_card"),
        shape = RoundedCornerShape(20.dp),
        color = BentoLavenderCard,
        border = BorderStroke(1.dp, BentoLavenderAccent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = BentoDeepPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Weekly Expenditure",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, BentoLavenderAccent)
                ) {
                    Text(
                        text = "7-Day Guide",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoDeepPurple,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Spent this week",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSecondaryLight
                    )
                    Text(
                        text = "$currencySymbol${SpendingCalculator.formatExactDecimal(weeklySpent)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isOverBudget) ExpenseRed else BentoDeepPurple
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Expected expenditure",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSecondaryLight
                    )
                    Text(
                        text = "$currencySymbol${SpendingCalculator.formatExactDecimal(expectedWeeklyExpenditure)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple
                    )
                }
            }

            // Progress bar
            AllowanceProgressBar(
                spent = weeklySpent,
                allowance = expectedWeeklyExpenditure,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Based on today's rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoSecondaryLight
                )
                Text(
                    text = if (weeklyRemaining >= 0) {
                        "$currencySymbol${SpendingCalculator.formatExactDecimal(weeklyRemaining)} remaining"
                    } else {
                        "$currencySymbol${SpendingCalculator.formatExactDecimal(kotlin.math.abs(weeklyRemaining))} over"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverBudget) ExpenseRed else BentoDeepPurple
                )
            }
        }
    }
}

/**
 * Monthly Savings Progress Card
 */
@Composable
fun MonthlyProgressCard(
    monthProgress: MonthProgressSummary,
    monthlyIncome: Double,
    monthlySavingsGoal: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val spendable = monthlyIncome - monthlySavingsGoal
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_progress_card"),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BentoOutlineLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Progress",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoDeepPurple
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (monthProgress.isOnTrack) BentoLavenderCard else ExpenseRedLight
                ) {
                    Text(
                        text = if (monthProgress.isOnTrack) "On Track ✨" else "Adjust Spending ⚠️",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (monthProgress.isOnTrack) BentoDeepPurple else ExpenseRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Savings goal",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSecondaryLight
                    )
                    Text(
                        text = "$currencySymbol${SpendingCalculator.formatExactDecimal(monthlySavingsGoal)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Spent so far",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSecondaryLight
                    )
                    Text(
                        text = "$currencySymbol${SpendingCalculator.formatExactDecimal(monthProgress.totalSpentSoFar)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Projected savings",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSecondaryLight
                    )
                    Text(
                        text = "$currencySymbol${SpendingCalculator.formatExactDecimal(monthProgress.projectedSavings)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (monthProgress.projectedSavings >= monthlySavingsGoal) BentoDeepPurple else ExpenseRed
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spendable budget: $currencySymbol${SpendingCalculator.formatExactDecimal(spendable)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = BentoSecondaryLight
                )
                Text(
                    text = "${monthProgress.daysRemainingInMonth} days left in month",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = BentoSecondaryLight
                )
            }
        }
    }
}

/**
 * Daily Breakdown Informational Card (Food, Transport, Other)
 */
@Composable
fun DailyBreakdownCard(
    breakdown: DailyBreakdown,
    dailyAllowance: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_breakdown_card"),
        shape = RoundedCornerShape(20.dp),
        color = BentoSecondaryContainerLight,
        border = BorderStroke(1.dp, BentoOutlineLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Daily Breakdown (Informational)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = BentoDeepPurple
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (breakdown.food > 0) {
                    Column {
                        Text(
                            text = "🍔 Expected food",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoSecondaryLight
                        )
                        Text(
                            text = "$currencySymbol${SpendingCalculator.formatExactDecimal(breakdown.food)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple
                        )
                    }
                }

                if (breakdown.transport > 0) {
                    Column {
                        Text(
                            text = "🚌 Expected transport",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoSecondaryLight
                        )
                        Text(
                            text = "$currencySymbol${SpendingCalculator.formatExactDecimal(breakdown.transport)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "✨ Other spending",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSecondaryLight
                    )
                    Text(
                        text = "$currencySymbol${SpendingCalculator.formatExactDecimal(breakdown.other)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple
                    )
                }
            }

            Text(
                text = "Food and transportation are part of your daily limit ($currencySymbol${SpendingCalculator.formatExactDecimal(dailyAllowance)}), not additional money.",
                style = MaterialTheme.typography.labelSmall,
                color = BentoSecondaryLight,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}
