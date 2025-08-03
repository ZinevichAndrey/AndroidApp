package com.example.counterforuno

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.concurrent.atomic.AtomicLong

class Player(
    private val id: Long = IdPlayer.initId(),
    var name: String,
    var avatar: AvatarSource,
    var numberOfVictories: Int = 1
) : Parcelable {

    var score by mutableStateOf(0)

    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        name = parcel.readString() ?: "",
        avatar = parcel.readParcelable(AvatarSource::class.java.classLoader)!!,
        numberOfVictories = parcel.readInt()
    )


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeParcelable(avatar, flags)
        parcel.writeInt(numberOfVictories)
    }

    override fun describeContents(): Int = 0

    fun getId(): Long = id

    companion object CREATOR : Parcelable.Creator<Player> {
        override fun createFromParcel(parcel: Parcel): Player = Player(parcel)
        override fun newArray(size: Int): Array<Player?> = arrayOfNulls(size)
    }

    private object IdPlayer {
        private val playerId = AtomicLong(0)
        fun initId(): Long = playerId.incrementAndGet()
    }
}
