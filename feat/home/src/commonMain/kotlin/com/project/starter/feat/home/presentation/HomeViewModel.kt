package com.project.starter.feat.home.presentation

import androidx.lifecycle.viewModelScope
import com.project.starter.common.base.BaseViewModel
import com.project.starter.core.domain.usecase.GetExamplesUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HomeViewModel(
    private val getExamplesUseCase: GetExamplesUseCase,
) : BaseViewModel<HomeEvent, HomeState, HomeEffect>(HomeState()) {
    override fun handleEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadItems -> loadItems()
            is HomeEvent.OnItemClicked -> onItemClicked(event)
        }
    }

    private fun loadItems() {
        safeLaunch(key = "load_items") {
            getExamplesUseCase()
                .onEach { result ->
                    result.fold(
                        onSuccess = { items ->
                            updateState { copy(items = items) }
                        },
                        onFailure = { error ->
                            setEffect { HomeEffect.ShowToast(error.message ?: "Failed to load") }
                        },
                    )
                }.launchIn(viewModelScope)
        }
    }

    private fun onItemClicked(event: HomeEvent.OnItemClicked) {
        setEffect {
            HomeEffect.NavigateToDetail(
                id = event.item.id,
                title = event.item.name,
            )
        }
    }
}
