package com.project.starter.shared.router

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.project.starter.core.domain.repository.SessionManager
import com.project.starter.feat.auth.presentation.AuthScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppRouter(mainContent: @Composable () -> Unit) {
    val viewModel: RouterViewModel = koinViewModel()
    val state by viewModel.routerState.collectAsState()

    // Quick injection to demonstrate fake login
    val sessionManager: SessionManager = koinInject()
    val coroutineScope = rememberCoroutineScope()

    when (state) {
        RouterState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        RouterState.Unauthenticated -> {
            AuthScreen(onLoginClick = {
                // Fake login: save a dummy token
                coroutineScope.launch {
                    sessionManager.saveToken("dummy_token_123")
                }
            })
        }
        RouterState.Authenticated -> {
            mainContent()
        }
    }
}
