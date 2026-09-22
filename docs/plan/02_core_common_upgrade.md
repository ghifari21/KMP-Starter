# Plan 02 — Core Common Module Upgrade

## Objective

Upgrade modul `:common` menjadi fondasi MVI yang robust, thread-safe, dan feature-complete.
Semua base class harus siap digunakan oleh semua feature module di seluruh platform KMP.

## Requirements

- `BaseViewModel` harus menggunakan `_uiState.update {}` (atomic, bukan assignment langsung)
- Effects channel harus `Channel.BUFFERED` (mencegah deadlock saat tidak ada observer)
- `safeLaunch(key)` harus mendukung request deduplication (cancel job lama jika key sama)
- `UiState<T>` wrapper `data class` harus tersedia dengan field `data`, `isLoading`, `error`
- `BaseRepository.safeCall()` harus tersedia untuk semua repository di seluruh platform
- `BasePagingSource<T>` harus menggunakan `androidx.paging:paging-common` (KMP-compatible sejak 3.3.x)
- `collectMvi()` Composable extension harus tersedia untuk menyederhanakan penggunaan di Screen
- `DispatcherProvider` harus tersedia untuk injeksi dispatcher dalam testing
- `AppException` harus diperluas: Network, Server, Api, Unauthorized, LocalStorage, Unknown (6 types)

## Steps

### Step 1 — Tambah Dependencies ke `libs.versions.toml`

```toml
[versions]
paging = "3.3.5"
turbine = "1.2.0"   # untuk testing (dipakai di plan 06)

[libraries]
paging-common = { module = "androidx.paging:paging-common", version.ref = "paging" }
paging-compose = { module = "androidx.paging:paging-compose", version.ref = "paging" }
```

### Step 2 — Update `common/build.gradle.kts`

Tambahkan paging dependency:
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            // existing deps...
            implementation(libs.paging.common)
        }
    }
}
```

### Step 3 — Rewrite `BaseViewModel.kt`

**Path:** `common/src/commonMain/kotlin/com/project/starter/common/base/BaseViewModel.kt`

```kotlin
package com.project.starter.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.starter.common.exception.AppException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<Event : UiEvent, State : UiState, Effect : UiEffect>(
    initialStateData: State
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiStateWrapper(data = initialStateData))
    val uiState: StateFlow<UiStateWrapper<State>> = _uiState.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    private val _event = MutableSharedFlow<Event>()
    private val activeRequests = mutableMapOf<String, Job>()

    init {
        viewModelScope.launch {
            _event.collect { event -> handleEvent(event) }
        }
    }

    abstract fun handleEvent(event: Event)

    fun setEvent(event: Event) {
        viewModelScope.launch { _event.emit(event) }
    }

    protected fun updateState(reducer: State.() -> State) {
        _uiState.update { it.copy(data = it.data.reducer()) }
    }

    protected fun setEffect(builder: () -> Effect) {
        viewModelScope.launch { _effect.send(builder()) }
    }

    protected fun safeLaunch(
        key: String? = null,
        showLoading: Boolean = true,
        block: suspend () -> Unit,
    ) {
        if (key != null) activeRequests.remove(key)?.cancel()

        val job = viewModelScope.launch {
            if (showLoading) _uiState.update { it.copy(isLoading = true) }
            _uiState.update { it.copy(error = null) }
            try {
                block()
            } catch (e: AppException) {
                _uiState.update { it.copy(error = e) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = AppException.UnknownException(e.message ?: "Unknown error")) }
            } finally {
                if (showLoading) _uiState.update { it.copy(isLoading = false) }
            }
        }

        if (key != null) {
            activeRequests[key] = job
            job.invokeOnCompletion { if (activeRequests[key] === job) activeRequests.remove(key) }
        }
    }

    fun cancelLoads() {
        activeRequests.values.forEach { it.cancel() }
        activeRequests.clear()
    }

    val currentState: State get() = _uiState.value.data
}
```

### Step 4 — Buat `UiStateWrapper.kt`

**Path:** `common/src/commonMain/kotlin/com/project/starter/common/base/UiStateWrapper.kt`

```kotlin
package com.project.starter.common.base

import com.project.starter.common.exception.AppException

data class UiStateWrapper<T>(
    val data: T,
    val isLoading: Boolean = false,
    val error: AppException? = null,
)
```

> Catatan: Nama `UiStateWrapper` digunakan untuk menghindari konflik nama dengan interface `UiState`.

### Step 5 — Update `UiContract.kt` (interface tidak berubah, pastikan konsisten)

```kotlin
package com.project.starter.common.base

interface UiState
interface UiEvent
interface UiEffect
```

### Step 6 — Buat `BaseRepository.kt`

**Path:** `common/src/commonMain/kotlin/com/project/starter/common/base/BaseRepository.kt`

```kotlin
package com.project.starter.common.base

import com.project.starter.common.exception.AppException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

abstract class BaseRepository {
    protected fun <T, R> safeCall(
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        block: suspend () -> T,
        transform: (T) -> R,
    ): Flow<Result<R>> = flow {
        emit(Result.success(transform(block())))
    }.catch { e ->
        val appException = when (e) {
            is AppException -> e
            else -> AppException.UnknownException(e.message ?: "Unknown error")
        }
        emit(Result.failure(appException))
    }.flowOn(dispatcher)

    protected fun <T> safeCall(
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        block: suspend () -> T,
    ): Flow<Result<T>> = safeCall(dispatcher, block) { it }
}
```

> Catatan: Gunakan `Dispatchers.Default` bukan `Dispatchers.IO` karena `IO` tidak tersedia di semua KMP targets.

### Step 7 — Buat `BasePagingSource.kt`

**Path:** `common/src/commonMain/kotlin/com/project/starter/common/base/BasePagingSource.kt`

```kotlin
package com.project.starter.common.base

import androidx.paging.PagingSource
import androidx.paging.PagingState

abstract class BasePagingSource<T : Any> : PagingSource<Int, T>() {
    protected open val initialPageIndex: Int = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val position = params.key ?: initialPageIndex
        return try {
            val data = fetchData(position, params.loadSize)
            LoadResult.Page(
                data = data,
                prevKey = if (position == initialPageIndex) null else position - 1,
                nextKey = if (data.isEmpty()) null else position + 1,
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    abstract suspend fun fetchData(page: Int, size: Int): List<T>

    override fun getRefreshKey(state: PagingState<Int, T>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
}
```

### Step 8 — Buat `ViewModelHelper.kt`

**Path:** `common/src/commonMain/kotlin/com/project/starter/common/utils/ViewModelHelper.kt`

```kotlin
package com.project.starter.common.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.project.starter.common.base.BaseViewModel
import com.project.starter.common.base.UiEffect
import com.project.starter.common.base.UiEvent
import com.project.starter.common.base.UiState
import com.project.starter.common.base.UiStateWrapper
import kotlinx.coroutines.flow.collectLatest

@Composable
fun <Event : UiEvent, StateData : UiState, Effect : UiEffect>
BaseViewModel<Event, StateData, Effect>.collectMvi(
    onEffect: (Effect) -> Unit,
): State<UiStateWrapper<StateData>> {
    val state = this.uiState.collectAsState()
    LaunchedEffect(Unit) {
        this@collectMvi.effect.collectLatest { onEffect(it) }
    }
    return state
}
```

### Step 9 — Buat `DispatcherProvider.kt`

**Path:** `common/src/commonMain/kotlin/com/project/starter/common/utils/DispatcherProvider.kt`

```kotlin
package com.project.starter.common.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.Default  // IO tidak universal di KMP
    override val default: CoroutineDispatcher = Dispatchers.Default
}
```

### Step 10 — Update `AppException.kt`

```kotlin
package com.project.starter.common.exception

sealed class AppException(override val message: String) : Exception(message) {
    /** Koneksi gagal (timeout, no network) */
    class NetworkException(message: String) : AppException(message)

    /** Server error 5xx */
    class ServerException(message: String) : AppException(message)

    /** API error 4xx (selain 401) */
    class ApiException(val code: Int, message: String) : AppException(message)

    /** Token expired / tidak valid (401) */
    class UnauthorizedException(message: String = "Unauthorized") : AppException(message)

    /** Local storage read/write error */
    class LocalStorageException(message: String) : AppException(message)

    /** Error tidak diketahui */
    class UnknownException(message: String) : AppException(message)
}
```

## Files

```
[MODIFY] gradle/libs.versions.toml
[MODIFY] common/build.gradle.kts
[MODIFY] common/src/commonMain/.../base/BaseViewModel.kt
[NEW]    common/src/commonMain/.../base/UiStateWrapper.kt
[NEW]    common/src/commonMain/.../base/BaseRepository.kt
[NEW]    common/src/commonMain/.../base/BasePagingSource.kt
[NEW]    common/src/commonMain/.../utils/ViewModelHelper.kt
[NEW]    common/src/commonMain/.../utils/DispatcherProvider.kt
[MODIFY] common/src/commonMain/.../exception/AppException.kt
```

## Verification

### Automated Tests
```bash
./gradlew :common:build
./gradlew :common:compileKotlinJvm
./gradlew :common:compileKotlinAndroid
```

### Manual Verification
- [ ] Semua module yang bergantung pada `:common` masih compile
- [ ] Tidak ada breaking change pada `HomeViewModel` (update jika perlu)
