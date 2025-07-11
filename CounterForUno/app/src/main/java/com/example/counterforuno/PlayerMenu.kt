package com.example.counterforuno


import android.app.Activity
import android.content.Intent
import android.media.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class PlayerMenu:  ComponentActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Settings.hideActionBar(this)
            createTopPlayerMenu()
        }
    }

    @Composable
    private fun createTopPlayerMenu() {
        var name by remember {
            mutableStateOf("")
        }

        Box(Modifier.fillMaxSize().background(Color.Yellow)) {

            Column(
                Modifier.fillMaxWidth().fillMaxHeight()
                    .paint(painterResource(R.drawable.game_bg), contentScale = ContentScale.Crop)
            ) {
                ->
                Row(
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    //-----------------//
                    Text("New Hero", fontSize = 40.sp, color = Color.Yellow)
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 25.dp),
                    Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        getString(R.string.plMenu_name),
                        textAlign = TextAlign.Center,
                        fontSize = 25.sp,
                        color = Color.White
                    )
                    TextField(
                        name,
                        onValueChange = { newTxt -> name = newTxt },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        textStyle = TextStyle(fontSize = 30.sp)
                    )
                }
                Row(Modifier.fillMaxWidth().padding(top = 35.dp), Arrangement.SpaceAround) {
                    Image(
                        painter = painterResource(R.drawable.main_fon),
                        contentDescription = null,
                        Modifier.size(90.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.main_fon),
                        contentDescription = null,
                        Modifier.size(90.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.main_fon),
                        contentDescription = null,
                        Modifier.size(90.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.main_fon),
                        contentDescription = null,
                        Modifier.size(90.dp)
                    )
                }
                Row(Modifier.fillMaxWidth().padding(top = 15.dp), Arrangement.SpaceAround) {
                    Image(
                        painter = painterResource(R.drawable.main_fon),
                        contentDescription = null,
                        Modifier.size(90.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.main_fon),
                        contentDescription = null,
                        Modifier.size(90.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.main_fon),
                        contentDescription = null,
                        Modifier.size(90.dp)
                    )
                    //   Image(painter = painterResource(R.drawable.main_fon), contentDescription = null, Modifier.size(90.dp))
                }


            }
            Row(Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(start = 20.dp, end = 20.dp, bottom = 20.dp), Arrangement.SpaceAround) {
                Box(
                    Modifier
                        .pressClickEffect()
                        .clickable(interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = {
                            finish()
                        })
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.button_finish),
                        contentDescription = null,
                        Modifier
                            .height(80.dp)
                            .width(174.dp)
                    )
                }
                Box(
                    Modifier
                        .pressClickEffect()
                        .clickable(interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = {
                            if (!name.isBlank()) {
                                //----------------//
                                println("Working")
                            }
                        })
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.main_fon),
                        contentDescription = null,
                        Modifier
                            .height(80.dp)
                            .width(174.dp)
                    )
                }
            }

        }

    }


    private fun create(player: Player) {
        val resultIntent = Intent()
        resultIntent.putExtra(Settings.getCreatePlayerKey(), player)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

}