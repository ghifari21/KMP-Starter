# Plan 00 — Overview & Master Index

## Objective

Menjadikan **KMPStarter** sebagai *production-grade* Compose Multiplatform Starter yang terinspirasi dari `Android-Compose-Starter-Template`, diadaptasi agar berjalan di semua platform KMP: Android, iOS, Desktop JVM, dan WasmJS.

## Requirements

- Semua fitur inti dari Android native starter harus diadaptasi ke KMP
- Tidak ada dependency yang hanya tersedia di Android (gunakan KMP alternatives: Koin, Ktor, Store5)
- Setiap plan harus dapat dieksekusi oleh AI agent secara mandiri berdasarkan dokumen ini
- Build harus berhasil di semua platform setelah semua plan selesai

## Execution Order (Dependency Graph)

```
Plan 01 (Bug Fixes)   ──────────────────────────────────────► Plan 08 (Platforms)
Plan 01               ──► Plan 02 (Common)
Plan 02               ──► Plan 03 (Design System)
Plan 02               ──► Plan 05 (Domain/Data)
Plan 03               ──► Plan 04 (Navigation)
Plan 05               ──► Plan 04 (Navigation)
Plan 05               ──► Plan 06 (Testing)
Plan 04, 06           ──► Plan 07 (Tooling)
```

## Plans Index

| File | Scope | Estimated Effort |
|---|---|---|
| `01_bug_fixes_critical.md` | 5 bug crash kritis | ~30 menit |
| `02_core_common_upgrade.md` | BaseViewModel, UiState, BaseRepository, BasePagingSource | ~2 jam |
| `03_design_system.md` | AppTheme, Color, Dimens, Typography | ~1 jam |
| `04_navigation_upgrade.md` | BaseNavHost, per-feature nav, DetailScreen, AppRouter, AuthScreen | ~2 jam |
| `05_domain_data_layer.md` | UseCases, Ktor auth, API service, ConnectivityObserver | ~2 jam |
| `06_testing_infrastructure.md` | core:testing, fakes, unit tests | ~2 jam |
| `07_build_tooling.md` | Detekt, product flavors, hooks, create_feature.sh | ~1.5 jam |
| `08_platform_targets.md` | Desktop fix, WasmJS, iOS init fix | ~1 jam |

## Confirmed Technical Decisions

| # | Decision | Value |
|---|---|---|
| DI Framework | Koin 3.5.6 (Hilt tidak kompatibel KMP) | |
| Network | Ktor (Retrofit tidak kompatibel KMP) | |
| Local Cache | Store5 5.1.0-alpha06 | |
| Product Flavors | Dev / Staging / Prod | BASE_URL per flavor |
| Auth Routing | AppRouter + RouterViewModel (Koin-based) | |
| BasePagingSource | `paging-common:3.3.x` (multiplatform) | |
| BASE_URL Dev | `https://api.dev.example.com/` | |
| BASE_URL Staging | `https://api.staging.example.com/` | |
| BASE_URL Prod | `https://api.example.com/` | |

## Verification (After All Plans)

```bash
./gradlew spotlessCheck
./gradlew detekt
./gradlew testDebugUnitTest jvmTest
./gradlew assembleDebug
./gradlew :desktopApp:run
./gradlew :webApp:wasmJsBrowserDistribution
```

Manual:
- [ ] Android app berjalan, auth routing bekerja
- [ ] Desktop app berjalan
- [ ] Navigasi Home → Detail → Back
- [ ] Dark/Light mode berfungsi
