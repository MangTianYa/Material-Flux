package com.anomaly.currency.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * Material 3 type scale. Roboto is the platform default on Android, so
 * [FontFamily.Default] already resolves to it without bundling font files.
 */
private val trim = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    tracking: Double,
) = TextStyle(
    fontFamily = FontFamily.Default,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = tracking.sp,
    lineHeightStyle = trim,
)

val AppTypography = Typography(
    displayLarge = style(57, 64, FontWeight.Normal, -0.25),
    displayMedium = style(45, 52, FontWeight.Normal, 0.0),
    displaySmall = style(36, 44, FontWeight.Normal, 0.0),
    headlineLarge = style(32, 40, FontWeight.Normal, 0.0),
    headlineMedium = style(28, 36, FontWeight.Normal, 0.0),
    headlineSmall = style(24, 32, FontWeight.Normal, 0.0),
    titleLarge = style(22, 28, FontWeight.Normal, 0.0),
    titleMedium = style(16, 24, FontWeight.Medium, 0.15),
    titleSmall = style(14, 20, FontWeight.Medium, 0.1),
    bodyLarge = style(16, 24, FontWeight.Normal, 0.5),
    bodyMedium = style(14, 20, FontWeight.Normal, 0.25),
    bodySmall = style(12, 16, FontWeight.Normal, 0.4),
    labelLarge = style(14, 20, FontWeight.Medium, 0.1),
    labelMedium = style(12, 16, FontWeight.Medium, 0.5),
    labelSmall = style(11, 16, FontWeight.Medium, 0.5),
)

/** Tabular, tightly-tracked style for the big converted amount. */
val AmountDisplayStyle: TextStyle = style(44, 52, FontWeight.Medium, -1.0)
