plugins {
    id("convention.data")
    alias(libs.plugins.kotlinSerialization)
    // alias(libs.plugins.ksp)
    // alias(libs.plugins.room)
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
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.datastore.preferences)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

dependencies {
    // add("kspAndroid", libs.room.compiler)
    // add("kspIosSimulatorArm64", libs.room.compiler)
    // add("kspIosArm64", libs.room.compiler)
    // add("kspIosX64", libs.room.compiler)
}




