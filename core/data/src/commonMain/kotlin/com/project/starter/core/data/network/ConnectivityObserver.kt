package com.project.starter.core.data.network

import kotlinx.coroutines.flow.Flow

/**
 * Interface to observe the device's network connectivity status.
 */
interface ConnectivityObserver {
    val status: Flow<Status>

    enum class Status {
        Available,
        Unavailable,
        Losing,
        Lost,
    }
}

/**
 * Factory or expect function to provide platform-specific implementation.
 */
expect fun createConnectivityObserver(): ConnectivityObserver
