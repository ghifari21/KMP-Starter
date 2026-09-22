# Plan 01 — Bug Fixes Kritis

## Objective

Perbaiki 5 bug yang menyebabkan crash runtime atau silent failure sehingga semua platform dapat dijalankan sebagai baseline sebelum penambahan fitur.

## Requirements

- Desktop app harus bisa dijalankan via `./gradlew :desktopApp:run` tanpa `ClassNotFoundException`
- HTTP errors harus di-map ke `AppException` (tidak ditelan diam-diam)
- iOS tidak boleh crash dengan `KoinAppAlreadyStartedException`
- `AppNavigator` harus terhubung ke `NavHostController` agar navigasi event-driven berfungsi
- Desktop target harus memiliki Ktor engine agar network call tidak crash

## Steps

### Step 1 — Fix Desktop `mainClass`

**File:** `desktopApp/build.gradle.kts`

Masalah: `mainClass` menunjuk ke `com.project.starter.MainKt` yang tidak ada.
Kelas yang benar ada di package `com.project.starter.desktop`.

```diff
- mainClass = "com.project.starter.MainKt"
+ mainClass = "com.project.starter.desktop.MainKt"
```

### Step 2 — Tambah Ktor Engine untuk Desktop JVM

**File:** `core/data/build.gradle.kts`

Masalah: `jvmMain` tidak memiliki Ktor engine sehingga `HttpClient` crash di Desktop.

Tambahkan di `jvmMain.dependencies`:
```kotlin
implementation(libs.ktor.client.okhttp)
```

Pastikan `ktor-client-okhttp` sudah ada di `libs.versions.toml`. Jika belum, tambahkan:
```toml
[libraries]
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
```

### Step 3 — Pasang `GlobalErrorMapper` di HttpClientFactory

**File:** `core/data/src/commonMain/kotlin/com/project/starter/core/data/network/HttpClientFactory.kt`

Masalah: `installGlobalErrorMapper()` sudah ada tapi tidak pernah dipanggil.

```diff
  fun createHttpClient(): HttpClient =
      HttpClient {
          install(ContentNegotiation) {
              json(Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true })
          }
+         installGlobalErrorMapper()
      }
```

### Step 4 — Lindungi iOS `initKoin()` dari Double-Call

**File:** `shared/src/iosMain/kotlin/com/project/starter/shared/MainViewController.kt`

Masalah: `initKoin()` dipanggil setiap kali `MainViewController()` diinstansiasi.

```diff
+ import org.koin.core.context.KoinContext
+ 
  fun MainViewController() = ComposeUIViewController {
-     initKoin()
      App()
  }
+ 
+ fun initApp() {
+     if (org.koin.core.context.GlobalContext.getOrNull() == null) {
+         initKoin()
+     }
+ }
```

Juga update `iosApp/iosApp/iOSApp.swift` untuk memanggil `KoinInitKt.initApp()` bukan dari MainViewController.

### Step 5 — Register dan Hubungkan `AppNavigator`

**File:** `core/data/src/commonMain/kotlin/com/project/starter/core/data/di/DataModule.kt`

Tambahkan:
```kotlin
single { AppNavigator() }
```

**File:** `shared/src/commonMain/kotlin/com/project/starter/shared/App.kt`

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

            NavHost(navController = navController, startDestination = Screen.Home) {
                composable<Screen.Home> {
                    val viewModel: HomeViewModel = koinViewModel()
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
```

Catatan: Ganti `koinInject<HomeViewModel>()` dengan `koinViewModel<HomeViewModel>()` agar terikat ke ViewModelStoreOwner.

## Files Modified

```
[MODIFY] desktopApp/build.gradle.kts
[MODIFY] core/data/build.gradle.kts
[MODIFY] gradle/libs.versions.toml            (jika ktor-client-okhttp belum ada)
[MODIFY] core/data/.../network/HttpClientFactory.kt
[MODIFY] shared/src/iosMain/.../MainViewController.kt
[MODIFY] core/data/.../di/DataModule.kt
[MODIFY] shared/src/commonMain/.../App.kt
```

## Verification

### Automated Tests
```bash
./gradlew assembleDebug
./gradlew :desktopApp:jvmJar
./gradlew compileKotlinJvm
```

### Manual Verification
- [ ] `./gradlew :desktopApp:run` berjalan tanpa ClassNotFoundException
- [ ] Build Android debug berhasil
- [ ] Tidak ada build error di semua module
