package io.github.schmeh.chatincolor.commands

import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder

import io.github.schmeh.chatincolor.players.setPlayerColor
import io.github.schmeh.chatincolor.players.unsetPlayerColor
import io.github.schmeh.chatincolor.players.getSetPlayerColors

import io.github.schmeh.chatincolor.util.hexToInt

import io.github.schmeh.chatincolor.suggestions.OnlinePlayerNameSuggestionProvider
import io.github.schmeh.chatincolor.suggestions.SetPlayerNameSuggestionProvider

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*

/**
 * Build command to set a player's chat color
 */
fun buildSetColorCommand() = literal("setColor")
        .then(argument("playerName", StringArgumentType.word())
                .suggests(OnlinePlayerNameSuggestionProvider)
                .then(argument("hex", StringArgumentType.greedyString())
                        .executes { ctx ->
                            val player = StringArgumentType.getString(ctx, "playerName")
                            val hex = StringArgumentType.getString(ctx, "hex").trim()

                            try {
                                val color = hexToInt(hex)
                                setPlayerColor(player, color)
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

                    if (unsetPlayerColor(player)) {
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
            val setPlayerColors = getSetPlayerColors()
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
 * Build help command
 */
fun buildHelpCommand() = literal("help").executes { ctx ->
    val messages = listOf(
            "/chatincolor setColor <player> <#HEX> - Set a player's color",
            "/chatincolor unsetColor <player> - Remove a player's custom color",
            "/chatincolor listColors - List all players with custom colors"
    )

    for (msg in messages) {
        ctx.source.sendFeedback(
                Text.literal(msg)
                        .setStyle(Style.EMPTY.withColor(Formatting.WHITE)))
    }
    Command.SINGLE_SUCCESS
}
