# KMP Starter (Kotlin Multiplatform)

A modern, production-ready Kotlin Multiplatform (KMP) starter template targeting **Android**, **iOS**, and **Desktop (JVM)**. Built with **Compose Multiplatform** and structured using **Clean Architecture** and **MVI**.

## 🚀 Features

- **Compose Multiplatform**: UI shared across Android, iOS, and Desktop.
- **Clean Architecture & Multi-module**: Separated into `core`, `feat`, and `common` to scale properly.
- **MVI Pattern**: Predictable state management using `StateFlow`, `BaseViewModel`, and `UiState`.
- **Jetpack Navigation KMP**: Type-safe and standard declarative navigation.
- **Dependency Injection**: Powered by **Koin** for seamless injection across platforms.
- **Local Database**: Powered by **Room KMP** (SQLite).
- **Session Management**: Powered by **DataStore Preferences (KMP)**.
- **Network**: Integrated with **Ktor Client**.
- **Centralized Build Logic**: Uses Gradle **Convention Plugins** (`build-logic`) for DRY build scripts.

## 📂 Project Structure

This project adopts a highly modular structure to improve build times and separation of concerns:

- **`build-logic/`**: Contains Gradle convention plugins (e.g., `cmp-feature`, `kmp-library`) to easily manage module configurations.
- **`common/`**: Contains platform-agnostic utilities, base classes (`BaseViewModel`, `UiContract`), and exceptions.
- **`core/`**: Foundational modules shared across features.
  - `:core:data`: Handles APIs, Database (Room), and Preferences (DataStore).
  - `:core:domain`: Contains Interfaces, UseCases, and Business Logic.
  - `:core:model`: Shared data models (Entities, DTOs).
  - `:core:navigation`: Routing definitions and destinations.
  - `:core:designsystem`: Custom themes, typography, and reusable components.
- **`feat/`**: Contains individual feature modules (e.g., `:feat:home`) handling their own UI and ViewModels.
- **`shared/`**: The umbrella module that aggregates all features and core modules to bridge them into the native iOS/Android/Desktop entry points. It also initializes DI (`initKoin`).

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

### iOS
1. Ensure you are on macOS and have Xcode installed.
2. Open the `/iosApp` directory in Xcode and run it from there.
3. Or, run via Android Studio using the `iosApp` run configuration.

## 🔄 Renaming the Package

If you are starting a new project using this template, you can easily rename the package `com.project.starter` to your own company or app name.

Run the provided Python script in the root directory:
```bash
python rename.py
```
It will prompt you for your new package name (e.g., `com.mycompany.myapp`) and automatically rename all folders, `build.gradle` namespaces, and import statements across the entire project.