package com.example.unocounter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.unocounter.Settings.centerCropToSquare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PlayerMenu : ComponentActivity() {

    // Изменено: теперь список имён ресурсов (String)
    private val avatarList = listOf(
        "dog_ava", "rat_ava", "spider_ava",
        "toad_ava", "cat_ava", "bird_ava"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Settings.hideActionBar(this)
            CreatePlayerScreen { playerStats -> onPlayerCreated(playerStats) }
        }
    }

    @Composable
    private fun CreatePlayerScreen(onCreate: (PlayerStats) -> Unit) {
        val context = LocalContext.current
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        var name by remember { mutableStateOf("") }
        // Изменено: начальное значение с именем ресурса
        var avatarSource by remember { mutableStateOf<AvatarSource>(AvatarSource.Resource("main_fon")) }
        val imageUri = remember { mutableStateOf<Uri?>(null) }
        val widthDp = Settings.getConf().screenWidthDp / 3
        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                val uri = imageUri.value ?: return@rememberLauncherForActivityResult
                scope.launch(Dispatchers.IO) {
                    try {
                        val options = BitmapFactory.Options().apply { inSampleSize = 2 }
                        val originalBitmap = context.contentResolver.openInputStream(uri)?.use {
                            BitmapFactory.decodeStream(it, null, options)
                        } ?: return@launch
                        val targetSizePx = with(density) { widthDp.dp.toPx().toInt() }
                        val squareBitmap = originalBitmap.centerCropToSquare(targetSizePx)
                        val tempFile = File(context.cacheDir, "avatar_temp.jpg")
                        tempFile.outputStream().use { out ->
                            squareBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        }
                        val croppedUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            tempFile
                        )
                        withContext(Dispatchers.Main) {
                            avatarSource = AvatarSource.AvatarUri(croppedUri.toString())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        fun openCameraAndCapture() {
            val photoFile = File.createTempFile("avatar_temp", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
            imageUri.value = uri
            cameraLauncher.launch(uri)
        }

        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .paint(painterResource(R.drawable.game_bg), contentScale = ContentScale.Crop)
            ) {
                Text(
                    text = context.getString(R.string.createPlayer),
                    fontSize = 40.sp,
                    color = Color.Yellow,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 25.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        context.getString(R.string.plMenu_name),
                        textAlign = TextAlign.Center,
                        fontSize = 25.sp,
                        color = colorResource(R.color.metallic)
                    )
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        textStyle = TextStyle(fontSize = 30.sp),
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    )
                }
                val width = Settings.getConf().screenWidthDp / 3
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp), Arrangement.Center
                ) {
                    Box(Modifier.size(width.dp), contentAlignment = Alignment.Center) {
                        // Используем общую функцию
                        val painter = rememberAvatarPainter(avatarSource)
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(0.9f),
                            contentScale = ContentScale.Crop
                        )
                        Image(
                            painter = painterResource(R.drawable.icon_back_ava),
                            contentDescription = null,
                            modifier = Modifier.size(width.dp)
                        )
                    }
                }
                ShowAvatarList(avatarList) { selectedResName ->
                    avatarSource = AvatarSource.Resource(selectedResName)
                }
                Image(
                    painter = painterResource(R.drawable.photo_ava),
                    contentDescription = "Open camera",
                    modifier = Modifier
                        .size(width.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(5.dp)
                        .clickable { openCameraAndCapture() }
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Image(
                    painter = painterResource(R.drawable.button_cancel),
                    contentDescription = null,
                    modifier = Modifier
                        .height(80.dp)
                        .width(174.dp)
                        .clickable { (context as Activity).finish() }
                )
                Image(
                    painter = painterResource(R.drawable.button_create),
                    contentDescription = null,
                    modifier = Modifier
                        .height(80.dp)
                        .width(174.dp)
                        .clickable {
                            if (name.isNotBlank() &&
                                (avatarSource !is AvatarSource.Resource ||
                                        (avatarSource as AvatarSource.Resource).resName != "main_fon")
                            ) {
                                val playerStats = PlayerStats(
                                    name = name,
                                    avatar = avatarSource,
                                    numberOfVictories = 0
                                )
                                onCreate(playerStats)
                            } else {
                                Toast.makeText(
                                    context,
                                    getString(R.string.emptyAvatar),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                )
            }
        }
    }

    private fun onPlayerCreated(playerStats: PlayerStats) {
        val resultIntent = Intent()
        resultIntent.putExtra(Settings.getCreatePlayerKey(), playerStats)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    @Composable
    private fun ShowAvatarList(list: List<String>, onAvatarSelected: (String) -> Unit) {
        val rows = list.chunked(3)
        val width = Settings.getConf().screenWidthDp / 3
        Column {
            rows.forEach { rowItems ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    rowItems.forEach { item ->
                        Box(Modifier.size(width.dp)) {
                            Image(
                                painter = painterResource(AvatarResources.getResId(item)),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(5.dp)
                                    .clickable { onAvatarSelected(item) },
                                contentScale = ContentScale.Crop
                            )
                            Image(
                                painter = painterResource(R.drawable.icon_back_ava),
                                contentDescription = null,
                                Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
