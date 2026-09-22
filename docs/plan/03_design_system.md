# Plan 03 — Design System

## Objective

Mengisi modul `:core:designsystem` yang saat ini kosong dengan sistem desain lengkap yang kompatibel dengan semua platform KMP (tidak ada dependency khusus Android seperti `Activity` atau `window`).

## Requirements

- `AppTheme {}` composable harus mendukung dark/light mode via `isSystemInDarkTheme()`
- Tidak boleh ada reference ke `Activity`, `window`, atau API Android-only
- `Dimens` dan `TextDimens` harus dapat diakses via `AppTheme.dimens` dan `AppTheme.textDimens`
- `AppTheme` harus digunakan di `shared/App.kt` menggantikan `MaterialTheme` langsung
- `core:designsystem` harus menggunakan `convention.cmp.library` convention plugin

## Steps

### Step 1 — Update `core/designsystem/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.convention.cmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Semua Compose dependency sudah disupply oleh convention.cmp.library
        }
    }
}

android {
    namespace = "com.project.starter.core.designsystem"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
}
```

### Step 2 — Buat `Color.kt`

**Path:** `core/designsystem/src/commonMain/kotlin/com/project/starter/core/designsystem/theme/Color.kt`

```kotlin
package com.project.starter.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Light palette
val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)

// Dark palette
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
```

### Step 3 — Buat `Dimens.kt`

**Path:** `core/designsystem/src/commonMain/kotlin/com/project/starter/core/designsystem/theme/Dimens.kt`

```kotlin
package com.project.starter.core.designsystem.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
```

### Step 4 — Buat `TextDimens.kt`

**Path:** `core/designsystem/src/commonMain/kotlin/com/project/starter/core/designsystem/theme/TextDimens.kt`

```kotlin
package com.project.starter.core.designsystem.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

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
```

### Step 5 — Buat `Typography.kt`

**Path:** `core/designsystem/src/commonMain/kotlin/com/project/starter/core/designsystem/theme/Typography.kt`

```kotlin
package com.project.starter.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val StarterTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
```

### Step 6 — Buat `AppTheme.kt`

**Path:** `core/designsystem/src/commonMain/kotlin/com/project/starter/core/designsystem/theme/AppTheme.kt`

```kotlin
package com.project.starter.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
)

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

/** Accessor object for custom design tokens */
object AppTheme {
    val dimens: Dimens
        @Composable @ReadOnlyComposable
        get() = LocalDimens.current

    val textDimens: TextDimens
        @Composable @ReadOnlyComposable
        get() = LocalTextDimens.current
}
```

### Step 7 — Update `shared/src/commonMain/.../App.kt`

Ganti `MaterialTheme` dengan `AppTheme` dari designsystem:

```diff
+ import com.project.starter.core.designsystem.theme.AppTheme
- import androidx.compose.material3.MaterialTheme

  @Composable
  fun App() {
      KoinContext {
-         MaterialTheme {
+         AppTheme {
              // ...
          }
      }
  }
```

### Step 8 — Tambah dependency `:core:designsystem` ke `:shared`

**File:** `shared/build.gradle.kts`

```kotlin
commonMain.dependencies {
    // ...
    implementation(projects.core.designsystem)
}
```

## Files

```
[MODIFY] core/designsystem/build.gradle.kts
[NEW]    core/designsystem/src/commonMain/.../theme/Color.kt
[NEW]    core/designsystem/src/commonMain/.../theme/Dimens.kt
[NEW]    core/designsystem/src/commonMain/.../theme/TextDimens.kt
[NEW]    core/designsystem/src/commonMain/.../theme/Typography.kt
[NEW]    core/designsystem/src/commonMain/.../theme/AppTheme.kt
[MODIFY] shared/build.gradle.kts
[MODIFY] shared/src/commonMain/.../App.kt
```

## Verification

### Automated Tests
```bash
./gradlew :core:designsystem:build
./gradlew assembleDebug
./gradlew :desktopApp:jvmJar
```

### Manual Verification
- [ ] App Android berjalan dengan AppTheme (warna Material3)
- [ ] Tidak ada compile error terkait import MaterialTheme
- [ ] Dark mode bisa diuji via device developer options
