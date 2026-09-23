package com.project.starter.core.designsystem.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing and sizing tokens for the app's design system.
 * Access via [AppTheme.dimens] inside a Composable.
 */
data class Dimens(
    val extraSmall: Dp = 2.dp,
    val small: Dp = 4.dp,
    val medium: Dp = 8.dp,
    val large: Dp = 16.dp,
    val extraLarge: Dp = 32.dp,
    val paddingSmall: Dp = 8.dp,
    val paddingMedium: Dp = 16.dp,
    val paddingLarge: Dp = 24.dp,
    val cornerSmall: Dp = 4.dp,
    val cornerMedium: Dp = 8.dp,
    val cornerLarge: Dp = 16.dp,
)

val LocalDimens = compositionLocalOf { Dimens() }
