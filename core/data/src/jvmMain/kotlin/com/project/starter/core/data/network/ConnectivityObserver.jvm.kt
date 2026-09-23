package com.project.starter.core.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// For Desktop JVM, pinging or socket checking is typically needed.
// Returning dummy available state for now.
class JvmConnectivityObserver : ConnectivityObserver {
    override val status: Flow<ConnectivityObserver.Status> = flowOf(ConnectivityObserver.Status.Available)
}

actual fun createConnectivityObserver(): ConnectivityObserver = JvmConnectivityObserver()
