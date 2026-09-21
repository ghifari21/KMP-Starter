plugins {
    id("convention.cmp.library")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        compileSdk = 37
        minSdk = 24
        namespace = "com.project.starter.core.navigation"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.navigation.compose)
        }
    }
}

