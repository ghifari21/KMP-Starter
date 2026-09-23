package com.project.starter.core.designsystem.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Typography size tokens for the app's design system.
 * Access via [AppTheme.textDimens] inside a Composable.
 */
data class TextDimens(
    val extraSmall: TextUnit = 10.sp,
    val small: TextUnit = 12.sp,
    val medium: TextUnit = 14.sp,
    val large: TextUnit = 16.sp,
    val extraLarge: TextUnit = 20.sp,
    val headlineSmall: TextUnit = 24.sp,
    val headlineMedium: TextUnit = 28.sp,
    val headlineLarge: TextUnit = 32.sp,
)

val LocalTextDimens = compositionLocalOf { TextDimens() }
