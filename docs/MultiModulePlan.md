## Goal Description
Restructure the `KMPStarter` project into a scalable, multi-module Compose Multiplatform (CMP) architecture. The structure will be heavily inspired by the reference `Android-Compose-Starter-Template`, adapted to support KMP targets (Android, iOS, Web, Desktop). This will improve build times, enforce separation of concerns, and make the starter project scalable for large applications.

## User Review Required
> [!IMPORTANT]
> - **Module Names**: The monolithic `shared` module will be split into multiple modules (`core:*`, `feat:*`, `common`). `shared` itself will act as the main umbrella module for iOS/Android bridging.
> - **Convention Plugins**: We will introduce a `build-logic` composite build. Since this is KMP, the Android-specific convention plugins from the reference will be adapted into KMP convention plugins (`convention.kmp.library`, `convention.cmp.feature`, etc.).

## Open Questions
> [!WARNING]
> - **Navigation Library**: The reference project uses Android Navigation. For Compose Multiplatform, we need a KMP navigation library like **Voyager** or **Jetpack Navigation for KMP**. Which one do you prefer for this starter template?
> - **Umbrella Module Name**: Do you prefer to keep the name `shared` for the umbrella module that iOS links to, or rename it to `composeApp` (which is the modern JetBrains default)?

## Proposed Changes

### 1. Build Logic & Conventions
Introduce a `build-logic` directory to share Gradle configurations across all multi-module KMP modules.

#### [NEW] build-logic/settings.gradle.kts
Setup the composite build.
#### [NEW] build-logic/convention/build.gradle.kts
Register convention plugins for KMP.
#### [NEW] build-logic/convention/src/main/kotlin/plugins/KmpLibraryConventionPlugin.kt
Base plugin for Kotlin Multiplatform modules, configuring Android, Desktop, iOS, and Web/Wasm targets.
#### [NEW] build-logic/convention/src/main/kotlin/plugins/CmpFeatureConventionPlugin.kt
Plugin for Compose Multiplatform UI modules (combining KMP + JetBrains Compose plugins).

---

### 2. Core Modules (`core:*`)
Create business logic and shared infrastructure modules.

#### [NEW] core/model/build.gradle.kts
Data classes, enums, and domain entities. Pure Kotlin.
#### [NEW] core/domain/build.gradle.kts
Use cases and interfaces. Pure Kotlin.
#### [NEW] core/data/build.gradle.kts
Repositories, Network (Ktor), and Database (SQLDelight).
#### [NEW] core/designsystem/build.gradle.kts
Compose Multiplatform theme, colors, typography, and reusable UI components.
#### [NEW] core/navigation/build.gradle.kts
Shared navigation routes and definitions.

---

### 3. Feature Modules (`feat:*`)
Extract UI features into independent modules.

#### [NEW] feat/home/build.gradle.kts
Move the `Greeting` logic and initial screen from `shared/` to a dedicated `feat:home` module.

---

### 4. Common Utils (`common`)
General utilities and helpers.

#### [NEW] common/build.gradle.kts
String extensions, date formatters, logging.

---

### 5. App & Umbrella Module
Update the entry points to depend on the new modules.

#### [MODIFY] settings.gradle.kts
Include the `build-logic` composite build and all the new `:core:*`, `:feat:*`, and `:common` modules.
#### [MODIFY] shared/build.gradle.kts
The `shared` module will now act as an umbrella module. It will depend on `:feat:home` and `:core:designsystem` and export them to iOS.

## Verification Plan
### Automated Tests
```bash
./gradlew check
./gradlew build
```
### Manual Verification
1. Run the Android app and verify it launches successfully.
2. Run the Desktop app (`./gradlew :desktopApp:run`) and verify it launches.
3. Open `iosApp/iosApp.xcworkspace` in Xcode and verify it builds.
