package com.project.starter.core.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AndroidConnectivityObserver(
    private val context: Context,
) : ConnectivityObserver {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override val status: Flow<ConnectivityObserver.Status> =
        callbackFlow {
            val callback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        trySend(ConnectivityObserver.Status.Available)
                    }

                    override fun onLosing(
                        network: Network,
                        maxMsToLive: Int,
                    ) {
                        trySend(ConnectivityObserver.Status.Losing)
                    }

                    override fun onLost(network: Network) {
                        trySend(ConnectivityObserver.Status.Lost)
                    }

                    override fun onUnavailable() {
                        trySend(ConnectivityObserver.Status.Unavailable)
                    }
                }

            val request =
                NetworkRequest
                    .Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()

            connectivityManager.registerNetworkCallback(request, callback)

            // Initial state
            val currentState =
                connectivityManager.activeNetwork?.let {
                    val caps = connectivityManager.getNetworkCapabilities(it)
                    if (caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
                        ConnectivityObserver.Status.Available
                    } else {
                        ConnectivityObserver.Status.Unavailable
                    }
                } ?: ConnectivityObserver.Status.Unavailable

            trySend(currentState)

            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }.distinctUntilChanged()
}

class ConnectivityObserverProvider : KoinComponent {
    val context: Context by inject()
}

actual fun createConnectivityObserver(): ConnectivityObserver {
    val provider = ConnectivityObserverProvider()
    return AndroidConnectivityObserver(provider.context)
}
