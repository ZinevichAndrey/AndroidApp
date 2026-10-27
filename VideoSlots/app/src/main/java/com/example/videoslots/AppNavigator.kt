package com.example.videoslots

import androidx.navigation.NavHostController

class AppNavigator(private val navController: NavHostController) {

    fun navigateTo(destination: Destination) {
        navController.navigate(destination.route) {
            launchSingleTop = true
        }
    }

    fun navigateBack() {
        navController.popBackStack()
    }

}