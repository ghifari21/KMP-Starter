package com.project.starter.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.project.starter.core.designsystem.theme.AppTheme
import com.project.starter.core.navigation.AppNavigator
import com.project.starter.core.navigation.BaseNavHost
import com.project.starter.feat.home.navigation.HomeRoute
import com.project.starter.feat.home.navigation.homeGraph
import com.project.starter.shared.router.AppRouter
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinContext {
        AppTheme {
            val navController = rememberNavController()
            val navigator: AppNavigator = koinInject()

            // Connect AppNavigator event-driven navigation to NavController
            LaunchedEffect(Unit) {
                navigator.navigationEvents.collect { screen ->
                    navController.navigate(screen)
                }
            }

            AppRouter {
                BaseNavHost(
                    navHostController = navController,
                    startDestination = HomeRoute,
                ) {
                    homeGraph(navController)
                }
            }
        }
    }
}
