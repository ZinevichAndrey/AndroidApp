package com.example.counterforuno

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class Player(
    private val id: Long = IdPlayer.initId(),
    var name: String,
    var avatar: Int,
){
     var score by mutableStateOf(0)

    fun getId(): Long {
        return this.id
    }


    private object IdPlayer {
        private var playerId: Long = 0;

        fun initId(): Long {
            playerId++
            return playerId
        }
    }


}



