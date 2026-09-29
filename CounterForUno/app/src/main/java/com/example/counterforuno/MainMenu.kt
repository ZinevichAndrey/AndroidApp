package com.example.counterforuno

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.core.content.ContextCompat


class MainMenu : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, R.string.camera_not_permission, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        setContent {
            Settings.hideActionBar(this)
            CreateMainMenu()
        }
    }

    @Composable
    private fun DrawBackground() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(R.drawable.main_bg),
                    contentScale = ContentScale.Crop
                )
        )
    }


    @SuppressLint("UnusedBoxWithConstraintsScope")
    @Composable
    private fun CreateMainMenu() {
        val context = LocalContext.current
        val modeOptions = listOf(
            ModeOption(
                "points",
                stringResource(R.string.mode_points)
            ),
            ModeOption(
                "wins",
                stringResource(R.string.mode_wins)
            )
        )
        var selectedOption by remember {
            mutableStateOf(modeOptions[0])
        }
        var expanded by remember {
            mutableStateOf(false)
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            val screenWidth = maxWidth
            val menuWidth = (screenWidth * 0.45f)
                .coerceIn(180.dp, 260.dp)
            val startWidth = (screenWidth * 0.55f)
                .coerceIn(190.dp, 300.dp)
            val bottomButtonWidth = (screenWidth * 0.32f)
                .coerceIn(110.dp, 170.dp)
            DrawBackground()
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp)
                    .width(menuWidth)
                    .zIndex(10f)
            ) {
                ModeSelector(
                    selected = selectedOption.displayName,
                    expanded = expanded,
                    onClick = {
                        expanded = !expanded
                    }
                )
                androidx.compose.animation.AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(
                            animationSpec = tween(200)
                        )
                                +
                                expandVertically(
                                    animationSpec = tween(250)
                                )
                                +
                                slideInVertically(
                                    animationSpec = tween(250),
                                    initialOffsetY = { -20 }
                                ),
                    exit = fadeOut(
                            animationSpec = tween(150)
                        )
                                +
                                shrinkVertically(
                                    animationSpec = tween(200)
                                )
                                +
                                slideOutVertically(
                                    animationSpec = tween(200),
                                    targetOffsetY = { -20 }
                                ),
                    modifier = Modifier
                        .padding(top = 50.dp)
                        .zIndex(10f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        modeOptions.forEach { option ->
                            MenuButton(
                                text = option.displayName,
                                onClick = {
                                    selectedOption = option
                                    Settings.setMode(option.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                gameButton(
                    image = R.drawable.button_start,
                    width = startWidth,
                    aspectRatio = 2.22f,
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                GameMenu::class.java
                            )
                        )
                    }
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 30.dp
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                gameButton(
                    image = R.drawable.button_score,
                    width = bottomButtonWidth,
                    aspectRatio = 2.22f,
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                TotalScore::class.java
                            )
                        )
                    }
                )
                gameButton(
                    image = R.drawable.button_exit,
                    width = bottomButtonWidth,
                    aspectRatio = 2.22f,
                    onClick = {
                        finishAndRemoveTask()
                        System.exit(0)
                    }
                )
            }
        }
    }

    @Composable
    private fun ModeSelector(
        selected: String,
        expanded: Boolean,
        onClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .paint(
                    painter = painterResource(R.drawable.game_bg),
                    contentScale = ContentScale.FillBounds
                )
                .clickable {
                    onClick()
                }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selected,
                    color = Color.White,
                    fontSize = 15.sp
                )
                Icon(
                    imageVector = if (expanded)
                        Icons.Default.KeyboardArrowUp
                    else
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }


    @Composable
    private fun MenuButton(
        text: String,
        onClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 8.dp)
                .paint(
                    painter = painterResource(
                        R.drawable.game_bg
                    ),
                    contentScale = ContentScale.FillBounds
                )
                .clickable {
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 15.sp
            )
        }
    }


    @Composable
    private fun gameButton(
        image: Int,
        width: Dp,
        aspectRatio: Float,
        onClick: () -> Unit
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier
                .width(width)
                .pressClickEffect()
                .aspectRatio(aspectRatio)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onClick()
                },
            contentScale = ContentScale.Fit
        )
    }


}