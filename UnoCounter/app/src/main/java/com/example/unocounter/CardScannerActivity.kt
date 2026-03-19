package com.example.unocounter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class CardScannerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FastManualScanner(
                onFinish = { score -> finishWithScore(score) },
                onCancel = { finish() }
            )
        }
    }

    private fun finishWithScore(score: Int) {
        val intent = Intent()
        intent.putExtra("score", score)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}

@Composable
fun FastManualScanner(
    onFinish: (Int) -> Unit,
    onCancel: () -> Unit
) {
    var scannedCards by remember { mutableStateOf(listOf<DetectedCard>()) }
    var totalScore by remember { mutableIntStateOf(0) }

    fun addCard(card: UnoCard) {
        val score = UnoCalculator.score(card)
        val detected = DetectedCard(card.type, card.number, score)
        scannedCards = scannedCards + detected
        totalScore += score
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Заголовок
            Text(
                "Подсчёт карт UNO",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Общий счёт
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16213e)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ОБЩИЙ СЧЁТ", color = Color.Gray, fontSize = 14.sp)
                    Text(
                        "$totalScore",
                        color = Color.White,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (scannedCards.isNotEmpty()) {
                        Text(
                            "${scannedCards.size} карт",
                            color = Color(0xFF4CAF50),
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Добавленные карты
            if (scannedCards.isNotEmpty()) {
                Text("Добавленные карты:", color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(scannedCards) { card ->
                        CardChipWithDelete(card) {
                            scannedCards = scannedCards - card
                            totalScore -= card.score
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // === БЫСТРЫЙ ВВОД КАРТ ===

            // Числа 0-9
            Text("Числа (0-9 очков):", color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (0..4).forEach { num ->
                    QuickCardButton("$num", Color(0xFF2196F3)) {
                        addCard(UnoCard(CardType.NUMBER, num))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (5..9).forEach { num ->
                    QuickCardButton("$num", Color(0xFF2196F3)) {
                        addCard(UnoCard(CardType.NUMBER, num))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Специальные карты (20 очков)
            Text("Специальные (20 очков):", color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickCardButton("Skip", Color(0xFFFF9800)) {
                    addCard(UnoCard(CardType.SKIP))
                }
                QuickCardButton("Rev", Color(0xFFFF9800)) {
                    addCard(UnoCard(CardType.REVERSE))
                }
                QuickCardButton("+2", Color(0xFFFF9800)) {
                    addCard(UnoCard(CardType.DRAW2))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Wild карты (50 очков)
            Text("Wild карты (50 очков):", color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickCardButton("Wild", Color(0xFF9C27B0), wide = true) {
                    addCard(UnoCard(CardType.WILD))
                }
                QuickCardButton("+4", Color(0xFF9C27B0), wide = true) {
                    addCard(UnoCard(CardType.WILD_DRAW4))
                }
            }

            Spacer(Modifier.weight(1f))

            // Кнопки действий
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Отмена")
                }

                OutlinedButton(
                    onClick = {
                        scannedCards = emptyList()
                        totalScore = 0
                    },
                    modifier = Modifier.weight(1f),
                    enabled = scannedCards.isNotEmpty(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Yellow)
                ) {
                    Text("Очистить")
                }

                Button(
                    onClick = { onFinish(totalScore) },
                    modifier = Modifier.weight(1.5f),
                    enabled = totalScore > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Готово ✓", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QuickCardButton(
    text: String,
    color: Color,
    wide: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .then(if (wide) Modifier.width(120.dp) else Modifier.size(56.dp))
            .height(56.dp)
            .background(color, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = if (wide) 18.sp else 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CardChipWithDelete(card: DetectedCard, onDelete: () -> Unit) {
    val bgColor = when (card.type) {
        CardType.NUMBER -> Color(0xFF2196F3)
        CardType.SKIP, CardType.REVERSE, CardType.DRAW2 -> Color(0xFFFF9800)
        CardType.WILD, CardType.WILD_DRAW4 -> Color(0xFF9C27B0)
    }

    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${cardToString(card)} +${card.score}",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Text("✕", color = Color.White, fontSize = 12.sp)
        }
    }
}

fun cardToString(card: DetectedCard): String {
    return when (card.type) {
        CardType.NUMBER -> "${card.number}"
        CardType.SKIP -> "Skip"
        CardType.REVERSE -> "Rev"
        CardType.DRAW2 -> "+2"
        CardType.WILD -> "Wild"
        CardType.WILD_DRAW4 -> "+4"
    }
}
