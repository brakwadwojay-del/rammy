package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Pixel-faithful, vector-rendered Rammy's Spend Tracker brand logo.
 * Renders the iconic 3D lavender wallet, Ghanaian Cedi gold coin, mint banknotes,
 * growth chart with rising arrow, orbital swoosh, and brand typography.
 */
@Composable
fun RammysLogo(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    showBrandingText: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .aspectRatio(1f)
            .testTag("rammys_logo_image"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val scale = w / 200f

            // 1. Background Rounded Squircle
            val bgCorner = 46f * scale
            val bgBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF281C4F),
                    Color(0xFF1B1337),
                    Color(0xFF140D2A)
                )
            )
            drawRoundRect(
                brush = bgBrush,
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = CornerRadius(bgCorner, bgCorner)
            )

            // Inner subtle border highlight
            drawRoundRect(
                color = Color(0x33A78BFA),
                topLeft = Offset(1.5f * scale, 1.5f * scale),
                size = Size(w - 3f * scale, h - 3f * scale),
                cornerRadius = CornerRadius(bgCorner - 1.5f * scale, bgCorner - 1.5f * scale),
                style = Stroke(width = 1.5f * scale)
            )

            // 2. Orbital Arc Swoosh behind / around wallet
            val swooshPath = Path().apply {
                val arcRect = Rect(
                    left = 28f * scale,
                    top = 22f * scale,
                    right = 165f * scale,
                    bottom = 145f * scale
                )
                arcTo(
                    rect = arcRect,
                    startAngleDegrees = 140f,
                    sweepAngleDegrees = 240f,
                    forceMoveTo = false
                )
            }
            drawPath(
                path = swooshPath,
                color = Color(0xFFC7B3ED).copy(alpha = 0.85f),
                style = Stroke(width = 4.5f * scale, cap = StrokeCap.Round)
            )

            // 3. Emergent Mint Green Banknotes (Money popping out of wallet)
            val note1X = 64f * scale
            val note1Y = 46f * scale
            val noteW = 72f * scale
            val noteH = 40f * scale
            val noteCorner = 7f * scale

            // Banknote shadow / back
            drawRoundRect(
                color = Color(0xFF4DB6AC),
                topLeft = Offset(note1X - 2f * scale, note1Y - 4f * scale),
                size = Size(noteW, noteH),
                cornerRadius = CornerRadius(noteCorner, noteCorner)
            )
            // Main Banknote (Mint)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFA7F3D0), Color(0xFF80CBC4))
                ),
                topLeft = Offset(note1X, note1Y),
                size = Size(noteW, noteH),
                cornerRadius = CornerRadius(noteCorner, noteCorner)
            )
            // Banknote inner border
            drawRoundRect(
                color = Color(0xFF5EEAD4),
                topLeft = Offset(note1X + 3f * scale, note1Y + 3f * scale),
                size = Size(noteW - 6f * scale, noteH - 6f * scale),
                cornerRadius = CornerRadius(noteCorner - 2f * scale, noteCorner - 2f * scale),
                style = Stroke(width = 1.2f * scale)
            )
            // Banknote center emblem with ₵ sign
            drawCircle(
                color = Color(0xFF4DB6AC).copy(alpha = 0.5f),
                radius = 7.5f * scale,
                center = Offset(note1X + noteW / 2f, note1Y + noteH / 2.5f)
            )

            // 4. Growth Bar Chart (Right side)
            val barW = 8f * scale
            val barCorner = 3.5f * scale
            // Bar 1 (short)
            drawRoundRect(
                color = Color(0xFF6EE7B7),
                topLeft = Offset(142f * scale, 98f * scale),
                size = Size(barW, 26f * scale),
                cornerRadius = CornerRadius(barCorner, barCorner)
            )
            // Bar 2 (medium)
            drawRoundRect(
                color = Color(0xFF4ADE80),
                topLeft = Offset(154f * scale, 88f * scale),
                size = Size(barW, 36f * scale),
                cornerRadius = CornerRadius(barCorner, barCorner)
            )
            // Bar 3 (tall)
            drawRoundRect(
                color = Color(0xFF34D399),
                topLeft = Offset(166f * scale, 74f * scale),
                size = Size(barW, 50f * scale),
                cornerRadius = CornerRadius(barCorner, barCorner)
            )

            // Upward Growth Arrow
            val arrowPath = Path().apply {
                moveTo(140f * scale, 92f * scale)
                cubicTo(
                    148f * scale, 80f * scale,
                    156f * scale, 65f * scale,
                    172f * scale, 52f * scale
                )
            }
            drawPath(
                path = arrowPath,
                color = Color(0xFF6EE7B7),
                style = Stroke(width = 4.5f * scale, cap = StrokeCap.Round)
            )
            // Arrow Head
            val arrowHead = Path().apply {
                moveTo(176f * scale, 48f * scale)
                lineTo(163f * scale, 52f * scale)
                lineTo(171f * scale, 64f * scale)
                close()
            }
            drawPath(path = arrowHead, color = Color(0xFF6EE7B7))

            // 5. 3D Lavender Wallet
            val walletX = 54f * scale
            val walletY = 58f * scale
            val walletW = 88f * scale
            val walletH = 68f * scale
            val walletCorner = 14f * scale

            // Wallet soft shadow
            drawRoundRect(
                color = Color(0x55090615),
                topLeft = Offset(walletX, walletY + 4f * scale),
                size = Size(walletW, walletH),
                cornerRadius = CornerRadius(walletCorner, walletCorner)
            )

            // Wallet Body (Lavender Gradient)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFC7B3ED),
                        Color(0xFFAF98DE),
                        Color(0xFF967ECB)
                    )
                ),
                topLeft = Offset(walletX, walletY),
                size = Size(walletW, walletH),
                cornerRadius = CornerRadius(walletCorner, walletCorner)
            )

            // Wallet Flap / Trim
            val flapPath = Path().apply {
                moveTo(walletX, walletY + 12f * scale)
                lineTo(walletX + walletW, walletY + 12f * scale)
                lineTo(walletX + walletW, walletY + 28f * scale)
                lineTo(walletX, walletY + 28f * scale)
                close()
            }
            drawPath(
                path = flapPath,
                color = Color(0xFF8B72C2).copy(alpha = 0.35f)
            )

            // Wallet Stitching Detail (Dotted / dashed edge)
            drawRoundRect(
                color = Color(0xFF7E66B5).copy(alpha = 0.5f),
                topLeft = Offset(walletX + 4f * scale, walletY + 4f * scale),
                size = Size(walletW - 8f * scale, walletH - 8f * scale),
                cornerRadius = CornerRadius(walletCorner - 3f * scale, walletCorner - 3f * scale),
                style = Stroke(
                    width = 1.2f * scale,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f * scale, 3f * scale), 0f)
                )
            )

            // Wallet Snap Strap / Button
            val strapX = walletX + walletW - 28f * scale
            val strapY = walletY + 30f * scale
            val strapW = 32f * scale
            val strapH = 22f * scale
            val strapCorner = 11f * scale

            // Strap base
            drawRoundRect(
                color = Color(0xFF7E65B5),
                topLeft = Offset(strapX, strapY),
                size = Size(strapW, strapH),
                cornerRadius = CornerRadius(strapCorner, strapCorner)
            )
            // Metal snap button
            drawCircle(
                color = Color(0xFF2E1C52),
                radius = 5.5f * scale,
                center = Offset(strapX + 11f * scale, strapY + strapH / 2f)
            )
            drawCircle(
                color = Color(0xFF9E8AC7),
                radius = 2.2f * scale,
                center = Offset(strapX + 11f * scale, strapY + strapH / 2f)
            )

            // 6. Shiny Ghanaian Cedi Gold Coin (Foreground Left)
            val coinX = 64f * scale
            val coinY = 110f * scale
            val coinR = 26f * scale

            // Coin Shadow
            drawCircle(
                color = Color(0x660B0718),
                radius = coinR + 1f * scale,
                center = Offset(coinX + 1.5f * scale, coinY + 3.5f * scale)
            )

            // Outer Coin Ridge (Golden gradient)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0xFFFFEE58),
                        Color(0xFFFFCA28),
                        Color(0xFFFFA000)
                    ),
                    center = Offset(coinX - 5f * scale, coinY - 5f * scale),
                    radius = coinR
                ),
                radius = coinR,
                center = Offset(coinX, coinY)
            )

            // Inner Coin Rim
            drawCircle(
                color = Color(0xFFFFB300),
                radius = coinR - 4f * scale,
                center = Offset(coinX, coinY),
                style = Stroke(width = 1.8f * scale)
            )

            // Coin Embossed Ghanaian Cedi Sign '₵'
            drawCediSymbol(
                center = Offset(coinX, coinY),
                scale = scale,
                color = Color(0xFFD97706)
            )

            // 7. Branding Typography: "Rammy's" & "SPEND TRACKER"
            if (showBrandingText) {
                val paintTitle = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 23f * scale
                    isFakeBoldText = true
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "Rammy's",
                    w / 2f,
                    158f * scale,
                    paintTitle
                )

                val paintSubtitle = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#6EE7B7")
                    textSize = 10f * scale
                    isFakeBoldText = true
                    letterSpacing = 0.22f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "SPEND TRACKER",
                    w / 2f,
                    174f * scale,
                    paintSubtitle
                )

                // Bottom Accent Pill Indicator
                drawRoundRect(
                    color = Color(0xFFA78BFA),
                    topLeft = Offset(w / 2f - 14f * scale, 182f * scale),
                    size = Size(28f * scale, 3.2f * scale),
                    cornerRadius = CornerRadius(2f * scale, 2f * scale)
                )
            }
        }
    }
}

/**
 * Draws the Ghanaian Cedi symbol (₵) in native canvas vector lines
 */
private fun DrawScope.drawCediSymbol(center: Offset, scale: Float, color: Color) {
    val cx = center.x
    val cy = center.y
    val r = 11f * scale

    // C arc
    val cPath = Path().apply {
        val rect = Rect(cx - r, cy - r, cx + r, cy + r)
        arcTo(rect, 45f, 270f, false)
    }
    drawPath(
        path = cPath,
        color = color,
        style = Stroke(width = 3.2f * scale, cap = StrokeCap.Round)
    )

    // Vertical slash through C
    drawLine(
        color = color,
        start = Offset(cx, cy - r - 4f * scale),
        end = Offset(cx, cy + r + 4f * scale),
        strokeWidth = 2.8f * scale,
        cap = StrokeCap.Round
    )
}
