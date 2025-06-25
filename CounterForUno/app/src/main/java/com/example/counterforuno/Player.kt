package com.example.counterforuno

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue



class Player(
    private val id: Long = IdPlayer.initId(),
    var name: String,
    var avatar: Int,
    var numberOfVictories: Int = 0
): Parcelable {

     var score by mutableStateOf(0)

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeInt(avatar)
        parcel.writeInt(numberOfVictories)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Player> {
        override fun createFromParcel(parcel: Parcel): Player {
            return Player(parcel)
        }

        override fun newArray(size: Int): Array<Player?> {
            return arrayOfNulls(size)
        }
    }


}



