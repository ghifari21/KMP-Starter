package com.project.starter.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

/**
 * A [NavHost] with built-in horizontal slide transitions for a consistent navigation experience.
 *
 * - Push forward: slides in from the right
 * - Pop back: slides back from the left
 *
 * @param T The type-safe start destination route
 */
@Composable
inline fun <reified T : Any> BaseNavHost(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    startDestination: T,
    noinline builder: NavGraphBuilder.() -> Unit,
) {
    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        },
        builder = builder,
    )
}
