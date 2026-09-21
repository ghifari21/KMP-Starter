## Goal Description
Develop the foundational classes and architecture layers for the core modules (`:common`, `:core:data`, `:core:domain`, `:core:model`, `:core:navigation`) in the Compose Multiplatform starter project. This will adapt the MVI + Clean Architecture structure from the Android template into idiomatic Kotlin Multiplatform code, providing a ready-to-use scaffolding for developers.

## User Review Required
> [!IMPORTANT]
> - **MVI BaseViewModel**: Since Jetpack `ViewModel` is now officially supported in KMP (via `androidx.lifecycle.viewmodel`), we will use it as the base class for MVI in `:common`. StateFlow and SharedFlow will be used to manage `UiState`, `UiEvent`, and `UiEffect`. 
> - **Koin DI**: The dependency injection will be structured using Koin. Each module will expose a `val moduleDi = module { ... }` which will be aggregated in the `:composeApp` (`shared`) module.

## Open Questions
> [!WARNING]
> - For `:core:data` local storage, do you want me to set up the boilerplate for **DataStore Preferences (KMP)** alongside Ktor, or just Ktor for now?

## Proposed Changes

### 1. Common Module (`:common`)
Provides MVI base classes, error handling, and utilities.
#### [NEW] common/src/commonMain/kotlin/com/project/starter/common/base/UiState.kt
Sealed interface representing UI states (Loading, Success, Error).
#### [NEW] common/src/commonMain/kotlin/com/project/starter/common/base/BaseViewModel.kt
Abstract class extending `androidx.lifecycle.ViewModel` to handle MVI state updates, events, and effects cleanly.
#### [NEW] common/src/commonMain/kotlin/com/project/starter/common/exception/AppException.kt
Standardized exceptions for the app (Network, Server, Unknown).

---

### 2. Core Model Module (`:core:model`)
Pure Kotlin objects representing the core business entities.
#### [NEW] core/model/src/commonMain/kotlin/com/project/starter/core/model/ExampleModel.kt
A sample data class to demonstrate cross-module usage.

---

### 3. Core Domain Module (`:core:domain`)
Business logic and repository contracts.
#### [NEW] core/domain/src/commonMain/kotlin/com/project/starter/core/domain/repository/ExampleRepository.kt
Interface defining data operations (e.g., `fun getExample(): Flow<List<ExampleModel>>`).
#### [NEW] core/domain/src/commonMain/kotlin/com/project/starter/core/domain/usecase/GetExampleUseCase.kt
Sample use case invoking the repository.

---

### 4. Core Data Module (`:core:data`)
Implementations of repositories, network clients, and local storage.
#### [NEW] core/data/src/commonMain/kotlin/com/project/starter/core/data/network/HttpClientFactory.kt
Creates the Ktor `HttpClient` with JSON serialization and logging.
#### [NEW] core/data/src/commonMain/kotlin/com/project/starter/core/data/repository/ExampleRepositoryImpl.kt
Implements `ExampleRepository`, making network calls via Ktor.
#### [NEW] core/data/src/commonMain/kotlin/com/project/starter/core/data/di/DataModule.kt
Koin module injecting `HttpClient`, DAOs, and Repositories as `single` or `factory`.

---

### 5. Core Navigation Module (`:core:navigation`)
Type-safe routes for Jetpack Navigation KMP.
#### [NEW] core/navigation/src/commonMain/kotlin/com/project/starter/core/navigation/Screen.kt
Uses `kotlinx.serialization.Serializable` objects to define routes (e.g., `Serializable data object HomeRoute`).
#### [NEW] core/navigation/src/commonMain/kotlin/com/project/starter/core/navigation/AppNavigator.kt
A wrapper class or interface for navigating between screens without tightly coupling ViewModels to NavHostController.

## Verification Plan
### Automated Tests
```bash
./gradlew check
```
### Manual Verification
1. Open the project in Android Studio / Fleet.
2. Verify that imports across modules resolve correctly (e.g., `core:data` resolving `core:domain`).
3. Verify that KMP classes are fully platform-agnostic (no `java.util` imports in `commonMain`).
