package com.example.counterforuno

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class GameMenu : ComponentActivity() {
    private val playerList: MutableList<Player> = ArrayList()
    private lateinit var screenSize: Pair<Int, Int>
    private var widthDp: Int = 0
    private var widthScreen = 0
    private val comparator = Comparator { p1: Player, p2: Player -> p2.score - p1.score }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Settings.hideActionBar(this)
            //For test
            playerList.add(Player(name = "Андрей", avatar = R.drawable.main_fon))
            playerList.add(Player(name = "Тётя Люба", avatar = R.drawable.main_fon))
            playerList.add(Player(name = "Катя", avatar = R.drawable.main_fon, ))
            playerList.add(Player(name = "Дядя Саша", avatar = R.drawable.main_fon,))
            playerList.add(Player(name = "Серёга", avatar = R.drawable.main_fon,))
            screenSize = Settings.getScreenResolution(this)
            widthDp = Settings.intToDp(screenSize.first)
            widthScreen = widthDp
            createTable(this.playerList)
        }
    }


    @Composable
    private fun createTable(playerList: MutableList<Player>) {
        val playersList = remember {
            mutableStateListOf<Player>()
        }
        playersList.addAll(playerList)

        Column(
            Modifier
                .fillMaxSize()
                .paint(
                    painterResource(id = R.drawable.game_bg),
                    contentScale = ContentScale.Crop
                )
        ) {
            LazyColumn(Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
                items(playersList ) { player ->

                        Column(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = player.name,
                                fontSize = 40.sp,
                                color = Color.White,
                                textAlign = TextAlign.Justify
                            )
                            Image(
                                painter = painterResource(id = player.avatar),
                                contentDescription = null,
                                Modifier.size(170.dp)
                            )
                        }
                    Row(
                        Modifier.fillMaxWidth().padding(10.dp),
                        Arrangement.SpaceBetween,
                        Alignment.CenterVertically
                    ) {

                        Button(onClick = {
                            if (player.score > 0) {
                                player.score -= 1
                                playerList.sortWith(comparator)
                                playersList.clear()
                                playersList.addAll(playerList)
                            }
                        }, Modifier.size(100.dp)) {
                            Text(text = "-", fontSize = 50.sp)
                        }
                        Text(
                            text = player.score.toString(),
                            fontSize = 50.sp,
                            color = Color.White
                        )


                        Button(onClick = {
                            println(player.score)
                            player.score += 1
                            playerList.sortWith(comparator)
                            playersList.clear()
                            playersList.addAll(playerList)

                        }, Modifier.size(100.dp)) {
                            Text(text = "+", fontSize = 50.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .fillMaxWidth()
                            .background(Color.Black)
                    )
                }
            }
            createNavigationGame()
        }
    }


    @Composable
    private fun createNavigationGame() {
        val context = LocalContext.current
        Row(
            Modifier
                .fillMaxSize()
                .padding(top = 10.dp)
        ) {
            Button(
                onClick = {
                },
                Modifier
                    .weight(1f)
                    .height(100.dp)
                    .width(widthScreen.dp)
            ) {
                Text(text = "Finish", fontSize = 20.sp, textAlign = TextAlign.Justify)
            }
            Button(
                onClick = { /*TODO*/ },
                Modifier
                    .weight(1f)
                    .height(100.dp)
                    .width(widthScreen.dp)
            ) {
                Text(text = "Create character", fontSize = 20.sp, textAlign = TextAlign.Center)
            }
            Button(
                onClick = {
                    context.startActivity(Intent(context, Direction::class.java))

                },
                Modifier
                    .weight(1f)
                    .height(100.dp)
                    .width(widthScreen.dp)
            ) {
                Text(text = "Direction", fontSize = 20.sp, textAlign = TextAlign.Justify)
            }
        }
    }



}