# Plan 06 — Testing Infrastructure

## Objective

Membangun infrastruktur testing yang lengkap di `:core:testing` dan mengimplementasikan unit tests yang sesungguhnya di seluruh lapisan arsitektur.

## Requirements

- `core:testing` harus mengeksport semua test dependency sebagai `api()` agar module lain tidak perlu mendeklarasikan ulang
- `MainDispatcherRule` harus tersedia dan kompatibel dengan KMP (commonMain)
- Fake repository dan fake session manager harus tersedia untuk isolasi unit test
- `HomeViewModelTest` harus menguji minimal: LoadItems sukses, LoadItems gagal, efek navigasi
- `GetExamplesUseCaseTest` harus menguji happy path dan error path
- Semua test harus berjalan di `jvmTest` dan `testDebugUnitTest`

## Steps

### Step 1 — Tambah Test Dependencies ke `libs.versions.toml`

```toml
[versions]
turbine = "1.2.0"
mockk = "1.13.10"

[libraries]
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }
```

### Step 2 — Update `core/testing/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.convention.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.junit)
            api(libs.turbine)
            api(libs.mockk)
            api(libs.kotlinx.coroutines.test)
            implementation(projects.common)
            implementation(projects.core.domain)
            implementation(projects.core.model)
        }
    }
}

android {
    namespace = "com.project.starter.core.testing"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
}
```

### Step 3 — Buat `MainDispatcherRule.kt`

**Path:** `core/testing/src/commonMain/kotlin/com/project/starter/core/testing/rules/MainDispatcherRule.kt`

```kotlin
package com.project.starter.core.testing.rules

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

### Step 4 — Buat `FakeExampleRepository.kt`

**Path:** `core/testing/src/commonMain/kotlin/com/project/starter/core/testing/fake/FakeExampleRepository.kt`

```kotlin
package com.project.starter.core.testing.fake

import com.project.starter.common.exception.AppException
import com.project.starter.core.domain.repository.ExampleRepository
import com.project.starter.core.model.ExampleModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeExampleRepository : ExampleRepository {
    var fakeData: List<ExampleModel> = emptyList()
    var shouldThrow: Exception? = null

    override fun getExamples(): Flow<List<ExampleModel>> = flow {
        shouldThrow?.let { throw it }
        emit(fakeData)
    }
}
```

### Step 5 — Buat `FakeSessionManager.kt`

**Path:** `core/testing/src/commonMain/kotlin/com/project/starter/core/testing/fake/FakeSessionManager.kt`

```kotlin
package com.project.starter.core.testing.fake

import com.project.starter.core.domain.repository.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSessionManager : SessionManager {
    private val tokenFlow = MutableStateFlow<String?>(null)

    override suspend fun saveToken(token: String) {
        tokenFlow.value = token
    }

    override suspend fun getToken(): String? = tokenFlow.value

    override suspend fun clearSession() {
        tokenFlow.value = null
    }

    override fun isLoggedIn(): Flow<Boolean> =
        tokenFlow.map { it != null && it.isNotBlank() }
}
```

### Step 6 — Buat `HomeViewModelTest.kt`

**Path:** `feat/home/src/commonTest/kotlin/com/project/starter/feat/home/presentation/HomeViewModelTest.kt`

```kotlin
package com.project.starter.feat.home.presentation

import app.cash.turbine.test
import com.project.starter.common.exception.AppException
import com.project.starter.core.domain.usecase.GetExamplesUseCase
import com.project.starter.core.model.ExampleModel
import com.project.starter.core.testing.fake.FakeExampleRepository
import com.project.starter.core.testing.rules.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepository = FakeExampleRepository()
    private val getExamplesUseCase = GetExamplesUseCase(fakeRepository)
    private val viewModel: HomeViewModel get() = HomeViewModel(getExamplesUseCase)

    @Test
    fun `LoadItems event updates state with items on success`() = runTest {
        // Given
        val expected = listOf(
            ExampleModel("1", "KMP", "Kotlin Multiplatform"),
            ExampleModel("2", "CMP", "Compose Multiplatform"),
        )
        fakeRepository.fakeData = expected

        // When
        val vm = viewModel
        vm.setEvent(HomeEvent.LoadItems)

        // Then
        vm.uiState.test {
            val state = awaitItem()
            assertEquals(expected, state.data.items)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `LoadItems shows error effect on failure`() = runTest {
        // Given
        fakeRepository.shouldThrow = AppException.NetworkException("No internet")
        val vm = viewModel

        // When + Then
        vm.effect.test {
            vm.setEvent(HomeEvent.LoadItems)
            val effect = awaitItem()
            assertTrue(effect is HomeEffect.ShowError)
            assertTrue((effect as HomeEffect.ShowError).message.contains("No internet"))
        }
    }

    @Test
    fun `OnItemClicked emits NavigateToDetail effect`() = runTest {
        val vm = viewModel
        vm.effect.test {
            vm.setEvent(HomeEvent.OnItemClicked("1", "KMP"))
            val effect = awaitItem()
            assertTrue(effect is HomeEffect.NavigateToDetail)
            assertEquals("1", (effect as HomeEffect.NavigateToDetail).id)
            assertEquals("KMP", effect.title)
        }
    }

    @Test
    fun `initial state has empty items and not loading`() = runTest {
        val vm = viewModel
        val state = vm.uiState.value
        assertTrue(state.data.items.isEmpty())
        assertFalse(state.isLoading)
    }
}
```

### Step 7 — Buat `GetExamplesUseCaseTest.kt`

**Path:** `core/domain/src/commonTest/kotlin/com/project/starter/core/domain/usecase/GetExamplesUseCaseTest.kt`

```kotlin
package com.project.starter.core.domain.usecase

import app.cash.turbine.test
import com.project.starter.core.model.ExampleModel
import com.project.starter.core.testing.fake.FakeExampleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetExamplesUseCaseTest {

    private val fakeRepository = FakeExampleRepository()
    private val useCase = GetExamplesUseCase(fakeRepository)

    @Test
    fun `invoke emits data from repository`() = runTest {
        val expected = listOf(ExampleModel("1", "KMP", "Kotlin Multiplatform"))
        fakeRepository.fakeData = expected

        useCase().test {
            val items = awaitItem()
            assertEquals(expected, items)
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits empty list when repository is empty`() = runTest {
        fakeRepository.fakeData = emptyList()

        useCase().test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            awaitComplete()
        }
    }
}
```

### Step 8 — Update `feat/home/build.gradle.kts` — tambah testing dependency

```kotlin
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(projects.core.testing)
            implementation(libs.kotlin.test)
        }
    }
}
```

### Step 9 — Update `core/domain/build.gradle.kts` — tambah testing dependency

```kotlin
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(projects.core.testing)
            implementation(libs.kotlin.test)
        }
    }
}
```

## Files

```
[MODIFY] gradle/libs.versions.toml
[MODIFY] core/testing/build.gradle.kts
[NEW]    core/testing/src/commonMain/.../rules/MainDispatcherRule.kt
[NEW]    core/testing/src/commonMain/.../fake/FakeExampleRepository.kt
[NEW]    core/testing/src/commonMain/.../fake/FakeSessionManager.kt
[NEW]    feat/home/src/commonTest/.../HomeViewModelTest.kt
[MODIFY] feat/home/build.gradle.kts
[NEW]    core/domain/src/commonTest/.../GetExamplesUseCaseTest.kt
[MODIFY] core/domain/build.gradle.kts
```

## Verification

### Automated Tests
```bash
./gradlew testDebugUnitTest   # Android
./gradlew jvmTest             # Desktop
# Output: BUILD SUCCESSFUL dengan X tests passed, 0 failed
```

### Manual Verification
- [ ] Semua test hijau di Android Studio Test Runner
- [ ] Tidak ada test yang di-skip atau dikecualikan
- [ ] CI pipeline (`testDebugUnitTest jvmTest`) berjalan dengan test aktual
