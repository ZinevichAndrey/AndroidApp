package com.example.counterforuno


import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
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



enum class ButtonEffect { Pressed, Normal }

fun Modifier.pressClickEffect() = composed {
    var imageState by remember { mutableStateOf(ButtonEffect.Normal) }
    val animate by animateFloatAsState(if (imageState == ButtonEffect.Normal) 0f else 20f)
    this.graphicsLayer {
            translationY = animate
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {}
        )
        .pointerInput(imageState) {
            awaitPointerEventScope {
                imageState = if (imageState == ButtonEffect.Pressed) {
                    waitForUpOrCancellation()
                    ButtonEffect.Normal
                }
                else {
                    awaitFirstDown(false)
                    ButtonEffect.Pressed
                }
            }
        }
}

fun Modifier.shakeClickEffect() = composed {
    var isPressed by remember { mutableStateOf(false) }
    val anim by animateFloatAsState(
        targetValue = if (isPressed) -50f else 0f,
        animationSpec = repeatable(
            iterations = 2,
            animation = tween(durationMillis = 150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    this.graphicsLayer {
            translationX = anim
        }
        .pointerInput(isPressed) {
            awaitPointerEventScope {
                isPressed = if (isPressed) {
                    waitForUpOrCancellation()
                    false
                }
                else {
                    awaitFirstDown(false)
                    true
                }
            }
        }
}

@SuppressLint("ClickableViewAccessibility")
fun setupButtonAnimation(button: View) {
    button.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.animate()
                    .scaleX(0.92f)
                    .scaleY(0.92f)
                    .setDuration(80)
                    .start()
                false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .start()
                false
            }
            else -> false
        }
    }
}