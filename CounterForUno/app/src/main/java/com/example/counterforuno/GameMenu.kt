package com.example.counterforuno


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.io.File

class GameMenu : ComponentActivity() {
    private val playerList: MutableList<Player> = ArrayList()
    private lateinit var screenSize: Pair<Int, Int>
    private var widthDp: Int = 0
    private var widthScreen = 0
    private val comparator = Comparator { p1: Player, p2: Player -> p2.score - p1.score }
    private val PLAYERS_FILE = "players.txt"
    private val CAMERA_REQUEST_CODE = 1001


    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val scoredPoints = data?.getIntExtra("SCORED_POINTS", 0) ?: 0
            val playerId = data?.getLongExtra("PLAYER_ID", -1L) ?: -1L

            if (scoredPoints > 0 && playerId != -1L) {
                val currentPlayer = playerList.find { it.getId() == playerId }
                currentPlayer?.let { player ->
                    player.score += scoredPoints
                    playerList.sortWith(comparator)
                    // Обновляем UI - нужно обновить список в композе
                    // Для этого можно использовать remember и mutableState
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Settings.hideActionBar(this)
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
                                        // ---------------------------------------------------->
                                        if (Settings.getMode() == "points") {

                                        }
                                        else {
                                            if (player.score > 0) {
                                                player.score -= 1
                                                playerList.sortWith(comparator)
                                                playersList.clear()
                                                playersList.addAll(playerList)
                                            }
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
                            val painter = when (val avatar = player.avatar) {
                                is AvatarSource.Resource -> painterResource(avatar.resId)
                                is AvatarSource.AvatarUri -> rememberAsyncImagePainter(Uri.parse(avatar.uri))
                            }
                            Box(Modifier.size(170.dp).weight(1f)) {
                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    Modifier
                                        .fillMaxSize().padding(5.dp).clickable(onClick = {
                                            dialog = true
                                        })
                                )
                                Image(
                                    painter = painterResource(R.drawable.icon_back_ava),
                                    contentDescription = null,
                                    Modifier.fillMaxSize()
                                )
                            }

                            if (dialog) {
                                DeletePlayerDialog(true,onDismissRequest = {dialog = false}, onConfirm = {
                                    playersList.remove(player)
                                    playerList.remove(player)
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
                                        if (Settings.getMode() == "points") {
                                            val intent = Intent(context, CameraActivity::class.java)
                                            intent.putExtra("PLAYER_ID", player.getId())
                                            cameraLauncher.launch(intent)
                                        }
                                        else {
                                            player.score += 1
                                            playerList.sortWith(comparator)
                                            playersList.clear()
                                            playersList.addAll(playerList)
                                            playersList.forEach {
                                                println(it.getId())
                                            }
                                        }

                                    }
                                    )
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
    fun DeletePlayerDialog(
        showDialog: Boolean,
        onDismissRequest: () -> Unit,
        onConfirm: () -> Unit
    ) {
        AnimatedVisibility(
            visible = showDialog,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AlertDialog(
                onDismissRequest = onDismissRequest,
                confirmButton = {
                    OutlinedButton(onClick = onConfirm, border = BorderStroke(2.dp, Color.White)) {
                        Text(getString(R.string.alert_confirm), color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = onDismissRequest, border = BorderStroke(2.dp, Color.White)) {
                        Text(getString(R.string.alert_cancel), color = Color.White)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_delete),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = getString(R.string.alert_delete),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                text = {
                    Text(
                        text = getString(R.string.alert_remove_mes),
                        color = Color.White
                    )
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.DarkGray,
                tonalElevation = 6.dp
            )
        }
    }




}