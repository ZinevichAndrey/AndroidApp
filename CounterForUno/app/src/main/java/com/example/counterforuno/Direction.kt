package com.example.counterforuno


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



class Direction : ComponentActivity() {
    private var mediaPlayerPo: GameVoiceover? = GameVoiceover()
    private var mediaPlayerPro: GameVoiceover? = GameVoiceover()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            if (mediaPlayerPo != null && mediaPlayerPro != null) {
                mediaPlayerPo!!.init(this@Direction, R.raw.po_dir)
                mediaPlayerPro!!.init(this@Direction, R.raw.pro_dir)
            }
            else {
                throw NullPointerException("object GameVoicecover is null")
            }
            Settings.hideActionBar(this)
            createDirection(
                imageClockwise = R.drawable.direction_clockwise_draw,
                imageCounterclockwise = R.drawable.direction_counterclockwise_draw
            )
        }
    }


    @Composable
    private fun createDirection(imageClockwise: Int, imageCounterclockwise: Int) {
        val isClock = remember {
            mutableStateOf(true)
        }
        Column(
            Modifier
                .fillMaxSize()
                .paint(
                    painterResource(id = R.drawable.game_bg_draw),
                    contentScale = ContentScale.FillBounds
                )
        ) {
            Box(
                Modifier
                    .fillMaxSize(0.9f)
                    .padding(start = 250.dp)
            ) {

                if (isClock.value) {
                    if (mediaPlayerPro!!.getMediaPlayer().isPlaying) {
                        mediaPlayerPro!!.restartAudioTrack()
                    }
                    mediaPlayerPo!!.startPlayer()
                    Image(painter = painterResource(id = imageClockwise), contentDescription = null,
                        Modifier
                            .rotate(90f)
                            .clickable {
                                isClock.value = !isClock.value
                            }, contentScale = ContentScale.FillBounds
                    )
                } else {
                    if (mediaPlayerPo!!.getMediaPlayer().isPlaying) {
                        mediaPlayerPo!!.restartAudioTrack()
                    }
                    mediaPlayerPro!!.startPlayer()
                    Image(painter = painterResource(id = imageCounterclockwise),
                        contentDescription = null,
                        modifier = Modifier
                            .rotate(90f)
                            .clickable {
                                isClock.value = !isClock.value
                            },
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
            Button(
                onClick = {
                    finish()
                },
                Modifier
                    .height(100.dp)
                    .width(200.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Back to the game", fontSize = 25.sp)

            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMp()
    }

    private fun releaseMp() {
        if (this.mediaPlayerPo != null && this.mediaPlayerPro != null) {
            this.mediaPlayerPo!!.releasePlayer()
            this.mediaPlayerPro!!.releasePlayer()
            this.mediaPlayerPo = null
            this.mediaPlayerPro = null
        }
    }



}