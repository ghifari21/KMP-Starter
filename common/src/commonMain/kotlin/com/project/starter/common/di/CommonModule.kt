package com.project.starter.common.di

import com.project.starter.common.utils.DefaultDispatcherProvider
import com.project.starter.common.utils.DispatcherProvider
import org.koin.dsl.module

val commonModule =
    module {
        single<DispatcherProvider> { DefaultDispatcherProvider() }
    }
