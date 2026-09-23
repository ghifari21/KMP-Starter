package com.project.starter.common.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.project.starter.common.base.BaseViewModel
import com.project.starter.common.base.UiEffect
import com.project.starter.common.base.UiEvent
import com.project.starter.common.base.UiState
import com.project.starter.common.base.UiStateWrapper
import kotlinx.coroutines.flow.collectLatest

/**
 * Convenience Composable extension that simultaneously:
 * 1. Collects [BaseViewModel.uiState] as [State] for Compose
 * 2. Launches a [LaunchedEffect] to consume one-time [UiEffect]s
 *
 * Usage:
 * ```kotlin
 * val state by viewModel.collectMvi { effect ->
 *     when (effect) {
 *         is MyEffect.ShowToast -> Toast.makeText(...)
 *         is MyEffect.Navigate -> navController.navigate(...)
 *     }
 * }
 * ```
 */
@Composable
fun <Event : UiEvent, StateData : UiState, Effect : UiEffect> BaseViewModel<Event, StateData, Effect>.collectMvi(
    onEffect: suspend (Effect) -> Unit,
): State<UiStateWrapper<StateData>> {
    val state = this.uiState.collectAsState()

    LaunchedEffect(Unit) {
        this@collectMvi.effect.collectLatest { effectValue ->
            onEffect(effectValue)
        }
    }

    return state
}
