package com.project.starter.core.domain.di

import com.project.starter.core.domain.usecase.GetExamplesUseCase
import org.koin.dsl.module

val domainModule =
    module {
        factory { GetExamplesUseCase(get()) }
    }
