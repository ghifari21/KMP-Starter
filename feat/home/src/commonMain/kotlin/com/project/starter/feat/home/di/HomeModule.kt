package com.project.starter.feat.home.di

import com.project.starter.feat.home.presentation.HomeViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule =
    module {
        viewModel { HomeViewModel() }
    }
