package com.project.starter.feat.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.project.starter.feat.home.presentation.DetailScreen
import com.project.starter.feat.home.presentation.HomeScreen
import com.project.starter.feat.home.presentation.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.homeGraph(navController: NavController) {
    composable<HomeRoute> {
        val viewModel = koinViewModel<HomeViewModel>()
        HomeScreen(
            viewModel = viewModel,
            navigateToDetail = { id, title ->
                navController.navigate(DetailRoute(id = id, title = title))
            },
        )
    }
    composable<DetailRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<DetailRoute>()
        DetailScreen(
            id = args.id,
            title = args.title,
            onNavigateBack = { navController.popBackStack() },
        )
    }
}
