package com.project.starter.core.data.di

import com.project.starter.core.data.local.AndroidPlatformPathProvider
import com.project.starter.core.data.local.PlatformPathProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() =
    module {
        single<PlatformPathProvider> { AndroidPlatformPathProvider(androidContext()) }
    }
