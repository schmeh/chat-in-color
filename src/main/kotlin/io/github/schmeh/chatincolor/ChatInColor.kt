package io.github.schmeh.chatincolor

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object ChatInColor : ModInitializer {
    private val logger = LoggerFactory.getLogger("chatincolor")

	override fun onInitialize() {
		logger.info("Chat in Color: successfully initialized!")
	}
}
