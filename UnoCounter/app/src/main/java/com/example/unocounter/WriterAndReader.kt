package com.example.unocounter

import android.content.Context
import java.io.File

object WriterAndReader {

    fun savePlayers(context: Context, players: List<PlayerStats>, fileName: String) {
        val file = File(context.filesDir, fileName)
        file.writeText("")
        players.forEach {
            val avatarString = when (val avatar = it.avatar) {
                is AvatarSource.Resource -> "res:${avatar.resName}"  // Изменено: resId -> resName
                is AvatarSource.AvatarUri -> "uri:${avatar.uri}"
            }
            val line = "${it.id}|${it.name}|$avatarString|${it.numberOfVictories}\n"
            file.appendText(line)
        }
    }

    fun loadPlayers(context: Context, fileName: String): List<PlayerStats> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            return emptyList()
        }
        return file.readLines().mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 4) return@mapNotNull null
            val id = parts[0]
            val name = parts[1]
            val avatarRaw = parts[2]
            val victories = parts[3].toIntOrNull() ?: 0

            val avatar = if (avatarRaw.startsWith("res:")) {
                // Изменено: теперь сохраняем имя ресурса (String), а не ID (Int)
                AvatarSource.Resource(avatarRaw.removePrefix("res:"))
            } else {
                AvatarSource.AvatarUri(avatarRaw.removePrefix("uri:"))
            }
            PlayerStats(id, name, avatar, victories)
        }
    }
}
