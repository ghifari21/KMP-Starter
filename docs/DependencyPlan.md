## Goal Description
Establish a clear, production-ready tech stack for the Compose Multiplatform (CMP) starter project, add the necessary dependencies to the versions catalog, and document them thoroughly in the `README.md`. This will mirror the clarity of your original Android Compose Starter Template while adapting to the KMP ecosystem.

## User Review Required
> [!IMPORTANT]
> - **Dependency Selection**: Below is the proposed tech stack for KMP. Please review if these libraries align with your preferences for the starter template.
>   - **Navigation**: Jetpack Navigation Compose (KMP support) or Voyager? *(Proposed: Jetpack Navigation KMP)*
>   - **Dependency Injection**: Koin (Industry standard for KMP).
>   - **Network**: Ktor Client + Kotlinx Serialization.
>   - **Local Storage**: Room (KMP support now available in alpha/beta) or SQLDelight? *(Proposed: Room KMP, to stay closer to your original template)*
>   - **Async/MVI**: Kotlinx Coroutines + StateFlow (Same as original).

## Open Questions
> [!WARNING]
> - Do you want me to automatically add these dependencies to `gradle/libs.versions.toml` and inject them into their respective `:core` modules right away, or do you just want the `README.md` documentation updated first?
> - Do you want a helper bash/python script to generate new KMP feature modules, similar to `create_feature.sh` in the original template?

## Proposed Changes

### Documentation (README)
#### [MODIFY] README.md
Update the README to reflect the Kotlin Multiplatform architecture.
- Change the **Architecture & Tech Stack** section to list Koin, Ktor, and Room KMP.
- Document the new Multi-Module structure (`:composeApp`, `:core:*`, `:feat:*`).
- Add a section on "Running the Targets" (Android, iOS via Xcode, Desktop via `./gradlew run`, Web via `./gradlew wasmJsBrowserRun`).

### Dependency Catalog
#### [MODIFY] gradle/libs.versions.toml
Add the agreed-upon KMP libraries:
- `io.insert-koin:koin-core` and `koin-compose`
- `io.ktor:ktor-client-core`, `ktor-client-content-negotiation`, and platform engines (Darwin, OkHttp, Java)
- `androidx.room:room-runtime` and compiler (KMP setup)
- `org.jetbrains.androidx.navigation:navigation-compose`

## Verification Plan
### Automated Tests
```bash
./gradlew build
```
### Manual Verification
1. Read the updated `README.md` to ensure the explanations are clear and accurate.
2. Check `gradle/libs.versions.toml` to verify all KMP libraries are available for use in the modules.
