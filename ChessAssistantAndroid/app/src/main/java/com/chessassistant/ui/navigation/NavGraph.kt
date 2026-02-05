package com.chessassistant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chessassistant.ui.screens.analyzer.AnalyzerScreen
import com.chessassistant.ui.screens.console.ConsoleScreen
import com.chessassistant.ui.screens.game.GameScreen
import com.chessassistant.ui.screens.home.HomeScreen
import com.chessassistant.ui.screens.livecamera.LiveCameraScreen
import com.chessassistant.ui.screens.settings.SettingsScreen

/**
 * Navigation routes.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Game : Screen("game")
    data object Analyzer : Screen("analyzer")
    data object LiveCamera : Screen("live_camera")
    data object Console : Screen("console")
    data object Settings : Screen("settings")
}

/**
 * Main navigation graph.
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToGame = { navController.navigate(Screen.Game.route) },
                onNavigateToAnalyzer = { navController.navigate(Screen.Analyzer.route) },
                onNavigateToLiveCamera = { navController.navigate(Screen.LiveCamera.route) },
                onNavigateToConsole = { navController.navigate(Screen.Console.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Analyzer.route) {
            AnalyzerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Console.route) {
            ConsoleScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LiveCamera.route) {
            LiveCameraScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
