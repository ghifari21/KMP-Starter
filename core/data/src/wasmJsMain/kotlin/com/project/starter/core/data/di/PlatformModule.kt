package com.project.starter.core.data.di

import com.project.starter.core.data.local.PlatformPathProvider
import com.project.starter.core.data.local.WasmPlatformPathProvider
import org.koin.dsl.module

actual fun platformModule() = module {
    single<PlatformPathProvider> { WasmPlatformPathProvider() }
}
