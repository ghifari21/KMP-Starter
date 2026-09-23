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
BASE_DIR="feat/$FEATURE_NAME"
PACKAGE_NAME="com.project.starter.feat.$FEATURE_NAME"
PACKAGE_DIR="feat/$FEATURE_NAME/src/commonMain/kotlin/com/project/starter/feat/$FEATURE_NAME"

echo "🚀 Creating KMP feature module: $FEATURE_NAME..."

# Create directory structure
mkdir -p "$PACKAGE_DIR/presentation"
mkdir -p "$PACKAGE_DIR/navigation"
mkdir -p "$PACKAGE_DIR/di"

# Create build.gradle.kts
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
        }
    }
}
EOF

# Create placeholder DI module
cat <<EOF > "$PACKAGE_DIR/di/${FEATURE_NAME^}Module.kt"
package $PACKAGE_NAME.di

import org.koin.dsl.module

val ${FEATURE_NAME}Module = module {
    // Add viewModels and other dependencies here
}
EOF

# Append to settings.gradle.kts if not exists
if ! grep -q "include(\":feat:$FEATURE_NAME\")" settings.gradle.kts; then
  echo "include(\":feat:$FEATURE_NAME\")" >> settings.gradle.kts
  echo "✅ Added :feat:$FEATURE_NAME to settings.gradle.kts"
fi

echo "🎉 Feature module $FEATURE_NAME created successfully at $BASE_DIR!"
echo "Don't forget to:"
echo "1. Sync Gradle"
echo "2. Add implementation(project(\":feat:$FEATURE_NAME\")) to shared/build.gradle.kts"
echo "3. Add ${FEATURE_NAME}Module to KoinInit.kt"
