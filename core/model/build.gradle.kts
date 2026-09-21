plugins {
    id("convention.kmp.library")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        compileSdk = 37
        minSdk = 24
        namespace = "com.project.starter.core.model"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.room.runtime)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

