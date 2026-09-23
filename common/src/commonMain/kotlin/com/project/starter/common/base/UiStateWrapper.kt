package com.project.starter.common.base

import com.project.starter.common.exception.AppException

/**
 * Wraps a UI state [T] with common loading and error metadata.
 * Used by [BaseViewModel] to expose a unified state representation.
 */
data class UiStateWrapper<T>(
    val data: T,
    val isLoading: Boolean = false,
    val error: AppException? = null,
)
