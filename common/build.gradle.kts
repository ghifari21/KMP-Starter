plugins {
    id("convention.common")
}

kotlin {
    android {
        compileSdk = 37
        minSdk = 24
        namespace = "com.project.starter.common"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

