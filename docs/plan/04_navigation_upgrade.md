# Plan 04 — Navigation Upgrade

## Objective

Upgrade sistem navigasi dengan: animasi slide horizontal, pattern per-feature NavGraphBuilder extension, contoh navigasi dengan argument (DetailScreen), dan auth state machine (AppRouter + RouterViewModel + AuthScreen).

## Requirements

- `BaseNavHost` harus menyediakan animasi slide horizontal secara default untuk semua composable
- Setiap feature module memiliki `Routes.kt` dan `Navigation.kt` (NavGraphBuilder extension) sendiri
- `DetailScreen` mendemonstrasikan navigasi dengan argument bertipe (`DetailRoute(id, title)`)
- `AppRouter` secara reaktif menampilkan `AuthScreen` atau `MainScreen` berdasarkan status session
- `SessionManager` harus memiliki `isLoggedIn(): Flow<Boolean>` untuk mendukung RouterViewModel
- `AppNavigator` harus terhubung dan diobservasi di `App.kt`
- Module baru `:feat:auth` harus ditambahkan ke `settings.gradle.kts`

## Steps

### Step 1 — Buat `BaseNavHost.kt`

**Path:** `core/navigation/src/commonMain/kotlin/com/project/starter/core/navigation/BaseNavHost.kt`

```kotlin
package com.project.starter.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
inline fun <reified T : Any> BaseNavHost(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    startDestination: T,
    noinline builder: NavGraphBuilder.() -> Unit,
) {
    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        },
        builder = builder,
    )
}
```

### Step 2 — Update `Screen.kt`

**Path:** `core/navigation/src/commonMain/kotlin/com/project/starter/core/navigation/Screen.kt`

```kotlin
package com.project.starter.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Home : Screen()

    @Serializable
    data class Detail(val id: String, val title: String) : Screen()
}
```

### Step 3 — Tambah `isLoggedIn()` ke `SessionManager`

**Path:** `core/domain/src/commonMain/.../repository/SessionManager.kt`

```kotlin
interface SessionManager {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clearSession()
    fun isLoggedIn(): Flow<Boolean>  // NEW
}
```

**Path:** `core/data/src/commonMain/.../local/EncryptedSessionManager.kt`

```kotlin
override fun isLoggedIn(): Flow<Boolean> =
    dataStore.data.map { preferences ->
        !preferences[TOKEN_KEY].isNullOrBlank()
    }
```

### Step 4 — Buat `feat/auth` Module

**`settings.gradle.kts`** — tambahkan:
```kotlin
include(":feat:auth")
```

**`feat/auth/build.gradle.kts`**:
```kotlin
plugins {
    alias(libs.plugins.convention.feature)
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
            implementation(projects.core.domain)
        }
    }
}
android {
    namespace = "com.project.starter.feat.auth"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
}
```

**`feat/auth/src/commonMain/.../AuthScreen.kt`**:
```kotlin
package com.project.starter.feat.auth.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.project.starter.core.designsystem.theme.AppTheme

@Composable
fun AuthScreen(onLoginClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.dimens.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.large))
            Text(
                text = "Please sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.extraLarge))
            Button(onClick = onLoginClick) {
                Text("Sign In (Demo)")
            }
        }
    }
}
```

### Step 5 — Buat `HomeRoutes.kt` dan `HomeNavigation.kt`

**Path:** `feat/home/src/commonMain/.../navigation/HomeRoutes.kt`

```kotlin
package com.project.starter.feat.home.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class DetailRoute(val id: String, val title: String)
```

**Path:** `feat/home/src/commonMain/.../navigation/HomeNavigation.kt`

```kotlin
package com.project.starter.feat.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.project.starter.feat.home.presentation.DetailScreen
import com.project.starter.feat.home.presentation.HomeScreen
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.homeGraph(navController: NavController) {
    composable<HomeRoute> {
        val viewModel = koinViewModel<HomeViewModel>()
        HomeScreen(
            viewModel = viewModel,
            navigateToDetail = { id, title ->
                navController.navigate(DetailRoute(id = id, title = title))
            },
        )
    }
    composable<DetailRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<DetailRoute>()
        DetailScreen(
            id = args.id,
            title = args.title,
            onNavigateBack = { navController.popBackStack() },
        )
    }
}
```

### Step 6 — Buat `DetailScreen.kt`

**Path:** `feat/home/src/commonMain/.../presentation/DetailScreen.kt`

```kotlin
package com.project.starter.feat.home.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.project.starter.core.designsystem.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    id: String,
    title: String,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(AppTheme.dimens.paddingMedium)) {
            Text(text = "ID: $id", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(AppTheme.dimens.medium))
            Text(text = "Title: $title", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
```

### Step 7 — Buat `RouterViewModel.kt` dan `AppRouter.kt`

**Path:** `shared/src/commonMain/.../router/RouterViewModel.kt`

```kotlin
package com.project.starter.shared.router

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.starter.core.domain.repository.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface RouterState {
    data object Loading : RouterState
    data object Authenticated : RouterState
    data object Unauthenticated : RouterState
}

class RouterViewModel(sessionManager: SessionManager) : ViewModel() {
    val routerState: StateFlow<RouterState> = sessionManager.isLoggedIn()
        .map { isLoggedIn -> if (isLoggedIn) RouterState.Authenticated else RouterState.Unauthenticated }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RouterState.Loading)
}
```

**Path:** `shared/src/commonMain/.../router/AppRouter.kt`

```kotlin
package com.project.starter.shared.router

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.project.starter.feat.auth.presentation.AuthScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppRouter(mainContent: @Composable () -> Unit) {
    val viewModel: RouterViewModel = koinViewModel()
    val state by viewModel.routerState.collectAsState()

    when (state) {
        RouterState.Loading -> { /* Show splash or loading */ }
        RouterState.Unauthenticated -> AuthScreen(onLoginClick = { /* TODO: trigger login */ })
        RouterState.Authenticated -> mainContent()
    }
}
```

### Step 8 — Update `KoinInit.kt` — tambah `routerModule`

```kotlin
val routerModule = module {
    viewModel { RouterViewModel(get()) }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(dataModule, homeModule, routerModule)
    }
}
```

### Step 9 — Update `App.kt` — integrasikan semua

```kotlin
@Composable
fun App() {
    KoinContext {
        AppTheme {
            val navController = rememberNavController()
            val navigator: AppNavigator = koinInject()

            LaunchedEffect(Unit) {
                navigator.navigationEvents.collect { screen ->
                    navController.navigate(screen)
                }
            }

            AppRouter {
                BaseNavHost(navHostController = navController, startDestination = HomeRoute) {
                    homeGraph(navController)
                }
            }
        }
    }
}
```

## Files

```
[NEW]    core/navigation/src/commonMain/.../BaseNavHost.kt
[MODIFY] core/navigation/src/commonMain/.../Screen.kt
[MODIFY] core/domain/src/commonMain/.../repository/SessionManager.kt
[MODIFY] core/data/src/commonMain/.../local/EncryptedSessionManager.kt
[NEW]    feat/auth/build.gradle.kts
[NEW]    feat/auth/src/commonMain/.../AuthScreen.kt
[NEW]    feat/home/src/commonMain/.../navigation/HomeRoutes.kt
[NEW]    feat/home/src/commonMain/.../navigation/HomeNavigation.kt
[NEW]    feat/home/src/commonMain/.../presentation/DetailScreen.kt
[MODIFY] feat/home/src/commonMain/.../presentation/HomeScreen.kt
[MODIFY] feat/home/src/commonMain/.../presentation/HomeContract.kt
[NEW]    shared/src/commonMain/.../router/RouterViewModel.kt
[NEW]    shared/src/commonMain/.../router/AppRouter.kt
[MODIFY] shared/src/commonMain/.../di/KoinInit.kt
[MODIFY] shared/src/commonMain/.../App.kt
[MODIFY] shared/build.gradle.kts
[MODIFY] settings.gradle.kts
```

## Verification

### Automated Tests
```bash
./gradlew assembleDebug
./gradlew :desktopApp:jvmJar
./gradlew :core:navigation:build
```

### Manual Verification
- [ ] App menampilkan `AuthScreen` saat tidak ada token tersimpan
- [ ] Tap "Sign In (Demo)" → set fake token → pindah ke `MainScreen`
- [ ] Di `HomeScreen`, tap item → navigasi ke `DetailScreen` dengan id dan title
- [ ] Tombol Back di `DetailScreen` kembali ke `HomeScreen` dengan animasi slide
