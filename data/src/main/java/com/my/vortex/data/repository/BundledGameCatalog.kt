package com.my.vortex.data.repository

import android.content.Context
import com.my.vortex.data.rpc.Constants
import com.my.vortex.domain.model.Game
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
private data class XboxCatalog(@SerialName("Games List") val groups: List<XboxGroup>)

@Serializable
private data class XboxGroup(@SerialName("Games") val games: List<XboxGame>)

@Serializable
private data class XboxGame(
    val titlename: String,
    val titleicon: String? = null,
    val type: Int? = null,
)

internal class BundledGameCatalog @Inject constructor(@ApplicationContext private val context: Context) {
    fun load(): List<Game> {
        val json = Json { ignoreUnknownKeys = true }
        val xbox = context.assets.open("catalog/xbox-games.json").bufferedReader().use { it.readText() }
        val xboxGames = json.decodeFromString<XboxCatalog>(xbox).groups.flatMap { it.games }
            .filter { it.type == null || it.type == 1 }
            .filter { it.titlename.isNotBlank() }
            .map { Game(Constants.XBOX, Constants.XBOX_LINK, it.titleicon?.takeIf(String::isNotBlank), it.titlename.trim()) }
        val systems = listOf(
            Triple("hac", Constants.NINTENDO, Constants.NINTENDO_LINK),
            Triple("bee", "Nintendo Switch 2", Constants.NINTENDO_LINK),
            Triple("ctr", Constants.NINTENDO_3DS, Constants.N3DS_LINK),
            Triple("wup", Constants.WII_U, Constants.WII_U_LINK),
        )
        val nintendoGames = systems.flatMap { (file, platform, smallImage) ->
            val text = context.assets.open("catalog/$file.csv").bufferedReader().use { it.readText() }
            parseCsv(text).drop(1).mapNotNull { row ->
                if (row.size < 7) return@mapNotNull null
                val title = row.subList(4, 7).firstOrNull { it.isNotBlank() }?.trim() ?: return@mapNotNull null
                val region = listOf("us", "eu", "jp").getOrNull((1..3).firstOrNull { row[it].isNotBlank() }?.minus(1) ?: -1)
                val id = row[0].trim()
                val image = if (region != null && id.matches(Regex("[A-Za-z0-9_-]+"))) {
                    "https://github.com/ninstar/Rich-Presence-U-DB/raw/37c9f6060328abb75b54307cb67b5ce581915e94/titles/$file/$id.$region.jpg"
                } else null
                Game(platform, smallImage, image, title)
            }
        }
        return (nintendoGames + xboxGames).distinctBy { it.platform.lowercase() to it.game_title.lowercase() }
    }
}

internal fun parseCsv(text: String): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    val row = mutableListOf<String>()
    val field = StringBuilder()
    var quoted = false
    var i = 0
    while (i < text.length) {
        val char = text[i]
        when {
            char == '"' && quoted && i + 1 < text.length && text[i + 1] == '"' -> {
                field.append('"')
                i++
            }
            char == '"' -> quoted = !quoted
            char == ',' && !quoted -> { row.add(field.toString()); field.clear() }
            (char == '\n' || char == '\r') && !quoted -> {
                row.add(field.toString()); field.clear()
                if (row.any { it.isNotBlank() }) rows.add(row.toList())
                row.clear()
                if (char == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
            }
            else -> field.append(char)
        }
        i++
    }
    if (field.isNotEmpty() || row.isNotEmpty()) {
        row.add(field.toString())
        rows.add(row.toList())
    }
    return rows
}
