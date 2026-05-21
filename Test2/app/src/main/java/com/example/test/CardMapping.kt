package com.example.test

object CardMapping {
    // Полная мапа ID модели → UnoCard (заполните вашими 108 маппингами)
    private val cards = mapOf(
        // Примеры (ЗАМЕНИТЕ на ваши реальные ID от модели)
        0 to UnoCard(CardColor.GREEN, CardType.ZERO),
        1 to UnoCard(CardColor.GREEN, CardType.ONE),
        2 to UnoCard(CardColor.GREEN, CardType.ONE),
        3 to UnoCard(CardColor.GREEN, CardType.TWO),
        4 to UnoCard(CardColor.GREEN, CardType.TWO),
        5 to UnoCard(CardColor.GREEN, CardType.THREE),
        6 to UnoCard(CardColor.GREEN, CardType.THREE),
        7 to UnoCard(CardColor.GREEN, CardType.FOUR),
        8 to UnoCard(CardColor.GREEN, CardType.FOUR),
        9 to UnoCard(CardColor.GREEN, CardType.FIVE),
        10 to UnoCard(CardColor.GREEN, CardType.FIVE),
        11 to UnoCard(CardColor.GREEN, CardType.SIX),
        12 to UnoCard(CardColor.GREEN, CardType.SIX),
        13 to UnoCard(CardColor.GREEN, CardType.SEVEN),
        14 to UnoCard(CardColor.GREEN, CardType.SEVEN),
        15 to UnoCard(CardColor.GREEN, CardType.EIGHT),
        16 to UnoCard(CardColor.GREEN, CardType.EIGHT),
        17 to UnoCard(CardColor.GREEN, CardType.NINE),
        18 to UnoCard(CardColor.GREEN, CardType.NINE),
        19 to UnoCard(CardColor.GREEN, CardType.REVERSE),
        20 to UnoCard(CardColor.GREEN, CardType.REVERSE),
        21 to UnoCard(CardColor.GREEN, CardType.SKIP),
        22 to UnoCard(CardColor.GREEN, CardType.SKIP),
        23 to UnoCard(CardColor.GREEN, CardType.PLUS_TWO),
        24 to UnoCard(CardColor.GREEN, CardType.PLUS_TWO),

        // Красные карты (25-49)
        25 to UnoCard(CardColor.RED, CardType.ZERO),
        26 to UnoCard(CardColor.RED, CardType.ONE),
        27 to UnoCard(CardColor.RED, CardType.ONE),
        28 to UnoCard(CardColor.RED, CardType.TWO),
        29 to UnoCard(CardColor.RED, CardType.TWO),
        30 to UnoCard(CardColor.RED, CardType.THREE),
        31 to UnoCard(CardColor.RED, CardType.THREE),
        32 to UnoCard(CardColor.RED, CardType.FOUR),
        33 to UnoCard(CardColor.RED, CardType.FOUR),
        34 to UnoCard(CardColor.RED, CardType.FIVE),
        35 to UnoCard(CardColor.RED, CardType.FIVE),
        36 to UnoCard(CardColor.RED, CardType.SIX),
        37 to UnoCard(CardColor.RED, CardType.SIX),
        38 to UnoCard(CardColor.RED, CardType.SEVEN),
        39 to UnoCard(CardColor.RED, CardType.SEVEN),
        40 to UnoCard(CardColor.RED, CardType.EIGHT),
        41 to UnoCard(CardColor.RED, CardType.EIGHT),
        42 to UnoCard(CardColor.RED, CardType.NINE),
        43 to UnoCard(CardColor.RED, CardType.NINE),
        44 to UnoCard(CardColor.RED, CardType.REVERSE),
        45 to UnoCard(CardColor.RED, CardType.REVERSE),
        46 to UnoCard(CardColor.RED, CardType.SKIP),
        47 to UnoCard(CardColor.RED, CardType.SKIP),
        48 to UnoCard(CardColor.RED, CardType.PLUS_TWO),
        49 to UnoCard(CardColor.RED, CardType.PLUS_TWO),

        // Желтые карты (50-74)
        50 to UnoCard(CardColor.YELLOW, CardType.ZERO),
        51 to UnoCard(CardColor.YELLOW, CardType.ONE),
        52 to UnoCard(CardColor.YELLOW, CardType.ONE),
        53 to UnoCard(CardColor.YELLOW, CardType.TWO),
        54 to UnoCard(CardColor.YELLOW, CardType.TWO),
        55 to UnoCard(CardColor.YELLOW, CardType.THREE),
        56 to UnoCard(CardColor.YELLOW, CardType.THREE),
        57 to UnoCard(CardColor.YELLOW, CardType.FOUR),
        58 to UnoCard(CardColor.YELLOW, CardType.FOUR),
        59 to UnoCard(CardColor.YELLOW, CardType.FIVE),
        60 to UnoCard(CardColor.YELLOW, CardType.FIVE),
        61 to UnoCard(CardColor.YELLOW, CardType.SIX),
        62 to UnoCard(CardColor.YELLOW, CardType.SIX),
        63 to UnoCard(CardColor.YELLOW, CardType.SEVEN),
        64 to UnoCard(CardColor.YELLOW, CardType.SEVEN),
        65 to UnoCard(CardColor.YELLOW, CardType.EIGHT),
        66 to UnoCard(CardColor.YELLOW, CardType.EIGHT),
        67 to UnoCard(CardColor.YELLOW, CardType.NINE),
        68 to UnoCard(CardColor.YELLOW, CardType.NINE),
        69 to UnoCard(CardColor.YELLOW, CardType.REVERSE),
        70 to UnoCard(CardColor.YELLOW, CardType.REVERSE),
        71 to UnoCard(CardColor.YELLOW, CardType.SKIP),
        72 to UnoCard(CardColor.YELLOW, CardType.SKIP),
        73 to UnoCard(CardColor.YELLOW, CardType.PLUS_TWO),
        74 to UnoCard(CardColor.YELLOW, CardType.PLUS_TWO),

        // Синие карты (75-99)
        75 to UnoCard(CardColor.BLUE, CardType.ZERO),
        76 to UnoCard(CardColor.BLUE, CardType.ONE),
        77 to UnoCard(CardColor.BLUE, CardType.ONE),
        78 to UnoCard(CardColor.BLUE, CardType.TWO),
        79 to UnoCard(CardColor.BLUE, CardType.TWO),
        80 to UnoCard(CardColor.BLUE, CardType.THREE),
        81 to UnoCard(CardColor.BLUE, CardType.THREE),
        82 to UnoCard(CardColor.BLUE, CardType.FOUR),
        83 to UnoCard(CardColor.BLUE, CardType.FOUR),
        84 to UnoCard(CardColor.BLUE, CardType.FIVE),
        85 to UnoCard(CardColor.BLUE, CardType.FIVE),
        86 to UnoCard(CardColor.BLUE, CardType.SIX),
        87 to UnoCard(CardColor.BLUE, CardType.SIX),
        88 to UnoCard(CardColor.BLUE, CardType.SEVEN),
        89 to UnoCard(CardColor.BLUE, CardType.SEVEN),
        90 to UnoCard(CardColor.BLUE, CardType.EIGHT),
        91 to UnoCard(CardColor.BLUE, CardType.EIGHT),
        92 to UnoCard(CardColor.BLUE, CardType.NINE),
        93 to UnoCard(CardColor.BLUE, CardType.NINE),
        94 to UnoCard(CardColor.BLUE, CardType.REVERSE),
        95 to UnoCard(CardColor.BLUE, CardType.REVERSE),
        96 to UnoCard(CardColor.BLUE, CardType.SKIP),
        97 to UnoCard(CardColor.BLUE, CardType.SKIP),
        98 to UnoCard(CardColor.BLUE, CardType.PLUS_TWO),
        99 to UnoCard(CardColor.BLUE, CardType.PLUS_TWO),

        // Wild карты (100-107)
        100 to UnoCard(CardColor.BLUE, CardType.WILD),
        101 to UnoCard(CardColor.BLUE, CardType.WILD),
        102 to UnoCard(CardColor.BLUE, CardType.WILD),
        103 to UnoCard(CardColor.BLUE, CardType.WILD),
        104 to UnoCard(CardColor.BLUE, CardType.WILD_PLUS_FOUR),
        105 to UnoCard(CardColor.BLUE, CardType.WILD_PLUS_FOUR),
        106 to UnoCard(CardColor.BLUE, CardType.WILD_PLUS_FOUR),
        107 to UnoCard(CardColor.BLUE, CardType.WILD_PLUS_FOUR)
    )

    // Получение карты по ID
    fun getCard(id: Int): UnoCard? {
        return cards[id]
    }

    // Получение читаемого названия (для отображения)
    fun getReadableName(id: Int): String {
        val card = cards[id]
        return if (card != null) {
            when (card.type) {
                CardType.WILD -> "WILD"
                CardType.WILD_PLUS_FOUR -> "WILD +4"
                else -> "${card.color.name} ${card.type.name}"
            }
        } else {
            "UNKNOWN_$id"
        }
    }


    fun getScoreById(id: Int): Int {
        return cards[id]?.type?.value ?: 0
    }
}