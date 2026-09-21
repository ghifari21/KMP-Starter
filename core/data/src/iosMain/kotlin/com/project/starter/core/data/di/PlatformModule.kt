package com.project.starter.core.data.di

import com.project.starter.core.data.local.IosPlatformPathProvider
import com.project.starter.core.data.local.PlatformPathProvider
import org.koin.dsl.module

actual fun platformModule() = module {
    single<PlatformPathProvider> { IosPlatformPathProvider() }
}
