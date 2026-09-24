plugins {
    id("convention.feature")
}
kotlin {
    android {
        compileSdk = 37
        minSdk = 24
        namespace = "com.project.starter.feat.home"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:domain"))
            implementation(project(":core:model"))
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.navigation.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.kotlin.test)
        }
    }
}
