package com.project.starter.core.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// WasmJS dummy implementation of ConnectivityObserver
class WasmJsConnectivityObserver : ConnectivityObserver {
    override val status: Flow<ConnectivityObserver.Status> = flowOf(ConnectivityObserver.Status.Available)
}

actual fun createConnectivityObserver(): ConnectivityObserver = WasmJsConnectivityObserver()
