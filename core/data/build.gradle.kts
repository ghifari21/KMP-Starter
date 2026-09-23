plugins {
    id("convention.data")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        compileSdk = 37
        minSdk = 24
        namespace = "com.project.starter.core.data"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:domain"))
            implementation(project(":common"))
            implementation(libs.koin.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.datastore.preferences)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.store5)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
    }
}
