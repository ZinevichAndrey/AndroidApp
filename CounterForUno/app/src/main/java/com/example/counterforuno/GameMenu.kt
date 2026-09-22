package com.example.counterforuno

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
    private val players = mutableStateListOf<Player>()

    private val cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode != RESULT_OK) {
                return@registerForActivityResult
            }
            val data = result.data ?: return@registerForActivityResult
            val score = data.getIntExtra(Settings.getSCORED_POINTS(), 0)
            val playerId = data.getStringExtra(Settings.getPlayerID())
                    ?: return@registerForActivityResult
            if (score <= 0) {
                return@registerForActivityResult
            }
            val player = players.find {
                    it.getId() == playerId
                } ?: return@registerForActivityResult
            player.addScore(score)
            sortPlayers()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        players.clear()
        players.addAll(
            WriterAndReader.loadFromFile(
                this,
                Settings.getPlayersFile()
            ){parcel -> Player.createFromParcel(parcel)}
        )
        sortPlayers()
        setContent {
            Settings.hideActionBar(this)
            CreateTable()
        }
    }

    @Composable
    private fun CreateTable() {
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painterResource(R.drawable.game_bg),
                    contentScale = ContentScale.Crop
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(
                    items = players,
                    key = { it.getId() }
                ) { player ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = player.name,
                            fontSize = 40.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 30.dp)
                                    .pressClickEffect()
                                    .clickable(
                                        interactionSource = remember {
                                            MutableInteractionSource()
                                        },
                                        indication = null
                                    ) {
                                        if (Settings.getMode() == "points") {
                                            if (player.removeLastScore()) {
                                                sortPlayers()
                                            }
                                        } else {
                                            if (player.score > 0) {
                                                player.removeWin()
                                                sortPlayers()
                                            }
                                        }
                                    }
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.button_minus),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            var dialog by remember {
                                mutableStateOf(false)
                            }
                            val painter = when (val avatar = player.avatar) {
                                    is AvatarSource.Resource ->
                                        painterResource(avatar.resId)
                                    is AvatarSource.AvatarUri ->
                                        rememberAsyncImagePainter(
                                            Uri.parse(avatar.uri)
                                        )
                                }
                            Box(
                                modifier = Modifier
                                    .size(170.dp)
                                    .weight(1f)
                            ) {
                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(5.dp)
                                        .clickable {
                                            dialog = true
                                        }
                                )
                                Image(
                                    painter = painterResource(
                                        R.drawable.icon_back_ava
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            if (dialog) {
                                DeletePlayerDialog(
                                    showDialog = true,
                                    onDismissRequest = {
                                        dialog = false
                                    },
                                    onConfirm = {
                                        players.remove(player)
                                        savePlayers()
                                        dialog = false
                                    }
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 30.dp)
                                    .pressClickEffect()
                                    .clickable(
                                        interactionSource = remember {
                                            MutableInteractionSource()
                                        },
                                        indication = null
                                    ) {
                                        if (Settings.getMode() == "points") {
                                            val intent = Intent(
                                                    context,
                                                    CameraActivity::class.java
                                                )
                                            intent.putExtra(
                                                Settings.getPlayerID(),
                                                player.getId()
                                            )
                                            cameraLauncher.launch(intent)
                                        } else {
                                            player.addWin()
                                            sortPlayers()
                                        }
                                    }
                            ) {
                                Image(
                                    painter = painterResource(
                                        R.drawable.button_plus
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    contentScale = ContentScale.Fit
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
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color.Black)
                    )
                }
            }
            CreateNavigation()
        }
    }

    @Composable
    private fun CreateNavigation() {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val player = result.data?.getParcelableExtra<Player>(
                    Settings.getCreatePlayerKey()
                )
                player?.let {
                    players.add(it)
                    sortPlayers()
                    savePlayers()
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 15.dp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 8.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .pressClickEffect()
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null
                    ) {
                        if (
                            players.isNotEmpty() &&
                            players.first().score > 0
                        ) {
                            val winnerPlayer = players.first()
                            val winner = Winner(
                                id = winnerPlayer.getId(),
                                name = winnerPlayer.name,
                                avatar = winnerPlayer.avatar,
                                victories = 1
                            )
                            players.forEach {
                                it.resetScore()
                            }
                            savePlayers()
                            val intent = Intent(
                                context,
                                TotalScore::class.java
                            )
                            intent.putExtra(
                                Settings.getKeyWinner(),
                                winner
                            )
                            context.startActivity(intent)
                            finish()
                        }
                    }
            ) {
                Image(
                    painter = painterResource(
                        R.drawable.button_finish
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentScale = ContentScale.Fit
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .pressClickEffect()
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null
                    ) {
                        launcher.launch(
                            Intent(
                                context,
                                PlayerMenu::class.java
                            )
                        )
                    }
            ) {
                Image(
                    painter = painterResource(
                        R.drawable.button_new_user
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentScale = ContentScale.Fit
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .pressClickEffect()
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null
                    ) {
                        context.startActivity(
                            Intent(
                                context,
                                Direction::class.java
                            )
                        )
                    }
            ) {
                Image(
                    painter = painterResource(
                        R.drawable.button_direction
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    private fun sortPlayers() {
        players.sortByDescending { it.score }
    }
    private fun savePlayers() {
        val file = File(filesDir, Settings.getPlayersFile())
        if (file.exists()) {
            file.delete()
        }
        players.forEach {
            WriterAndReader.savePlayersToFile(
                this,
                it,
                Settings.getPlayersFile()
            )
        }
    }
    @Composable
    private fun DeletePlayerDialog(
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
                    OutlinedButton(
                        onClick = onConfirm,
                        border = BorderStroke(
                            2.dp,
                            Color.White
                        )
                    ) {
                        Text(
                            getString(R.string.alert_confirm),
                            color = Color.White
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        border = BorderStroke(
                            2.dp,
                            Color.White
                        )
                    ) {
                        Text(
                            getString(R.string.alert_cancel),
                            color = Color.White
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(
                                android.R.drawable.ic_menu_delete
                            ),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = getString(R.string.alert_delete),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
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


