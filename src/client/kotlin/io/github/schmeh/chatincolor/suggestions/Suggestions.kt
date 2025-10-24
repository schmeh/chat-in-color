package io.github.schmeh.chatincolor.suggestions

import io.github.schmeh.chatincolor.players.getAllPlayerNames
import io.github.schmeh.chatincolor.players.getSetPlayerNames

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import java.util.concurrent.CompletableFuture

/**
 * Return a suggestion provider containing every player online in the server
 */
object OnlinePlayerNameSuggestionProvider : SuggestionProvider<FabricClientCommandSource> {

    override fun getSuggestions(
            context: CommandContext<FabricClientCommandSource>,
            builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val client = MinecraftClient.getInstance()

        for (name in getAllPlayerNames()) {
            if (name.startsWith(builder.remaining, ignoreCase = true)) {
                builder.suggest(name)
            }
        }

        return builder.buildFuture()
    }
}

/**
 * Return a suggestion provider containing every player with a set color
 */
object SetPlayerNameSuggestionProvider : SuggestionProvider<FabricClientCommandSource> {

    override fun getSuggestions(
            context: CommandContext<FabricClientCommandSource>,
            builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val client = MinecraftClient.getInstance()

        for (name in getSetPlayerNames()) {
            if (name.startsWith(builder.remaining, ignoreCase = true)) {
                builder.suggest(name)
            }
        }

        return builder.buildFuture()
    }
}
