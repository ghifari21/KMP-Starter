package com.project.starter.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.starter.common.exception.AppException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel implementing the MVI (Model-View-Intent) pattern.
 *
 * Features:
 * - Thread-safe state updates via [_uiState.update]
 * - Buffered effects channel to prevent deadlocks
 * - [safeLaunch] with optional key-based request deduplication
 * - Automatic loading/error state management
 *
 * @param Event Screen events dispatched from the UI
 * @param State Screen-specific state data
 * @param Effect One-time side effects (navigation, toasts, etc.)
 * @param initialStateData The starting value for [State]
 */
abstract class BaseViewModel<Event : UiEvent, State : UiState, Effect : UiEffect>(
    initialStateData: State,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiStateWrapper(data = initialStateData))
    val uiState: StateFlow<UiStateWrapper<State>> = _uiState.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    private val _event = MutableSharedFlow<Event>()
    private val activeRequests = mutableMapOf<String, Job>()

    init {
        viewModelScope.launch {
            _event.collect { event -> handleEvent(event) }
        }
    }

    /** Override to handle [Event]s dispatched from the UI. */
    abstract fun handleEvent(event: Event)

    /** Dispatch a UI [event] to be processed by [handleEvent]. */
    fun setEvent(event: Event) {
        viewModelScope.launch { _event.emit(event) }
    }

    /**
     * Update the [State] atomically using a reducer function.
     * Thread-safe — uses [MutableStateFlow.update] internally.
     */
    protected fun updateState(reducer: State.() -> State) {
        _uiState.update { it.copy(data = it.data.reducer()) }
    }

    /** Emit a one-time [Effect] to the UI. */
    protected fun setEffect(builder: () -> Effect) {
        viewModelScope.launch { _effect.send(builder()) }
    }

    /**
     * Launch a coroutine with optional request deduplication, loading state management,
     * and automatic [AppException] error handling.
     *
     * @param key If provided, any existing job with the same key is cancelled before starting.
     * @param showLoading Whether to set [UiStateWrapper.isLoading] = true during execution.
     * @param block The suspending block to execute.
     */
    protected fun safeLaunch(
        key: String? = null,
        showLoading: Boolean = true,
        block: suspend () -> Unit,
    ) {
        if (key != null) activeRequests.remove(key)?.cancel()

        val job =
            viewModelScope.launch {
                if (showLoading) _uiState.update { it.copy(isLoading = true) }
                _uiState.update { it.copy(error = null) }
                try {
                    block()
                } catch (e: AppException) {
                    _uiState.update { it.copy(error = e) }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(error = AppException.UnknownException(e.message ?: "An unknown error occurred"))
                    }
                } finally {
                    if (showLoading) _uiState.update { it.copy(isLoading = false) }
                }
            }

        if (key != null) {
            activeRequests[key] = job
            job.invokeOnCompletion { if (activeRequests[key] === job) activeRequests.remove(key) }
        }
    }

    /** Cancel all active keyed requests launched via [safeLaunch]. */
    fun cancelLoads() {
        activeRequests.values.forEach { it.cancel() }
        activeRequests.clear()
    }

    /** Convenience accessor for the current state data. */
    val currentState: State get() = _uiState.value.data
}
