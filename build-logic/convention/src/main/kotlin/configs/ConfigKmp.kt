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
        
        // WasmJS is temporarily disabled because Room KMP (2.7.0) 
        // does not yet support WasmJS targets.
        // @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
        // wasmJs {
        //     browser()
        // }
    }
}
