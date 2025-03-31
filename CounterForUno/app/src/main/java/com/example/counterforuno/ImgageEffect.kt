package com.example.counterforuno


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput



enum class ImgageEffect { Pressed, Normal }


fun Modifier.pressClickEffect() = composed {
    var imageState by remember { mutableStateOf(ImgageEffect.Normal) }
    val animate by animateFloatAsState(if (imageState == ImgageEffect.Pressed) 0f else -20f)

    this
        .graphicsLayer {
            translationY = animate
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {}
        )
        .pointerInput(imageState) {
            awaitPointerEventScope {
                imageState = if (imageState == ImgageEffect.Pressed) {
                    waitForUpOrCancellation()
                    ImgageEffect.Normal
                } else {
                    awaitFirstDown(false)
                    ImgageEffect.Pressed
                }
            }
        }
}