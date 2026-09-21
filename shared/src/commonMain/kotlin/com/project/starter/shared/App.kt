package com.project.starter.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.starter.core.navigation.Screen
import com.project.starter.feat.home.presentation.HomeScreen
import com.project.starter.feat.home.presentation.HomeViewModel
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinContext {
        MaterialTheme {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = Screen.Home,
            ) {
                composable<Screen.Home> {
                    // Inject ViewModel via Koin
                    val viewModel: HomeViewModel = koinInject()
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
