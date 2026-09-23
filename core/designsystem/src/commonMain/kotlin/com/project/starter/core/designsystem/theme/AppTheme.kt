package com.project.starter.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val LightColorScheme =
    lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80,
    )

/**
 * The app's Material3 theme.
 *
 * KMP-compatible: does NOT reference Android's [Activity] or [window],
 * making it safe for Desktop, iOS, and WasmJS targets.
 *
 * @param darkTheme Whether to use the dark color scheme. Defaults to the system setting.
 * @param content The themed content.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalDimens provides Dimens(),
        LocalTextDimens provides TextDimens(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = StarterTypography,
            content = content,
        )
    }
}

/**
 * Object providing access to custom design tokens from within [AppTheme].
 *
 * Usage:
 * ```kotlin
 * Spacer(modifier = Modifier.height(AppTheme.dimens.large))
 * Text(fontSize = AppTheme.textDimens.medium)
 * ```
 */
object AppTheme {
    val dimens: Dimens
        @Composable @ReadOnlyComposable
        get() = LocalDimens.current

    val textDimens: TextDimens
        @Composable @ReadOnlyComposable
        get() = LocalTextDimens.current
}
