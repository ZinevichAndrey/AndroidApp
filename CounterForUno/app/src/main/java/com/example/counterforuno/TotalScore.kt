package com.example.counterforuno


import android.os.Bundle
import android.os.Environment
import android.os.Parcel
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


class TotalScore : ComponentActivity() {
    private val winners = ArrayList<Player>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Settings.hideActionBar(this)
            val getIntentValue = intent.getParcelableExtra(Settings.getKeyWinner()) as? Player
            if (getIntentValue != null) {
                winners.add(getIntentValue)
            }

            

//            winners.add(Player(name = "Andrey", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Vas9", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Dima", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Kristina", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Andrey", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Vas9", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Dima", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Kristina", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Kat9", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Andrey", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Vas9", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Dima", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Kristina", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Kat9", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Andrey", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Vas9", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Dima", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Kristina", avatar = R.drawable.main_fon))
//            winners.add(Player(name = "Kat9", avatar = R.drawable.main_fon))
            createTable(list = winners)
        }
    }



    @Composable
    private fun createTable(list: MutableList<Player>) {
        val winnerList = remember {
            mutableStateListOf<Player>()
        }
        winnerList.addAll(list)
        Column(
            Modifier
                .fillMaxSize()
                .paint(
                    painterResource(id = R.drawable.bg_winners),
                    contentScale = ContentScale.Crop
                )
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.winners_760x140),
                    contentDescription = null
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)

            ) {
                items(winnerList) { winner ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, start = 20.dp, end = 20.dp)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth(0.5f)
                                .height(60.dp)
                                .padding(end = 50.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            Alignment.CenterVertically
                        ) {
                            Text(
                                "${winnerList.indexOf(winner) + 1}",
                                fontSize = 20.sp,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            shadowText(text = winner.name)
                        }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .padding(start = 50.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = winner.avatar),
                                contentDescription = null,
                                Modifier
                                    .size(60.dp)
                                    .padding(start = 10.dp)
                            )
                            Text(
                                text = winner.numberOfVictories.toString(),
                                color = Color.Cyan,
                                fontSize = 30.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(20.dp))
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .fillMaxWidth()
                            .background(Color.Black)
                    )
                }
            }
            createButtons(winnerList)
        }
    }

    @Composable
    private fun createButtons(list: MutableList<Player>) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 40.dp, end = 40.dp, top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            Alignment.CenterVertically
        ) {
                confirmationDeletion(list)
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
                    painter = painterResource(id = R.drawable.button_back),
                    contentDescription = null,
                    Modifier
                        .height(80.dp)
                        .width(174.dp)
                )
            }
        }
    }

    @Composable
    private fun shadowText(text: String) {
        val offset = Offset(5.0f, 10.0f)
        Text(
            text = text,
            style = TextStyle(
                fontSize = 30.sp,
                color = Color.Red,
                shadow = Shadow(color = Color.Yellow, offset, blurRadius = 3.0f)
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    @Composable
    private fun confirmationDeletion(list: MutableList<Player>) {
        val openDialog = remember {
            mutableStateOf(false)
        }
        val buttonColor = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.LightGray)
        Box(
            Modifier
                .pressClickEffect()
                .clickable(interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null, onClick = {
                    openDialog.value = true

                })
        ) {
            Image(
                painter = painterResource(id = R.drawable.button_cleaner),
                contentDescription = null,
                Modifier
                    .height(80.dp)
                    .width(174.dp)
            )
        }

        if (openDialog.value) {
            AlertDialog(onDismissRequest = {openDialog.value = false}, title = { Text(getString(R.string.alert_accept_mes))}, text = { Text(
                text = getString(R.string.alert_mes), fontSize = 20.sp
            )}, confirmButton = {
                Button(onClick = {
                    openDialog.value = false
                    list.clear()
                    winners.clear()
                }, colors = buttonColor, border = BorderStroke(1.dp, Color.LightGray)) {
                    Text(text = getString(R.string.alert_clear))
                }
            },
                dismissButton = {
                    Button(onClick = {openDialog.value = false}, colors = buttonColor, border = BorderStroke(1.dp, Color.LightGray)) {
                        Text(text = getString(R.string.alert_cancel))
                    }
                },
                containerColor = Color.DarkGray,
                titleContentColor = Color.LightGray,
                textContentColor = Color.LightGray,
                iconContentColor = Color.LightGray
                )
        }

    }



    fun Player.toByteArray(): ByteArray {
        val parcel = Parcel.obtain()
        this.writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        parcel.recycle()
        return bytes
    }

    fun saveWinnerToFile(winner: Player, filePath: String) {
        val byteArray = winner.toByteArray()
        val file = File(filePath)
        try {
            BufferedOutputStream(FileOutputStream(file)).use { outputStream -> outputStream.write(byteArray)
            }
            println("save")
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }




    fun loadWinnersFromFile(filePath: String): Player? {
            val file = File(filePath)
            return try {
                val byteArray = file.readBytes()
                val parcel = Parcel.obtain()
                parcel.unmarshall(byteArray, 0, byteArray.size)
                parcel.setDataPosition(0)
                val myData = Player.createFromParcel(parcel)
                parcel.recycle()
                myData
            } catch (e: Exception) {
               e.printStackTrace()
                null
            }

    }






}