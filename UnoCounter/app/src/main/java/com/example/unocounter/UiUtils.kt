package com.example.unocounter

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@Composable
fun rememberUriPainter(uri: Uri, fallbackRes: Int = R.drawable.main_fon): Painter {
    val context = LocalContext.current
    val bitmap = remember(uri) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            }
        }.getOrNull()
    }
    return bitmap?.let { BitmapPainter(it.asImageBitmap()) }
        ?: painterResource(fallbackRes)
}

@Composable
fun rememberAvatarPainter(avatar: AvatarSource): Painter {
    return when (avatar) {
        is AvatarSource.Resource -> painterResource(AvatarResources.getResId(avatar.resName))
        is AvatarSource.AvatarUri -> rememberUriPainter(Uri.parse(avatar.uri))
    }
}
