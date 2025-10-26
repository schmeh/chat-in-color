package io.github.schmeh.chatincolor.player

import io.github.schmeh.chatincolor.util.randomSaturatedColor

import net.minecraft.client.MinecraftClient

import org.slf4j.LoggerFactory

import kotlinx.serialization.Transient
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

import net.fabricmc.loader.api.FabricLoader

private val configDir = FabricLoader.getInstance().configDir.toFile()
private val configFile = File(configDir, "chatincolor/players.json")
private val json = Json { prettyPrint = true }

private val logger = LoggerFactory.getLogger("chatincolor")
private val client = MinecraftClient.getInstance()

@Serializable
data class Players(val players: MutableMap<String, Player>) {
    operator fun get(name: String): Player? {
        return players[name]
    }

    operator fun set(name: String, player: Player) {
        players[name] = player
    }

    /**
     * Returns an array of the usernames of all players
     */
    fun getPlayerNames(): Array<String> {
        return players.keys.toTypedArray()
    }

    /**
     * Returns an array of the usernames of all players with a set color
     */
    fun getSetPlayerNames(): Array<String> {
        return players.entries
                .filter { (_, player) -> player.setColor != null }
                .map { (name, _) -> name }
                .toTypedArray()
    }

    /**
     * Returns an array of Pairs of the player name and their set color
     */
    fun getSetPlayerColors(): Array<Pair<String, Int>> =
            players.entries
                    .filter { (_, player) -> player.setColor != null } // only keep players with setColor
                    .map { (name, player) -> name to player.setColor!! } // extract name and non-null color
                    .toTypedArray()

    /**
     * Returns an array of the usernames of all players currently online
     */
    fun getOnlinePlayerNames(): Array<String> {
        // TODO: cache online players map and only update on PLAYER_JOIN and PLAYER_LEAVE events
        val playerList = client.networkHandler?.playerList ?: return emptyArray()

        return playerList.mapNotNull { it.profile.name }.toTypedArray()
    }

    /**
     * Returns true if the name is a username in either the online players, set player colors, and random player colors maps
     */
    fun isPlayerName(word: String): Boolean {
        return players[word] != null
                // TODO: cache online players map and only update on PLAYER_JOIN and PLAYER_LEAVE events
                || client.networkHandler?.playerList?.any { it.profile.name == word } == true
    }

    /**
     * Clears all players that don't have a set color from the map
     */
    fun clearUnsetPlayers() {
        val iterator = players.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.setColor == null) {
                iterator.remove()
            }
        }
    }

    /**
     * Randomize the random color for all players
     */
    fun randomizeAllPlayerRandomColors() {
        for ((_, player) in players) {
            player.randomColor = randomSaturatedColor()
        }
    }

    companion object {
        /**
         * Load the set player colors and return the map. Return an empty map if no file is found or an error occurs.
         */
        fun loadPlayersFile(): Players {
            logger.info("Chat in Color: Loading player data file...")
            return if (!configFile.exists()) {
                logger.info("Player data file not found.")
                Players(mutableMapOf())
            } else {
                try {
                    json.decodeFromString<Players>(configFile.readText())
                } catch (e: Exception) {
                    logger.warn("Failed to load player data file: ${e.message}")
                    Players(mutableMapOf())
                }
            }
        }

        /**
         * Save players that have a set color.
         */
        fun savePlayersToFile(players: Players) {
            try {
                logger.info("Chat in Color: Saving set colors file...")

                // Filter players to only those with a set color
                val playersWithSetColor = players.players
                        .filter { (_, player) -> player.setColor != null }
                        .toMutableMap()
                val filteredPlayers = Players(playersWithSetColor)

                configFile.parentFile.mkdirs()
                configFile.writeText(json.encodeToString(filteredPlayers))
                logger.info("Chat in Color: Saved player data file.")
            } catch (e: Exception) {
                logger.warn("Chat in Color: Failed to save player data file: ${e.message}")
            }
        }
    }
}
