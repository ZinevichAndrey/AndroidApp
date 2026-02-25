package com.example.unocounter


import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration


object Settings{
    private val screenErrorTag = "ErrorScreen"
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


//    fun getScreenResolution(context: Context): Pair<Int, Int> {
//        try {
//            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//            val display = windowManager.defaultDisplay
//            val metrics = DisplayMetrics()
//            display.getMetrics(metrics)
//            val width = metrics.widthPixels
//            val height = metrics.heightPixels
//            return Pair(width, height)
//        }
//        catch (error: Exception) {
//            Log.d(screenErrorTag, error.stackTraceToString())
//        }
//        return Pair(0,0)
//    }
//
//    fun intToDp(int: Int): Int {
//       return (int * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
//    }

    fun hideActionBar(componentActivity: ComponentActivity) {
        componentActivity.window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }




}