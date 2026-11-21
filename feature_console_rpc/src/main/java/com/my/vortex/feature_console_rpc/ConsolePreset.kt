package com.my.vortex.feature_console_rpc

import com.my.vortex.domain.model.Game
import com.my.vortex.domain.model.rpc.RpcConfig

internal fun Game.toConsoleConfig(
    startTimeMillis: Long,
    status: String = "dnd",
    showElapsed: Boolean = true,
): RpcConfig = RpcConfig(
    name = platform,
    details = game_title,
    timestampsStart = if (showElapsed) startTimeMillis.toString() else "",
    status = status.takeIf { it in CONSOLE_STATUSES } ?: "dnd",
    largeImg = large_image.orEmpty(),
    smallImg = small_image,
    type = "0",
)

internal fun filterConsoleGames(games: List<Game>, query: String): List<Game> {
    val search = query.trim()
    return if (search.isEmpty()) games.toList() else games.filter {
        it.game_title.contains(search, ignoreCase = true) ||
            it.platform.contains(search, ignoreCase = true)
    }
}

internal val CONSOLE_STATUSES = listOf("online", "idle", "dnd")

internal fun Game.sameConsoleGame(other: Game): Boolean =
    platform.trim().equals(other.platform.trim(), ignoreCase = true) &&
        game_title.trim().equals(other.game_title.trim(), ignoreCase = true)

internal fun RpcConfig.toConsoleGame(): Game? =
    if (name.isBlank() || details.isBlank()) null
    else Game(name.trim(), smallImg, largeImg.ifBlank { null }, details.trim())

internal fun filterByPlatform(games: List<Game>, platform: String?): List<Game> =
    if (platform == null) games else games.filter { it.platform.equals(platform, ignoreCase = true) }

internal fun Game.consoleStartTime(previous: RpcConfig?, isRunning: Boolean, now: Long): Long {
    val previousGame = previous?.toConsoleGame()
    val previousStart = previous?.timestampsStart?.toLongOrNull()
    return if (isRunning && previousGame != null && sameConsoleGame(previousGame) && previousStart != null && previousStart > 0) {
        previousStart
    } else now
}
