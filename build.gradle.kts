plugins {
    alias(libs.plugins.spotless) apply false
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

subprojects {
    pluginManager.withPlugin("com.diffplug.spotless") {
        configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            kotlin {
                target("**/*.kt")
                targetExclude("**/build/**/*.kt")
                ktlint(libs.versions.ktlint.get())
            }
            kotlinGradle {
                target("*.gradle.kts")
                ktlint(libs.versions.ktlint.get())
            }
        }
    }
    
    // Apply spotless to every subproject
    apply(plugin = "com.diffplug.spotless")
}
