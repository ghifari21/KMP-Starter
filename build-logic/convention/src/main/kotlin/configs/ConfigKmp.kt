package configs

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configKmp(extension: KotlinMultiplatformExtension) {
    extension.apply {
        val isMac = System.getProperty("os.name").lowercase().contains("mac")
        if (isMac) {
            iosX64()
            iosArm64()
            iosSimulatorArm64()
        }
        
        jvm()
        
        // WasmJS is now enabled since we migrated to Store5
        @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
        wasmJs {
            browser {
                val projectDirPath = project.projectDir.path
                commonWebpackConfig {
                    outputFileName = "composeApp.js"
                    devServer = (devServer ?: org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer()).copy(
                        static = (devServer?.static ?: mutableListOf()).apply {
                            add(projectDirPath)
                        }
                    )
                }
            }
            binaries.executable()
        }
    }
}
