# Plan 05 — Domain & Data Layer

## Objective

Menghubungkan seluruh lapisan arsitektur dari UI hingga network agar data flow benar-benar berfungsi end-to-end: UseCase → Repository → ApiService → Ktor, dengan auth interceptor otomatis dan error mapping yang tepat.

## Requirements

- `GetExamplesUseCase` harus tersedia dan diinjeksi ke `HomeViewModel`
- `ExampleApiService` harus membuat Ktor request nyata (bukan mengembalikan emptyList)
- `installGlobalErrorMapper()` harus dipanggil dalam `HttpClientFactory`
- Ktor `Auth` plugin harus meng-inject Bearer token dari `SessionManager` secara otomatis
- Token expired (401) harus membersihkan session (`clearSession()`)
- `ExampleRepositoryImpl` harus menggunakan `apiService.getExamples()` sebagai Fetcher + `DataMapper`
- `HomeViewModel` harus menggunakan `GetExamplesUseCase` bukan data hardcoded
- `HomeScreen` harus menggunakan `LazyColumn` dan mengonsumsi `HomeEffect` (snackbar/toast)
- `ConnectivityObserver` harus tersedia dengan `expect/actual` untuk tiap platform

## Steps

### Step 1 — Buat `GetExamplesUseCase.kt`

**Path:** `core/domain/src/commonMain/kotlin/com/project/starter/core/domain/usecase/GetExamplesUseCase.kt`

```kotlin
package com.project.starter.core.domain.usecase

import com.project.starter.core.domain.repository.ExampleRepository
import com.project.starter.core.model.ExampleModel

class GetExamplesUseCase(private val repository: ExampleRepository) {
    suspend operator fun invoke(): Result<List<ExampleModel>> =
        runCatching { repository.getExamples().let { emptyList<ExampleModel>() } }
        // Note: getExamples() returns Flow — collect first emission
}
```

> Catatan: Karena `ExampleRepository.getExamples()` mengembalikan `Flow<List<ExampleModel>>`, use case bisa juga langsung expose flow atau gunakan `.first()`.

Alternatif yang lebih idiomatis untuk Flow-based repository:
```kotlin
class GetExamplesUseCase(private val repository: ExampleRepository) {
    operator fun invoke(): Flow<List<ExampleModel>> = repository.getExamples()
}
```

Gunakan versi Flow karena lebih sesuai dengan reactive pattern Store5.

### Step 2 — Update `ExampleApiService.kt`

**Path:** `core/data/src/commonMain/kotlin/com/project/starter/core/data/network/ExampleApiService.kt`

```kotlin
package com.project.starter.core.data.network

import com.project.starter.core.model.response.ExampleResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ExampleApiService(private val client: HttpClient) {
    suspend fun getExamples(): List<ExampleResponse> =
        client.get("/examples").body()

    suspend fun getExampleById(id: String): ExampleResponse =
        client.get("/examples/$id").body()
}
```

### Step 3 — Buat `AuthPlugin.kt`

**Path:** `core/data/src/commonMain/kotlin/com/project/starter/core/data/network/AuthPlugin.kt`

```kotlin
package com.project.starter.core.data.network

import com.project.starter.core.domain.repository.SessionManager
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import kotlinx.coroutines.runBlocking

fun HttpClientConfig<*>.installAuthPlugin(sessionManager: SessionManager) {
    install(Auth) {
        bearer {
            loadTokens {
                val token = sessionManager.getToken()
                if (token != null) BearerTokens(token, "") else null
            }
            refreshTokens {
                // Token expired (401) — clear session, force re-login
                runBlocking { sessionManager.clearSession() }
                null  // returning null forces 401 to propagate
            }
            sendWithoutRequest { true }
        }
    }
}
```

### Step 4 — Update `HttpClientFactory.kt`

```kotlin
package com.project.starter.core.data.network

import com.project.starter.core.domain.repository.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(sessionManager: SessionManager): HttpClient =
    HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        installGlobalErrorMapper()
        installAuthPlugin(sessionManager)
    }
```

### Step 5 — Update `ExampleRepositoryImpl.kt`

```kotlin
package com.project.starter.core.data.repository

import com.project.starter.core.data.network.ExampleApiService
import com.project.starter.core.domain.repository.ExampleRepository
import com.project.starter.core.model.ExampleModel
import com.project.starter.core.model.DataMapper.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse

class ExampleRepositoryImpl(
    private val apiService: ExampleApiService,
) : ExampleRepository {
    private val store = StoreBuilder
        .from(
            fetcher = Fetcher.of { _: String ->
                apiService.getExamples().map { it.toModel() }
            },
        ).build()

    override fun getExamples(): Flow<List<ExampleModel>> =
        store.stream(StoreReadRequest.cached("examples", refresh = true))
            .map { response ->
                when (response) {
                    is StoreReadResponse.Data -> response.value
                    is StoreReadResponse.Error -> throw response.errorMessageOrNull()
                        ?.let { Exception(it) } ?: Exception("Store error")
                    else -> emptyList()
                }
            }
}
```

### Step 6 — Update `DataModule.kt`

```kotlin
val dataModule = module {
    single { createHttpClient(get()) }  // inject sessionManager
    single { ExampleApiService(get()) }
    single { createDataStore(get()) }
    single<SessionManager> { EncryptedSessionManager(get()) }
    single<ExampleRepository> { ExampleRepositoryImpl(get()) }
    single { AppNavigator() }
    // platform-specific
    single { platformModule() }
}
```

Juga register `GetExamplesUseCase` (bisa di `dataModule` atau `domainModule`):
```kotlin
factory { GetExamplesUseCase(get()) }
```

### Step 7 — Update `HomeViewModel.kt`

```kotlin
package com.project.starter.feat.home.presentation

import com.project.starter.common.base.BaseViewModel
import com.project.starter.core.domain.usecase.GetExamplesUseCase
import com.project.starter.core.model.ExampleModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HomeViewModel(
    private val getExamplesUseCase: GetExamplesUseCase,
) : BaseViewModel<HomeEvent, HomeState, HomeEffect>(HomeState()) {

    override fun handleEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadItems -> loadItems()
            is HomeEvent.OnItemClicked -> onItemClicked(event.id, event.title)
        }
    }

    private fun loadItems() {
        safeLaunch(key = "load_items") {
            getExamplesUseCase()
                .onEach { items -> updateState { copy(items = items) } }
                .catch { e -> setEffect { HomeEffect.ShowError(e.message ?: "Error") } }
                .launchIn(this)
        }
    }

    private fun onItemClicked(id: String, title: String) {
        setEffect { HomeEffect.NavigateToDetail(id, title) }
    }
}
```

### Step 8 — Update `HomeContract.kt`

```kotlin
package com.project.starter.feat.home.presentation

import com.project.starter.common.base.UiEffect
import com.project.starter.common.base.UiEvent
import com.project.starter.common.base.UiState
import com.project.starter.core.model.ExampleModel

data class HomeState(
    val items: List<ExampleModel> = emptyList(),
    val searchQuery: String = "",
) : UiState

sealed interface HomeEvent : UiEvent {
    data object LoadItems : HomeEvent
    data class OnItemClicked(val id: String, val title: String) : HomeEvent
}

sealed interface HomeEffect : UiEffect {
    data class ShowError(val message: String) : HomeEffect
    data class NavigateToDetail(val id: String, val title: String) : HomeEffect
}
```

### Step 9 — Update `HomeScreen.kt`

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navigateToDetail: (String, String) -> Unit = { _, _ -> },
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val state by viewModel.collectMvi { effect ->
        when (effect) {
            is HomeEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            is HomeEffect.NavigateToDetail -> navigateToDetail(effect.id, effect.title)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setEvent(HomeEvent.LoadItems)
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(state.data.items) { item ->
                    ListItem(
                        headlineContent = { Text(item.name) },
                        supportingContent = { Text(item.description) },
                        modifier = Modifier.clickable {
                            viewModel.setEvent(HomeEvent.OnItemClicked(item.id, item.name))
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
```

### Step 10 — Buat `ConnectivityObserver` (expect/actual)

**commonMain:**
```kotlin
// common/src/commonMain/.../utils/ConnectivityObserver.kt
interface ConnectivityObserver {
    val isConnected: Flow<Boolean>
}

expect fun createConnectivityObserver(): ConnectivityObserver
```

**androidMain:**
```kotlin
// common/src/androidMain/.../utils/ConnectivityObserver.android.kt
actual fun createConnectivityObserver(): ConnectivityObserver = AndroidConnectivityObserver(context)
// Implementasi menggunakan ConnectivityManager.NetworkCallback
```

**jvmMain:**
```kotlin
// common/src/jvmMain/.../utils/ConnectivityObserver.jvm.kt
actual fun createConnectivityObserver(): ConnectivityObserver = object : ConnectivityObserver {
    override val isConnected: Flow<Boolean> = flowOf(true) // Desktop selalu dianggap connected
}
```

**iosMain:**
```kotlin
// Implementasi menggunakan NWPathMonitor via platform.Network
```

**wasmJsMain:**
```kotlin
// Menggunakan window.navigator.onLine
```

## Files

```
[NEW]    core/domain/src/commonMain/.../usecase/GetExamplesUseCase.kt
[MODIFY] core/data/src/commonMain/.../network/ExampleApiService.kt
[NEW]    core/data/src/commonMain/.../network/AuthPlugin.kt
[MODIFY] core/data/src/commonMain/.../network/HttpClientFactory.kt
[MODIFY] core/data/src/commonMain/.../repository/ExampleRepositoryImpl.kt
[MODIFY] core/data/src/commonMain/.../di/DataModule.kt
[MODIFY] feat/home/src/commonMain/.../presentation/HomeViewModel.kt
[MODIFY] feat/home/src/commonMain/.../presentation/HomeContract.kt
[MODIFY] feat/home/src/commonMain/.../presentation/HomeScreen.kt
[NEW]    common/src/commonMain/.../utils/ConnectivityObserver.kt
[NEW]    common/src/androidMain/.../utils/ConnectivityObserver.android.kt
[NEW]    common/src/jvmMain/.../utils/ConnectivityObserver.jvm.kt
[NEW]    common/src/iosMain/.../utils/ConnectivityObserver.ios.kt
[NEW]    common/src/wasmJsMain/.../utils/ConnectivityObserver.wasmJs.kt
[MODIFY] feat/home/src/commonMain/.../di/HomeModule.kt
```

## Verification

### Automated Tests
```bash
./gradlew :core:domain:build
./gradlew :core:data:build
./gradlew :feat:home:build
./gradlew assembleDebug
./gradlew jvmTest
```

### Manual Verification
- [ ] HomeScreen menampilkan loading indicator saat fetch
- [ ] Data dari API (atau Store5 fallback) tampil di LazyColumn
- [ ] Error ditampilkan sebagai Snackbar
- [ ] Klik item → navigasi ke DetailScreen
