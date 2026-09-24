#!/bin/bash

# ==========================================
# KMP Feature Module Generator Script
# Usage: ./create_feature.sh feature_name
# Example: ./create_feature.sh profile
# ==========================================

if [ -z "$1" ]; then
    echo "❌ Error: Feature name is required."
    echo "Usage: ./create_feature.sh <feature_name>"
    exit 1
fi

FEATURE_NAME=$(echo "$1" | tr '[:upper:]' '[:lower:]')
FEATURE_NAME_CAPITALIZED="$(echo "${FEATURE_NAME}" | head -c 1 | tr '[:lower:]' '[:upper:]')$(echo "${FEATURE_NAME}" | tail -c +2)"

BASE_DIR="feat/$FEATURE_NAME"
PACKAGE_NAME="com.project.starter.feat.$FEATURE_NAME"
PRESENTATION_DIR="$BASE_DIR/src/commonMain/kotlin/com/project/starter/feat/$FEATURE_NAME/presentation"
NAVIGATION_DIR="$BASE_DIR/src/commonMain/kotlin/com/project/starter/feat/$FEATURE_NAME/navigation"
DI_DIR="$BASE_DIR/src/commonMain/kotlin/com/project/starter/feat/$FEATURE_NAME/di"
TEST_DIR="$BASE_DIR/src/commonTest/kotlin/com/project/starter/feat/$FEATURE_NAME/presentation"

echo "🚀 Creating KMP feature module: $FEATURE_NAME..."

mkdir -p "$PRESENTATION_DIR"
mkdir -p "$NAVIGATION_DIR"
mkdir -p "$DI_DIR"
mkdir -p "$TEST_DIR"

# ── build.gradle.kts ──────────────────────────────────────────────────────────
cat <<EOF > "$BASE_DIR/build.gradle.kts"
plugins {
    id("convention.feature")
}

kotlin {
    android {
        namespace = "$PACKAGE_NAME"
        compileSdk = 37
        defaultConfig { minSdk = 24 }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:designsystem"))
            implementation(project(":core:domain"))
            implementation(project(":core:model"))
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
    }
}
EOF

# ── Contract ──────────────────────────────────────────────────────────────────
cat <<EOF > "$PRESENTATION_DIR/${FEATURE_NAME_CAPITALIZED}Contract.kt"
package $PACKAGE_NAME.presentation

import com.project.starter.common.base.UiEffect
import com.project.starter.common.base.UiEvent
import com.project.starter.common.base.UiState

data class ${FEATURE_NAME_CAPITALIZED}State(
    val isLoading: Boolean = false,
) : UiState

sealed interface ${FEATURE_NAME_CAPITALIZED}Event : UiEvent {
    data object Load : ${FEATURE_NAME_CAPITALIZED}Event
}

sealed interface ${FEATURE_NAME_CAPITALIZED}Effect : UiEffect {
    data class ShowToast(val message: String) : ${FEATURE_NAME_CAPITALIZED}Effect
}
EOF

# ── ViewModel ─────────────────────────────────────────────────────────────────
cat <<EOF > "$PRESENTATION_DIR/${FEATURE_NAME_CAPITALIZED}ViewModel.kt"
package $PACKAGE_NAME.presentation

import com.project.starter.common.base.BaseViewModel

class ${FEATURE_NAME_CAPITALIZED}ViewModel :
    BaseViewModel<${FEATURE_NAME_CAPITALIZED}Event, ${FEATURE_NAME_CAPITALIZED}State, ${FEATURE_NAME_CAPITALIZED}Effect>(
        ${FEATURE_NAME_CAPITALIZED}State(),
    ) {
    override fun handleEvent(event: ${FEATURE_NAME_CAPITALIZED}Event) {
        when (event) {
            is ${FEATURE_NAME_CAPITALIZED}Event.Load -> load()
        }
    }

    private fun load() {
        safeLaunch(key = "load") {
            // TODO: implement loading logic
        }
    }
}
EOF

# ── Screen ────────────────────────────────────────────────────────────────────
cat <<EOF > "$PRESENTATION_DIR/${FEATURE_NAME_CAPITALIZED}Screen.kt"
package $PACKAGE_NAME.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.project.starter.common.utils.collectMvi

@Composable
fun ${FEATURE_NAME_CAPITALIZED}Screen(viewModel: ${FEATURE_NAME_CAPITALIZED}ViewModel) {
    val state by viewModel.collectMvi { effect ->
        when (effect) {
            is ${FEATURE_NAME_CAPITALIZED}Effect.ShowToast -> { /* handle toast */ }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("$FEATURE_NAME_CAPITALIZED Screen")
    }
}
EOF

# ── Routes ────────────────────────────────────────────────────────────────────
cat <<EOF > "$NAVIGATION_DIR/${FEATURE_NAME_CAPITALIZED}Routes.kt"
package $PACKAGE_NAME.navigation

import kotlinx.serialization.Serializable

@Serializable
object ${FEATURE_NAME_CAPITALIZED}Route
EOF

# ── Navigation ────────────────────────────────────────────────────────────────
cat <<EOF > "$NAVIGATION_DIR/${FEATURE_NAME_CAPITALIZED}Navigation.kt"
package $PACKAGE_NAME.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import $PACKAGE_NAME.presentation.${FEATURE_NAME_CAPITALIZED}Screen
import $PACKAGE_NAME.presentation.${FEATURE_NAME_CAPITALIZED}ViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.${FEATURE_NAME}Graph() {
    composable<${FEATURE_NAME_CAPITALIZED}Route> {
        val viewModel = koinViewModel<${FEATURE_NAME_CAPITALIZED}ViewModel>()
        ${FEATURE_NAME_CAPITALIZED}Screen(viewModel = viewModel)
    }
}
EOF

# ── DI Module ─────────────────────────────────────────────────────────────────
cat <<EOF > "$DI_DIR/${FEATURE_NAME_CAPITALIZED}Module.kt"
package $PACKAGE_NAME.di

import $PACKAGE_NAME.presentation.${FEATURE_NAME_CAPITALIZED}ViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val ${FEATURE_NAME}Module = module {
    viewModel { ${FEATURE_NAME_CAPITALIZED}ViewModel() }
}
EOF

# ── ViewModel Test ────────────────────────────────────────────────────────────
cat <<EOF > "$TEST_DIR/${FEATURE_NAME_CAPITALIZED}ViewModelTest.kt"
package $PACKAGE_NAME.presentation

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ${FEATURE_NAME_CAPITALIZED}ViewModelTest {

    private fun createViewModel() = ${FEATURE_NAME_CAPITALIZED}ViewModel()

    @Test
    fun \`initial state is correct\`() = runTest {
        val viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
EOF

# ── Update settings.gradle.kts ────────────────────────────────────────────────
if ! grep -q "include(\":feat:$FEATURE_NAME\")" settings.gradle.kts; then
    echo "include(\":feat:$FEATURE_NAME\")" >> settings.gradle.kts
    echo "✅ Added :feat:$FEATURE_NAME to settings.gradle.kts"
fi

echo ""
echo "🎉 Feature module '$FEATURE_NAME' created at $BASE_DIR!"
echo ""
echo "📋 Files created:"
echo "   $PRESENTATION_DIR/${FEATURE_NAME_CAPITALIZED}Contract.kt"
echo "   $PRESENTATION_DIR/${FEATURE_NAME_CAPITALIZED}ViewModel.kt"
echo "   $PRESENTATION_DIR/${FEATURE_NAME_CAPITALIZED}Screen.kt"
echo "   $NAVIGATION_DIR/${FEATURE_NAME_CAPITALIZED}Routes.kt"
echo "   $NAVIGATION_DIR/${FEATURE_NAME_CAPITALIZED}Navigation.kt"
echo "   $DI_DIR/${FEATURE_NAME_CAPITALIZED}Module.kt"
echo "   $TEST_DIR/${FEATURE_NAME_CAPITALIZED}ViewModelTest.kt"
echo ""
echo "🔧 Next steps:"
echo "   1. Sync Gradle (Ctrl+Shift+O in Android Studio)"
echo "   2. Add implementation(project(\":feat:$FEATURE_NAME\")) to shared/build.gradle.kts"
echo "   3. Add ${FEATURE_NAME}Module to KoinInit.kt"
echo "   4. Add ${FEATURE_NAME}Graph() to your NavHost in App.kt"
