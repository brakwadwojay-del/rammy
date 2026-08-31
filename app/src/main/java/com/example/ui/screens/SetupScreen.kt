package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SpendingCalculator
import com.example.data.model.BudgetProfile
import com.example.ui.components.bounceClick
import com.example.ui.components.bringIntoViewOnFocus
import com.example.ui.theme.BentoDeepPurple
import com.example.ui.theme.BentoLavenderAccent
import com.example.ui.theme.BentoLavenderCard
import com.example.ui.theme.BentoOnSurfaceVariantLight
import com.example.ui.theme.BentoOutlineLight
import com.example.ui.theme.BentoSecondaryLight
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    initialProfile: BudgetProfile? = null,
    initialUserName: String = "",
    onSave: (
        userName: String,
        salaryAmount: Double,
        savingsGoal: Double,
        salaryReceivedDate: String,
        nextSalaryDate: String,
        food: Double,
        transport: Double
    ) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    var userNameText by remember(initialProfile, initialUserName) {
        mutableStateOf(
            initialProfile?.userName?.takeIf { it != "Friend" }
                ?: initialUserName.takeIf { it.isNotBlank() && it != "Friend" }
                ?: ""
        )
    }
    var salaryText by remember {
        mutableStateOf(initialProfile?.monthlyIncome?.let { SpendingCalculator.formatExactDecimal(it) } ?: "2100")
    }
    var savingsGoalText by remember {
        mutableStateOf(initialProfile?.monthlySavingsGoal?.let { SpendingCalculator.formatExactDecimal(it) } ?: "600")
    }

    var salaryReceivedDate by remember {
        mutableStateOf(
            initialProfile?.getSalaryReceivedLocalDate() ?: LocalDate.now()
        )
    }

    var nextSalaryDate by remember {
        mutableStateOf(
            initialProfile?.getNextSalaryLocalDate() ?: LocalDate.now().plusMonths(1)
        )
    }

    var showSalaryReceivedPicker by remember { mutableStateOf(false) }
    var showNextSalaryPicker by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    val salaryAmount = salaryText.toDoubleOrNull() ?: 0.0
    val savingsGoal = savingsGoalText.toDoubleOrNull() ?: 0.0

    val totalCycleDays = SpendingCalculator.calculateCycleDays(salaryReceivedDate, nextSalaryDate)
    val spendablePool = SpendingCalculator.calculateAvailableSpendingPool(salaryAmount, savingsGoal)
    val initialDailyAllowance = if (totalCycleDays > 0) spendablePool / totalCycleDays else 0.0

    val isFormValid = salaryAmount > 0.0 &&
            savingsGoal >= 0.0 &&
            savingsGoal < salaryAmount &&
            nextSalaryDate.isAfter(salaryReceivedDate)

    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

    // Salary Received Date Picker Dialog
    if (showSalaryReceivedPicker) {
        val initialEpoch = salaryReceivedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialEpoch)
        DatePickerDialog(
            onDismissRequest = { showSalaryReceivedPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            salaryReceivedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            if (!nextSalaryDate.isAfter(salaryReceivedDate)) {
                                nextSalaryDate = salaryReceivedDate.plusMonths(1)
                            }
                        }
                        showSalaryReceivedPicker = false
                    }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold, color = BentoDeepPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSalaryReceivedPicker = false }) {
                    Text("Cancel", color = BentoSecondaryLight)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Expected Next Salary Date Picker Dialog
    if (showNextSalaryPicker) {
        val initialEpoch = nextSalaryDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialEpoch)
        DatePickerDialog(
            onDismissRequest = { showNextSalaryPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selected = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            if (selected.isAfter(salaryReceivedDate)) {
                                nextSalaryDate = selected
                            }
                        }
                        showNextSalaryPicker = false
                    }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold, color = BentoDeepPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNextSalaryPicker = false }) {
                    Text("Cancel", color = BentoSecondaryLight)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Salary & Budget Setup",
                        fontWeight = FontWeight.Black,
                        color = BentoDeepPurple
                    )
                },
                navigationIcon = {
                    if (onCancel != null) {
                        IconButton(onClick = onCancel) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = BentoSecondaryLight
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle / Intro
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Budget Setup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = BentoDeepPurple
                )
                Text(
                    text = "Tell us when you receive your salary and how much you plan to save. We'll recommend how much you can spend each day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoOnSurfaceVariantLight
                )
            }

            // User Name Field
            OutlinedTextField(
                value = userNameText,
                onValueChange = { userNameText = it },
                label = { Text("What should we call you?") },
                placeholder = { Text("e.g. Alex, Maya, or Kojo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewOnFocus()
                    .testTag("user_name_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BentoDeepPurple,
                    unfocusedBorderColor = BentoOutlineLight
                )
            )

            // Salary Amount Field
            OutlinedTextField(
                value = salaryText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        salaryText = input
                    }
                },
                label = { Text("Salary Amount") },
                placeholder = { Text("2100.00") },
                prefix = {
                    Text(
                        text = "GH₵ ",
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewOnFocus()
                    .testTag("monthly_income_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BentoDeepPurple,
                    unfocusedBorderColor = BentoOutlineLight
                )
            )

            // Salary Received Date
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSalaryReceivedPicker = true }
                    .testTag("salary_received_date_picker"),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, BentoOutlineLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Salary Received Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = BentoSecondaryLight
                        )
                        Text(
                            text = salaryReceivedDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Select Salary Received Date",
                        tint = BentoDeepPurple
                    )
                }
            }

            // Expected Next Salary Date
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showNextSalaryPicker = true }
                    .testTag("next_salary_date_picker"),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, BentoOutlineLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Expected Next Salary Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = BentoSecondaryLight
                        )
                        Text(
                            text = nextSalaryDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Select Next Salary Date",
                        tint = BentoDeepPurple
                    )
                }
            }

            // Planned Monthly Savings Field
            OutlinedTextField(
                value = savingsGoalText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        savingsGoalText = input
                    }
                },
                label = { Text("Savings Target for this Cycle") },
                placeholder = { Text("600.00") },
                prefix = {
                    Text(
                        text = "GH₵ ",
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewOnFocus()
                    .testTag("monthly_savings_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BentoDeepPurple,
                    unfocusedBorderColor = BentoOutlineLight
                )
            )

            // Dynamic Calculation Preview Box
            AnimatedVisibility(visible = isFormValid) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = BentoLavenderCard,
                    border = BorderStroke(1.dp, BentoLavenderAccent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Today's Recommended Spending",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoSecondaryLight
                        )

                        Text(
                            text = "GH₵${SpendingCalculator.formatExactDecimal(initialDailyAllowance)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = BentoDeepPurple
                        )

                        HorizontalDivider(color = BentoLavenderAccent.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Available for spending",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BentoSecondaryLight
                            )
                            Text(
                                text = "GH₵${SpendingCalculator.formatAmount(spendablePool)}",
                                fontWeight = FontWeight.Bold,
                                color = BentoDeepPurple
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Duration",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BentoSecondaryLight
                            )
                            Text(
                                text = "$totalCycleDays days",
                                fontWeight = FontWeight.Bold,
                                color = BentoDeepPurple
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save / Start Button
            Button(
                onClick = {
                    if (isFormValid) {
                        keyboardController?.hide()
                        val finalName = userNameText.trim().ifBlank { "Friend" }
                        val receivedStr = salaryReceivedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val nextStr = nextSalaryDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        onSave(
                            finalName,
                            salaryAmount,
                            savingsGoal,
                            receivedStr,
                            nextStr,
                            0.0,
                            0.0
                        )
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .bounceClick()
                    .bringIntoViewOnFocus()
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoDeepPurple,
                    disabledContainerColor = BentoDeepPurple.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = if (initialProfile == null) "Start Tracking" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
