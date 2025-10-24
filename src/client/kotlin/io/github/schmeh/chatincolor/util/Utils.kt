package io.github.schmeh.chatincolor.util

import java.util.Random

private val random = Random()

/**
 * Generates a random saturated RGB color as an integer (0xRRGGBB).
 * A Saturated RGB value has the one component set to 255, another to 0, and a third one a random number 0-255
 */
fun randomSaturatedColor(): Int {
    // Create array with one component at 0, one at 255, and one random from 0-255
    val rgb = intArrayOf(0, 255, random.nextInt(256))

    // Shuffle the array to randomize which component is which
    rgb.shuffle()

    // Combine into single integer
    return (rgb[0] shl 16) or (rgb[1] shl 8) or rgb[2]
}

/**
 * Returns the integer from a hex string formatted "0x000000", or "0X000000", "#000000", or "000000"
 */
fun hexToInt(hex: String): Int {
    // Remove leading "#" or "0x" if present
    val cleanHex = hex.trim()
            .removePrefix("#")
            .removePrefix("0x")
            .removePrefix("0X")

    // Parse as base-16 integer
    return cleanHex.toInt(16)
}

data class ValidWord(val string: String, val isValid: Boolean)

/**
 * Splits a string into valid and invalid words.
 * A valid word is only letters, numbers, and underscores
 *
 * For example: " Hello    there!-This_is_100_percent_valid_"
 * is split into: " ", "Hello", "    ", "there", "!-", "This_is_100_percent_valid_"
 */
fun splitValidAndInvalidWords(text: String): List<ValidWord> {
    if (text.isEmpty()) return emptyList()

    val result = mutableListOf<ValidWord>()
    val sb = StringBuilder()
    var currentValid = isWordChar(text[0])

    // Iterate through characters and build chunks
    for (c in text) {
        if (isWordChar(c) == currentValid) {
            sb.append(c)
        } else {
            result.add(ValidWord(sb.toString(), currentValid))
            sb.clear()
            sb.append(c)
            currentValid = !currentValid
        }
    }
    // Add the last chunk
    if (sb.isNotEmpty()) {
        result.add(ValidWord(sb.toString(), currentValid))
    }

    return result
}

/**
 * Return if the character is a valid character for a word
 * A valid character is a letter, number, or underscore
 */
fun isWordChar(c: Char) = c.isLetterOrDigit() || c == '_'
