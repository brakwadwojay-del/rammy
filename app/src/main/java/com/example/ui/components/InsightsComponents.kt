package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SpendingCalculator
import com.example.data.model.ExpenseCategory
import com.example.ui.InsightsPeriodType
import com.example.ui.theme.BentoAmberContainer
import com.example.ui.theme.BentoAmberText
import com.example.ui.theme.BentoDeepPurple
import com.example.ui.theme.BentoLavenderAccent
import com.example.ui.theme.BentoLavenderCard
import com.example.ui.theme.BentoOnSurfaceLight
import com.example.ui.theme.BentoOnSurfaceVariantLight
import com.example.ui.theme.BentoOutlineLight
import com.example.ui.theme.BentoOutlineVariantLight
import com.example.ui.theme.BentoSecondaryLight
import com.example.ui.theme.BentoSurfaceVariantLight
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.SavingsGreen
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Period Selector Segmented Switcher (This month / Previous month / Custom)
 */
@Composable
fun InsightsPeriodSelector(
    periodType: InsightsPeriodType,
    selectedMonth: YearMonth,
    availableMonths: List<YearMonth>,
    onSelectPeriod: (InsightsPeriodType, YearMonth?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomDropdown by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PeriodTabItem(
                title = "This month",
                isSelected = periodType == InsightsPeriodType.THIS_MONTH,
                onClick = { onSelectPeriod(InsightsPeriodType.THIS_MONTH, YearMonth.now()) },
                modifier = Modifier.weight(1f)
            )

            PeriodTabItem(
                title = "Previous",
                isSelected = periodType == InsightsPeriodType.PREVIOUS_MONTH,
                onClick = { onSelectPeriod(InsightsPeriodType.PREVIOUS_MONTH, YearMonth.now().minusMonths(1)) },
                modifier = Modifier.weight(1f)
            )

            Box(modifier = Modifier.weight(1f)) {
                PeriodTabItem(
                    title = if (periodType == InsightsPeriodType.CUSTOM) {
                        selectedMonth.format(DateTimeFormatter.ofPattern("MMM yy"))
                    } else {
                        "Custom"
                    },
                    isSelected = periodType == InsightsPeriodType.CUSTOM,
                    trailingIcon = Icons.Default.ExpandMore,
                    onClick = { showCustomDropdown = true },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = showCustomDropdown,
                    onDismissRequest = { showCustomDropdown = false },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, BentoOutlineLight, RoundedCornerShape(12.dp))
                ) {
                    availableMonths.forEach { month ->
                        val isCurrent = month == selectedMonth && periodType == InsightsPeriodType.CUSTOM
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) BentoDeepPurple else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingIcon = if (isCurrent) {
                                { Icon(Icons.Default.Check, contentDescription = null, tint = BentoDeepPurple) }
                            } else null,
                            onClick = {
                                showCustomDropdown = false
                                onSelectPeriod(InsightsPeriodType.CUSTOM, month)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
    val textColor = if (isSelected) BentoDeepPurple else BentoOnSurfaceVariantLight
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = modifier.height(38.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = fontWeight,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Section 3 & 13: Goal Progress with Animated Circular Indicator and Timeline
 */
@Composable
fun GoalProgressCard(
    goalProgress: SpendingCalculator.GoalProgressData,
    goalProjection: SpendingCalculator.GoalProjectionData?,
    currencySymbol: String = "GH₵",
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(goalProgress.progressPercent) {
        animatedProgress.animateTo(
            targetValue = goalProgress.progressPercent / 100f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BentoOutlineLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("goal_progress_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🎯", fontSize = 20.sp)
                    Text(
                        text = "Savings Goal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (goalProgress.isGoalReached) SavingsGreen.copy(alpha = 0.15f) else BentoLavenderCard,
                    border = BorderStroke(1.dp, if (goalProgress.isGoalReached) SavingsGreen.copy(alpha = 0.3f) else BentoLavenderAccent)
                ) {
                    Text(
                        text = "${goalProgress.progressPercent.toInt()}% complete",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (goalProgress.isGoalReached) SavingsGreen else BentoDeepPurple,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Main Circular Visual & Figure
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Circular Canvas
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(112.dp)
                ) {
                    Canvas(modifier = Modifier.size(112.dp)) {
                        val strokeWidth = 12.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val radius = diameter / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        // Background track
                        drawCircle(
                            color = BentoSurfaceVariantLight,
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Animated Progress Arc
                        val sweep = animatedProgress.value * 360f
                        val progressGradient = Brush.sweepGradient(
                            listOf(
                                BentoLavenderAccent,
                                BentoDeepPurple,
                                if (goalProgress.isGoalReached) SavingsGreen else BentoDeepPurple
                            )
                        )

                        drawArc(
                            brush = progressGradient,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(diameter, diameter),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(animatedProgress.value * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = BentoDeepPurple
                        )
                        Text(
                            text = "saved",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoOnSurfaceVariantLight
                        )
                    }
                }

                // Figures & Explanation
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$currencySymbol${SpendingCalculator.formatAmount(goalProgress.savedAmountSoFar)} / $currencySymbol${SpendingCalculator.formatAmount(goalProgress.monthlySavingsGoal)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = goalProgress.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (goalProgress.isGoalReached) SavingsGreen else BentoOnSurfaceVariantLight
                    )
                }
            }

            // Section 13: Goal Timeline Track
            GoalTimelineBar(
                savedAmount = goalProgress.savedAmountSoFar,
                targetAmount = goalProgress.monthlySavingsGoal,
                currencySymbol = currencySymbol
            )

            // Section 4: Goal Projection Banner
            if (goalProjection != null) {
                HorizontalDivider(color = BentoOutlineVariantLight.copy(alpha = 0.5f))

                val (bannerBg, borderCol, titleColor) = when (goalProjection.status) {
                    SpendingCalculator.ProjectionStatus.ON_TRACK -> Triple(SavingsGreen.copy(alpha = 0.08f), SavingsGreen.copy(alpha = 0.25f), SavingsGreen)
                    SpendingCalculator.ProjectionStatus.SLIGHTLY_BEHIND -> Triple(BentoAmberContainer.copy(alpha = 0.4f), BentoAmberText.copy(alpha = 0.3f), BentoAmberText)
                    SpendingCalculator.ProjectionStatus.DIFFICULT -> Triple(ExpenseRed.copy(alpha = 0.08f), ExpenseRed.copy(alpha = 0.25f), ExpenseRed)
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = bannerBg,
                    border = BorderStroke(1.dp, borderCol),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = goalProjection.headline,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                        Text(
                            text = goalProjection.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Section 13: Interactive Goal Timeline Bar (GH₵0 ──●── GH₵800)
 */
@Composable
fun GoalTimelineBar(
    savedAmount: Double,
    targetAmount: Double,
    currencySymbol: String = "GH₵",
    modifier: Modifier = Modifier
) {
    val progressRatio = if (targetAmount > 0) (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 1f
    val animatedRatio by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "timeline_ratio"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Labels above
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${currencySymbol}0",
                style = MaterialTheme.typography.labelSmall,
                color = BentoOnSurfaceVariantLight,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$currencySymbol${SpendingCalculator.formatAmount(targetAmount)}",
                style = MaterialTheme.typography.labelSmall,
                color = BentoOnSurfaceVariantLight,
                fontWeight = FontWeight.Bold
            )
        }

        // Timeline Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Track line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(BentoSurfaceVariantLight)
            )

            // Progress fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedRatio)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(BentoLavenderAccent, BentoDeepPurple)
                        )
                    )
            )

            // Pin / Thumb
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedRatio)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(16.dp)
                        .shadow(3.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, BentoDeepPurple, CircleShape)
                )
            }
        }
    }
}

/**
 * Section 5: Spending Breakdown — Interactive Pie/Donut Chart
 */
@Composable
fun SpendingBreakdownSection(
    breakdown: List<SpendingCalculator.CategoryBreakdownItem>,
    totalSpent: Double,
    selectedCategory: ExpenseCategory?,
    onSelectCategory: (ExpenseCategory?) -> Unit,
    currencySymbol: String = "GH₵",
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BentoOutlineLight),
        modifier = modifier
            .fillMaxWidth()
            .testTag("spending_breakdown_section")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Spending Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Where your money went this period",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoOnSurfaceVariantLight
                    )
                }

                if (selectedCategory != null) {
                    TextButton(
                        onClick = { onSelectCategory(null) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelMedium, color = BentoDeepPurple)
                    }
                }
            }

            if (breakdown.isEmpty() || totalSpent <= 0.0) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BentoLavenderCard.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No expenses recorded in this period yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BentoOnSurfaceVariantLight,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                // Interactive Donut Chart
                val activeSelectedItem = breakdown.find { it.category == selectedCategory }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Donut Canvas
                    InteractiveDonutCanvas(
                        breakdown = breakdown,
                        totalSpent = totalSpent,
                        selectedCategory = selectedCategory,
                        onSelectCategory = onSelectCategory,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.size(150.dp)
                    )

                    // Selected Detail Box or Overview
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .animateContentSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (activeSelectedItem != null) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = BentoLavenderCard,
                                border = BorderStroke(1.dp, BentoLavenderAccent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = activeSelectedItem.category.emoji, fontSize = 16.sp)
                                        Text(
                                            text = activeSelectedItem.category.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoDeepPurple
                                        )
                                    }
                                    Text(
                                        text = "$currencySymbol${SpendingCalculator.formatExactDecimal(activeSelectedItem.amount)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${activeSelectedItem.percentage.toInt()}% of monthly spending (${activeSelectedItem.count} items)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BentoOnSurfaceVariantLight
                                    )
                                }
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Total Spent",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BentoOnSurfaceVariantLight
                                )
                                Text(
                                    text = "$currencySymbol${SpendingCalculator.formatAmount(totalSpent)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap a slice or category below for details",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BentoOnSurfaceVariantLight
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = BentoOutlineVariantLight.copy(alpha = 0.5f))

                // Category List Rows
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    breakdown.forEach { item ->
                        val isSelected = item.category == selectedCategory
                        CategoryRowItem(
                            item = item,
                            isSelected = isSelected,
                            currencySymbol = currencySymbol,
                            onClick = {
                                if (isSelected) onSelectCategory(null) else onSelectCategory(item.category)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveDonutCanvas(
    breakdown: List<SpendingCalculator.CategoryBreakdownItem>,
    totalSpent: Double,
    selectedCategory: ExpenseCategory?,
    onSelectCategory: (ExpenseCategory?) -> Unit,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(breakdown) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .pointerInput(breakdown) {
                detectTapGestures { tapOffset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = tapOffset.x - center.x
                    val dy = tapOffset.y - center.y
                    val distance = Math.sqrt((dx * dx + dy * dy).toDouble())
                    val outerRadius = size.width / 2f
                    val innerRadius = outerRadius * 0.58f

                    if (distance in innerRadius..outerRadius) {
                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        // Normalize angle to start at -90 deg (top)
                        angle = (angle + 90f + 360f) % 360f

                        var currentAngle = 0f
                        var tappedCategory: ExpenseCategory? = null
                        for (item in breakdown) {
                            val sweep = (item.amount / totalSpent).toFloat() * 360f
                            if (angle >= currentAngle && angle <= currentAngle + sweep) {
                                tappedCategory = item.category
                                break
                            }
                            currentAngle += sweep
                        }

                        if (tappedCategory != null) {
                            if (tappedCategory == selectedCategory) onSelectCategory(null)
                            else onSelectCategory(tappedCategory)
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * 0.22f
            val diameter = size.minDimension - strokeWidth
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            var startAngle = -90f
            val totalAnimSweep = animatedProgress.value * 360f

            breakdown.forEach { item ->
                val fullSweep = (item.amount / totalSpent).toFloat() * 360f
                val sweep = (fullSweep * animatedProgress.value).coerceAtLeast(0f)
                val isSelected = item.category == selectedCategory

                val itemColor = Color(item.category.colorHex)
                val stroke = if (isSelected) strokeWidth * 1.25f else strokeWidth

                drawArc(
                    color = itemColor,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter),
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )

                startAngle += sweep
            }
        }

        // Center Content
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$currencySymbol${SpendingCalculator.formatAmount(totalSpent)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = BentoDeepPurple,
                maxLines = 1
            )
            Text(
                text = "spent",
                style = MaterialTheme.typography.labelSmall,
                color = BentoOnSurfaceVariantLight
            )
        }
    }
}

@Composable
private fun CategoryRowItem(
    item: SpendingCalculator.CategoryBreakdownItem,
    isSelected: Boolean,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val bg = if (isSelected) BentoLavenderCard else MaterialTheme.colorScheme.surface
    val borderCol = if (isSelected) BentoDeepPurple else BentoOutlineVariantLight.copy(alpha = 0.5f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, borderCol),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(item.category.colorHex).copy(alpha = 0.18f))
                ) {
                    Text(text = item.category.emoji, fontSize = 16.sp)
                }

                Column {
                    Text(
                        text = item.category.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.percentage.toInt()}% of total",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoOnSurfaceVariantLight
                    )
                }
            }

            Text(
                text = "$currencySymbol${SpendingCalculator.formatExactDecimal(item.amount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Section 6: Spending Trends Graph (Line Graph Week vs Month with Planned Allowance)
 */
@Composable
fun SpendingTrendsSection(
    trendData: SpendingCalculator.SpendingTrendData?,
    isWeekView: Boolean,
    onToggleTrendView: (Boolean) -> Unit,
    currencySymbol: String = "GH₵",
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BentoOutlineLight),
        modifier = modifier
            .fillMaxWidth()
            .testTag("spending_trends_section")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Week / Month Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Spending Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Daily spending vs planned target",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoOnSurfaceVariantLight
                    )
                }

                // Week / Month Toggle Pills
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BentoSurfaceVariantLight)
                        .padding(2.dp)
                ) {
                    TrendTogglePill(
                        title = "Week",
                        isSelected = isWeekView,
                        onClick = { onToggleTrendView(true) }
                    )
                    TrendTogglePill(
                        title = "Month",
                        isSelected = !isWeekView,
                        onClick = { onToggleTrendView(false) }
                    )
                }
            }

            if (trendData == null || trendData.points.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BentoLavenderCard.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No trend data available for this period.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BentoOnSurfaceVariantLight,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(BentoDeepPurple)
                        )
                        Text(
                            text = "Actual spending",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height(2.dp)
                                .background(BentoAmberText)
                        )
                        Text(
                            text = "Planned allowance ($currencySymbol${SpendingCalculator.formatAmount(trendData.plannedDailyAllowance)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoOnSurfaceVariantLight
                        )
                    }
                }

                // Smooth Line Graph Canvas
                SpendingTrendLineCanvas(
                    trendData = trendData,
                    currencySymbol = currencySymbol,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                )

                // Day / Month Labels (Mon..Sun for Week, Jan..Dec for Month)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    trendData.points.forEach { pt ->
                        Text(
                            text = pt.shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = BentoOnSurfaceVariantLight,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendTogglePill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = Modifier.height(30.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) BentoDeepPurple else BentoOnSurfaceVariantLight
            )
        }
    }
}

@Composable
private fun SpendingTrendLineCanvas(
    trendData: SpendingCalculator.SpendingTrendData,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    var selectedPointIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(trendData) {
        selectedPointIndex = -1
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = LinearOutSlowInEasing)
        )
    }

    val points = trendData.points
    val maxVal = trendData.maxAmount.toFloat()

    Box(
        modifier = modifier
            .pointerInput(points) {
                detectTapGestures { tapOffset ->
                    if (points.isNotEmpty()) {
                        val spacing = size.width / points.size
                        val index = (tapOffset.x / spacing).toInt().coerceIn(0, points.size - 1)
                        selectedPointIndex = if (selectedPointIndex == index) -1 else index
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height - 24.dp.toPx() // Reserve space for x-axis labels
            val bottomY = height
            val topY = 16.dp.toPx()

            if (points.isEmpty()) return@Canvas

            // 1. Draw Planned Daily Allowance Guideline (Dashed line)
            val allowanceY = bottomY - ((trendData.plannedDailyAllowance.toFloat() / maxVal) * (bottomY - topY))
            drawLine(
                color = BentoAmberText.copy(alpha = 0.7f),
                start = Offset(0f, allowanceY),
                end = Offset(width, allowanceY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )

            // 2. Compute Points Coordinates centered over columns
            val paddingX = width / (points.size * 2f).coerceAtLeast(1f)
            val stepX = if (points.size > 1) (width - 2 * paddingX) / (points.size - 1) else 0f
            val coords = points.mapIndexed { index, pt ->
                val x = paddingX + index * stepX
                val normalizedY = (pt.actualSpent.toFloat() / maxVal).coerceIn(0f, 1f)
                val y = bottomY - (normalizedY * (bottomY - topY))
                Offset(x, y)
            }

            // 3. Animated Path & Area Fill
            val path = Path()
            val fillPath = Path()

            val maxIndexToDraw = (coords.size * animProgress.value).toInt().coerceIn(0, coords.size)

            if (coords.isNotEmpty()) {
                path.moveTo(coords[0].x, coords[0].y)
                fillPath.moveTo(coords[0].x, bottomY)
                fillPath.lineTo(coords[0].x, coords[0].y)

                for (i in 1 until maxIndexToDraw) {
                    val p0 = coords[i - 1]
                    val p1 = coords[i]
                    val cx = (p0.x + p1.x) / 2f
                    path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                }

                if (maxIndexToDraw > 0) {
                    val lastX = coords[maxIndexToDraw - 1].x
                    fillPath.lineTo(lastX, bottomY)
                    fillPath.close()

                    // Gradient fill underneath
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            listOf(
                                BentoDeepPurple.copy(alpha = 0.25f),
                                BentoLavenderAccent.copy(alpha = 0.02f)
                            ),
                            startY = topY,
                            endY = bottomY
                        )
                    )

                    // Line stroke
                    drawPath(
                        path = path,
                        color = BentoDeepPurple,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            // 4. Draw Data Point Dots
            for (i in 0 until maxIndexToDraw) {
                val pt = points[i]
                val coord = coords[i]
                val isSelected = (i == selectedPointIndex)

                if (pt.hasRecordedSpending || isSelected) {
                    // Outer glow
                    drawCircle(
                        color = if (isSelected) BentoAmberText else BentoLavenderAccent,
                        radius = if (isSelected) 7.dp.toPx() else 4.5.dp.toPx(),
                        center = coord
                    )
                    // Inner dot
                    drawCircle(
                        color = if (isSelected) Color.White else BentoDeepPurple,
                        radius = if (isSelected) 4.dp.toPx() else 2.5.dp.toPx(),
                        center = coord
                    )
                }
            }
        }

        // Selected Point Tooltip Banner
        if (selectedPointIndex in points.indices) {
            val pt = points[selectedPointIndex]
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BentoDeepPurple,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
            ) {
                Text(
                    text = "${pt.label}: $currencySymbol${SpendingCalculator.formatExactDecimal(pt.actualSpent)} (Allowance: $currencySymbol${SpendingCalculator.formatAmount(pt.allowance)})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

/**
 * Section 7: Spending Habits Section
 */
@Composable
fun SpendingHabitsSection(
    habits: List<SpendingCalculator.SpendingHabitInsight>,
    modifier: Modifier = Modifier
) {
    if (habits.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Your Spending Habits",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        habits.forEach { habit ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BentoOutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(BentoLavenderCard)
                    ) {
                        Text(text = habit.icon, fontSize = 20.sp)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = habit.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BentoSurfaceVariantLight
                            ) {
                                Text(
                                    text = habit.tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BentoOnSurfaceVariantLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoOnSurfaceVariantLight,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Section 8: Compare With Previous Period
 */
@Composable
fun PeriodComparisonSection(
    comparison: SpendingCalculator.PeriodSpendingComparison?,
    currencySymbol: String = "GH₵",
    modifier: Modifier = Modifier
) {
    if (comparison == null || comparison.previousPeriodTotal <= 0.0) return

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BentoOutlineLight),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Compared with last month",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Spending changes across periods",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoOnSurfaceVariantLight
                    )
                }
            }

            // Total spending comparison banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BentoLavenderCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Total Spending",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple
                        )
                        Text(
                            text = "$currencySymbol${SpendingCalculator.formatAmount(comparison.previousPeriodTotal)} → $currencySymbol${SpendingCalculator.formatAmount(comparison.currentPeriodTotal)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (comparison.totalPercentageChange != null) {
                        val isLower = comparison.totalDifference < 0
                        val badgeColor = if (isLower) SavingsGreen else ExpenseRed
                        val badgeBg = badgeColor.copy(alpha = 0.12f)
                        val icon = if (isLower) Icons.Default.TrendingDown else Icons.Default.TrendingUp

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = badgeBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "${if (isLower) "↓" else "↑"} ${Math.abs(comparison.totalPercentageChange.toInt())}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            }
                        }
                    }
                }
            }

            // Category Comparisons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                comparison.categoryComparisons.take(3).forEach { catComp ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = catComp.category.emoji, fontSize = 16.sp)
                            Text(
                                text = catComp.category.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "$currencySymbol${SpendingCalculator.formatAmount(catComp.previousAmount)} → $currencySymbol${SpendingCalculator.formatAmount(catComp.currentAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = BentoOnSurfaceVariantLight
                            )

                            if (catComp.percentageChange != null) {
                                val isLower = catComp.difference < 0
                                val color = if (isLower) SavingsGreen else ExpenseRed
                                Text(
                                    text = "${if (isLower) "↓" else "↑"} ${Math.abs(catComp.percentageChange.toInt())}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section 9: Suggestions / Financial Advice
 */
@Composable
fun FinancialSuggestionsSection(
    suggestions: List<SpendingCalculator.FinancialAdviceItem>,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Suggestions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        suggestions.forEach { tip ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BentoOutlineLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BentoAmberContainer.copy(alpha = 0.4f))
                    ) {
                        Text(text = tip.iconEmoji, fontSize = 18.sp)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = tip.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (tip.actionTag != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BentoLavenderCard
                                ) {
                                    Text(
                                        text = tip.actionTag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BentoDeepPurple,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = tip.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoOnSurfaceVariantLight,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Section 10: Daily Budget Performance Section
 */
@Composable
fun DailyPerformanceSection(
    performance: SpendingCalculator.DailyPerformanceData?,
    currencySymbol: String = "GH₵",
    modifier: Modifier = Modifier
) {
    if (performance == null || performance.totalDaysEvaluated == 0) return

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BentoOutlineLight),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Performance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Days stayed within spending limits",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoOnSurfaceVariantLight
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BentoLavenderCard,
                    border = BorderStroke(1.dp, BentoLavenderAccent)
                ) {
                    Text(
                        text = "${performance.daysUnderBudgetCount} / ${performance.totalDaysEvaluated} (${performance.percentageUnderBudget.toInt()}%)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoDeepPurple,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = "You stayed within your planned spending limit on ${performance.daysUnderBudgetCount} of the last ${performance.totalDaysEvaluated} recorded days.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Compact Calendar-Style Day Dots / Badges
            OptInFlowRowPerformance(performance = performance, currencySymbol = currencySymbol)

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PerformanceLegendItem(color = SavingsGreen, label = "Under budget")
                PerformanceLegendItem(color = BentoAmberText, label = "Close")
                PerformanceLegendItem(color = ExpenseRed, label = "Over budget")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptInFlowRowPerformance(
    performance: SpendingCalculator.DailyPerformanceData,
    currencySymbol: String
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        performance.dayStatuses.forEach { dayItem ->
            val (badgeColor, dotBg) = when (dayItem.status) {
                SpendingCalculator.DayBudgetStatus.UNDER -> Pair(SavingsGreen, SavingsGreen.copy(alpha = 0.15f))
                SpendingCalculator.DayBudgetStatus.CLOSE -> Pair(BentoAmberText, BentoAmberContainer.copy(alpha = 0.4f))
                SpendingCalculator.DayBudgetStatus.OVER -> Pair(ExpenseRed, ExpenseRed.copy(alpha = 0.15f))
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = dotBg,
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f)),
                modifier = Modifier.size(36.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${dayItem.dayNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = badgeColor
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = BentoOnSurfaceVariantLight
        )
    }
}

/**
 * Section 11: Monthly Financial Summary (Compact Grid)
 */
@Composable
fun MonthlySummaryGrid(
    summary: SpendingCalculator.MonthlyFinancialSummaryData?,
    currencySymbol: String = "GH₵",
    modifier: Modifier = Modifier
) {
    if (summary == null) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Monthly Financial Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryTile(
                title = "Income",
                amount = "$currencySymbol${SpendingCalculator.formatAmount(summary.monthlyIncome)}",
                subtitle = "Monthly budget pool",
                modifier = Modifier.weight(1f)
            )
            SummaryTile(
                title = "Planned Savings",
                amount = "$currencySymbol${SpendingCalculator.formatAmount(summary.plannedSavings)}",
                subtitle = "Protected target",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryTile(
                title = "Spent",
                amount = "$currencySymbol${SpendingCalculator.formatAmount(summary.spentSoFar)}",
                subtitle = "Actual recorded spend",
                modifier = Modifier.weight(1f)
            )
            SummaryTile(
                title = "Remaining Pool",
                amount = "$currencySymbol${SpendingCalculator.formatAmount(summary.remainingSpendingMoney)}",
                subtitle = "Available this month",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryTile(
    title: String,
    amount: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BentoOutlineLight),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = BentoOnSurfaceVariantLight,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = BentoOnSurfaceVariantLight
            )
        }
    }
}

/**
 * Section 12: Financial Health Overview Banner
 */
@Composable
fun FinancialHealthOverviewBanner(
    health: SpendingCalculator.FinancialHealthOverviewData?,
    modifier: Modifier = Modifier
) {
    if (health == null) return

    val bg = if (health.isOnTrack) BentoLavenderCard else BentoAmberContainer.copy(alpha = 0.4f)
    val borderCol = if (health.isOnTrack) BentoLavenderAccent else BentoAmberText.copy(alpha = 0.3f)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        border = BorderStroke(1.dp, borderCol),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = health.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (health.isOnTrack) BentoDeepPurple else BentoAmberText
            )
            Text(
                text = health.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * Section 16: Empty State when insufficient data exists
 */
@Composable
fun InsightsEmptyState(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BentoOutlineLight),
        modifier = modifier
            .fillMaxWidth()
            .testTag("insights_empty_state")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BentoLavenderCard)
            ) {
                Text(text = "📊", fontSize = 28.sp)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Not enough data yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Keep recording your daily spending and Rammys Spend Tracker will start showing your spending habits, trends, and projections here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoOnSurfaceVariantLight,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
