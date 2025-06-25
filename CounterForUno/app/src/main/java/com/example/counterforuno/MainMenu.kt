package com.example.counterforuno

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
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
                    contentScale = ContentScale.Crop
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
                Box(Modifier.pressClickEffect().clickable(interactionSource = remember {
                    MutableInteractionSource()
                }, null, onClick = {
                    context.startActivity(Intent(context, GameMenu::class.java))
                }) ) {
                    Image(
                        painter = painterResource(id = R.drawable.button_start),
                        contentDescription = "btn_start",
                        Modifier
                            .width(218.dp)
                            .height(100.dp), contentScale = ContentScale.Crop
                    )
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 250.dp, start = 10.dp, end = 10.dp),
                    Arrangement.SpaceBetween
                ) {
                    Box(Modifier.pressClickEffect().clickable(interactionSource = remember {
                        MutableInteractionSource()
                    }, null, onClick = {
                        context.startActivity(Intent(context, TotalScore::class.java))
                    }) ) {
                        Image(
                            painter = painterResource(id = R.drawable.button_score),
                            contentDescription = "btn_score",
                            Modifier
                                .width(174.dp)
                                .height(80.dp), contentScale = ContentScale.Crop
                        )
                    }
                    Box(Modifier.pressClickEffect().clickable(interactionSource = remember {
                        MutableInteractionSource()
                    }, null, onClick = {
                        finishAndRemoveTask()
                        System.exit(0)
                    }) ) {
                        Image(
                            painter = painterResource(id = R.drawable.button_exit),
                            contentDescription = "btn_exit",
                            Modifier
                                .width(174.dp)
                                .height(80.dp), contentScale = ContentScale.Crop
                        )
                    }

                }

            }
        }
    }


}

