package io.github.schmeh.chatincolor.manager

import io.github.schmeh.chatincolor.player.Players

object PlayerManager {
    var players: Players = Players(mutableMapOf())
        private set

    fun load() {
        players = Players.loadPlayersFile()
    }

    fun save() {
        Players.savePlayersToFile(players)
    }
}
