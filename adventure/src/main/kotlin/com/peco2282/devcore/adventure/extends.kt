package com.peco2282.devcore.adventure

import com.peco2282.devcore.adventure.builder.Componenter
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration

/// Style.Builder
/**
 * Applies the obfuscated text decoration to this style builder.
 * @return this builder for chaining
 */
fun Style.Builder.obfuscated(): Style.Builder = apply { decorate(TextDecoration.OBFUSCATED) }

/**
 * Applies the bold text decoration to this style builder.
 * @return this builder for chaining
 */
fun Style.Builder.bold(): Style.Builder = apply { decorate(TextDecoration.BOLD) }

/**
 * Applies the italic text decoration to this style builder.
 * @return this builder for chaining
 */
fun Style.Builder.italic(): Style.Builder = apply { decorate(TextDecoration.ITALIC) }

/**
 * Applies the underlined text decoration to this style builder.
 * @return this builder for chaining
 */
fun Style.Builder.underlined(): Style.Builder = apply { decorate(TextDecoration.UNDERLINED) }

/**
 * Applies the strikethrough text decoration to this style builder.
 * @return this builder for chaining
 */
fun Style.Builder.strikethrough(): Style.Builder = apply { decorate(TextDecoration.STRIKETHROUGH) }

/**
 * Removes all text decorations from this style builder.
 * @return this builder for chaining
 */
fun Style.Builder.reset(): Style.Builder = apply { decorations(TextDecoration.entries.toSet(), false) }

/**
 * Sets the font using the specified namespace and path.
 * @param ns the namespace of the font
 * @param path the path of the font
 * @return this builder for chaining
 */
fun Style.Builder.font(ns: String, path: String): Style.Builder = apply { font(Key.key(ns, path)) }

/**
 * Sets the font using a namespaced path string (e.g., "namespace:path").
 * @param nsPath the namespaced path string
 * @return this builder for chaining
 */
fun Style.Builder.font(nsPath: String): Style.Builder = apply { font(Key.key(nsPath)) }

/**
 * Sets the text color to black.
 * @return this builder for chaining
 */
fun Style.Builder.black(): Style.Builder = apply { color(NamedTextColor.BLACK) }

/**
 * Sets the text color to dark blue.
 * @return this builder for chaining
 */
fun Style.Builder.darkBlue(): Style.Builder = apply { color(NamedTextColor.DARK_BLUE) }

/**
 * Sets the text color to dark green.
 * @return this builder for chaining
 */
fun Style.Builder.darkGreen(): Style.Builder = apply { color(NamedTextColor.DARK_GREEN) }

/**
 * Sets the text color to dark aqua.
 * @return this builder for chaining
 */
fun Style.Builder.darkAqua(): Style.Builder = apply { color(NamedTextColor.DARK_AQUA) }

/**
 * Sets the text color to dark red.
 * @return this builder for chaining
 */
fun Style.Builder.darkRed(): Style.Builder = apply { color(NamedTextColor.DARK_RED) }

/**
 * Sets the text color to dark purple.
 * @return this builder for chaining
 */
fun Style.Builder.darkPurple(): Style.Builder = apply { color(NamedTextColor.DARK_PURPLE) }

/**
 * Sets the text color to gold.
 * @return this builder for chaining
 */
fun Style.Builder.gold(): Style.Builder = apply { color(NamedTextColor.GOLD) }

/**
 * Sets the text color to gray.
 * @return this builder for chaining
 */
fun Style.Builder.gray(): Style.Builder = apply { color(NamedTextColor.GRAY) }

/**
 * Sets the text color to dark gray.
 * @return this builder for chaining
 */
fun Style.Builder.darkGray(): Style.Builder = apply { color(NamedTextColor.DARK_GRAY) }

/**
 * Sets the text color to blue.
 * @return this builder for chaining
 */
fun Style.Builder.blue(): Style.Builder = apply { color(NamedTextColor.BLUE) }

/**
 * Sets the text color to green.
 * @return this builder for chaining
 */
fun Style.Builder.green(): Style.Builder = apply { color(NamedTextColor.GREEN) }

/**
 * Sets the text color to aqua.
 * @return this builder for chaining
 */
fun Style.Builder.aqua(): Style.Builder = apply { color(NamedTextColor.AQUA) }

/**
 * Sets the text color to red.
 * @return this builder for chaining
 */
fun Style.Builder.red(): Style.Builder = apply { color(NamedTextColor.RED) }

/**
 * Sets the text color to light purple.
 * @return this builder for chaining
 */
fun Style.Builder.lightPurple(): Style.Builder = apply { color(NamedTextColor.LIGHT_PURPLE) }

/**
 * Sets the text color to yellow.
 * @return this builder for chaining
 */
fun Style.Builder.yellow(): Style.Builder = apply { color(NamedTextColor.YELLOW) }

/**
 * Sets the text color to white.
 * @return this builder for chaining
 */
fun Style.Builder.white(): Style.Builder = apply { color(NamedTextColor.WHITE) }

/// [Style]
/**
 * Applies the obfuscated text decoration to this style.
 * @return this style for chaining
 */
fun Style.obfuscated(): Style = apply { decorate(TextDecoration.OBFUSCATED) }

/**
 * Applies the bold text decoration to this style.
 * @return this style for chaining
 */
fun Style.bold(): Style = apply { decorate(TextDecoration.BOLD) }

/**
 * Applies the italic text decoration to this style.
 * @return this style for chaining
 */
fun Style.italic(): Style = apply { decorate(TextDecoration.ITALIC) }

/**
 * Applies the underlined text decoration to this style.
 * @return this style for chaining
 */
fun Style.underlined(): Style = apply { decorate(TextDecoration.UNDERLINED) }

/**
 * Applies the strikethrough text decoration to this style.
 * @return this style for chaining
 */
fun Style.strikethrough(): Style = apply { decorate(TextDecoration.STRIKETHROUGH) }

/**
 * Removes all text decorations from this style.
 * @return this style for chaining
 */
fun Style.reset(): Style = apply { decorations(TextDecoration.entries.toSet(), false) }

/**
 * Sets the font using the specified namespace and path.
 * @param ns the namespace of the font
 * @param path the path of the font
 * @return this style for chaining
 */
fun Style.font(ns: String, path: String): Style = apply { font(Key.key(ns, path)) }

/**
 * Sets the font using a namespaced path string (e.g., "namespace:path").
 * @param nsPath the namespaced path string
 * @return this style for chaining
 */
fun Style.font(nsPath: String): Style = apply { font(Key.key(nsPath)) }

/**
 * Sets the text color to black.
 * @return this style for chaining
 */
fun Style.black(): Style = apply { color(NamedTextColor.BLACK) }

/**
 * Sets the text color to dark blue.
 * @return this style for chaining
 */
fun Style.darkBlue(): Style = apply { color(NamedTextColor.DARK_BLUE) }

/**
 * Sets the text color to dark green.
 * @return this style for chaining
 */
fun Style.darkGreen(): Style = apply { color(NamedTextColor.DARK_GREEN) }

/**
 * Sets the text color to dark aqua.
 * @return this style for chaining
 */
fun Style.darkAqua(): Style = apply { color(NamedTextColor.DARK_AQUA) }

/**
 * Sets the text color to dark red.
 * @return this style for chaining
 */
fun Style.darkRed(): Style = apply { color(NamedTextColor.DARK_RED) }

/**
 * Sets the text color to dark purple.
 * @return this style for chaining
 */
fun Style.darkPurple(): Style = apply { color(NamedTextColor.DARK_PURPLE) }

/**
 * Sets the text color to gold.
 * @return this style for chaining
 */
fun Style.gold(): Style = apply { color(NamedTextColor.GOLD) }

/**
 * Sets the text color to gray.
 * @return this style for chaining
 */
fun Style.gray(): Style = apply { color(NamedTextColor.GRAY) }

/**
 * Sets the text color to dark gray.
 * @return this style for chaining
 */
fun Style.darkGray(): Style = apply { color(NamedTextColor.DARK_GRAY) }

/**
 * Sets the text color to blue.
 * @return this style for chaining
 */
fun Style.blue(): Style = apply { color(NamedTextColor.BLUE) }

/**
 * Sets the text color to green.
 * @return this style for chaining
 */
fun Style.green(): Style = apply { color(NamedTextColor.GREEN) }

/**
 * Sets the text color to aqua.
 * @return this style for chaining
 */
fun Style.aqua(): Style = apply { color(NamedTextColor.AQUA) }

/**
 * Sets the text color to red.
 * @return this style for chaining
 */
fun Style.red(): Style = apply { color(NamedTextColor.RED) }

/**
 * Sets the text color to light purple.
 * @return this style for chaining
 */
fun Style.lightPurple(): Style = apply { color(NamedTextColor.LIGHT_PURPLE) }

/**
 * Sets the text color to yellow.
 * @return this style for chaining
 */
fun Style.yellow(): Style = apply { color(NamedTextColor.YELLOW) }

/**
 * Sets the text color to white.
 * @return this style for chaining
 */
fun Style.white(): Style = apply { color(NamedTextColor.WHITE) }

/**
 * Sets the text color to red.
 */
fun Componenter.red(): Componenter = withColor(NamedTextColor.RED)

/**
 * Sets the text color to green.
 */
fun Componenter.green(): Componenter = withColor(NamedTextColor.GREEN)

/**
 * Sets the text color to yellow.
 */
fun Componenter.yellow(): Componenter = withColor(NamedTextColor.YELLOW)

/**
 * Sets the text color to blue.
 */
fun Componenter.blue(): Componenter = withColor(NamedTextColor.BLUE)

/**
 * Sets the text color to dark blue.
 */
fun Componenter.darkBlue(): Componenter = withColor(NamedTextColor.DARK_BLUE)

/**
 * Sets the text color to light purple.
 */
fun Componenter.purple(): Componenter = withColor(NamedTextColor.LIGHT_PURPLE)

/**
 * Sets the text color to dark purple.
 */
fun Componenter.darkPurple(): Componenter = withColor(NamedTextColor.DARK_PURPLE)

/**
 * Sets the text color to aqua.
 */
fun Componenter.aqua(): Componenter = withColor(NamedTextColor.AQUA)

/**
 * Sets the text color to gray.
 */
fun Componenter.gray(): Componenter = withColor(NamedTextColor.GRAY)

/**
 * Sets the text color to dark gray.
 */
fun Componenter.darkGray(): Componenter = withColor(NamedTextColor.DARK_GRAY)

/**
 * Sets the text color to white.
 */
fun Componenter.white(): Componenter = withColor(NamedTextColor.WHITE)

/**
 * Sets the text color to black.
 */
fun Componenter.black(): Componenter = withColor(NamedTextColor.BLACK)

/**
 * Sets the text color to gold.
 */
fun Componenter.gold(): Componenter = withColor(NamedTextColor.GOLD)

/**
 * Sets the text color to dark red.
 */
fun Componenter.darkRed(): Componenter = withColor(NamedTextColor.DARK_RED)

/**
 * Sets the text color to dark green.
 */
fun Componenter.darkGreen(): Componenter = withColor(NamedTextColor.DARK_GREEN)

/**
 * Sets the text color to dark aqua.
 */
fun Componenter.darkAqua(): Componenter = withColor(NamedTextColor.DARK_AQUA)

/// library

fun Componenter.errorMessage(text: String) {
  text("Error: ") {
    red(); bold()
  }
  text(text) { red() }
}
