package io.github.schmeh.chatincolor

import io.github.schmeh.chatincolor.commands.*

import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.text.StringVisitable

import net.minecraft.util.Formatting

import io.github.schmeh.chatincolor.players.clearRandomColors
import io.github.schmeh.chatincolor.players.getPlayerColor
import io.github.schmeh.chatincolor.players.isPlayerName
import io.github.schmeh.chatincolor.players.getOnlinePlayerNames
import io.github.schmeh.chatincolor.players.setRandomColor

import io.github.schmeh.chatincolor.util.ValidWord
import io.github.schmeh.chatincolor.util.splitValidAndInvalidWords

import net.minecraft.client.MinecraftClient

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

import java.util.Optional

object ChatInColorClient : ClientModInitializer {

	private var arePlayersInit = false

	override fun onInitializeClient() {
		// Color chat messages sent by players
		ClientReceiveMessageEvents.ALLOW_CHAT.register { message, _, _, _, _ ->
			// Add a random color for each online player.
			// This is used in case a leave message is sent for a player that was already connected and the player
			// didn't previously type any message.
			//
			// Can't use ClientPlayConnectionEvents.JOIN because we don't know the players yet.
			// Instead we try to initialize when we get a message and haven't successfully initialized yet
			if (!arePlayersInit) { arePlayersInit = initPlayers() }
			printColoredMessage(message)
		}
		// Color chat messages sent by the server.
		// Servers with custom messages may send players' messages as server messages
		ClientReceiveMessageEvents.ALLOW_GAME.register { message, _ ->
			if (!arePlayersInit) { arePlayersInit = initPlayers() }
			printColoredMessage(message)
		}

		// Reset player colors when disconnecting
		ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
			clearRandomColors()
			arePlayersInit = false
		}

		// Register commands for setting player colors
		ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
			dispatcher.register(literal("chatincolor")
					.then(buildSetColorCommand())
					.then(buildUnsetColorCommand())
					.then(buildGetSetColorsCommand())
					.then(buildHelpCommand())
			)
		}
	}

	/**
	 * Add a random color for each online player. Return true if successful false if the player list is empty.
	 */
	private fun initPlayers(): Boolean {
		var players = getOnlinePlayerNames()
		for (player in getOnlinePlayerNames()) {
			setRandomColor(player)
		}
		return (!players.isEmpty())
	}

	/**
	 * Print the message with colored player names. Return false if a name was found and the message was printed,
	 * true otherwise.
	 */
	private fun printColoredMessage(message: Text): Boolean {
		val coloredMessage = Text.literal("")
		var foundName = false

		// Visit each part of the message to preserve original formatting
		message.visit<Void>(object : StringVisitable.StyledVisitor<Void> {
			override fun accept(style: Style, content: String): Optional<Void> {
				// Split content into words (or spaces) while preserving spacing
				for (word in splitValidAndInvalidWords(content)) {
					val textPart = if (isPlayerName(word.string)) {
						foundName = true
						val color = getPlayerColor(word.string)
						Text.literal(word.string)
								.setStyle(style.withColor(TextColor.fromRgb(color))) // override color only
					} else {
						Text.literal(word.string).setStyle(style) // preserve original formatting
					}
					coloredMessage.append(textPart)
				}
				return Optional.empty() // continue visiting
			}
		}, Style.EMPTY) // starting style

		if (!foundName) return true
		MinecraftClient.getInstance().inGameHud.chatHud.addMessage(coloredMessage)

		return false
	}
}
