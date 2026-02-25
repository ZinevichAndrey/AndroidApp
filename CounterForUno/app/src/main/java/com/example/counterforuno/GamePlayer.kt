package com.example.counterforuno

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class GamePlayer(
    val playerStats: PlayerStats,
    scoreInit: Int = 0
) {
    var score by mutableStateOf(scoreInit)
}