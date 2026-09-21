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
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
    }
}
