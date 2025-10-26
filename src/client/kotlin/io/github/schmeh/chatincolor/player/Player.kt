package io.github.schmeh.chatincolor.player

import io.github.schmeh.chatincolor.util.randomSaturatedColor

import kotlinx.serialization.Transient
import kotlinx.serialization.Serializable

@Serializable
data class Player(var setColor: Int?) {

    @Transient
    var randomColor: Int = randomSaturatedColor()

    /**
     * Returns the effective color: the set color if set, otherwise the random color
     */
    fun getEffectiveColor(): Int {
        return setColor ?: randomColor
    }

    /**
     * Sets the player's color to a color Int
     */
    fun setPlayerColor(color: Int?) {
        setColor = color
    }

    /**
     * Unsets the player's custom color
     * Return true if successful and false if unsuccessful (no custom color was set)
     */
    fun unsetPlayerColor(): Boolean {
        if (setColor == null) return false
        setPlayerColor(null)
        return true
    }
}
