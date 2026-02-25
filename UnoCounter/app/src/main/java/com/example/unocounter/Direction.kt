package com.example.unocounter


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


class Direction : ComponentActivity() {
    private var mediaPlayerPo: GameVoiceover? = GameVoiceover()
    private var mediaPlayerPro: GameVoiceover? = GameVoiceover()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            mediaPlayerPo!!.init(this@Direction, R.raw.po_dir)
            mediaPlayerPro!!.init(this@Direction, R.raw.pro_dir)
            Settings.hideActionBar(this)
            createDirection(
                imageClockwise = R.drawable.direction_clockwise,
                imageCounterclockwise = R.drawable.direction_counterclockwise
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
                    painterResource(id = R.drawable.game_bg),
                    contentScale = ContentScale.Crop
                )
        ) {
            Box(
                Modifier
                    .padding(top = 130.dp)
                    .fillMaxSize(0.8f)
                    .align(Alignment.CenterHorizontally)
            ) {

                if (isClock.value) {
                    if (mediaPlayerPro!!.getMediaPlayer().isPlaying) {
                        mediaPlayerPro!!.restartAudioTrack()
                    }
                    mediaPlayerPo!!.startPlayer()

                } else {
                    if (mediaPlayerPo!!.getMediaPlayer().isPlaying) {
                        mediaPlayerPo!!.restartAudioTrack()
                    }
                    mediaPlayerPro!!.startPlayer()
                }

                Box(
                    Modifier.Companion
                        .shakeClickEffect()
                        .clickable(interactionSource = remember {
                            MutableInteractionSource()
                        }, indication = null, onClick = {
                            isClock.value = !isClock.value
                        })
                ) {
                    if (isClock.value) {
                        Image(
                            painter = painterResource(id = imageClockwise),
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = imageCounterclockwise),
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .pressClickEffect()
                    .clickable(interactionSource = remember {
                        MutableInteractionSource()
                    }, null, onClick = {
                        finish()
                    })
            ) {
                Image(
                    painter = painterResource(id = R.drawable.button_back),
                    contentDescription = "btn_back",
                    Modifier
                        .width(174.dp)
                        .height(80.dp), contentScale = ContentScale.Crop
                )
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMp()
    }

    private fun releaseMp() {
        this.mediaPlayerPo!!.releasePlayer()
        this.mediaPlayerPro!!.releasePlayer()
        this.mediaPlayerPo = null
        this.mediaPlayerPro = null
    }


}


