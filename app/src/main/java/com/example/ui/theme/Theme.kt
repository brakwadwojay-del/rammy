package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = BentoPrimaryDark,
    onPrimary = BentoOnPrimaryDark,
    primaryContainer = BentoPrimaryContainerDark,
    onPrimaryContainer = BentoOnPrimaryContainerDark,
    secondary = BentoSecondaryDark,
    onSecondary = BentoOnSecondaryDark,
    secondaryContainer = BentoSecondaryContainerDark,
    onSecondaryContainer = BentoOnSecondaryContainerDark,
    tertiary = BentoTertiaryDark,
    onTertiary = BentoOnTertiaryDark,
    tertiaryContainer = BentoTertiaryContainerDark,
    onTertiaryContainer = BentoOnTertiaryContainerDark,
    background = BentoBackgroundDark,
    onBackground = BentoOnBackgroundDark,
    surface = BentoSurfaceDark,
    onSurface = BentoOnSurfaceDark,
    surfaceVariant = BentoSurfaceVariantDark,
    onSurfaceVariant = BentoOnSurfaceVariantDark,
    outline = BentoOutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BentoPrimaryLight,
    onPrimary = BentoOnPrimaryLight,
    primaryContainer = BentoPrimaryContainerLight,
    onPrimaryContainer = BentoOnPrimaryContainerLight,
    secondary = BentoSecondaryLight,
    onSecondary = BentoOnSecondaryLight,
    secondaryContainer = BentoSecondaryContainerLight,
    onSecondaryContainer = BentoOnSecondaryContainerLight,
    tertiary = BentoTertiaryLight,
    onTertiary = BentoOnTertiaryLight,
    tertiaryContainer = BentoTertiaryContainerLight,
    onTertiaryContainer = BentoOnTertiaryContainerLight,
    background = BentoBackgroundLight,
    onBackground = BentoOnBackgroundLight,
    surface = BentoSurfaceLight,
    onSurface = BentoOnSurfaceLight,
    surfaceVariant = BentoSurfaceVariantLight,
    onSurfaceVariant = BentoOnSurfaceVariantLight,
    outline = BentoOutlineLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

