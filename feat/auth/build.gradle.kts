plugins {
    id("convention.feature")
}

kotlin {
    android {
        compileSdk = 37
        minSdk = 24
        namespace = "com.project.starter.feat.auth"
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:designsystem"))
            implementation(project(":core:domain"))
        }
    }
}
