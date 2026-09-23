plugins {
    `kotlin-dsl`
}

group = "com.project.starter.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.agp.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.7")
}

gradlePlugin {
    plugins {
        // Base Plugins
        register("kmpLibrary") {
            id = "convention.kmp.library"
            implementationClass = "plugins.KmpLibraryConventionPlugin"
        }
        register("cmpLibrary") {
            id = "convention.cmp.library"
            implementationClass = "plugins.CmpLibraryConventionPlugin"
        }
        register("detekt") {
            id = "convention.detekt"
            implementationClass = "plugins.DetektConventionPlugin"
        }
        
        // Modules
        register("data") {
            id = "convention.data"
            implementationClass = "modules.DataModuleConventionPlugin"
        }
        register("domain") {
            id = "convention.domain"
            implementationClass = "modules.DomainModuleConventionPlugin"
        }
        register("model") {
            id = "convention.model"
            implementationClass = "modules.ModelModuleConventionPlugin"
        }
        register("feature") {
            id = "convention.feature"
            implementationClass = "modules.FeatureModuleConventionPlugin"
        }
        register("common") {
            id = "convention.common"
            implementationClass = "modules.CommonModuleConventionPlugin"
        }
    }
}
