package com.project.starter.shared.di

import com.project.starter.common.di.commonModule
import com.project.starter.core.data.di.dataModule
import com.project.starter.core.domain.di.domainModule
import com.project.starter.feat.home.di.homeModule
import com.project.starter.shared.router.RouterViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val routerModule =
    module {
        viewModel { RouterViewModel(get()) }
    }

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            commonModule,
            dataModule,
            domainModule,
            homeModule,
            routerModule,
        )
    }
}
