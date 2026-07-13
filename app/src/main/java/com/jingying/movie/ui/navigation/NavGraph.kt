package com.jingying.movie.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jingying.movie.ui.category.CategoryScreen
import com.jingying.movie.ui.debug.DebugScreen
import com.jingying.movie.ui.detail.DetailScreen
import com.jingying.movie.ui.history.HistoryScreen
import com.jingying.movie.ui.home.HomeScreen
import com.jingying.movie.ui.player.PlayerScreen
import com.jingying.movie.ui.search.SearchScreen

object Routes {
    const val HOME = "home"
    const val CATEGORY = "category"
    const val SEARCH = "search"
    const val DETAIL = "detail/{vodId}"
    const val PLAYER = "player/{vodId}/{episodeIndex}"
    const val HISTORY = "history"
    const val DEBUG = "debug"

    fun detail(vodId: Int) = "detail/$vodId"
    fun player(vodId: Int, episodeIndex: Int) = "player/$vodId/$episodeIndex"
}

@Composable
fun JingyingNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                navController = navController,
                onSearchClick = { navController.navigate(Routes.SEARCH) },
                onCategoryClick = { navController.navigate(Routes.CATEGORY) },
                onMovieClick = { vodId -> navController.navigate(Routes.detail(vodId)) },
                onHistoryClick = { navController.navigate(Routes.HISTORY) },
                onDebugClick = { navController.navigate(Routes.DEBUG) }
            )
        }

        composable(Routes.CATEGORY) {
            CategoryScreen(
                onBack = { navController.popBackStack() },
                onTypeSelected = { typeId ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("selectedTypeId", typeId)
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { vodId -> navController.navigate(Routes.detail(vodId)) }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("vodId") { type = NavType.IntType })
        ) { backStackEntry ->
            val vodId = backStackEntry.arguments?.getInt("vodId") ?: 0
            DetailScreen(
                vodId = vodId,
                onBack = { navController.popBackStack() },
                onPlayClick = { movieVodId, episodeIndex ->
                    navController.navigate(Routes.player(movieVodId, episodeIndex))
                }
            )
        }

        composable(
            route = Routes.PLAYER,
            arguments = listOf(
                navArgument("vodId") { type = NavType.IntType },
                navArgument("episodeIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val vodId = backStackEntry.arguments?.getInt("vodId") ?: 0
            val episodeIndex = backStackEntry.arguments?.getInt("episodeIndex") ?: 0
            PlayerScreen(
                vodId = vodId,
                initialEpisodeIndex = episodeIndex,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { vodId -> navController.navigate(Routes.detail(vodId)) }
            )
        }

        composable(Routes.DEBUG) {
            DebugScreen(onBack = { navController.popBackStack() })
        }
    }
}
