package com.example.unocounter

data class ScanState(
    val isScanning: Boolean = false,
    val isPaused: Boolean = false,           // ДОБАВЛЕНО: пауза после обнаружения
    val cardsFound: Int = 0,
    val currentScore: Int = 0,
    val statusMessage: String = "Наведите камеру на карты UNO",
    val detectedCards: List<DetectedCard> = emptyList()
)

data class DetectedCard(
    val type: CardType,
    val number: Int = 0,
    val score: Int
)
