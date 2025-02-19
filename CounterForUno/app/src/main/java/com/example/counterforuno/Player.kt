package com.example.counterforuno

data class Player(
    private val id: Long = IdPlayer.initId(),
    var name: String,
    var avatar: Int,
    var score: Int) {

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



