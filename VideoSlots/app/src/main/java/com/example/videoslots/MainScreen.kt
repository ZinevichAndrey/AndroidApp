package com.example.videoslots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    onPlayWheelOfFortune: () -> Unit,
    onOpenGameView: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center) {
        Text("Главный экран")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onPlayWheelOfFortune) {
            Text("Колесо фортуны")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onOpenGameView) {
            Text("Игра")
        }
    }
}