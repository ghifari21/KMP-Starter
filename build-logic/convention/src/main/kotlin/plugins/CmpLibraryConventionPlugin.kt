package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposeExtension

class CmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("convention.kmp.library")
            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val kotlin = extensions.getByName("kotlin") as KotlinMultiplatformExtension
            val compose = extensions.getByName("compose") as ComposeExtension
            
            kotlin.sourceSets.getByName("commonMain").dependencies {
                implementation(compose.dependencies.runtime)
                implementation(compose.dependencies.foundation)
                implementation(compose.dependencies.material3)
                implementation(compose.dependencies.ui)
                implementation(compose.dependencies.components.resources)
                implementation(compose.dependencies.components.uiToolingPreview)
            }
        }
    }
}
