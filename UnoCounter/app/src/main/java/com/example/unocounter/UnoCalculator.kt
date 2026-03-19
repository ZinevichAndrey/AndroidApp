package com.example.unocounter

object UnoCalculator {
    fun score(card: UnoCard): Int {
        return when (card.type) {
            CardType.NUMBER -> card.number
            CardType.SKIP, CardType.REVERSE, CardType.DRAW2 -> 20
            CardType.WILD, CardType.WILD_DRAW4 -> 50
        }
    }
}