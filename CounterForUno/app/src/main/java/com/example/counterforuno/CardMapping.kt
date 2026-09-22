package com.example.counterforuno

import android.content.Context
object CardMapping {

    private val cards = mapOf(
        45 to listOf(UnoCard(CardColor.BLUE, CardType.SIX)),
        32 to listOf(UnoCard(CardColor.GREEN, CardType.SIX)),
        19 to listOf(UnoCard(CardColor.YELLOW, CardType.SIX)),
        21 to listOf(UnoCard(CardColor.YELLOW, CardType.SIX), UnoCard(CardColor.YELLOW, CardType.EIGHT)),
        6 to listOf(UnoCard(CardColor.RED, CardType.SIX), UnoCard(CardColor.RED, CardType.NINE)),
        13 to listOf(UnoCard(CardColor.YELLOW, CardType.FIVE), UnoCard(CardColor.BLUE, CardType.ZERO)),
        18 to listOf(UnoCard(CardColor.YELLOW, CardType.FIVE), UnoCard(CardColor.MULTICOLORED, CardType.WILD_PLUS_FOUR)),
        20 to listOf(UnoCard(CardColor.YELLOW, CardType.FIVE), UnoCard(CardColor.YELLOW, CardType.SEVEN)),
        44 to listOf(UnoCard(CardColor.BLUE, CardType.FIVE), UnoCard(CardColor.BLUE, CardType.THREE)),
        46 to listOf(UnoCard(CardColor.BLUE, CardType.FIVE), UnoCard(CardColor.BLUE, CardType.SEVEN)),
        5 to listOf(UnoCard(CardColor.RED, CardType.FIVE), UnoCard(CardColor.RED, CardType.THREE), UnoCard(CardColor.MULTICOLORED, CardType.WILD_PLUS_FOUR)),
        31 to listOf(UnoCard(CardColor.GREEN, CardType.FIVE)),
        43 to listOf(UnoCard(CardColor.BLUE, CardType.FOUR)),
        4 to listOf(UnoCard(CardColor.RED, CardType.FOUR)),
        17 to listOf(UnoCard(CardColor.YELLOW, CardType.FOUR)),
        30 to listOf(UnoCard(CardColor.GREEN, CardType.FOUR), UnoCard(CardColor.YELLOW, CardType.REVERSE)),
        42 to listOf(UnoCard(CardColor.BLUE, CardType.THREE)),
        41 to listOf(UnoCard(CardColor.BLUE, CardType.THREE), UnoCard(CardColor.BLUE, CardType.TWO), UnoCard(CardColor.BLUE, CardType.PLUS_TWO)),
        34 to listOf(UnoCard(CardColor.GREEN, CardType.THREE), UnoCard(CardColor.GREEN, CardType.EIGHT)),
        8 to listOf(UnoCard(CardColor.RED, CardType.THREE), UnoCard(CardColor.RED, CardType.EIGHT)),
        16 to listOf(UnoCard(CardColor.YELLOW, CardType.THREE)),
        9 to listOf(UnoCard(CardColor.RED, CardType.NINE)),
        48 to listOf(UnoCard(CardColor.BLUE, CardType.NINE)),
        22 to listOf(UnoCard(CardColor.YELLOW, CardType.NINE)),
        35 to listOf(UnoCard(CardColor.GREEN, CardType.NINE)),
        23 to listOf(UnoCard(CardColor.YELLOW, CardType.REVERSE)),
        36 to listOf(UnoCard(CardColor.YELLOW, CardType.REVERSE)),
        49 to listOf(UnoCard(CardColor.BLUE, CardType.REVERSE)),
        47 to listOf(UnoCard(CardColor.BLUE, CardType.EIGHT)),
        33 to listOf(UnoCard(CardColor.GREEN, CardType.SEVEN)),
        7 to listOf(UnoCard(CardColor.RED, CardType.SEVEN)),
        2 to listOf(UnoCard(CardColor.RED, CardType.TWO), UnoCard(CardColor.RED, CardType.PLUS_TWO)),
        28 to listOf(UnoCard(CardColor.GREEN, CardType.TWO), UnoCard(CardColor.GREEN, CardType.PLUS_TWO)),
        15 to listOf(UnoCard(CardColor.YELLOW, CardType.TWO), UnoCard(CardColor.YELLOW, CardType.PLUS_TWO)),
        27 to listOf(UnoCard(CardColor.GREEN, CardType.ONE)),
        40 to listOf(UnoCard(CardColor.BLUE, CardType.ONE)),
        1 to listOf(UnoCard(CardColor.RED, CardType.ONE)),
        14 to listOf(UnoCard(CardColor.YELLOW, CardType.ONE)),
        26 to listOf(UnoCard(CardColor.GREEN, CardType.ZERO)),
        39 to listOf(UnoCard(CardColor.BLUE, CardType.ZERO), UnoCard(CardColor.BLUE, CardType.SIX)),
        0 to listOf(UnoCard(CardColor.RED, CardType.ZERO)),
        11 to listOf(UnoCard(CardColor.RED, CardType.SKIP)),
        24 to listOf(UnoCard(CardColor.YELLOW, CardType.SKIP), UnoCard(CardColor.BLUE, CardType.SIX)),
        37 to listOf(UnoCard(CardColor.GREEN, CardType.SKIP), UnoCard(CardColor.GREEN, CardType.SIX)),
        50 to listOf(UnoCard(CardColor.BLUE, CardType.SKIP), UnoCard(CardColor.BLUE, CardType.SIX)),
        53 to listOf(UnoCard(CardColor.MULTICOLORED, CardType.WILD)),
        52 to listOf(UnoCard(CardColor.MULTICOLORED, CardType.WILD_PLUS_FOUR), UnoCard(CardColor.YELLOW, CardType.THREE)),
        12 to listOf(UnoCard(CardColor.RED, CardType.PLUS_TWO)),
        51 to listOf(UnoCard(CardColor.BLUE, CardType.PLUS_TWO)),
        3 to listOf(UnoCard(CardColor.RED, CardType.THREE)),
        10 to listOf(UnoCard(CardColor.RED, CardType.REVERSE)),
        25 to listOf(UnoCard(CardColor.YELLOW, CardType.PLUS_TWO)),
        38 to listOf(UnoCard(CardColor.GREEN, CardType.PLUS_TWO)),
        29 to listOf(UnoCard(CardColor.GREEN, CardType.THREE)),
    )

    fun getCards(id: Int): List<UnoCard> {
        return cards[id] ?: emptyList()
    }

    fun getCard(id: Int): UnoCard? {
        return getCards(id).firstOrNull()
    }

    fun isKnown(id: Int): Boolean {
        return cards.containsKey(id)
    }

    fun isAmbiguous(id: Int): Boolean {
        return getCards(id).size > 1
    }

    fun getReadableName(id: Int, context: Context): String {
        val cardList = getCards(id)
        return when (cardList.size) {
            0 -> "UNKNOWN_$id"
            1 -> formatCardForDisplay(cardList.first(), context)
            else -> cardList.joinToString(" / ") { formatCardForDisplay(it, context) }
        }
    }

    fun formatCardForDisplay(card: UnoCard, context: Context): String {
        return when (card.type) {
            CardType.WILD -> context.getString(R.string.card_wild)
            CardType.WILD_PLUS_FOUR -> context.getString(R.string.card_wild_plus_four)
            CardType.REVERSE -> context.getString(R.string.card_reverse)
            CardType.SKIP -> context.getString(R.string.card_skip)
            CardType.PLUS_TWO -> context.getString(R.string.card_plus_two)
            CardType.ZERO -> context.getString(R.string.card_zero)
            CardType.ONE -> context.getString(R.string.card_one)
            CardType.TWO -> context.getString(R.string.card_two)
            CardType.THREE -> context.getString(R.string.card_three)
            CardType.FOUR -> context.getString(R.string.card_four)
            CardType.FIVE -> context.getString(R.string.card_five)
            CardType.SIX -> context.getString(R.string.card_six)
            CardType.SEVEN -> context.getString(R.string.card_seven)
            CardType.EIGHT -> context.getString(R.string.card_eight)
            CardType.NINE -> context.getString(R.string.card_nine)
        }
    }


}