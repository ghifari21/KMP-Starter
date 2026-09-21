package modules

import org.gradle.api.Plugin
import org.gradle.api.Project
import ext.id

class DataModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.id("convention.kmp.library")
            // Apply Ktor/SQLDelight/Room conventions here if needed
        }
    }
}
