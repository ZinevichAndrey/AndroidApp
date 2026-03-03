package com.example.unocounter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import java.io.File

class TotalScore : ComponentActivity() {
    private val FILE_NAME = "players.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Settings.hideActionBar(this)
            TotalScoreScreen()
        }
    }

    @Composable
    private fun TotalScoreScreen() {
        val context = LocalContext.current
        val players = remember {
            mutableStateListOf<PlayerStats>().apply {
                addAll(
                    WriterAndReader.loadPlayers(context, FILE_NAME)
                        .sortedByDescending { it.numberOfVictories }
                )
            }
        }
        var showDialog by remember { mutableStateOf(false) }
        Column(
            Modifier
                .fillMaxSize()
                .paint(
                    painterResource(R.drawable.game_bg),
                    contentScale = ContentScale.Crop
                )
                .padding(10.dp)
        ) {
//            Text(
//                text = stringResource(R.string.total_score_title),
//                fontSize = 40.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.Yellow,
//                modifier = Modifier.fillMaxWidth(),
//                textAlign = TextAlign.Center
//            )
            Image(painterResource(R.drawable.winners_inscription), contentDescription = null,
                Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(20.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                itemsIndexed(
                    items = players,
                    key = { _, player -> player.id }
                ) { index, player ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(index * 120L)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(
                            animationSpec = tween(400)
                        ) + slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(400)
                        )
                    ) {
                        PlayerScoreRow(player, index)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .height(80.dp)
                        .width(174.dp)
                        .pressClickEffect()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.button_cleaner),
                        contentDescription = null
                    )
                }
                Box(
                    modifier = Modifier
                        .height(80.dp)
                        .width(174.dp)
                        .pressClickEffect()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            context.startActivity(Intent(context, GameMenu::class.java))
                            (context as ComponentActivity).finish()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.button_back),
                        contentDescription = null
                    )
                }
            }
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            players.clear()
                            val file = File(context.filesDir, FILE_NAME)
                            if (file.exists()){
                                file.delete()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.alert_clear))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(R.string.alert_cancel))
                    }
                },
                title = { Text(stringResource(R.string.alert_confirm)) },
                text = { Text(stringResource(R.string.alert_mes)) }
            )
        }
    }

    @Composable
    private fun PlayerScoreRow(player: PlayerStats, index: Int) {
        val medalColor = when (index) {
            0 -> Color(0xFFFFD700)
            1 -> Color(0xFFC0C0C0)
            2 -> Color(0xFFCD7F32)
            else -> Color.White
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painter = when (val avatar = player.avatar) {
                is AvatarSource.Resource -> painterResource(avatar.resId)
                is AvatarSource.AvatarUri -> rememberAsyncImagePainter(Uri.parse(avatar.uri))
            }
            Box(
                Modifier
                    .size(80.dp)
                    .padding(end = 10.dp)
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                Image(
                    painter = painterResource(R.drawable.icon_back_ava),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column {
                Text(
                    text = player.name,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = medalColor
                )
                Text(
                    text = "${player.numberOfVictories} ${stringResource(R.string.wins)}",
                    fontSize = 20.sp,
                    color = medalColor
                )
            }
        }
        Spacer(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth()
                .background(Color.Black)
        )
    }
}