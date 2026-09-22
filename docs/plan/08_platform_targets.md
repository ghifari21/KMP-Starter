# Plan 08 — Platform Targets

## Objective

Memastikan semua platform target KMP berjalan dengan benar: memperbaiki Desktop JVM (crash mainClass + missing Ktor engine), mengaktifkan kembali WasmJS/Web target, dan memperbaiki iOS double-init crash.

## Requirements

- Desktop app harus bisa dijalankan via `./gradlew :desktopApp:run` tanpa `ClassNotFoundException`
- `jvmMain` di `core:data` harus memiliki Ktor engine (OkHttp atau CIO)
- WasmJS build `./gradlew :webApp:wasmJsBrowserDistribution` harus berhasil
- iOS tidak boleh crash dengan `KoinAppAlreadyStartedException` saat `MainViewController()` dipanggil dua kali
- Semua platform harus mendapatkan Ktor JS/WasmJS engine yang tepat

## Steps

### Step 1 — Fix Desktop `mainClass` (jika belum dari Plan 01)

**File:** `desktopApp/build.gradle.kts`

```diff
  compose.desktop {
      application {
-         mainClass = "com.project.starter.MainKt"
+         mainClass = "com.project.starter.desktop.MainKt"
      }
  }
```

### Step 2 — Tambah Ktor Engines ke `libs.versions.toml`

```toml
[libraries]
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-js = { module = "io.ktor:ktor-client-js", version.ref = "ktor" }
```

### Step 3 — Update `core/data/build.gradle.kts` — tambah engines per platform

```kotlin
kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)   // Desktop JVM
        }
        // iosMain — dikonfigurasi lewat AppleMain jika pakai apple()
        val appleMain by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        // WasmJS
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
    }
}
```

> Catatan: Konfigurasi ini mungkin memerlukan penyesuaian berdasarkan cara convention plugin KmpLibraryConventionPlugin mengatur source sets. Sesuaikan dengan yang sudah ada di `core/data/build.gradle.kts`.

### Step 4 — Aktifkan WasmJS di `ConfigKmp.kt`

**File:** `build-logic/convention/src/main/kotlin/configs/ConfigKmp.kt`

```diff
  internal fun Project.configKmp(extension: KotlinMultiplatformExtension) {
      extension.apply {
          val isMac = System.getProperty("os.name").lowercase().contains("mac")
          if (isMac) {
              iosX64()
              iosArm64()
              iosSimulatorArm64()
          }
          
          jvm()
          
-         // WasmJS is temporarily disabled because Room KMP (2.7.0) 
-         // does not yet support WasmJS targets.
-         // @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
-         // wasmJs {
-         //     browser()
-         // }
+         @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
+         wasmJs {
+             browser()
+         }
      }
  }
```

> Catatan: Room sudah diganti dengan Store5 yang mendukung WasmJS, sehingga blokir ini bisa dihapus.

### Step 5 — Aktifkan `:webApp` di `settings.gradle.kts`

```diff
  include(":androidApp")
  include(":desktopApp")
  include(":shared")
- // include(":webApp")
+ include(":webApp")
```

### Step 6 — Update `webApp/build.gradle.kts`

Pastikan webApp dikonfigurasi dengan benar untuk WasmJS:

```kotlin
plugins {
    alias(libs.plugins.convention.cmp.library)
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "webApp"
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
                devServer = (devServer ?: org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(project.file("src/webMain/resources").path)
                    }
                }
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        val webMain by getting {
            dependencies {
                implementation(projects.shared)
            }
        }
    }
}
```

### Step 7 — Fix iOS Double Init (jika belum dari Plan 01)

**File:** `shared/src/iosMain/kotlin/com/project/starter/shared/MainViewController.kt`

```kotlin
package com.project.starter.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.project.starter.shared.di.initKoin
import org.koin.core.context.GlobalContext

fun MainViewController() = ComposeUIViewController { App() }

/** Call this from AppDelegate/iOSApp.swift ONCE at startup */
fun initApp() {
    if (GlobalContext.getOrNull() == null) {
        initKoin()
    }
}
```

**File:** `iosApp/iosApp/iOSApp.swift`

```swift
import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.initApp()  // Call once at startup
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

**File:** `iosApp/iosApp/ContentView.swift`

```swift
import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()  // Do NOT call initKoin here
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

### Step 8 — Verifikasi Web Target Entry Point

**File:** `webApp/src/webMain/kotlin/com/project/starter/main.kt`

```kotlin
package com.project.starter

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.project.starter.shared.App
import com.project.starter.shared.di.initKoin
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    val body = document.body ?: return
    ComposeViewport(body) {
        App()
    }
}
```

### Step 9 — Update CI untuk Semua Platform

**File:** `.github/workflows/ci.yml`

```yaml
- name: Build WasmJS Web
  run: ./gradlew :webApp:wasmJsBrowserDistribution

- name: Upload APK
  if: github.ref == 'refs/heads/main'
  uses: actions/upload-artifact@v4
  with:
    name: debug-apk-${{ github.sha }}
    path: androidApp/build/outputs/apk/**/debug/*.apk
    retention-days: 7
```

## Files

```
[MODIFY] desktopApp/build.gradle.kts
[MODIFY] gradle/libs.versions.toml
[MODIFY] core/data/build.gradle.kts
[MODIFY] build-logic/convention/src/.../configs/ConfigKmp.kt
[MODIFY] settings.gradle.kts
[MODIFY] webApp/build.gradle.kts
[MODIFY] webApp/src/webMain/kotlin/.../main.kt
[MODIFY] shared/src/iosMain/.../MainViewController.kt
[MODIFY] iosApp/iosApp/iOSApp.swift
[MODIFY] iosApp/iosApp/ContentView.swift
[MODIFY] .github/workflows/ci.yml
```

## Verification

### Automated Tests (CI-compatible)
```bash
# Android
./gradlew assembleDevDebug

# Desktop  
./gradlew :desktopApp:jvmJar
# Manual: ./gradlew :desktopApp:run

# WasmJS Web
./gradlew :webApp:wasmJsBrowserDistribution

# iOS (macOS only)
# ./gradlew :shared:iosX64Test
```

### Manual Verification
- [ ] `./gradlew :desktopApp:run` membuka jendela desktop app
- [ ] `./gradlew :webApp:wasmJsBrowserDistribution` menghasilkan output di `webApp/build/dist/`
- [ ] Membuka `webApp/build/dist/wasmJs/productionExecutable/index.html` di browser menampilkan app
- [ ] iOS app tidak crash saat dibuka (test di simulator)
- [ ] Semua 4 platform menampilkan UI yang sama dari shared composable
