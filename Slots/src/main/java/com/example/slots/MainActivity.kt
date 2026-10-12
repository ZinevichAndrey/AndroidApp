package com.example.slots

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.example.slots.ui.theme.SlotsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

        }
    }

    @Composable
    private fun drawBackground() {
        Box(modifier = Modifier
            .fillMaxSize()
            .paint(painter = painterResource(R.drawable.ic_launcher_background),
                contentScale = ContentScale.Crop))
    }

    @Composable
    private fun menuButton(
        image: Int,
        width: Dp,
        aspectRatio: Float,
        onClick:() -> Unit
    ) {
        Image(painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier
                .width(width)
                .aspectRatio(aspectRatio)
                .clickable(interactionSource = remember { MutableInteractionSource() },
                    indication = null) {
                    onClick()
                },
            contentScale = ContentScale.Fit)
    }







}

