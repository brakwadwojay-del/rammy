package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SpendingCalculator
import com.example.ui.theme.BentoAmberText
import com.example.ui.theme.BentoDeepPurple
import com.example.ui.theme.BentoLavenderAccent
import com.example.ui.theme.BentoLavenderCard
import com.example.ui.theme.BentoSecondaryLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.theme.ExpenseRed

/**
 * Animated number counter that smoothly transitions between values.
 */
@Composable
fun AnimatedAmountText(
    targetAmount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    fontWeight: FontWeight = FontWeight.Black,
    color: Color = MaterialTheme.colorScheme.onSurface,
    prefix: String = "GH₵ "
) {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(targetAmount) {
        animatable.animateTo(
            targetValue = targetAmount.toFloat(),
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    Text(
        text = "$prefix${SpendingCalculator.formatExactDecimal(animatable.value.toDouble())}",
        style = style,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}

/**
 * Animated thin progress bar showing allowance consumption.
 */
@Composable
fun AllowanceProgressBar(
    spent: Double,
    allowance: Double,
    modifier: Modifier = Modifier
) {
    val progressRatio = remember(spent, allowance) {
        if (allowance <= 0) 0f else (spent / allowance).toFloat().coerceIn(0f, 1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "allowance_progress"
    )

    val barColor = when {
        spent > allowance -> ExpenseRed
        progressRatio > 0.8f -> BentoAmberText
        else -> BentoDeepPurple
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(BentoLavenderAccent.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(barColor)
        )
    }
}

/**
 * Animated Donut Chart for category spending breakdown.
 */
@Composable
fun DonutChart(
    breakdown: List<SpendingCalculator.CategoryBreakdownItem>,
    totalAmount: Double,
    currencySymbol: String = "GH₵",
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animateSweep by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "donut_animation"
    )

    LaunchedEffect(breakdown) {
        animationPlayed = true
    }

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        if (breakdown.isEmpty() || totalAmount <= 0.0) {
            Canvas(modifier = Modifier.size(170.dp)) {
                drawCircle(
                    color = Color(0xFFE2E8F0),
                    style = Stroke(width = 24.dp.toPx())
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$currencySymbol 0.00",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoSecondaryLight
                )
                Text(
                    text = "No spend yet",
                    style = MaterialTheme.typography.labelSmall,
                    color = BentoSecondaryLight
                )
            }
        } else {
            Canvas(modifier = Modifier.size(170.dp)) {
                val strokeWidth = 24.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val topLeftOffset = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

                // Background track
                drawCircle(
                    color = Color(0xFFF1F5F9),
                    style = Stroke(width = strokeWidth)
                )

                var startAngle = -90f
                for (item in breakdown) {
                    val sweepAngle = (item.percentage / 100f) * 360f * animateSweep
                    if (sweepAngle > 0.5f) {
                        drawArc(
                            color = item.category.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle - 1f, // small gap
                            useCenter = false,
                            topLeft = topLeftOffset,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    startAngle += sweepAngle
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "TODAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = BentoSecondaryLight
                )
                Text(
                    text = "$currencySymbol${SpendingCalculator.formatExactDecimal(totalAmount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = BentoDeepPurple
                )
                Text(
                    text = "${breakdown.sumOf { it.count }} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoSecondaryLight
                )
            }
        }
    }
}

/**
 * Subtle spring scale effect on click/press for interactive cards and buttons.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: (() -> Unit)? = null
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "bounce_scale"
    )

    this
        .scale(scale)
        .pointerInput(isPressed) {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null && onClick != null) {
                        onClick()
                    }
                }
            }
        }
}

/**
 * Shimmer effect for skeleton loading.
 */
@Composable
fun ShimmerSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        Color(0xFFE2E8F0),
        Color(0xFFF1F5F9),
        Color(0xFFE2E8F0)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim - 200f, y = 0f),
        end = Offset(x = translateAnim, y = 0f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(brush)
    )
}

/**
 * Modifier that ensures any focused input field is automatically positioned
 * above the on-screen keyboard with comfortable spacing (24-40dp) and remains
 * visible throughout typing and orientation / keyboard height adjustments.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.bringIntoViewOnFocus(): Modifier = composed {
    val requester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    val isImeOpen = imeInsets.getBottom(density) > 0
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(isImeOpen, isFocused) {
        if (isFocused && isImeOpen) {
            delay(100)
            requester.bringIntoView()
            delay(150)
            requester.bringIntoView()
        }
    }

    this
        .bringIntoViewRequester(requester)
        .onFocusEvent { focusState ->
            isFocused = focusState.isFocused
            if (focusState.isFocused) {
                coroutineScope.launch {
                    delay(100)
                    requester.bringIntoView()
                    delay(200)
                    requester.bringIntoView()
                }
            }
        }
}

