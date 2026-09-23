# KMP Starter (Kotlin Multiplatform)

A modern, production-ready Kotlin Multiplatform (KMP) starter template targeting **Android**, **iOS**, and **Desktop (JVM)**. Built with **Compose Multiplatform** and structured using **Clean Architecture** and **MVI**.

## 🚀 Features

- **Compose Multiplatform**: UI shared across Android, iOS, and Desktop.
- **Clean Architecture & Multi-module**: Separated into `core`, `feat`, and `common` to scale properly.
- **MVI Pattern**: Predictable state management using `StateFlow`, `BaseViewModel`, and `UiState`.
- **Jetpack Navigation KMP**: Type-safe and standard declarative navigation.
- **Dependency Injection**: Powered by **Koin** for seamless injection across platforms.
- **Caching & Data Loading**: Powered by **Store5**.
- **Session Management**: Powered by **DataStore Preferences (KMP)**.
- **Network**: Integrated with **Ktor Client**.
- **Centralized Build Logic**: Uses Gradle **Convention Plugins** (`build-logic`) for DRY build scripts.

## 📂 Project Structure

This project adopts a highly modular structure to improve build times and separation of concerns:

```text
KMPStarter/
├── build-logic/      # Gradle convention plugins (e.g., cmp-feature, kmp-library)
├── common/           # Platform-agnostic utilities, exceptions, and base MVI classes
├── core/             # Foundational modules shared across features
│   ├── data/         # Repositories, APIs (Ktor), Store5, and Preferences (DataStore)
│   ├── designsystem/ # Custom themes, typography, and reusable UI components
│   ├── domain/       # Interfaces, UseCases, and Business Logic
│   ├── model/        # Shared data models (Entities, DTOs)
│   └── navigation/   # Routing definitions and destinations
├── feat/             # Individual feature modules handling UI and ViewModels
│   └── home/         # Example home feature
├── shared/           # Umbrella module aggregating features and initializing DI (Koin)
├── androidApp/       # Android application entry point
├── iosApp/           # iOS Xcode project and application entry point
└── desktopApp/       # Desktop JVM entry point
```

## 🛠️ Requirements

- **JDK 17** or higher.
- **Android Studio Ladybug** (or IntelliJ IDEA with KMP plugins).
- **Xcode** (for running the iOS app on Mac).
- macOS is required to build and run the iOS target (Apple targets are disabled by default on Windows/Linux environments).

## ▶️ Running the Apps

### Android
Open the project in Android Studio, select the `androidApp` run configuration, and press **Run**.
Alternatively, via terminal:
```bash
./gradlew :androidApp:assembleDebug
```

### Desktop (JVM)
You can run the desktop application from the terminal:
```bash
./gradlew :desktopApp:run
```

### WasmJS (Browser)
Currently experimental, but active. To build for web:
```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

## 🎨 Environments (Flavors)

The Android target supports **Dev**, **Staging**, and **Prod** environments mapped via Product Flavors. Base URLs are automatically injected.
To build a specific flavor:
```bash
./gradlew :androidApp:assembleDevDebug
```

## 🛠 Tooling & Code Quality

- **Detekt**: Static code analysis. Run `./gradlew detekt`.
- **Spotless**: Code formatting (ktlint). Run `./gradlew spotlessApply`.
- **Testing**: Pre-configured `MainDispatcherRule`, `Turbine`, `MockK`, and Fake patterns.
- **Git Hooks**: Pre-push hook automatically checks Spotless and Detekt.

### Generating a New Feature Module

We provide a bash script to quickly scaffold new feature modules compliant with the architecture:
```bash
./create_feature.sh feature_name
```
This automatically sets up `build.gradle.kts`, `di`, `presentation`, and adds the module to `settings.gradle.kts`.

## 🔄 Renaming the Package

If you are starting a new project using this template, you can easily rename the package `com.project.starter` to your own company or app name.

Run the provided Python script in the root directory:
```bash
python rename.py
```