package modules

import org.gradle.api.Plugin
import org.gradle.api.Project
import ext.id

class FeatureModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.id("convention.cmp.library")
        }
    }
}
