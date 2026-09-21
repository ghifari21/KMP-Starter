## Goal Description
After carefully examining the reference `Android-Compose-Starter-Template`, I've identified several structural patterns, utilities, and components that are present in the Android template but still need to be ported to our KMP architecture. This plan aims to achieve 100% feature parity with the reference template while adapting it to Kotlin Multiplatform.

## User Review Required
> [!IMPORTANT]
> - **Room KMP**: The reference uses Room. Room now supports KMP (in beta). We can implement `AppDatabase` using Room KMP in `:core:data`.
> - **WorkManager**: The reference uses `SyncWorker.kt` (WorkManager). KMP does not have a direct universal background worker yet. We will leave Android-specific background work out of `commonMain`, or implement it using `expect/actual` if strictly necessary. For now, I propose skipping `SyncWorker`.
> - **Network Interceptors**: Ktor handles Auth and Error Mapping differently than Retrofit. I will translate `AuthInterceptor` and `GlobalErrorMapper` into Ktor's `HttpResponseValidator` and `Auth` plugins.

## Proposed Changes

### 1. Common Utilities (`:common`)
Translate Android extensions into pure Kotlin extensions.
#### [NEW] common/src/commonMain/kotlin/com/project/starter/common/utils/StringExt.kt
Basic string manipulations.
#### [NEW] common/src/commonMain/kotlin/com/project/starter/common/utils/DateExt.kt
Date formatters (using `kotlinx-datetime`).

### 2. Core Model (`:core:model`)
Introduce the standard data separation layers.
#### [NEW] core/model/src/commonMain/kotlin/com/project/starter/core/model/request/ExampleRequest.kt
#### [NEW] core/model/src/commonMain/kotlin/com/project/starter/core/model/response/ExampleResponse.kt
#### [NEW] core/model/src/commonMain/kotlin/com/project/starter/core/model/entity/ExampleEntity.kt
#### [NEW] core/model/src/commonMain/kotlin/com/project/starter/core/model/DataMapper.kt

### 3. Core Domain (`:core:domain`)
Add missing session contracts.
#### [NEW] core/domain/src/commonMain/kotlin/com/project/starter/core/domain/repository/SessionManager.kt
Interface for managing user sessions (tokens, login state).

### 4. Core Data (`:core:data`)
Implement database, API services, and session management.
#### [NEW] core/data/src/commonMain/kotlin/com/project/starter/core/data/local/EncryptedSessionManager.kt
Implement `SessionManager` using KMP DataStore (we created `DataStoreFactory` earlier).
#### [NEW] core/data/src/commonMain/kotlin/com/project/starter/core/data/network/GlobalErrorMapper.kt
Ktor validation block to map HTTP exceptions to `AppException.ServerException`.
#### [NEW] core/data/src/commonMain/kotlin/com/project/starter/core/data/source/remote/ExampleApiService.kt
Ktor-based API service class (equivalent to the Retrofit interface).
#### [NEW] core/data/src/commonMain/kotlin/com/project/starter/core/data/source/local/db/AppDatabase.kt
Room KMP Database setup and DAOs.

### 5. Feature Home (`:feat:home`)
Implement a fully functional MVI screen.
#### [NEW] feat/home/src/commonMain/kotlin/com/project/starter/feat/home/presentation/HomeContract.kt
Define `HomeState`, `HomeEvent`, and `HomeEffect` inheriting from `:common`'s `UiContract`.
#### [NEW] feat/home/src/commonMain/kotlin/com/project/starter/feat/home/presentation/HomeViewModel.kt
Implement MVI using `BaseViewModel`.
#### [NEW] feat/home/src/commonMain/kotlin/com/project/starter/feat/home/presentation/HomeScreen.kt
Compose UI observing `uiState` and handling `HomeEvent`s.

## Verification Plan
### Automated Tests
```bash
./gradlew check
```
### Manual Verification
1. Open the project and verify `HomeViewModel` correctly inherits from `BaseViewModel` and handles states.
2. Verify that Ktor is correctly configured with error mapping.
