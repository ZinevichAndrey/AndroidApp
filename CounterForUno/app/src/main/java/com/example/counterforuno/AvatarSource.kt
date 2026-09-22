package com.example.counterforuno


import android.os.Parcel
import android.os.Parcelable

sealed class AvatarSource : Parcelable {

    class Resource(val resId: Int) : AvatarSource(), Parcelable {
        constructor(parcel: Parcel) : this(parcel.readInt())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(resId)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<Resource> {
            override fun createFromParcel(parcel: Parcel): Resource = Resource(parcel)
            override fun newArray(size: Int): Array<Resource?> = arrayOfNulls(size)
        }
    }

    class AvatarUri(val uri: String) : AvatarSource(), Parcelable {
        constructor(parcel: Parcel) : this(parcel.readString() ?: "")

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(uri)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<AvatarUri> {
            override fun createFromParcel(parcel: Parcel): AvatarUri = AvatarUri(parcel)
            override fun newArray(size: Int): Array<AvatarUri?> = arrayOfNulls(size)
        }
    }

    companion object CREATOR : Parcelable.Creator<AvatarSource> {
        override fun createFromParcel(parcel: Parcel): AvatarSource {
            val type = parcel.readInt()
            return when (type) {
                0 -> Resource(parcel)
                1 -> AvatarUri(parcel)
                else -> throw IllegalArgumentException("Unknown AvatarSource type")
            }
        }
        override fun newArray(size: Int): Array<AvatarSource?> = arrayOfNulls(size)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        when (this) {
            is Resource -> {
                parcel.writeInt(0)
                parcel.writeInt(resId)
            }
            is AvatarUri -> {
                parcel.writeInt(1)
                parcel.writeString(uri)
            }
        }
    }
}