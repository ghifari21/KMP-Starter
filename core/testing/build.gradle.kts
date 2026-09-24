plugins {
    id("convention.kmp.library")
}

kotlin {
    android {
        compileSdk = 37
        minSdk = 24
        namespace = "com.project.starter.core.testing"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":common"))
            api(project(":core:domain"))
            api(project(":core:model"))

            api(libs.kotlinx.coroutines.test)
            api(libs.turbine)
            api(libs.mockk)
            api(kotlin("test"))
        }
    }
}
