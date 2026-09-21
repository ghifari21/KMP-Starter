package com.project.starter.shared.di

import com.project.starter.core.data.di.dataModule
import com.project.starter.feat.home.di.homeModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            dataModule,
            homeModule
        )
    }
}
