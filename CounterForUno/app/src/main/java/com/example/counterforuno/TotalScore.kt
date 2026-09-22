package com.example.counterforuno

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.rememberAsyncImagePainter
import java.io.File

class TotalScore : ComponentActivity() {
    private val winners = mutableStateListOf<Winner>()
    private val colorMetall: Color by lazy {
        Color(ContextCompat.getColor(this, R.color.metallic))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this
        val winnerFromIntent = intent.getParcelableExtra<Winner>(Settings.getKeyWinner())
        val loadedWinners = WriterAndReader.loadFromFile(context, Settings.getWinnersFile()){parcel ->
            Winner.createFromParcel(parcel)
        }.toMutableList()
        winnerFromIntent?.let { newWinner ->
            var found = false
            for (existingWinner in loadedWinners) {
                if (existingWinner.getId() == newWinner.getId()) {
                    existingWinner.victories++
                    found = true
                    break
                }
            }
            if (!found) {
                loadedWinners.add(newWinner)
            }
            saveWinners(context, loadedWinners)
        }
        winners.clear()
        winners.addAll(loadedWinners)
        winners.sortByDescending { it.victories }
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
                    painterResource(id = R.drawable.bg_winners),
                    contentScale = ContentScale.Crop
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.winners_760x140),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentScale = ContentScale.FillWidth
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(
                    items = winners,
                    key = { it.getId() }
                ) { winner ->
                    val index = winners.indexOf(winner)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 20.dp,
                                start = 20.dp,
                                end = 20.dp
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .padding(end = 20.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 20.sp,
                                color = Color.Gray
                            )
                        }
                        Row(
                            modifier = Modifier
                                .weight(2f)
                                .height(60.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            shadowText(text = winner.name)
                        }
                        Row(
                            modifier = Modifier
                                .weight(2f)
                                .height(60.dp)
                                .padding(start = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val painter = when (val avatar = winner.avatar) {
                                is AvatarSource.Resource ->
                                    painterResource(avatar.resId)

                                is AvatarSource.AvatarUri ->
                                    rememberAsyncImagePainter(
                                        Uri.parse(avatar.uri)
                                    )
                            }
                            Box(
                                modifier = Modifier.size(80.dp)
                            ) {
                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp)
                                )
                                Image(
                                    painter = painterResource(
                                        R.drawable.icon_back_ava
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Text(
                                text = winner.victories.toString(),
                                color = colorMetall,
                                fontSize = 30.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .fillMaxWidth()
                            .background(Color.Black)
                    )
                }
            }
            CreateButtons(context)
        }
    }

    @Composable
    private fun shadowText(text: String) {
        val offset = Offset(5.0f, 10.0f)
        Text(
            text = text,
            style = TextStyle(
                fontSize = 40.sp,
                color = colorMetall,
                shadow = Shadow(color = Color.Black, offset = offset, blurRadius = 1.0f)
            )
        )
    }

    @Composable
    private fun CreateButtons(context: Context) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 10.dp,
                    bottom = 10.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConfirmationDeletion(context)
            Box(
                modifier = Modifier
                    .pressClickEffect()
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = { finish() }
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.button_back),
                    contentDescription = null,
                    modifier = Modifier
                        .height(80.dp)
                        .width(174.dp)
                )
            }
        }
    }

    @Composable
    private fun ConfirmationDeletion(context: Context) {
        var showDialog by remember { mutableStateOf(false) }
        val buttonColor = ButtonDefaults.buttonColors(
            containerColor = Color.DarkGray,
            contentColor = Color.LightGray
        )
        Box(
            modifier = Modifier
                .pressClickEffect()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showDialog = true }
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.button_cleaner),
                contentDescription = null,
                modifier = Modifier
                    .height(80.dp)
                    .width(174.dp)
            )
        }

        if (showDialog) {
            Dialog(
                onDismissRequest = { showDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = true)
            ) {
                val view = LocalView.current
                LaunchedEffect(Unit) {
                    val window = (view.parent as? DialogWindowProvider)?.window
                    window?.let {
                        WindowCompat.setDecorFitsSystemWindows(it, false)
                        WindowInsetsControllerCompat(it, it.decorView).apply {
                            hide(WindowInsetsCompat.Type.navigationBars())
                            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        }
                    }
                }
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = Color.DarkGray,
                    modifier = Modifier
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.alert_confirm),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.LightGray,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.alert_mes),
                            fontSize = 20.sp,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { showDialog = false },
                                colors = buttonColor,
                                border = BorderStroke(2.dp, Color.LightGray),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(text = stringResource(R.string.alert_cancel))
                            }

                            Button(
                                onClick = {
                                    showDialog = false
                                    winners.clear()
                                    val file = File(context.filesDir, Settings.getWinnersFile())
                                    if (file.exists()) {
                                        file.delete()
                                    }
                                },
                                colors = buttonColor,
                                border = BorderStroke(2.dp, Color.LightGray)
                            ) {
                                Text(text = stringResource(R.string.alert_clear))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveWinners(context: Context, winnersList: MutableList<Winner>) {
        val file = File(context.filesDir, Settings.getWinnersFile())
        if (file.exists()) {
            file.delete()
        }

        winnersList.forEach { winner ->
            WriterAndReader.savePlayersToFile(context, winner, Settings.getWinnersFile())
        }
    }
}