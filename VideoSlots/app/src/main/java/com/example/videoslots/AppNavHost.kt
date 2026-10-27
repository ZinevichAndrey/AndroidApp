package com.example.videoslots

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navigator = remember(navController) {
        AppNavigator(navController)
    }
    NavHost(navController = navController, startDestination = Destination.Main.route) {
        composable(Destination.Main.route) {
            MainScreen(
                onPlayWheelOfFortune = { navigator.navigateTo(Destination.WheelOfFortune) },
                onOpenGameView = { navigator.navigateTo(Destination.GameView) }
            )
        }
        composable(Destination.WheelOfFortune.route) {
            WheelOfFortuneScreen(
                onBack = navigator::navigateBack
            )
        }

        composable(Destination.GameView.route) {
            GameViewScreen(
                onBack = navigator::navigateBack
            )
        }

    }

}