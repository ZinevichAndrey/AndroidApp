package com.example.counterforuno

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.ArrayDeque
import java.util.UUID

class Player(
    private val id: String = UUID.randomUUID().toString(),
    var name: String,
    var avatar: AvatarSource,
) : Parcelable {

    var score by mutableStateOf(0)
        private set
    private val scoreHistory = ArrayDeque<Int>()

    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: UUID.randomUUID().toString(),
        name = parcel.readString() ?: "",
        avatar = parcel.readParcelable(AvatarSource::class.java.classLoader)!!
    ) {
        score = parcel.readInt()
        val historySize = parcel.readInt()
        repeat(historySize) {
            scoreHistory.addLast(parcel.readInt())
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeParcelable(avatar, flags)
        parcel.writeInt(score)
        parcel.writeInt(scoreHistory.size)
        scoreHistory.forEach {
            parcel.writeInt(it)
        }
    }
    override fun describeContents(): Int = 0
    fun getId(): String = id

    fun addScore(points: Int) {
        if (points <= 0) {
            return
        }
        score += points
        scoreHistory.addLast(points)
    }

    fun removeLastScore(): Boolean {
        if (scoreHistory.isEmpty()) {
            return false
        }
        score -= scoreHistory.removeLast()
        return true
    }

    fun addWin() {
        score++
    }

    fun removeWin() {
        if (score > 0) {
            score--
        }
    }

    fun resetScore() {
        score = 0
        scoreHistory.clear()
    }

    companion object CREATOR : Parcelable.Creator<Player> {
        override fun createFromParcel(parcel: Parcel): Player = Player(parcel)

        override fun newArray(size: Int): Array<Player?> = arrayOfNulls(size)
    }


}