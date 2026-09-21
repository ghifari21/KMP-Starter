package modules

import org.gradle.api.Plugin
import org.gradle.api.Project
import ext.id

class DomainModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.id("convention.kmp.library")
        }
    }
}
