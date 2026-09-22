package com.example.counterforuno


import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration


data class ModeOption(
    val id: String,
    val displayName: String
)

object Settings{
    private const val WINNER_KEY = "WINNER"
    private const val CREATE_PLAYER_KEY = "CreatePlayer"
    private  var mode_points: String = "points"
    private const val PLAYER_ID: String = "PLAYER_ID"
    private const val CARDS_COUNT: String = "CARDS_COUNT"
    private const val SCORED_POINTS: String = "SCORED_POINTS"
    private const val PLAYERS_FILE = "players.txt"
    private const val WINNERS_FILE = "winners_list.txt"




    fun getWinnersFile(): String {
        return WINNERS_FILE
    }

    fun getPlayersFile(): String {
        return PLAYERS_FILE
    }
    fun getSCORED_POINTS(): String {
        return this.SCORED_POINTS
    }
    fun getCards_Count(): String {
        return this.CARDS_COUNT
    }

    fun getPlayerID(): String {
        return this.PLAYER_ID
    }

    fun setMode(mode: String){
        this.mode_points = mode
    }
    fun getMode(): String {
        return this.mode_points
    }



    fun getCreatePlayerKey(): String {
        return this.CREATE_PLAYER_KEY
    }
    fun Bitmap.centerCropToSquare(targetSize: Int): Bitmap {
        val dimension = minOf(width, height)
        val xOffset = (width - dimension) / 2
        val yOffset = (height - dimension) / 2
        val squareBitmap = Bitmap.createBitmap(this, xOffset, yOffset, dimension, dimension)
        return Bitmap.createScaledBitmap(squareBitmap, targetSize, targetSize, true)
    }

    @Composable
     fun getConf(): Configuration {
        return LocalConfiguration.current
    }

    fun getKeyWinner(): String {
        return this.WINNER_KEY
    }


    fun hideActionBar(componentActivity: ComponentActivity) {
        componentActivity.window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }

    fun Context.dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }




}