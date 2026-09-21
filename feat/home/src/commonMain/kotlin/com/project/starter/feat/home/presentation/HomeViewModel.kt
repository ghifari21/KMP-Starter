package com.project.starter.feat.home.presentation

import com.project.starter.common.base.BaseViewModel

class HomeViewModel : BaseViewModel<HomeEvent, HomeState, HomeEffect>() {
    override fun createInitialState(): HomeState = HomeState()

    override fun handleEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadItems -> {
                setState { copy(isLoading = true) }
                // Simulate load
                setState { copy(isLoading = false, items = listOf("KMP", "CMP", "Room", "DataStore")) }
                setEffect { HomeEffect.ShowToast("Items loaded") }
            }
        }
    }
}
