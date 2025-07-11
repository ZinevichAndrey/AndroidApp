package com.example.counterforuno


import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import java.io.File

class GameMenu : ComponentActivity() {
    private val playerList: MutableList<Player> = ArrayList()
    private lateinit var screenSize: Pair<Int, Int>
    private var widthDp: Int = 0
    private var widthScreen = 0
    private val comparator = Comparator { p1: Player, p2: Player -> p2.score - p1.score }
    private val PLAYERS_FILE = "players.txt"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Settings.hideActionBar(this)
            //For test
//            playerList.add(Player(name = "Андрей", avatar = R.drawable.main_fon))
//            playerList.add(Player(name = "Тётя Люба", avatar = R.drawable.main_fon))
//            playerList.add(Player(name = "Катя", avatar = R.drawable.main_fon))
//            playerList.add(Player(name = "Дядя Саша", avatar = R.drawable.main_fon))
//            playerList.add(Player(name = "Серёга", avatar = R.drawable.main_fon))
            val list = WriterAndReader.loadWinnersFromFile(this, PLAYERS_FILE)
            playerList.addAll(list)
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
        val context = LocalContext.current
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
                            var dialog by remember {
                                mutableStateOf(false)
                            }
                            Image(
                                painter = painterResource(id = player.avatar),
                                contentDescription = null,
                                Modifier
                                    .weight(1f)
                                    .size(170.dp).clickable(onClick = {
                                        dialog = true
                                    })
                            )

                            if (dialog) {
                                deletePlayer(onDismissRequest = {dialog = false}, onConfirm = {
                                    playersList.remove(player)
                                    val file = File(context.filesDir, PLAYERS_FILE)
                                    if (file.exists()) {
                                        file.delete()
                                        playersList.forEach {
                                            WriterAndReader.saveWinnerToFile(context, it, PLAYERS_FILE)
                                        }
                                    }
                                    dialog = false
                                })
                            }

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
            createNavigationGame(playersList)
        }
    }


    @Composable
    private fun createNavigationGame(mutableList: SnapshotStateList<Player>) {
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
                            val file = File(context.filesDir, PLAYERS_FILE)
                            if (file.exists()) {
                                file.delete()
                            }
                            playerList.forEach {
                                WriterAndReader.saveWinnerToFile(context, it, PLAYERS_FILE)
                            }
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

            val context = LocalContext.current
            var result by remember {
                mutableStateOf<Player?>(null)
            }
            val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
                    activityResult ->
                if (activityResult.resultCode == RESULT_OK) {
                    val data = activityResult.data?.getParcelableExtra<Player>(Settings.getCreatePlayerKey())
                    result = data
                    playerList.add(result!!)
                    mutableList.add(result!!)
                    WriterAndReader.saveWinnerToFile(context,result!!, PLAYERS_FILE)
                }
            }

            Box(
                Modifier.weight(1f)
                    .pressClickEffect()
                    .clickable(interactionSource = remember {
                        MutableInteractionSource()
                    }, null, onClick = {
                        val intent = Intent(context, PlayerMenu::class.java)
                        launcher.launch(intent)
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


    @Composable
    private fun deletePlayer(onDismissRequest: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(onDismissRequest = onDismissRequest,
            title = {Text("Remove")},
            text = {Text("This is test")},
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Confirm")
                }
            },
            dismissButton = {TextButton(onClick = onDismissRequest) {
                Text("Dismiss")
            }})
    }




}