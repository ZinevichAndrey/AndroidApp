package com.example.videoslots

sealed interface Destination {
    val route: String

    data object Main: Destination {
        override val route = "main"
    }

    data object WheelOfFortune: Destination {
        override val route = "wheelOfFortune"
    }

    data object GameView: Destination {
        override val route = "gameView"
    }


}