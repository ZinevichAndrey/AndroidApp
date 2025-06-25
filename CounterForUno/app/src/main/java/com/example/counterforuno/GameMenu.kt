package com.example.counterforuno

import android.content.Intent
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.ButtonDefaults
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
            playerList.add(Player(name = "Катя", avatar = R.drawable.main_fon))
            playerList.add(Player(name = "Дядя Саша", avatar = R.drawable.main_fon))
            playerList.add(Player(name = "Серёга", avatar = R.drawable.main_fon))
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
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                items(playersList) { player ->

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = player.name,
                            fontSize = 40.sp,
                            color = Color.White,
                            textAlign = TextAlign.Justify
                        )

                        Row(
                            Modifier
                                .fillMaxWidth(),
                            Arrangement.SpaceBetween,
                            Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .pressClickEffect()
                                    .padding(top = 30.dp)
                                    .clickable(interactionSource = remember {
                                        MutableInteractionSource()
                                    }, indication = null, onClick = {
                                        if (player.score > 0) {
                                            player.score -= 1
                                            playerList.sortWith(comparator)
                                            playersList.clear()
                                            playersList.addAll(playerList)
                                        }
                                    })
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.button_minus),
                                    contentDescription = null,
                                    Modifier
                                        .height(80.dp)
                                        .width(174.dp)
                                )
                            }

                            Image(
                                painter = painterResource(id = player.avatar),
                                contentDescription = null,
                                Modifier
                                    .weight(1f)
                                    .size(170.dp)
                            )

                            Box(
                                Modifier
                                    .weight(1f)
                                    .pressClickEffect()
                                    .padding(top = 30.dp)
                                    .clickable(interactionSource = remember {
                                        MutableInteractionSource()
                                    }, indication = null, onClick = {
                                        player.score += 1
                                        playerList.sortWith(comparator)
                                        playersList.clear()
                                        playersList.addAll(playerList)
                                    })
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.button_plus),
                                    contentDescription = null,
                                    Modifier
                                        .height(80.dp)
                                        .width(174.dp)
                                )
                            }

                        }
                        Text(
                            text = player.score.toString(),
                            fontSize = 50.sp,
                            color = Color.White
                        )
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
                .padding(top = 15.dp)
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .pressClickEffect()
                    .clickable(interactionSource = remember {
                        MutableInteractionSource()
                    }, null, onClick = {
                        val player = playerList[0]
                        if (player.score != 0) {
                            val intent = Intent(context, TotalScore::class.java)
                            intent.putExtra(Settings.getKeyWinner(), player)
                            context.startActivity(intent)
                            finish()
                        }
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
                    .weight(1f)
                    .pressClickEffect()
                    .clickable(interactionSource = remember {
                        MutableInteractionSource()
                    }, null, onClick = {

                    })
            ) {
                Image(
                    painter = painterResource(id = R.drawable.button_new_user),
                    contentDescription = null,
                    Modifier
                        .height(80.dp)
                        .width(174.dp)
                )
            }
            Box(
                Modifier
                    .weight(1f)
                    .pressClickEffect()
                    .clickable(interactionSource = remember {
                        MutableInteractionSource()
                    }, null, onClick = {
                        context.startActivity(Intent(context, Direction::class.java))
                    })
            ) {
                Image(
                    painter = painterResource(id = R.drawable.button_direction),
                    contentDescription = null,
                    Modifier
                        .height(80.dp)
                        .width(174.dp)
                )
            }
        }
    }


}