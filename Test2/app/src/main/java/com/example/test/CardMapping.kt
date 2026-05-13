package com.example.test

object CardMapping {

    fun getName(id: Int): String = when (id) {
        0 -> "0"
        1 -> "1"
        2 -> "2"
        3 -> "3"
        4 -> "4"
        5 -> "5"
        6 -> "6"
        7 -> "7"
        8 -> "8"
        9 -> "9"
        10 -> "Reverse"
        11 -> "Skip"
        12 -> "+2"
        13 -> "Wild"
        14 -> "Wild +4"
        else -> "Card $id"
    }

    fun getScore(name: String): Int = when {
        name.matches(Regex("\\d+")) -> name.toInt()
        name == "Reverse" || name == "Skip" || name == "+2" -> 20
        name == "Wild" || name == "Wild +4" -> 50
        else -> 0
    }
}