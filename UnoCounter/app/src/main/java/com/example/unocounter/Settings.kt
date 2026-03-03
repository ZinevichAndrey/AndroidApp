package com.example.unocounter


import android.content.res.Configuration
import android.graphics.Bitmap
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration


object Settings{
    private const val WINNER_KEY = "WINNER"
    private const val CREATE_PLAYER_KEY = "CreatePlayer"

    fun getCreatePlayerKey(): String {
        return CREATE_PLAYER_KEY
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
        return WINNER_KEY
    }


    fun hideActionBar(componentActivity: ComponentActivity) {
        componentActivity.window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }




}