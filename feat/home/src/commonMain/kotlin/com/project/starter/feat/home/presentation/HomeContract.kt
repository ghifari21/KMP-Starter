package com.project.starter.feat.home.presentation

import com.project.starter.common.base.UiEffect
import com.project.starter.common.base.UiEvent
import com.project.starter.common.base.UiState
import com.project.starter.core.model.ExampleModel

data class HomeState(
    val items: List<ExampleModel> = emptyList(),
) : UiState

sealed interface HomeEvent : UiEvent {
    data object LoadItems : HomeEvent

    data class OnItemClicked(
        val item: ExampleModel,
    ) : HomeEvent
}

sealed interface HomeEffect : UiEffect {
    data class ShowToast(
        val message: String,
    ) : HomeEffect

    data class NavigateToDetail(
        val id: String,
        val title: String,
    ) : HomeEffect
}
