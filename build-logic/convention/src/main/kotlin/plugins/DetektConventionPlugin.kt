package plugins

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            val extension = extensions.getByType<DetektExtension>()
            extension.apply {
                toolVersion = "1.23.7"
                buildUponDefaultConfig = true
                allRules = false
                config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
            }

            tasks.withType<Detekt>().configureEach {
                reports {
                    html.required.set(true)
                    xml.required.set(false)
                    txt.required.set(false)
                }
            }

            dependencies {
                "detektPlugins"("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
            }
        }
    }
}
