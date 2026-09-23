package com.project.starter.shared.router

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.starter.core.domain.repository.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface RouterState {
    data object Loading : RouterState

    data object Authenticated : RouterState

    data object Unauthenticated : RouterState
}

class RouterViewModel(
    sessionManager: SessionManager,
) : ViewModel() {
    val routerState: StateFlow<RouterState> =
        sessionManager
            .isLoggedIn()
            .map { isLoggedIn ->
                if (isLoggedIn) RouterState.Authenticated else RouterState.Unauthenticated
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RouterState.Loading,
            )
}
