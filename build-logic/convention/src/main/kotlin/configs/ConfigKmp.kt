package configs

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configKmp(extension: KotlinMultiplatformExtension) {
    extension.apply {
        jvmToolchain(17)
        val isMac = System.getProperty("os.name").lowercase().contains("mac")
        if (isMac) {
            iosX64()
            iosArm64()
            iosSimulatorArm64()
        }

        jvm()

        // WasmJS is conditionally enabled via project property.
        // Enable it in a module by adding: wasmJs = true to gradle.properties
        // or by explicitly calling wasmJs{} in its own build.gradle.kts.
        //
        // NOT enabled here by default because several alpha-stage KMP libraries
        // (koin-compose-viewmodel 1.2.0-Beta5, store5-alpha, paging-common 3.3.x)
        // do not yet publish wasmJs artifacts — enabling it here causes
        // "Unresolved platforms: [wasmJs]" compilation failures across all modules.
        //
        // Once those libraries stabilize, uncomment the block below:
        // @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
        // wasmJs {
        //     browser()
        //     binaries.executable()
        // }
    }
}
