package io.github.schmeh.chatincolor.players

import io.github.schmeh.chatincolor.util.randomSaturatedColor

import net.minecraft.client.MinecraftClient

import org.slf4j.LoggerFactory

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

import net.fabricmc.loader.api.FabricLoader

private val configDir = FabricLoader.getInstance().configDir.toFile()
private val configFile = File(configDir, "chatincolor/players.json")
private val json = Json { prettyPrint = true }

private val logger = LoggerFactory.getLogger("chatincolor")
private val client = MinecraftClient.getInstance()

// Maps of each player in chat and their color
private val randomPlayerColors = mutableMapOf<String, Int>()
private val setPlayerColors: MutableMap<String, Int> = loadSetPlayerColors()

@Serializable
data class PlayerColors(val colors: Map<String, Int>)

/**
 * Load the set player colors and return the map. Return an empty map if no file is found or an error occurs.
 */
fun loadSetPlayerColors(): MutableMap<String, Int> {
    logger.info("Chat in Color: Loading set colors file...")
    if (!configFile.exists()) {
        logger.info("Chat in Color: Set colors file not found.")
        return mutableMapOf()
    }
    return try {
        val data = json.decodeFromString<PlayerColors>(configFile.readText())
        data.colors.toMutableMap()
    } catch (e: Exception) {
        logger.warn("Chat in Color: Failed to load set colors: ${e.message}")
        mutableMapOf()
    }
}

/**
 * Save the set player colors map.
 */
fun saveSetPlayerColors() {
    try {
        logger.info("Chat in Color: Saving set colors file...")
        val data = PlayerColors(setPlayerColors)
        configFile.parentFile.mkdirs()
        configFile.writeText(json.encodeToString(data))
        logger.info("Chat in Color: Saved set colors file.")
    } catch (e: Exception) {
        logger.warn("Chat in Color: Failed to load set colors: ${e.message}")
    }
}

/**
 * Returns the player's set color if set.
 * If not, it returns the player's random color.
 * If the player doesn't have a random color, one is assigned and that is returned
 */
fun getPlayerColor(playerName: String): Int {
    // If the player's color isn't explicitly set, get the player's random color.
    //  If the player doesn't have a random color assigned, generate one and add it to the map
    return setPlayerColors[playerName]
            ?: randomPlayerColors.getOrPut(playerName) { randomSaturatedColor() }
}

/**
 * Set a random color for the player
 */
fun setRandomColor(playerName: String) {
    randomPlayerColors[playerName] = randomSaturatedColor()
}

/**
 * Sets the player's color to a color Int
 */
fun setPlayerColor(playerName: String, color: Int) {
    setPlayerColors[playerName] = color
    saveSetPlayerColors()
}

/**
 * Returns true if successful and false if unsuccessful
 */
fun unsetPlayerColor(playerName: String): Boolean {
    return setPlayerColors.remove(playerName)?.also { saveSetPlayerColors() } != null
}

/**
 * Clears the random player colors map
 */
fun clearRandomColors() {
    randomPlayerColors.clear()
}

/**
 * Returns an array of the usernames of all players with set colors
 */
fun getSetPlayerNames(): Array<String> {
    return setPlayerColors.keys.toTypedArray()
}

/**
 * Returns an array of the usernames of all players with random colors
 */
fun getRandomPlayerNames(): Array<String> {
    return randomPlayerColors.keys.toTypedArray()
}

/**
 * Returns an array of the usernames of all players currently online
 */
fun getOnlinePlayerNames(): Array<String> {
    // TODO: cache online players map and only update on PLAYER_JOIN and PLAYER_LEAVE events
    val playerList = client.networkHandler?.playerList ?: return emptyArray()

    return playerList.mapNotNull { it.profile.name }.toTypedArray()
}

/**
 * Returns an array of all players with set colors, random colors, or are online, while removing duplicates
 */
fun getAllPlayerNames(): Array<String> {
    val setPlayers = getSetPlayerNames()
    val onlinePlayers = getOnlinePlayerNames()
    val randomPlayers = getRandomPlayerNames()

    // Combine and remove duplicates
    return (setPlayers + randomPlayers + onlinePlayers)
            .distinct()
            .toTypedArray()
}

/**
 * Returns an array of Pairs of the player name and their set color
 */
fun getSetPlayerColors(): Array<Pair<String, Int>> =
        setPlayerColors.entries.map { it.key to it.value }.toTypedArray()

/**
 * Returns true if the name is a username in either the online players, set player colors, and random player colors maps
 */
fun isPlayerName(word: String): Boolean {
    return setPlayerColors.get(word) != null
            || randomPlayerColors.get(word) != null
            // TODO: cache online players map and only update on PLAYER_JOIN and PLAYER_LEAVE events
            || client.networkHandler?.playerList?.any { it.profile.name == word } == true
}
