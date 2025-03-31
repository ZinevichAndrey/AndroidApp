package com.example.counterforuno

import android.content.Intent
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

class MainMenu : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Settings.hideActionBar(this)
            createMainMenu()
        }
    }

    @Composable
    private fun drawMainCanvas() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painterResource(id = R.drawable.main_bg),
                    contentScale = ContentScale.FillBounds
                )
        )
    }




    @Composable
    private fun createMainMenu() {
        drawMainCanvas()
        val context = LocalContext.current

        Box(
            Modifier
                .fillMaxSize()
                .padding(top = 350.dp)
        ) {
            Column(
                modifier = Modifier.padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.button_start),
                    contentDescription = null,
                    Modifier
                        .pressClickEffect()
                        .clickable {
                            context.startActivity(Intent(context, GameMenu::class.java))
                        })
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 230.dp, start = 40.dp, end = 40.dp),
                    Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.button_score),
                        contentDescription = null,
                        Modifier.pressClickEffect().clickable {
                            context.startActivity(Intent(context, TotalScore::class.java))
                        })
                    Image(
                        painter = painterResource(id = R.drawable.button_exit),
                        contentDescription = null,
                        Modifier.pressClickEffect().clickable {
                            finishAndRemoveTask()
                            System.exit(0)
                        })
                }

            }
        }
    }


}

