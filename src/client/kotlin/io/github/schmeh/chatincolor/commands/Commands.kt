package io.github.schmeh.chatincolor.commands

import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder

import io.github.schmeh.chatincolor.manager.PlayerManager

import io.github.schmeh.chatincolor.player.Player
import io.github.schmeh.chatincolor.player.Players

import io.github.schmeh.chatincolor.util.hexToInt
import io.github.schmeh.chatincolor.util.randomSaturatedColor

import io.github.schmeh.chatincolor.suggestions.AllPlayerNameSuggestionProvider
import io.github.schmeh.chatincolor.suggestions.SetPlayerNameSuggestionProvider

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*

/**
 * Build command to set a player's chat color
 */
fun buildSetColorCommand() = literal("setColor")
        .then(argument("playerName", StringArgumentType.word())
                .suggests(AllPlayerNameSuggestionProvider)
                .then(argument("hex", StringArgumentType.greedyString())
                        .executes { ctx ->
                            val player = StringArgumentType.getString(ctx, "playerName")
                            val hex = StringArgumentType.getString(ctx, "hex").trim()

                            try {
                                val color = hexToInt(hex)
                                val playerData = PlayerManager.players.players.getOrPut(player) { Player(null) }
                                playerData.setPlayerColor(color)
                                PlayerManager.save()
                                ctx.source.sendFeedback(Text.literal("Set custom color for $player to $hex"))
                            } catch (e: Exception) {
                                ctx.source.sendFeedback(Text.literal("Invalid hex color: $hex"))
                            }
                            Command.SINGLE_SUCCESS
                        }
                )
        )

/**
 * Build command to unset a player's chat color
 */
fun buildUnsetColorCommand() = literal("unsetColor")
        .then(argument("playerName", StringArgumentType.word())
                .suggests(SetPlayerNameSuggestionProvider)
                .executes { ctx ->
                    val player = StringArgumentType.getString(ctx, "playerName")

                    if (PlayerManager.players[player] != null && PlayerManager.players[player]!!.unsetPlayerColor()) {
                        PlayerManager.save()
                        ctx.source.sendFeedback(Text.literal("Unset custom color for $player"))
                    } else {
                        ctx.source.sendFeedback(Text.literal("No custom color was set for $player"))
                    }
                    Command.SINGLE_SUCCESS
                }
        )

/**
 * Build command to list all players with set chat colors
 */
fun buildGetSetColorsCommand() = literal("getSetColors")
        .executes { ctx ->
            val setPlayerColors = PlayerManager.players.getSetPlayerColors()
            if (setPlayerColors.size == 0) {
                ctx.source.sendFeedback(Text.literal("No player colors set"))
            } else {
                ctx.source.sendFeedback(Text.literal("Player colors:"))
                for ((name, color) in setPlayerColors) {
                    // Convert color Int to hex string
                    val hex = String.format("%06X", color)
                    ctx.source.sendFeedback(
                            Text.literal("$name #$hex")
                                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)))
                    )
                }
            }
            Command.SINGLE_SUCCESS
        }

/**
 * Build command to randomize all player random colors
 */
fun buildRandomizeColorsCommand() = literal("randomizeColors")
        .executes { ctx ->
            PlayerManager.players.randomizeAllPlayerRandomColors()
            ctx.source.sendFeedback(Text.literal("Randomized all player colors"))
            Command.SINGLE_SUCCESS
        }

/**
 * Build help command
 */
fun buildHelpCommand() = literal("help").executes { ctx ->
    val messages = listOf(
            "/chatincolor setColor <player> <#HEX> - Set a player's color",
            "/chatincolor unsetColor <player> - Remove a player's custom color",
            "/chatincolor listColors - List all players with custom colors",
            "/chatincolor randomizeColors - Randomize all random colors. Does not affect players with set colors",
    )

    for (msg in messages) {
        ctx.source.sendFeedback(
                Text.literal(msg)
                        .setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
    }
    Command.SINGLE_SUCCESS
}
