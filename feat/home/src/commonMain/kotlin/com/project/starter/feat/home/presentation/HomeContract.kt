package com.project.starter.feat.home.presentation

import com.project.starter.common.base.UiEffect
import com.project.starter.common.base.UiEvent
import com.project.starter.common.base.UiState

data class HomeState(
    val items: List<String> = emptyList(),
) : UiState

sealed interface HomeEvent : UiEvent {
    data object LoadItems : HomeEvent
}

sealed interface HomeEffect : UiEffect {
    data class ShowToast(
        val message: String,
    ) : HomeEffect
}
