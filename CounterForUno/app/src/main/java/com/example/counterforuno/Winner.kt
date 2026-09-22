package com.example.counterforuno

import android.os.Parcel
import android.os.Parcelable

class Winner(
    private val id: String,
    var name: String,
    var avatar: AvatarSource,
    var victories: Int = 1
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readString()?: "",
        name = parcel.readString() ?: "",
        avatar = parcel.readParcelable(AvatarSource::class.java.classLoader)!!,
        victories = parcel.readInt()
    )

    fun getId(): String = id

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeParcelable(avatar, flags)
        parcel.writeInt(victories)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Winner> {

        override fun createFromParcel(parcel: Parcel): Winner {
            return Winner(parcel)
        }

        override fun newArray(size: Int): Array<Winner?> {
            return arrayOfNulls(size)
        }
    }
}