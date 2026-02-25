package com.example.unocounter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

class GameMenu : ComponentActivity() {
    private val playerStatsList = mutableListOf<PlayerStats>()
    private val PLAYERS_FILE = "players.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Settings.hideActionBar(this)
            val loadedPlayers = WriterAndReader.loadPlayers(this, PLAYERS_FILE)
            playerStatsList.clear()
            playerStatsList.addAll(loadedPlayers)
            val gamePlayers = playerStatsList
                .map { GamePlayer(it) }
                .toMutableStateList()
            GameTable(gamePlayers)
        }
    }

    @Composable
    private fun GameTable(players: SnapshotStateList<GamePlayer>) {
        val listState = rememberLazyListState()
        var scrollTrigger by remember { mutableStateOf(0) }
        LaunchedEffect(scrollTrigger) {
            if (players.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .paint(painterResource(R.drawable.game_bg), contentScale = ContentScale.Crop)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(
                    items = players,
                    key = { it.playerStats.id }
                ) { gamePlayer ->
                    PlayerRow(
                        gamePlayer = gamePlayer,
                        players = players,
                        onScoreChanged = {
                            players.sortByDescending { it.score }
                            scrollTrigger++
                        },
                        modifier = Modifier.animateItem()
                    )
                }
            }

            NavigationRow(players)
        }
    }

    @Composable
    private fun PlayerRow(
        gamePlayer: GamePlayer,
        players: SnapshotStateList<GamePlayer>,
        onScoreChanged: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val playerStats = gamePlayer.playerStats
        val context = LocalContext.current
        var showDeleteDialog by remember { mutableStateOf(false) }

        Column(
            modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = playerStats.name,
                fontSize = 40.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .pressClickEffect()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (gamePlayer.score > 0) {
                                gamePlayer.score--
                                onScoreChanged()
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(R.drawable.button_minus),
                        contentDescription = null,
                        modifier = Modifier
                            .height(80.dp)
                            .width(174.dp)
                    )
                }
                Box(
                    Modifier
                        .size(170.dp)
                        .clickable { showDeleteDialog = true }
                ) {
                    val painter = when (val avatar = playerStats.avatar) {
                        is AvatarSource.Resource -> painterResource(avatar.resId)
                        is AvatarSource.AvatarUri -> rememberAsyncImagePainter(Uri.parse(avatar.uri))
                    }
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(5.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.icon_back_ava),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    Modifier
                        .weight(1f)
                        .pressClickEffect()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            gamePlayer.score++
                            onScoreChanged()
                        }
                ) {
                    Image(
                        painter = painterResource(R.drawable.button_plus),
                        contentDescription = null,
                        modifier = Modifier
                            .height(80.dp)
                            .width(174.dp)
                    )
                }
            }
            Text(
                text = gamePlayer.score.toString(),
                fontSize = 50.sp,
                color = Color.White
            )
        }
        if (showDeleteDialog) {
            DeletePlayerDialog(
                onDismissRequest = { showDeleteDialog = false },
                onConfirm = {
                    players.remove(gamePlayer)
                    playerStatsList.remove(playerStats)
                    WriterAndReader.savePlayers(context, playerStatsList, PLAYERS_FILE)
                    showDeleteDialog = false
                }
            )
        }
        Spacer(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth()
                .background(Color.Black)
        )
    }

    @Composable
    private fun NavigationRow(players: SnapshotStateList<GamePlayer>) {
        val context = LocalContext.current
        Row(
            Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .pressClickEffect()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (players.isNotEmpty() && players[0].score > 0) {
                            val winner = players[0].playerStats
                            winner.numberOfVictories++
                            WriterAndReader.savePlayers(context, playerStatsList, PLAYERS_FILE)
                            val intent = Intent(context, TotalScore::class.java)
                            intent.putExtra(Settings.getKeyWinner(), winner)
                            context.startActivity(intent)
                            finish()
                        }
                    }
            ) {
                Image(
                    painter = painterResource(R.drawable.button_finish),
                    contentDescription = null,
                    modifier = Modifier
                        .height(80.dp)
                        .width(174.dp)
                )
            }
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    val newPlayer =
                        result.data?.getParcelableExtra<PlayerStats>(
                            Settings.getCreatePlayerKey()
                        )
                    newPlayer?.let {
                        playerStatsList.add(it)
                        players.add(GamePlayer(it))
                        WriterAndReader.savePlayers(context, playerStatsList, PLAYERS_FILE)
                    }
                }
            }
            Box(
                Modifier
                    .weight(1f)
                    .pressClickEffect()
                    .clickable {
                        launcher.launch(Intent(context, PlayerMenu::class.java))
                    }
            ) {
                Image(
                    painter = painterResource(R.drawable.button_new_user),
                    contentDescription = null,
                    modifier = Modifier
                        .height(80.dp)
                        .width(174.dp)
                )
            }
            Box(
                Modifier
                    .weight(1f)
                    .pressClickEffect()
                    .clickable {
                        context.startActivity(Intent(context, Direction::class.java))
                    }
            ) {
                Image(
                    painter = painterResource(R.drawable.button_direction),
                    contentDescription = null,
                    modifier = Modifier
                        .height(80.dp)
                        .width(174.dp)
                )
            }
        }
    }

    @Composable
    private fun DeletePlayerDialog(
        onDismissRequest: () -> Unit,
        onConfirm: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                OutlinedButton(onClick = onConfirm) {
                    Text(getString(R.string.alert_confirm), color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissRequest) {
                    Text(getString(R.string.alert_cancel), color = Color.White)
                }
            },
            title = {
                Text(
                    getString(R.string.alert_delete),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    getString(R.string.alert_remove_mes),
                    color = Color.White
                )
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.DarkGray
        )
    }
}