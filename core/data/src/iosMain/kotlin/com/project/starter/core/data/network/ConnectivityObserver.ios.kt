package com.project.starter.core.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// For now, return a dummy implementation for iOS.
// Real implementation requires NWPathMonitor via cocoapods or custom swift interop.
class IosConnectivityObserver : ConnectivityObserver {
    override val status: Flow<ConnectivityObserver.Status> = flowOf(ConnectivityObserver.Status.Available)
}

actual fun createConnectivityObserver(): ConnectivityObserver = IosConnectivityObserver()
