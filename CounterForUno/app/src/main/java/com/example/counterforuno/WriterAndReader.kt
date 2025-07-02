package com.example.counterforuno

import android.content.Context
import android.os.Parcel
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object WriterAndReader {



    fun Player.toByteArray(): ByteArray {
        val parcel = Parcel.obtain()
        this.writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        parcel.recycle()
        return bytes
    }

    fun saveWinnerToFile(context: Context, winner: Player, fileName: String) {
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



    fun loadWinnersFromFile(context: Context, filePath: String): List<Player> {
        val list: MutableList<Player> = mutableListOf()
        val file = File(context.filesDir, filePath)
        try {
            DataInputStream(BufferedInputStream(FileInputStream(file))).use { dis ->
                while (dis.available() > 0) {
                    val length = dis.readInt()
                    val byteArray = ByteArray(length)
                    dis.readFully(byteArray)
                    val parcel = Parcel.obtain()
                    parcel.unmarshall(byteArray,0,length)
                    parcel.setDataPosition(0)
                    val player = Player.createFromParcel(parcel)
                    list.add(player)
                    parcel.recycle()
                }
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

}