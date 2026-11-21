package com.my.vortex.feature_console_rpc

import com.my.vortex.domain.model.Game
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.my.vortex.domain.model.rpc.RpcConfig
import com.my.vortex.domain.model.rpc.forConsoleRestart

class ConsolePresetTest {
    @Test
    fun presetContainsGamePlatformImagesAndStartTime() {
        val game = Game("Switch", "small-image", "large-image", "Example Game")
        val config = game.toConsoleConfig(123456789L)
        assertEquals("Switch", config.name)
        assertEquals("Example Game", config.details)
        assertEquals("small-image", config.smallImg)
        assertEquals("large-image", config.largeImg)
        assertEquals("123456789", config.timestampsStart)
        assertEquals("0", config.type)
    }

    @Test
    fun savedPresetRoundTrips() {
        val original = Game("Switch", "small", "large", "Example").toConsoleConfig(42L)
        val restored = Json.decodeFromString<RpcConfig>(Json.encodeToString(original))
        assertEquals(original, restored)
    }

    @Test
    fun searchMatchesTitleAndPlatform() {
        val games = listOf(Game("Switch", "", null, "Zelda"), Game("Xbox", "", null, "Halo"))
        assertEquals(listOf(games[0]), filterConsoleGames(games, " zEL "))
        assertEquals(listOf(games[1]), filterConsoleGames(games, "XBOX"))
        assertEquals(games, filterConsoleGames(games, "  "))
    }

    @Test
    fun manualPresetWorksWithoutImages() {
        val config = Game("PC", "", null, "My Game").toConsoleConfig(100L)
        assertEquals("PC", config.name)
        assertEquals("My Game", config.details)
        assertEquals("", config.largeImg)
        assertEquals("", config.smallImg)
    }

    @Test
    fun optionsControlStatusAndElapsedTime() {
        val game = Game("Xbox", "", null, "Halo")
        val noTimer = game.toConsoleConfig(42L, "online", false)
        assertEquals("online", noTimer.status)
        assertEquals("", noTimer.timestampsStart)
        assertEquals("idle", game.toConsoleConfig(42L, "idle", true).status)
        assertEquals("dnd", game.toConsoleConfig(42L, "invalid", true).status)
    }

    @Test
    fun favoritesRestoreAndMatchWithoutCaseSensitivity() {
        val game = Game("Switch", "small", "large", "Zelda")
        val restored = Json.decodeFromString<List<RpcConfig>>(Json.encodeToString(listOf(game.toConsoleConfig(0))))
            .mapNotNull { it.toConsoleGame() }
        assertEquals(listOf(game), restored)
        assertEquals(true, game.sameConsoleGame(Game(" switch ", "", null, "zelda")))
        assertEquals(null, RpcConfig().toConsoleGame())
    }

    @Test
    fun platformFilteringWorksWithSearch() {
        val games = listOf(Game("Switch", "", null, "Zelda"), Game("Xbox", "", null, "Halo"))
        assertEquals(listOf(games[0]), filterByPlatform(games, "switch"))
        assertEquals(games, filterByPlatform(games, null))
        assertEquals(emptyList<Game>(), filterByPlatform(filterConsoleGames(games, "halo"), "Switch"))
    }

    @Test
    fun restartResetsEnabledTimerWithoutEnablingDisabledTimer() {
        val game = Game("Switch", "", null, "Zelda")
        assertEquals("200", game.toConsoleConfig(100).forConsoleRestart(200).timestampsStart)
        assertEquals("", game.toConsoleConfig(100, showElapsed = false).forConsoleRestart(200).timestampsStart)
    }

    @Test
    fun updateKeepsTimerForSameActiveGame() {
        val game = Game("Switch", "", null, "Zelda")
        val saved = game.toConsoleConfig(100)
        assertEquals(100L, game.consoleStartTime(saved, true, 200))
        assertEquals(200L, game.consoleStartTime(saved, false, 200))
        assertEquals(200L, Game("Xbox", "", null, "Halo").consoleStartTime(saved, true, 200))
        assertEquals(200L, game.consoleStartTime(game.toConsoleConfig(100, showElapsed = false), true, 200))
    }
}
