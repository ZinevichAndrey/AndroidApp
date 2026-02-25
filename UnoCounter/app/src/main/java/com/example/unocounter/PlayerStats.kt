package com.example.unocounter

import android.os.Parcel
import android.os.Parcelable
import java.util.UUID

data class PlayerStats(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatar: AvatarSource,
    var numberOfVictories: Int = 0
) : Parcelable {

    @Transient
    var score: Int = 0

    constructor(parcel: Parcel) : this(
        id = parcel.readString()!!,
        name = parcel.readString()!!,
        avatar = parcel.readParcelable(AvatarSource::class.java.classLoader)!!,
        numberOfVictories = parcel.readInt()
    ) {
        score = 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeParcelable(avatar, flags)
        parcel.writeInt(numberOfVictories)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<PlayerStats> {
        override fun createFromParcel(parcel: Parcel) = PlayerStats(parcel)
        override fun newArray(size: Int) = arrayOfNulls<PlayerStats?>(size)
    }
}