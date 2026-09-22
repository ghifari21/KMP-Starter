# Plan 07 — Build Logic & Tooling

## Objective

Melengkapi developer tooling dengan: Detekt static analysis, multi-environment product flavors (Dev/Staging/Prod), Git pre-push quality gate, `create_feature.sh` script untuk scaffolding feature module baru secara otomatis, dan perbaikan duplikasi pada build-logic.

## Requirements

- Detekt harus berjalan di `commonMain`, `androidMain`, dan `jvmMain` source sets
- Product flavors Dev/Staging/Prod harus dikonfigurasi di `androidApp` dengan `BASE_URL` masing-masing
- `ConventionConstants.kt` harus menggunakan nilai yang benar (`COMPILE_SDK = 37`, tidak ada `MAX_SDK_VERSION` yang salah)
- Build-logic tidak boleh memiliki dependency yang diduplikasi
- `create_feature.sh` harus dapat membuat feature module KMP dengan struktur yang benar
- Git pre-push hook harus menjalankan spotless, detekt, dan unit tests sebelum push

## Steps

### Step 1 — Tambah Detekt ke `libs.versions.toml`

```toml
[versions]
detekt = "1.23.7"

[libraries]
detekt-formatting = { module = "io.gitlab.arturbosch.detekt:detekt-formatting", version.ref = "detekt" }

[plugins]
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```

### Step 2 — Buat `config/detekt/detekt.yml`

```yaml
# config/detekt/detekt.yml
build:
  maxIssues: 0
  weights:
    complexity: 2
    naming: 1
    style: 1

complexity:
  ComplexCondition:
    threshold: 4
  LongMethod:
    threshold: 60
  LongParameterList:
    threshold: 7
  TooManyFunctions:
    threshold: 15

naming:
  FunctionNaming:
    active: false   # Disabled: Compose @Composable functions use PascalCase
  FunctionParameterNaming:
    active: true

style:
  MagicNumber:
    active: true
    ignoreNumbers: ['-1', '0', '1', '2']
    ignoreAnnotated: ['Preview']
  UnusedImports:
    active: true

formatting:
  active: true
  android: true
  autoCorrect: false
  Indentation:
    active: false   # handled by ktlint
```

### Step 3 — Update Root `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.detekt) apply false
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    
    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        source.setFrom(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/jvmMain/kotlin",
        )
    }
    
    dependencies {
        "detektPlugins"(libs.detekt.formatting)
    }
}
```

### Step 4 — Fix `ConventionConstants.kt`

```kotlin
package constants

object ConventionConstants {
    const val BASE_NAME = "com.project.starter"
    const val MIN_SDK_VERSION = 24
    const val COMPILE_SDK_VERSION = 37
    const val TARGET_SDK_VERSION = 37
}
```

### Step 5 — Fix Duplikasi di `build-logic/convention/build.gradle.kts`

```diff
  dependencies {
      implementation(libs.kotlin.gradlePlugin)
-     implementation(libs.kotlin.gradlePlugin)  // REMOVE: duplikat
      implementation(libs.agp.gradlePlugin)
      implementation(libs.compose.gradlePlugin)
      implementation(libs.compose.compiler.gradlePlugin)
-     implementation("androidx.room:room-gradle-plugin:2.7.0-alpha11")  // REMOVE: hardcoded
  }
```

### Step 6 — Tambah Product Flavors ke `androidApp/build.gradle.kts`

```kotlin
android {
    // ...existing config...
    
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "BASE_URL", "\"https://api.dev.example.com/\"")
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String", "BASE_URL", "\"https://api.staging.example.com/\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://api.example.com/\"")
        }
    }
    
    buildFeatures {
        buildConfig = true
    }
}
```

Gunakan `BuildConfig.BASE_URL` di `DataModule.kt` untuk inject ke Ktor client:
```kotlin
// androidMain DataModule
actual fun platformModule() = module {
    single { BuildConfig.BASE_URL }  // inject BASE_URL
}
```

### Step 7 — Buat `create_feature.sh`

**Path:** `create_feature.sh` (root project)

```bash
#!/bin/bash
# Usage: ./create_feature.sh <feature_name>
# Example: ./create_feature.sh profile

set -e

FEATURE_NAME=$1
FEATURE_NAME_LOWER=$(echo "$FEATURE_NAME" | tr '[:upper:]' '[:lower:]')
FEATURE_NAME_CAPITALIZED=$(echo "$FEATURE_NAME_LOWER" | sed 's/./\u&/')
BASE_PACKAGE="com/project/starter/feat/$FEATURE_NAME_LOWER"
MODULE_PATH="feat/$FEATURE_NAME_LOWER"

if [ -z "$FEATURE_NAME" ]; then
    echo "❌ Usage: ./create_feature.sh <feature_name>"
    exit 1
fi

echo "🚀 Creating KMP feature module: feat:$FEATURE_NAME_LOWER"

# Create directory structure
mkdir -p "$MODULE_PATH/src/commonMain/kotlin/$BASE_PACKAGE/di"
mkdir -p "$MODULE_PATH/src/commonMain/kotlin/$BASE_PACKAGE/navigation"
mkdir -p "$MODULE_PATH/src/commonMain/kotlin/$BASE_PACKAGE/presentation"
mkdir -p "$MODULE_PATH/src/commonTest/kotlin/$BASE_PACKAGE/presentation"

# build.gradle.kts
cat > "$MODULE_PATH/build.gradle.kts" << EOF
plugins {
    alias(libs.plugins.convention.feature)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystem)
        }
        commonTest.dependencies {
            implementation(projects.core.testing)
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.project.starter.feat.$FEATURE_NAME_LOWER"
    compileSdk = 37
    defaultConfig { minSdk = 24 }
}
EOF

# Contract (MVI)
cat > "$MODULE_PATH/src/commonMain/kotlin/$BASE_PACKAGE/presentation/${FEATURE_NAME_CAPITALIZED}Contract.kt" << EOF
package com.project.starter.feat.$FEATURE_NAME_LOWER.presentation

import com.project.starter.common.base.UiEffect
import com.project.starter.common.base.UiEvent
import com.project.starter.common.base.UiState

data class ${FEATURE_NAME_CAPITALIZED}State(
    // TODO: Add state fields
) : UiState

sealed interface ${FEATURE_NAME_CAPITALIZED}Event : UiEvent {
    data object LoadData : ${FEATURE_NAME_CAPITALIZED}Event
}

sealed interface ${FEATURE_NAME_CAPITALIZED}Effect : UiEffect {
    data class ShowError(val message: String) : ${FEATURE_NAME_CAPITALIZED}Effect
}
EOF

# ViewModel
cat > "$MODULE_PATH/src/commonMain/kotlin/$BASE_PACKAGE/presentation/${FEATURE_NAME_CAPITALIZED}ViewModel.kt" << EOF
package com.project.starter.feat.$FEATURE_NAME_LOWER.presentation

import com.project.starter.common.base.BaseViewModel

class ${FEATURE_NAME_CAPITALIZED}ViewModel : BaseViewModel<
    ${FEATURE_NAME_CAPITALIZED}Event,
    ${FEATURE_NAME_CAPITALIZED}State,
    ${FEATURE_NAME_CAPITALIZED}Effect
>(${FEATURE_NAME_CAPITALIZED}State()) {

    override fun handleEvent(event: ${FEATURE_NAME_CAPITALIZED}Event) {
        when (event) {
            is ${FEATURE_NAME_CAPITALIZED}Event.LoadData -> loadData()
        }
    }

    private fun loadData() {
        safeLaunch(key = "load_data") {
            // TODO: Implement
        }
    }
}
EOF

# Screen
cat > "$MODULE_PATH/src/commonMain/kotlin/$BASE_PACKAGE/presentation/${FEATURE_NAME_CAPITALIZED}Screen.kt" << EOF
package com.project.starter.feat.$FEATURE_NAME_LOWER.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.project.starter.common.utils.collectMvi

@Composable
fun ${FEATURE_NAME_CAPITALIZED}Screen(viewModel: ${FEATURE_NAME_CAPITALIZED}ViewModel) {
    val state by viewModel.collectMvi { effect ->
        when (effect) {
            is ${FEATURE_NAME_CAPITALIZED}Effect.ShowError -> { /* Show snackbar */ }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("${FEATURE_NAME_CAPITALIZED} Screen")
    }
}
EOF

# Routes
cat > "$MODULE_PATH/src/commonMain/kotlin/$BASE_PACKAGE/navigation/${FEATURE_NAME_CAPITALIZED}Routes.kt" << EOF
package com.project.starter.feat.$FEATURE_NAME_LOWER.navigation

import kotlinx.serialization.Serializable

@Serializable
object ${FEATURE_NAME_CAPITALIZED}Route
EOF

# Navigation (NavGraphBuilder extension)
cat > "$MODULE_PATH/src/commonMain/kotlin/$BASE_PACKAGE/navigation/${FEATURE_NAME_CAPITALIZED}Navigation.kt" << EOF
package com.project.starter.feat.$FEATURE_NAME_LOWER.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.project.starter.feat.$FEATURE_NAME_LOWER.presentation.${FEATURE_NAME_CAPITALIZED}Screen
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.${FEATURE_NAME_LOWER}Graph(navController: NavController) {
    composable<${FEATURE_NAME_CAPITALIZED}Route> {
        val viewModel = koinViewModel<${FEATURE_NAME_CAPITALIZED}ViewModel>()
        ${FEATURE_NAME_CAPITALIZED}Screen(viewModel = viewModel)
    }
}
EOF

# DI Module
cat > "$MODULE_PATH/src/commonMain/kotlin/$BASE_PACKAGE/di/${FEATURE_NAME_CAPITALIZED}Module.kt" << EOF
package com.project.starter.feat.$FEATURE_NAME_LOWER.di

import com.project.starter.feat.$FEATURE_NAME_LOWER.presentation.${FEATURE_NAME_CAPITALIZED}ViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val ${FEATURE_NAME_LOWER}Module = module {
    viewModel { ${FEATURE_NAME_CAPITALIZED}ViewModel() }
}
EOF

# Update settings.gradle.kts
if ! grep -q "feat:$FEATURE_NAME_LOWER" settings.gradle.kts; then
    echo "include(\":feat:$FEATURE_NAME_LOWER\")" >> settings.gradle.kts
    echo "✅ Added include(\":feat:$FEATURE_NAME_LOWER\") to settings.gradle.kts"
fi

echo ""
echo "✅ Feature module created: feat:$FEATURE_NAME_LOWER"
echo ""
echo "Next steps:"
echo "1. Add ${FEATURE_NAME_LOWER}Module to KoinInit.kt"
echo "2. Add ${FEATURE_NAME_CAPITALIZED}Route to Screen.kt if needed"
echo "3. Wire ${FEATURE_NAME_LOWER}Graph() into App.kt NavHost"
echo "4. Add implementation(projects.feat.$FEATURE_NAME_LOWER) to :shared build.gradle.kts"
```

```bash
chmod +x create_feature.sh
```

### Step 8 — Buat `scripts/git-hooks/pre-push`

**Path:** `scripts/git-hooks/pre-push`

```bash
#!/bin/bash
echo "🔍 Running pre-push checks..."

echo "→ Spotless check..."
./gradlew spotlessCheck --quiet
if [ $? -ne 0 ]; then
    echo "❌ Spotless check failed. Run './gradlew spotlessApply' to fix."
    exit 1
fi

echo "→ Detekt check..."
./gradlew detekt --quiet
if [ $? -ne 0 ]; then
    echo "❌ Detekt check failed. Fix issues above."
    exit 1
fi

echo "→ Unit tests..."
./gradlew testDebugUnitTest jvmTest --quiet
if [ $? -ne 0 ]; then
    echo "❌ Unit tests failed."
    exit 1
fi

echo "✅ All checks passed!"
```

Tambahkan ke `androidApp/build.gradle.kts` atau root untuk auto-install:
```kotlin
tasks.register("installGitHooks") {
    doLast {
        val hooksDir = rootProject.file(".git/hooks")
        val preCommitFile = file("scripts/git-hooks/pre-push")
        preCommitFile.copyTo(File(hooksDir, "pre-push"), overwrite = true)
        File(hooksDir, "pre-push").setExecutable(true)
    }
}
tasks.named("preBuild") { dependsOn("installGitHooks") }
```

## Files

```
[MODIFY] gradle/libs.versions.toml
[NEW]    config/detekt/detekt.yml
[MODIFY] build.gradle.kts
[MODIFY] build-logic/convention/build.gradle.kts
[MODIFY] build-logic/convention/src/.../constants/ConventionConstants.kt
[MODIFY] androidApp/build.gradle.kts
[NEW]    create_feature.sh
[NEW]    scripts/git-hooks/pre-push
```

## Verification

### Automated Tests
```bash
./gradlew spotlessCheck
./gradlew detekt
./gradlew assembleDevDebug    # pastikan flavor "dev" berhasil build
./gradlew assembleStagingDebug
./gradlew assembleProdDebug
./create_feature.sh test      # test create_feature script
```

### Manual Verification
- [ ] `./gradlew detekt` berjalan tanpa error
- [ ] 3 flavor muncul di Android Studio Build Variants panel
- [ ] `./create_feature.sh profile` membuat struktur folder yang benar
- [ ] Pre-push hook terinstall di `.git/hooks/pre-push` setelah build
