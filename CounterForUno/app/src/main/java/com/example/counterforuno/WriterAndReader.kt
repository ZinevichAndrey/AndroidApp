package com.example.counterforuno

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object WriterAndReader {

    private fun Parcelable.toByteArray(): ByteArray {
        val parcel = Parcel.obtain()
        return try {
            writeToParcel(parcel, 0)
            parcel.marshall()
        } finally {
            parcel.recycle()
        }
    }

    fun<T: Parcelable> savePlayersToFile(context: Context, winner: T, fileName: String) {
        val byteArray = winner.toByteArray()
        val file = File(context.filesDir, fileName)
        try {
            DataOutputStream(BufferedOutputStream(FileOutputStream(file, true))).use { dos ->
                dos.writeInt(byteArray.size)
                dos.write(byteArray)
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun <T> loadFromFile(
        context: Context,
        fileName: String,
        creator: (Parcel) -> T
    ): List<T> {
        val list = mutableListOf<T>()
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            return list
        }
        try {
            DataInputStream(BufferedInputStream(FileInputStream(file))).use { dis ->
                while (dis.available() > 0) {
                    val length = dis.readInt()
                    val byteArray = ByteArray(length)
                    dis.readFully(byteArray)
                    val parcel = Parcel.obtain()
                    parcel.unmarshall(byteArray, 0, length)
                    parcel.setDataPosition(0)
                    val item = creator(parcel)
                    list.add(item)
                    parcel.recycle()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }



}